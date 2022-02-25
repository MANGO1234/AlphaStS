# format

import random as rand
import time
import math
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


def convertToOnnx(model, input_len):
    spec = (tf.TensorSpec((1, input_len), tf.float32, name="input"),)
    output_path = "tmp/model.onnx"
    model_proto, _ = tf2onnx.convert.from_keras(model, input_signature=spec, opset=12, output_path=output_path)


lens_str = subprocess.run(['java', '-classpath', f'F:/git/alphaStS/agent/target/classes;{os.getenv("M2_HOME")}/repository/com/microsoft/onnxruntime/onnxruntime/1.10.0/onnxruntime-1.10.0.jar;./agent/src/resources/mallet.jar;./agent/src/resources/mallet-deps.jar',
                           'com.alphaStS.Main', '--get-lengths'], capture_output=True).stdout.decode('ascii').split(',')
input_len = int(lens_str[0])
num_of_actions = int(lens_str[1])
print(f'input_len={input_len}, policy_len={num_of_actions}')


def softmax_cross_entropy_with_logits(y_true, y_pred):
    p = y_pred
    pi = y_true
    loss = tf.nn.softmax_cross_entropy_with_logits(labels=pi, logits=p)
    return loss

if os.path.exists('saves/model'):
    custom_objects = {"softmax_cross_entropy_with_logits": softmax_cross_entropy_with_logits}
    with keras.utils.custom_object_scope(custom_objects):
        model = tf.keras.models.load_model('saves/model')
else:
    inputs = keras.Input(shape=(input_len,))
    x = layers.Dense(input_len, activation="linear", use_bias=True, name="layer1")(inputs)
    x = layers.BatchNormalization(axis=1)(x)
    x = layers.LeakyReLU()(x)
    x = layers.Dense((input_len + 1) // 2, activation="linear", use_bias=True, name="layer2")(x)
    x = layers.BatchNormalization(axis=1)(x)
    x = layers.LeakyReLU()(x)
    x1 = layers.Dense((input_len + 1) // 4, activation="linear", use_bias=True, name="layer3")(x)
    x1 = layers.BatchNormalization(axis=1)(x1)
    x1 = layers.LeakyReLU()(x1)
    x2 = layers.Dense((input_len + 1) // 4, activation="linear", use_bias=True, name="layer4")(x)
    x2 = layers.BatchNormalization(axis=1)(x2)
    x2 = layers.LeakyReLU()(x2)
    exp_win_head = layers.Dense(1, name="exp_win_head", use_bias=True, activation='sigmoid')(x1)
    exp_health_head = layers.Dense(1, name="exp_health_head", use_bias=True, activation='sigmoid')(x1)
    policy_head = layers.Dense(num_of_actions, use_bias=True, activation='linear', name="policy_head")(x2)
    # x = layers.Dense(math.floor((input_len + 1) / 2), activation="linear", use_bias=True, name="layer1")(inputs)
    # x = layers.BatchNormalization(axis=1)(x)
    # x = layers.LeakyReLU()(x)
    # exp_win_head = layers.Dense(1, name="exp_win_head", use_bias=True, activation='sigmoid')(x)
    # exp_health_head = layers.Dense(1, name="exp_health_head", use_bias=True, activation='sigmoid')(x)
    # policy_head = layers.Dense(num_of_actions, use_bias=True, activation='linear', name="policy_head")(x)
    model = keras.Model(inputs=[inputs], outputs=[exp_health_head, exp_win_head, policy_head])
    model.compile(loss={
        'exp_health_head': 'mean_squared_error',
        'exp_win_head': 'mean_squared_error',
        'policy_head': softmax_cross_entropy_with_logits
    },
        optimizer=tf.keras.optimizers.SGD(learning_rate=0.1, momentum=0.9),
        # optimizer='adam',
        # loss_weights={'exp_health_head': 0.5, 'policy_head': 0.5}
        loss_weights={'exp_health_head': 0.33, 'policy_head': 0.33, 'exp_win_head': 0.33}
    )

convertToOnnx(model, input_len)
start = time.time()
# np.set_printoptions(threshold=np.inf)

if DO_TRAINING:
    training_f = open('./tmp/training.txt', 'a+')
    for _iterations in range(0, ITERATION_COUNT):
        if not SKIP_TRAINING_MATCHES and _iterations > 0:
            agent_output = subprocess.run(['java', '-classpath', f'./agent/target/classes;{os.getenv("M2_HOME")}/repository/com/microsoft/onnxruntime/onnxruntime/1.10.0/onnxruntime-1.10.0.jar;./agent/src/resources/mallet.jar;./agent/src/resources/mallet-deps.jar', 'com.alphaStS.Main', '-tm', '-c', '100', '-n', '500', '-t', '-dir', './tmp'], capture_output=True)
        else:
            agent_output = subprocess.run(['java', '-classpath', f'./agent/target/classes;{os.getenv("M2_HOME")}/repository/com/microsoft/onnxruntime/onnxruntime/1.10.0/onnxruntime-1.10.0.jar;./agent/src/resources/mallet.jar;./agent/src/resources/mallet-deps.jar', 'com.alphaStS.Main', '-t', '-dir', './tmp'], capture_output=True)
        if len(agent_output.stderr) > 0:
            print(agent_output.stderr.decode('ascii'))
            raise "agent error"
        agent_output = agent_output.stdout

        if not SKIP_TRAINING_MATCHES and _iterations > 0:
            split = agent_output.find(b'--------------------')
            print(agent_output[0: split + 20].decode('ascii'))
            agent_output = agent_output[split + 20:]

        training_f.write(f'Iteration {_iterations + 1}\n')
        split = agent_output.find(b'--------------------')
        print(agent_output[0: split + 20].decode('ascii'))
        agent_output = agent_output[split + 20:]

        training_pool = []
        offset = 0
        # technically N^2
        while len(agent_output) > 0:
            x_fmt = '>' + ('f' * input_len)
            x = struct.unpack(x_fmt, agent_output[0: 4 * input_len])
            [v_health, v_win] = struct.unpack('>ff', agent_output[4 * input_len: 4 * (input_len + 2)])
            p_fmt = '>' + ('f' * num_of_actions)
            p = struct.unpack(p_fmt, agent_output[4 * (input_len + 2): 4 * (input_len + 2 + num_of_actions)])
            agent_output = agent_output[4 * (input_len + 2 + num_of_actions):]
            training_pool.append([list(x), [v_health], [v_win], list(p)])
        if len(agent_output) != 0:
            print(f'{len(agent_output)} bytes remaining for decoding')
            raise "agent error"

        print(f'number of samples={len(training_pool)}')
        for i in range(10):
            # for i in range(1):
            minibatch = rand.sample(training_pool, len(training_pool) // 20)
            minibatch = training_pool
            x_train = []
            exp_health_head_train = []
            exp_win_head_train = []
            policy_head_train = []
            for x, v_health, v_win, p in minibatch:
                x_train.append(np.asarray(x))
                exp_health_head_train.append(np.asarray(v_health).reshape(1, 1))
                exp_win_head_train.append(np.asarray(v_win).reshape(1, 1))
                policy_head_train.append(np.asarray(p).reshape(1, num_of_actions))
            x_train = np.asarray(x_train)
            exp_health_head_train = np.asarray(exp_health_head_train)
            exp_win_head_train = np.asarray(exp_win_head_train)
            policy_head_train = np.asarray(policy_head_train)
            model.fit(np.asarray(x_train), [exp_health_head_train, exp_win_head_train, policy_head_train], epochs=2)
        model.save('saves/model')
        convertToOnnx(model, input_len)

        if _iterations == ITERATION_COUNT - 1:
            agent_output = subprocess.run(['java', '-classpath', f'./agent/target/classes;{os.getenv("M2_HOME")}/repository/com/microsoft/onnxruntime/onnxruntime/1.10.0/onnxruntime-1.10.0.jar;./agent/src/resources/mallet.jar;./agent/src/resources/mallet-deps.jar', 'com.alphaStS.Main', '-tm', '-c', '100', '-n', '500', '-dir', './tmp'], capture_output=True)
            if len(agent_output.stderr) > 0:
                print(agent_output.stderr.decode('ascii'))
                raise "agent error"
            agent_output = agent_output.stdout
            print(agent_output.decode('ascii'))

        training_f.write(f'Time Accumulated: {time.time() - start}\n')
        training_f.flush()
    training_f.close()

if PLAY_A_GAME:
    print(time.time() - start)

if PLAY_MATCHES:
    print(time.time() - start)
