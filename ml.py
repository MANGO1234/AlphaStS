# format

import random as rand
import platform
import time
import math
import json
import re
import os
import subprocess
import struct
import shutil
import tensorflow as tf
import numpy as np
import lz4.frame
from tensorflow import keras
from tensorflow.keras import layers
from misc import getFlag, getFlagValue

print(f'GUP Devices: {tf.config.list_physical_devices("GPU")}')

# rand.seed(5)
# np.random.seed(5)
# tf.random.set_seed(5)

DO_TRAINING = getFlag('-training')
USE_GPU = getFlag('-gpu')
USE_GPU_AGENT_ONLY = getFlag('-gpu_agent')
SKIP_TRAINING_MATCHES = getFlag('-s')
PLAY_A_GAME = getFlag('-p')
PLAY_MATCHES = getFlag('-m')
NUMBER_OF_THREADS = int(getFlagValue('-t', 1))
BATCH_PER_THREAD = int(getFlagValue('-b', 1))
NUMBER_OF_THREADS_TRAINING = int(getFlagValue('-tt', 0))
ITERATION_COUNT = int(getFlagValue('-c', 5))
Z_TRAIN_WINDOW_END = int(getFlagValue('-z', -1))
TRAINING_NUM_OF_GAMES = int(getFlagValue('-training-c', 200))
TRAINING_NODE_COUNT = int(getFlagValue('-training-n', 100))
DYNAMIC_BATCH_SIZE_FACTOR = int(getFlagValue('-dynamic_batch', 0))
SAVES_DIR = getFlagValue('-dir', './saves')
SKIP_FIRST = getFlag('-skip_first')
USE_KAGGLE = getFlagValue('-kaggle')
KAGGLE_USER_NAME = getFlagValue('-kaggle_user', None)
KAGGLE_DATASET_NAME = 'dataset'

if NUMBER_OF_THREADS_TRAINING > 0:
    tf.config.threading.set_intra_op_parallelism_threads(1)
    tf.config.threading.set_inter_op_parallelism_threads(1)
    os.environ["OMP_NUM_THREADS"] = f"{NUMBER_OF_THREADS_TRAINING}"
    os.environ['TF_NUM_INTEROP_THREADS'] = f"{1}"
    os.environ['TF_NUM_INTRAOP_THREADS'] = f"{1}"

if not USE_GPU:
    import tf2onnx
    onnx_jar = "onnxruntime_gpu/1.10.0/onnxruntime_gpu-1.10.0.jar" if USE_GPU_AGENT_ONLY else "onnxruntime/1.10.0/onnxruntime-1.10.0.jar"
else:
    onnx_jar = "onnxruntime_gpu/1.10.0/onnxruntime_gpu-1.10.0.jar"


def convertToOnnx(model, input_len, output_dir):
    if USE_GPU:
        output = subprocess.run(['powershell', '-Command', f'conda activate alphasts; python .\\ml_conv.py -training -t 10 -c 35 -z 20 -dir {SAVES_DIR}'], capture_output=True)
        print(output.stderr.decode('ascii'))
        print(output.stdout.decode('ascii'))
    else:
        spec = (tf.TensorSpec((None, input_len), tf.float32, name="input"),)
        output_path = output_dir + "/model.onnx"
        model_proto, _ = tf2onnx.convert.from_keras(model, input_signature=spec, opset=13, output_path=output_path)

sep = ':'
if platform.system() == 'Windows':
    sep = ';'

CLASS_PATH = f'./agent/target/classes{sep}{os.getenv("M2_HOME")}/repository/com/microsoft/onnxruntime/{onnx_jar}{sep}./agent/src/resources/mallet.jar{sep}./agent/src/resources/mallet-deps.jar{sep}{os.getenv("M2_HOME")}/repository/org/jdom/jdom/1.1/jdom-1.1.jar{sep}{os.getenv("M2_HOME")}/repository/com/fasterxml/jackson/core/jackson-databind/2.12.4/jackson-databind-2.12.4.jar{sep}{os.getenv("M2_HOME")}/repository/com/fasterxml/jackson/core/jackson-annotations/2.12.4/jackson-annotations-2.12.4.jar{sep}{os.getenv("M2_HOME")}/repository/com/fasterxml/jackson/core/jackson-core/2.12.4/jackson-core-2.12.4.jar{sep}{os.getenv("M2_HOME")}/repository/org/apache/commons/commons-compress/1.21/commons-compress-1.21.jar{sep}{os.getenv("M2_HOME")}/repository/org/apache/commons/commons-math3/3.6.1/commons-math3-3.6.1.jar'


def snake_case(s):
    return ''.join(['_' + i.lower() if i.isupper()else i for i in s]).lstrip('_')

p = subprocess.run(['java', '--add-opens', 'java.base/java.util=ALL-UNNAMED', '-classpath', CLASS_PATH, 'com.alphaStS.Main', '--get-lengths'], capture_output=True)
lens_str = p.stdout.decode('ascii').split(',')
input_len = int(lens_str[0])
num_of_actions = int(lens_str[1])
v_other_lens = []
v_other_label = []
for i in range(2, len(lens_str), 2):
    v_other_label.append(snake_case(lens_str[i]))
    v_other_lens.append(int(lens_str[i + 1]))
v_other_len = sum(v_other_lens)
print(f'input_len={input_len}, policy_len={num_of_actions}, v_other_len={v_other_len}')


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


def softmax_cross_entropy_with_logits_simple(y_true, y_pred):
    return tf.nn.softmax_cross_entropy_with_logits(labels=y_true, logits=y_pred)


def mse_ignoring_out_of_bound(y_true, y_pred):
    # allow agent to output negative values to ignore training data for losses when training
    p = y_pred
    pi = y_true
    zero = tf.zeros(shape=tf.shape(pi), dtype=tf.float32)
    where = tf.less(pi, zero - 2)
    pi = tf.where(where, p, pi)
    return tf.keras.losses.MeanSquaredError()(p, pi)


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

custom_objects = {"softmax_cross_entropy_with_logits": softmax_cross_entropy_with_logits,
                  "softmax_cross_entropy_with_logits_simple": softmax_cross_entropy_with_logits_simple, "mse_ignoring_out_of_bound": mse_ignoring_out_of_bound}
if os.path.exists(f'{SAVES_DIR}/iteration{training_info["iteration"] - 1}'):
    with keras.utils.custom_object_scope(custom_objects):
        model = tf.keras.models.load_model(f'{SAVES_DIR}/iteration{training_info["iteration"] - 1}')
        # model.optimizer.lr.assign(0.01)
else:
    inputs = keras.Input(shape=(input_len,))
    # x = layers.BatchNormalization(axis=1)(inputs)
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
    exp_other_heads = []
    loss = {
        'exp_health_head': mse_ignoring_out_of_bound,
        'exp_win_head': mse_ignoring_out_of_bound,
        'policy_head': softmax_cross_entropy_with_logits
    }
    loss_weights = {'exp_health_head': 0.45, 'policy_head': 0.1, 'exp_win_head': 0.45}
    idx = 0
    for v_other in v_other_lens:
        if v_other == 1:
            if v_other_label[idx].startswith('z_'):
                loss[f'{v_other_label[idx]}_head'] = mse_ignoring_out_of_bound
                exp_other_heads.append(layers.Dense(1, name=f"{v_other_label[idx]}_head", use_bias=True, activation='tanh')(layers.Lambda(lambda x: tf.stop_gradient(x))(x)))
                loss_weights[f'{v_other_label[idx]}_head'] = 0.25
            else:
                loss[f'{v_other_label[idx]}_head'] = mse_ignoring_out_of_bound
                exp_other_heads.append(layers.Dense(1, name=f"{v_other_label[idx]}_head", use_bias=True, activation='tanh')(x))
                loss_weights[f'{v_other_label[idx]}_head'] = 0.25
        else:
            loss[f'{v_other_label[idx]}_head'] = softmax_cross_entropy_with_logits_simple
            exp_other_heads.append(layers.Dense(v_other, name=f"{v_other_label[idx]}_head", use_bias=True, activation='linear')(x))
            loss_weights[f'{v_other_label[idx]}_head'] = 0.45 if v_other_label[idx] == 'dmg_distribution' else 0.25
        idx += 1
    model = keras.Model(inputs=[inputs], outputs=[exp_health_head, exp_win_head, policy_head] + exp_other_heads)
    model.compile(
        loss=loss,
        loss_weights=loss_weights,
        optimizer=tf.keras.optimizers.SGD(learning_rate=0.1, momentum=0.9)
    )
    os.mkdir(f'{SAVES_DIR}/iteration0')
    model.save(f'{SAVES_DIR}/iteration0')
    convertToOnnx(model, input_len, f'{SAVES_DIR}/iteration0')


def init_layer(layer_name):
    for layer in model.layers:
        if layer.name == layer_name:
            layer.set_weights([layer.kernel_initializer(shape=np.asarray(layer.kernel.shape)),
                               layer.bias_initializer(shape=np.asarray(layer.bias.shape))])


def reset_model(model):
    layer = model.get_layer('exp_win_head')
    init_layer(layer)
    layer = model.get_layer('exp_health_head')
    init_layer(layer)
    layer = model.get_layer('policy_head')
    init_layer(layer)
    idx = 0
    for v_other in v_other_lens:
        layer = model.get_layer(f'{v_other_label[idx]}_head')
        init_layer(layer)
        idx += 1
    layer = model.get_layer('layer2')
    init_layer(layer)

start = time.time()
# np.set_printoptions(threshold=np.inf)


def get_training_samples(training_pool, iteration, file_path):
    if not os.path.exists(file_path) and not os.path.exists(file_path + '.lz4'):
        return
    if os.path.exists(file_path):
        with open(file_path, 'rb') as f:
            content = f.read()
    else:
        with lz4.frame.open(file_path + '.lz4', mode='r') as f:
            content = f.read()
    offset = 0
    while offset != len(content):
        x_fmt = '>' + ('f' * input_len)
        x = struct.unpack(x_fmt, content[offset: offset + 4 * input_len])
        v_fmt = '>' + 'f' * (2 + v_other_len)
        v = struct.unpack(v_fmt, content[offset + 4 * input_len:offset + 4 * (input_len + 2 + v_other_len)])
        p_fmt = '>' + ('f' * num_of_actions)
        p = struct.unpack(p_fmt, content[offset + 4 * (input_len + 2 + v_other_len):offset + 4 * (input_len + 2 + v_other_len + num_of_actions)])
        offset += 4 * (input_len + 2 + v_other_len + num_of_actions)
        target = [list(x), v[0], v[1], list(p), []]
        idx = 2
        for v_other in v_other_lens:
            if v_other == 1:
                target[4].append(v[idx])
            else:
                target[4].append(list(v[idx:idx + v_other]))
            idx += v_other
        training_pool.append((iteration, target))
    if len(content) != offset:
        print(f'{len(content) - offset} bytes remaining for decoding')
        raise "agent error"


SLOW_WINDOW_END = 3
TRAINING_WINDOW_SIZE = 6
CURRICULUM_TRAINING_END = SLOW_WINDOW_END + TRAINING_WINDOW_SIZE - 1


def expire_training_samples(training_pool, iteration):
    if iteration < SLOW_WINDOW_END:
        cutoff = iteration - 1
    else:
        cutoff = max(SLOW_WINDOW_END - 1, iteration - TRAINING_WINDOW_SIZE)
    i = 0
    while i < len(training_pool):
        if training_pool[i][0] >= cutoff:
            break
        i += 1
    return training_pool[i:]


def get_batch_size(training_pool_size):
    if DYNAMIC_BATCH_SIZE_FACTOR <= 0:
        return 32
    if training_pool_size > DYNAMIC_BATCH_SIZE_FACTOR * 128:
        return 128
    if training_pool_size > DYNAMIC_BATCH_SIZE_FACTOR * 64:
        return 64
    return 32


def save_stats(training_info, iteration, out):
    death_rate = float(re.findall('\nDeaths: \d+/\d+ \((\d+\.\d+)\%\)', out)[0])
    avg_dmg = float(re.findall('\nAvg Damage: (\-?\d+\.\d+)', out)[0])
    avg_dmg_tmp = re.findall('\nAvg Damage \(Not Including Deaths\): (\-?\d+\.\d+)', out)
    avg_dmg_no_death = float(avg_dmg if len(avg_dmg_tmp) == 0 else avg_dmg_tmp[0])
    dagger_killed_per_tmp = re.findall('\nDagger Killed Percentage: (\d+\.\d+)', out)
    dagger_killed_per = float(0 if len(dagger_killed_per_tmp) == 0 else dagger_killed_per_tmp[0])
    avg_final_q_tmp = re.findall('\nAverage Final Q: (\d+\.\d+)', out)
    avg_final_q = float(0 if len(avg_final_q_tmp) == 0 else avg_final_q_tmp[0])
    potion_stats = re.findall('\n(.*Used Percentage: \d+\.\d+)', out)
    for stat in potion_stats:
        key, value = stat.split(':')
        key = key.strip().lower().replace(' ', '_')
        value = float(value.strip())
        training_info['iteration_info'][str(iteration)][key] = value
    training_info['iteration_info'][str(iteration)]['death_rate'] = death_rate
    training_info['iteration_info'][str(iteration)]['avg_dmg'] = avg_dmg
    training_info['iteration_info'][str(iteration)]['avg_dmg_no_death'] = avg_dmg_no_death
    training_info['iteration_info'][str(iteration)]['dagger_killed_per'] = dagger_killed_per
    training_info['iteration_info'][str(iteration)]['avg_final_q'] = avg_final_q


def init_kaggle(dataset_name):
    if not os.path.exists(f'./kaggle/{dataset_name}/dataset-metadata.json'):
        try:
            os.mkdir(f'./kaggle')
        except:
            pass
        try:
            os.mkdir(f'./kaggle/{dataset_name}')
        except:
            pass
        with open(f'./kaggle/{dataset_name}/dataset-metadata.json', 'w') as f:
            f.write('{\n')
            f.write(f'    "title": "alphaStS-{dataset_name}",\n')
            f.write(f'    "id": "{KAGGLE_USER_NAME}/alphaStS-{dataset_name}",\n')
            f.write(f'    "licenses": [{{\n')
            f.write(f'        "name": "CC0-1.0"\n')
            f.write(f'    }}]\n')
            f.write('}\n')
        with open(f'./kaggle/{dataset_name}/training.json', 'w') as f:
            pass
        output = subprocess.run(['kaggle', 'datasets', 'create', '-p', f'./kaggle/{dataset_name}', '--dir-mode', 'tar'], capture_output=True)
        print(output.stderr.decode('ascii'))
        print(output.stdout.decode('ascii'))
        wait_for_kaggle_dataset_to_be_ready(dataset_name)


def copy_to_kaggle_folder(iter_dir, model, dataset_name):
    iter_folder = os.path.split(os.path.split(iter_dir)[0])[1]
    src = f'{iter_dir}.lz4'
    dst = f'./kaggle/{dataset_name}/{iter_folder}/training_data.bin.lz4'
    if not os.path.exists(os.path.split(dst)[0]):
        try:
            os.mkdir(os.path.split(dst)[0])
        except:
            pass
    shutil.copyfile(src, dst)
    if model is not None:
        model.save(f'./kaggle/{dataset_name}/{iter_folder}')


def expire_kaggle_folder(dataset_name, iteration):
    if iteration < SLOW_WINDOW_END:
        cutoff = iteration - 1
    else:
        cutoff = max(SLOW_WINDOW_END - 1, iteration - TRAINING_WINDOW_SIZE)
    dir_list = os.listdir(f'./kaggle/{dataset_name}')
    for p in dir_list:
        if p.startswith('iteration'):
            i = int(p[9:])
            if i < cutoff or i > iteration:
                shutil.rmtree(f'./kaggle/{dataset_name}/{p}')
            elif i != iteration - 1:
                for p2 in os.listdir(f'./kaggle/{dataset_name}/{p}'):
                    if not p2.endswith('.lz4'):
                        if os.path.isdir(f'./kaggle/{dataset_name}/{p}/{p2}'):
                            shutil.rmtree(f'./kaggle/{dataset_name}/{p}/{p2}')
                        else:
                            os.remove(f'./kaggle/{dataset_name}/{p}/{p2}')


def update_kaggle_dataset(dataset_name, iteration):
    with open(f'./kaggle/{dataset_name}/training.json', 'w') as f:
        json.dump({
            'iteration': iteration,
            'input_len': input_len,
            'num_of_actions': num_of_actions,
            'v_other_lens': v_other_lens,
            'SLOW_WINDOW_END': SLOW_WINDOW_END,
            'TRAINING_WINDOW_SIZE': TRAINING_WINDOW_SIZE,
        }, f)
    output = subprocess.run(['kaggle', 'datasets', 'version', '-p', f'./kaggle/{dataset_name}', '--delete-old-versions', '--dir-mode', 'tar', '-m', 'update'], capture_output=True)
    print(output.stderr.decode('ascii'))
    print(output.stdout.decode('ascii'))
    wait_for_kaggle_dataset_to_be_ready(dataset_name)


def wait_for_kaggle_dataset_to_be_ready(dataset_name):
    while True:
        output = subprocess.run(['kaggle', 'datasets', 'status', f'{KAGGLE_USER_NAME}/alphaStS-{dataset_name}'], capture_output=True)
        if output.stderr is not None and len(output.stderr) > 0:
            print(output.stderr.decode('ascii'))
            raise "Error"
        if output.stdout.decode('ascii') == 'ready':
            break
        time.sleep(1)


def run_kaggle_kernel(dataset_name):
    if not os.path.exists('./kaggle/kernel/kernel-metadata.json'):
        try:
            os.mkdir('./kaggle/kernel')
        except:
            pass
        with open(f'./kaggle/kernel/kernel-metadata.json', 'w') as f:
            f.write('{\n')
            f.write(f'    "id": "{KAGGLE_USER_NAME}/alphaStS-kernel",')
            f.write(f'    "title": "AlphaStS kernel",')
            f.write(f'    "code_file": "ml_kaggle.ipynb",')
            f.write(f'    "language": "python",')
            f.write(f'    "kernel_type": "notebook",')
            f.write(f'    "is_private": "true",')
            f.write(f'    "enable_gpu": "true",')
            f.write(f'    "enable_internet": "true",')
            f.write(f'    "dataset_sources": ["{KAGGLE_USER_NAME}/alphaStS-dataset"],')
            f.write(f'    "competition_sources": [],')
            f.write(f'    "kernel_sources": []')
            f.write('}\n')
        shutil.copyfile('ml_kaggle.ipynb', './kaggle/kernel/ml_kaggle.ipynb')
    output = subprocess.run(['kaggle', 'kernels', 'push', '-p', './kaggle/kernel'], capture_output=True)
    print(output.stderr.decode('ascii'))
    print(output.stdout.decode('ascii'))


def wait_for_kaggle_kernel_to_finish():
    while True:
        output = subprocess.run(['kaggle', 'kernels', 'status', f'{KAGGLE_USER_NAME}/alphasts-kernel'], capture_output=True)
        if output.stderr is not None and len(output.stderr) > 0:
            print(output.stderr.decode('ascii'))
            raise "Error"
        o = output.stdout.decode('ascii')
        if 'complete' in o:
            return
        elif 'error' in o:
            retrieve_kaggle_kernel_output(False, 0)
            raise "Error"
        time.sleep(1)


def retrieve_kaggle_kernel_output(copy_model, iteration):
    output = subprocess.run(['kaggle', 'kernels', 'output', f'{KAGGLE_USER_NAME}/alphasts-kernel', '-p', './kaggle/output'], capture_output=True)
    if output.stderr is not None and len(output.stderr) > 0:
        print(output.stderr.decode('ascii'))
        raise "Error"
    print(output.stdout.decode('ascii'))
    if copy_model:
        if os.path.exists(f'{SAVES_DIR}/iteration{iteration}'):
            shutil.rmtree(f'{SAVES_DIR}/iteration{iteration}')
        shutil.copytree(f'./kaggle/output/iteration{iteration}', f'{SAVES_DIR}/iteration{iteration}')


accumualted_time_base = 0
if training_info['iteration'] > 1:
    accumualted_time_base = training_info['iteration_info'][str(int(training_info['iteration']) - 1)]['accumulated_time']

if DO_TRAINING:
    if USE_KAGGLE:
        init_kaggle(KAGGLE_DATASET_NAME)
    training_pool = []
    start_window = 0
    if training_info['iteration'] >= SLOW_WINDOW_END:
        start_window = max(SLOW_WINDOW_END, training_info['iteration'] - TRAINING_WINDOW_SIZE)
        for i in range(start_window, training_info['iteration'] - 1):
            print(f'loading data from {SAVES_DIR}/iteration{i}/training_data.bin')
            if USE_KAGGLE:
                copy_to_kaggle_folder(f'{SAVES_DIR}/iteration{i}/training_data.bin', None, KAGGLE_DATASET_NAME)
            else:
                get_training_samples(training_pool, i, f'{SAVES_DIR}/iteration{i}/training_data.bin')

    start = time.time()
    for _iteration in range(training_info["iteration"], ITERATION_COUNT + 1):
        training_info['iteration_info'][str(_iteration)] = {}
        iteration_info = training_info['iteration_info'][str(_iteration)]
        iter_start = time.time()

        agent_args = ['java', '--add-opens', 'java.base/java.util=ALL-UNNAMED', '-classpath',
                      'com.alphaStS.Main', '--training', '-t', str(NUMBER_OF_THREADS), '-b', str(BATCH_PER_THREAD), '-training-c', str(TRAINING_NUM_OF_GAMES), '-training-n', str(TRAINING_NODE_COUNT), '-dir', SAVES_DIR]
        if not SKIP_TRAINING_MATCHES and _iteration > 1:
            if training_info["iteration"] < 17:
                matches_count = 1000
            elif training_info["iteration"] < 25:
                matches_count = 1000 + 500 * (training_info["iteration"] - 17)
            else:
                matches_count = 5000
            agent_args += ['-c', str(matches_count), '-n', '1']
        if Z_TRAIN_WINDOW_END > 0:
            agent_args += ['-z_train', str(Z_TRAIN_WINDOW_END)]
        if training_info['iteration'] < CURRICULUM_TRAINING_END:
            agent_args += ['-curriculum_training']
        agent_output = ''
        if not SKIP_FIRST:
            p = subprocess.Popen(agent_args, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
            while p.poll() is None:
                line = p.stdout.readline().decode('ascii')
                print(line, end='', flush=True)
                agent_output += line
            err = p.stderr.readlines()
            if len(err) > 0:
                [print(line.decode('ascii'), end='', flush=True) for line in err]
                raise "agent error"
        else:
            SKIP_FIRST = False

        if len(agent_output) > 0 and not SKIP_TRAINING_MATCHES and _iteration > 1:
            split = agent_output.find('--------------------')
            out = agent_output[2 if agent_output[0] == '\r' else 0: split + 20]
            save_stats(training_info, _iteration - 1, out)

        print(f'Iteration {training_info["iteration"]}')
        if training_info["iteration"] > 10 and (training_info["iteration"] - 21) % 15 == 0:
            print("Model layers reset!!!")
            reset_model(model)

        iteration_info['agent_time'] = round(time.time() - iter_start, 2)
        iteration_info['num_of_samples'] = len(training_pool)
        print(f'agent time={iteration_info["agent_time"]}')

        iter_start = time.time()
        if USE_KAGGLE:
            copy_to_kaggle_folder(f'{SAVES_DIR}/iteration{training_info["iteration"] - 1}/training_data.bin', model, KAGGLE_DATASET_NAME)
            expire_kaggle_folder(KAGGLE_DATASET_NAME, training_info["iteration"])
            update_kaggle_dataset(KAGGLE_DATASET_NAME, training_info["iteration"])
            time.sleep(5)
            run_kaggle_kernel(KAGGLE_DATASET_NAME)
            wait_for_kaggle_kernel_to_finish()
            retrieve_kaggle_kernel_output(True, training_info["iteration"])
            with keras.utils.custom_object_scope(custom_objects):
                model = tf.keras.models.load_model(f'{SAVES_DIR}/iteration{training_info["iteration"]}')
        else:
            get_training_samples(training_pool, training_info["iteration"] - 1, f'{SAVES_DIR}/iteration{training_info["iteration"] - 1}/training_data.bin')
            training_pool = expire_training_samples(training_pool, training_info["iteration"])
            print(f'number of samples={len(training_pool)}')
            print(f'sample oldest iteration={training_pool[0][0]}')
            train_iter = 10 if training_info['iteration'] < SLOW_WINDOW_END + TRAINING_WINDOW_SIZE - 1 else 5
            for _i in range(1):
                minibatch = training_pool
                x_train = np.zeros((len(minibatch), input_len), dtype=float)
                exp_health_head_train = np.zeros((len(minibatch), 1), dtype=float)
                exp_win_head_train = np.zeros((len(minibatch), 1), dtype=float)
                policy_head_train = np.zeros((len(minibatch), num_of_actions), dtype=float)
                exp_other_heads_train = []
                for v_other in v_other_lens:
                    exp_other_heads_train.append(np.zeros((len(minibatch), v_other), dtype=float))
                minibatch_idx = 0
                for _, (x, v_win, v_health, p, v_others) in minibatch:
                    x_train[minibatch_idx] = np.asarray(x)
                    exp_health_head_train[minibatch_idx] = np.asarray(v_health).reshape(1)
                    exp_win_head_train[minibatch_idx] = np.asarray(v_win).reshape(1)
                    policy_head_train[minibatch_idx] = np.asarray(p).reshape(num_of_actions)
                    for i in range(len(v_other_lens)):
                        exp_other_heads_train[i][minibatch_idx] = np.asarray(v_others[i]).reshape(v_other_lens[i])
                    minibatch_idx += 1
                target = [exp_health_head_train, exp_win_head_train, policy_head_train] + exp_other_heads_train
                print(f"batch_size={get_batch_size(len(training_pool))}")
                fit_result = model.fit(x_train, target, epochs=train_iter, batch_size=get_batch_size(len(training_pool)))
                iteration_info['loss'] = fit_result.history['loss'][-1]
            if not os.path.exists(f'{SAVES_DIR}/iteration{training_info["iteration"]}'):
                os.mkdir(f'{SAVES_DIR}/iteration{training_info["iteration"]}')
            model.save(f'{SAVES_DIR}/iteration{training_info["iteration"]}')

        convertToOnnx(model, input_len, f'{SAVES_DIR}/iteration{training_info["iteration"]}')
        training_info['iteration'] += 1
        iteration_info['training_time'] = round(time.time() - iter_start, 2)
        iteration_info['accumulated_time'] = accumualted_time_base + round(time.time() - start, 2)
        print(f'training time={iteration_info["training_time"]}')
        print(f'accumulated time={iteration_info["accumulated_time"]}')
        with open(f'{SAVES_DIR}/training.json', 'w') as f:
            json.dump(training_info, f)

        if _iteration == ITERATION_COUNT:
            agent_output = subprocess.run(['java', '--add-opens', 'java.base/java.util=ALL-UNNAMED', '-classpath', CLASS_PATH,
                                           'com.alphaStS.Main', '--training', '-tm', '-t', str(NUMBER_OF_THREADS), '-b', str(BATCH_PER_THREAD), '-c', '5000', '-n', '1', '-dir', SAVES_DIR], capture_output=True)
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
    agent_args = ['java', '--add-opens', 'java.base/java.util=ALL-UNNAMED', '-classpath', CLASS_PATH, 'com.alphaStS.Main', '--server']
    agent_output = subprocess.run(agent_args, capture_output=True)
    print(agent_output.stdout.decode('ascii'))
    print(agent_output.stderr.decode('ascii'))
    print(time.time() - start)

if PLAY_MATCHES:
    agent_args = ['java', '--add-opens', 'java.base/java.util=ALL-UNNAMED', '-classpath', CLASS_PATH, 'com.alphaStS.Main', '--client']
    agent_output = subprocess.run(agent_args, capture_output=True)
    print(agent_output.stdout.decode('ascii'))
    print(agent_output.stderr.decode('ascii'))
    print(time.time() - start)
