# format

import random as rand
import time
import math
import json
import re
import os
import subprocess
import struct
import tensorflow as tf
import numpy as np
from tensorflow import keras
from tensorflow.keras import layers
import tf2onnx
from scipy.special import softmax
from misc import getFlag, getFlagValue

# rand.seed(5)
# np.random.seed(5)
# tf.random.set_seed(5)

DO_TRAINING = getFlag('-t')
SKIP_TRAINING_MATCHES = getFlag('-s')
PLAY_A_GAME = getFlag('-p')
PLAY_MATCHES = getFlag('-m')
ITERATION_COUNT = int(getFlagValue('-c', 5))
NODE_COUNT = int(getFlagValue('-n', 1000))
SAVES_DIR = getFlagValue('-dir', './saves')


def convertToOnnx(model, input_len, output_dir):
    spec = (tf.TensorSpec((1, input_len), tf.float32, name="input"),)
    output_path = output_dir + "/model.onnx"
    model_proto, _ = tf2onnx.convert.from_keras(model, input_signature=spec, opset=12, output_path=output_path)


lens_str = subprocess.run(['java', '-classpath', f'F:/git/alphaStS/agent/target/classes;{os.getenv("M2_HOME")}/repository/com/microsoft/onnxruntime/onnxruntime/1.10.0/onnxruntime-1.10.0.jar;./agent/src/resources/mallet.jar;./agent/src/resources/mallet-deps.jar',
                           'com.alphaStS.Main', '--get-lengths'], capture_output=True).stdout.decode('ascii').split(',')
input_len = int(lens_str[0])
num_of_actions = int(lens_str[1])
print(f'input_len={input_len}, policy_len={num_of_actions}')


def softmax_cross_entropy_with_logits(y_true, y_pred):
    p = y_pred
    pi = y_true
    zero = tf.zeros(shape=tf.shape(pi), dtype=tf.float32)
    where = tf.less(pi, zero)
    negatives = tf.fill(tf.shape(pi), -1000.0)
    p = tf.where(where, negatives, p)
    pi = tf.where(where, zero, pi)
    loss = tf.nn.softmax_cross_entropy_with_logits(labels=pi, logits=p)
    return loss


if os.path.exists(f'{SAVES_DIR}/training.json'):
    with open(f'{SAVES_DIR}/training.json', 'r') as f:
        training_info = json.load(f)
else:
    try:
        os.mkdir(SAVES_DIR)
    except:
        pass
    training_info = {'iteration': 1, 'iteration_info': {}}
    with open(f'{SAVES_DIR}/training.json', 'w') as f:
        json.dump(training_info, f)

if os.path.exists(f'{SAVES_DIR}/iteration{training_info["iteration"] - 1}'):
    custom_objects = {"softmax_cross_entropy_with_logits": softmax_cross_entropy_with_logits}
    with keras.utils.custom_object_scope(custom_objects):
        model = tf.keras.models.load_model(f'{SAVES_DIR}/iteration{training_info["iteration"] - 1}')
        # model.optimizer.lr.assign(0.01)
else:
    inputs = keras.Input(shape=(input_len,))
    x = layers.Dense(input_len, activation="linear", name="layer1")(inputs)
    x = layers.BatchNormalization(axis=1)(x)
    x = layers.LeakyReLU()(x)
    x = layers.Dense(input_len, activation="linear", name="layer2")(x)
    x = layers.BatchNormalization(axis=1)(x)
    x = layers.LeakyReLU()(x)
    # x = layers.Dense((input_len + 1) // 2, activation="linear", use_bias=True, name="layer2")(x)
    # x = layers.BatchNormalization(axis=1)(x)
    # x = layers.LeakyReLU()(x)
    # x1 = layers.Dense((input_len + 1) // 4, activation="linear", use_bias=True, name="layer3")(x)
    # x1 = layers.BatchNormalization(axis=1)(x1)
    # x1 = layers.LeakyReLU()(x1)
    # x2 = layers.Dense((input_len + 1) // 4, activation="linear", use_bias=True, name="layer4")(x)
    # x2 = layers.BatchNormalization(axis=1)(x2)
    # x2 = layers.LeakyReLU()(x2)
    exp_win_head = layers.Dense(1, name="exp_win_head", use_bias=True, activation='tanh')(x)
    exp_health_head = layers.Dense(1, name="exp_health_head", use_bias=True, activation='tanh')(x)
    policy_head = layers.Dense(num_of_actions, use_bias=True, activation='linear', name="policy_head")(x)
    model = keras.Model(inputs=[inputs], outputs=[exp_health_head, exp_win_head, policy_head])
    model.compile(loss={
        'exp_health_head': 'mean_squared_error',
        'exp_win_head': 'mean_squared_error',
        'policy_head': softmax_cross_entropy_with_logits
    },
        optimizer=tf.keras.optimizers.SGD(learning_rate=0.1, momentum=0.9),
        loss_weights={'exp_health_head': 1 / 3, 'policy_head': 1 / 3, 'exp_win_head': 1 / 3}
    )
    model.save(f'{SAVES_DIR}/iteration0')
    convertToOnnx(model, input_len, f'{SAVES_DIR}/iteration0')

start = time.time()
# np.set_printoptions(threshold=np.inf)

CLASS_PATH = f'./agent/target/classes;{os.getenv("M2_HOME")}/repository/com/microsoft/onnxruntime/onnxruntime/1.10.0/onnxruntime-1.10.0.jar;./agent/src/resources/mallet.jar;./agent/src/resources/mallet-deps.jar;{os.getenv("M2_HOME")}/repository/org/jdom/jdom/1.1/jdom-1.1.jar;{os.getenv("M2_HOME")}/repository/com/fasterxml/jackson/core/jackson-databind/2.12.4/jackson-databind-2.12.4.jar;{os.getenv("M2_HOME")}/repository/com/fasterxml/jackson/core/jackson-annotations/2.12.4/jackson-annotations-2.12.4.jar;{os.getenv("M2_HOME")}/repository/com/fasterxml/jackson/core/jackson-core/2.12.4/jackson-core-2.12.4.jar'


def get_training_samples(training_pool, iteration, file_path):
    if not os.path.exists(file_path):
        return
    with open(file_path, 'rb') as f:
        content = f.read()
        offset = 0
        while offset != len(content):
            x_fmt = '>' + ('f' * input_len)
            x = struct.unpack(x_fmt, content[offset: offset + 4 * input_len])
            [v_health, v_win] = struct.unpack('>ff', content[offset + 4 * input_len: offset + 4 * (input_len + 2)])
            p_fmt = '>' + ('f' * num_of_actions)
            p = struct.unpack(p_fmt, content[offset + 4 * (input_len + 2): offset + 4 * (input_len + 2 + num_of_actions)])
            offset += 4 * (input_len + 2 + num_of_actions)
            training_pool.append((iteration, [list(x), [v_health], [v_win], list(p)]))
        if len(content) != offset:
            print(f'{len(content) - offset} bytes remaining for decoding')
            raise "agent error"


SLOW_WINDOW_END = 3
TRAINING_WINDOW_SIZE = 6
CURRICULUM_TRAINING_END = SLOW_WINDOW_END + TRAINING_WINDOW_SIZE - 1
USE_LINE_SEARCH_TO_TRAIN_END = 0


def expire_training_samples(training_pool, iteration):
    if iteration < SLOW_WINDOW_END:
        cutoff = iteration - 1
    else:
        cutoff = max(SLOW_WINDOW_END - 1, training_info['iteration'] - TRAINING_WINDOW_SIZE)
    i = 0
    while i < len(training_pool):
        if training_pool[i][0] >= cutoff:
            break
        i += 1
    return training_pool[i:]


def save_stats(training_info, iteration, out):
    death_rate = float(re.findall('Deaths: \d+/\d+ \((\d+\.\d+)\%\)', out)[0])
    avg_dmg = float(re.findall('Avg Damage: (\d+\.\d+)', out)[0])
    avg_dmg_tmp = re.findall('Avg Damage \(Not Including Deaths\): (\d+\.\d+)', out)
    avg_dmg_no_death = float(avg_dmg if len(avg_dmg_tmp) == 0 else avg_dmg_tmp[0])
    training_info['iteration_info'][str(iteration)]['death_rate'] = death_rate
    training_info['iteration_info'][str(iteration)]['avg_dmg'] = avg_dmg
    training_info['iteration_info'][str(iteration)]['avg_dmg_no_death'] = avg_dmg_no_death

accumualted_time_base = 0
if training_info['iteration'] > 1:
    accumualted_time_base = training_info['iteration_info'][str(int(training_info['iteration']) - 1)]['accumulated_time']

training_pool = []
start_window = 0
if training_info['iteration'] >= SLOW_WINDOW_END:
    start_window = max(SLOW_WINDOW_END, training_info['iteration'] - TRAINING_WINDOW_SIZE)
    for i in range(start_window, training_info['iteration'] - 1):
        print(f'loading data from {SAVES_DIR}/iteration{i}/training_data.bin')
        get_training_samples(training_pool, i, f'{SAVES_DIR}/iteration{i}/training_data.bin')

if DO_TRAINING:
    start = time.time()
    for _iteration in range(training_info["iteration"], ITERATION_COUNT + 1):
        training_info['iteration_info'][str(_iteration)] = {}
        iteration_info = training_info['iteration_info'][str(_iteration)]
        iter_start = time.time()

        agent_args = ['java', '-classpath', CLASS_PATH, 'com.alphaStS.Main', '-training', '-t', '2', '-dir', SAVES_DIR]
        if not SKIP_TRAINING_MATCHES and _iteration > 1:
            if training_info["iteration"] < 17:
                matches_count = 1000
            elif training_info["iteration"] < 25:
                matches_count = 1000 + 500 * (training_info["iteration"] - 17)
            else:
                matches_count = 5000
            agent_args += ['-tm', '-c', str(matches_count), '-n', '1']
        if training_info['iteration'] < SLOW_WINDOW_END:
            agent_args += ['-slow']
        if training_info['iteration'] < CURRICULUM_TRAINING_END:
            agent_args += ['-curriculum_training']
        if training_info['iteration'] < USE_LINE_SEARCH_TO_TRAIN_END:
            agent_args += ['-training_with_line']
        agent_output = subprocess.run(agent_args, capture_output=True)
        if len(agent_output.stderr) > 0:
            print(agent_output.stdout.decode('ascii'))
            print(agent_output.stderr.decode('ascii'))
            raise "agent error"
        agent_output = agent_output.stdout

        if not SKIP_TRAINING_MATCHES and _iteration > 1:
            split = agent_output.find(b'--------------------')
            out = agent_output[2 if agent_output[0] == '\r' else 0: split + 20].decode('ascii')
            save_stats(training_info, _iteration - 1, out)
            print(out)
            agent_output = agent_output[split + 20:]

        print(f'Iteration {training_info["iteration"]}')
        split = agent_output.find(b'--------------------')
        print(agent_output[2 if agent_output[0] == 13 else 0: split + 20].decode('ascii'))
        agent_output = agent_output[split + 20:]
        split = agent_output.find(b'--------------------')
        if split >= 0:
            print(agent_output[2 if agent_output[0] == 13 else 0: split + 20].decode('ascii'))
            agent_output = agent_output[split + 20:]

        get_training_samples(training_pool, training_info["iteration"] - 1, f'{SAVES_DIR}/iteration{training_info["iteration"] - 1}/training_data.bin')
        training_pool = expire_training_samples(training_pool, training_info["iteration"])
        iteration_info['agent_time'] = round(time.time() - iter_start, 2)
        iteration_info['num_of_samples'] = len(training_pool)
        print(f'agent time={iteration_info["agent_time"]}')
        print(f'number of samples={len(training_pool)}')
        print(f'sample oldest iteration={training_pool[0][0]}')

        iter_start = time.time()
        # for i in range(200 if training_info['iteration'] >= SLOW_WINDOW_END else 200):
        #     if training_info['iteration'] >= SLOW_WINDOW_END:
        #         minibatch = rand.sample(training_pool, len(training_pool) // 200)
        #     else:
        #         minibatch = rand.sample(training_pool, len(training_pool) // 200)
        #         # minibatch = training_pool
        train_iter = 10 if training_info['iteration'] < SLOW_WINDOW_END + TRAINING_WINDOW_SIZE - 1 else 5
        for i in range(train_iter):
            minibatch = training_pool
            x_train = []
            exp_health_head_train = []
            exp_win_head_train = []
            policy_head_train = []
            for _, (x, v_health, v_win, p) in minibatch:
                x_train.append(np.asarray(x))
                exp_health_head_train.append(np.asarray(v_health).reshape(1))
                exp_win_head_train.append(np.asarray(v_win).reshape(1))
                policy_head_train.append(np.asarray(p).reshape(num_of_actions))
            x_train = np.asarray(x_train)
            exp_health_head_train = np.asarray(exp_health_head_train)
            exp_win_head_train = np.asarray(exp_win_head_train)
            policy_head_train = np.asarray(policy_head_train)
            fit_result = model.fit(np.asarray(x_train), [exp_health_head_train, exp_win_head_train, policy_head_train], epochs=1)
        model.save(f'{SAVES_DIR}/iteration{training_info["iteration"]}')
        convertToOnnx(model, input_len, f'{SAVES_DIR}/iteration{training_info["iteration"]}')

        training_info['iteration'] += 1
        iteration_info['training_time'] = round(time.time() - iter_start, 2)
        iteration_info['accumulated_time'] = accumualted_time_base + round(time.time() - start, 2)
        iteration_info['loss'] = fit_result.history['loss'][-1]
        print(f'training time={iteration_info["training_time"]}')
        print(f'accumulated time={iteration_info["accumulated_time"]}')
        with open(f'{SAVES_DIR}/training.json', 'w') as f:
            json.dump(training_info, f)

        if _iteration == ITERATION_COUNT:
            agent_output = subprocess.run(['java', '-classpath', CLASS_PATH, 'com.alphaStS.Main', '-tm', '-c', '5000', '-n', '1', '-dir', SAVES_DIR], capture_output=True)
            if len(agent_output.stderr) > 0:
                print(agent_output.stdout.decode('ascii'))
                print(agent_output.stderr.decode('ascii'))
                raise "agent error"
            agent_output = agent_output.stdout
            out = agent_output.decode('ascii')
            save_stats(training_info, _iteration, out)
            print(out)
            with open(f'{SAVES_DIR}/training.json', 'w') as f:
                json.dump(training_info, f)


if PLAY_A_GAME:
    agent_args = ['java', '-classpath', CLASS_PATH, 'com.alphaStS.Main', '--server']
    agent_output = subprocess.run(agent_args, capture_output=True)
    print(agent_output.stdout.decode('ascii'))
    print(agent_output.stderr.decode('ascii'))
    print(time.time() - start)

if PLAY_MATCHES:
    agent_args = ['java', '-classpath', CLASS_PATH, 'com.alphaStS.Main', '--client']
    agent_output = subprocess.run(agent_args, capture_output=True)
    print(agent_output.stdout.decode('ascii'))
    print(agent_output.stderr.decode('ascii'))
    print(time.time() - start)
