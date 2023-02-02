!pip install lz4

import numpy as np
import lz4.frame
import struct
import json
import tensorflow as tf
from tensorflow import keras
from tensorflow.keras import layers

import os
import os.path
for dirname, _, filenames in os.walk('/kaggle/input'):
    for filename in filenames:
        print(os.path.join(dirname, filename))

SAVES_DIR = '/kaggle/input/alphaStS-dataset'

with open(f'{SAVES_DIR}/training.json', 'r') as f:
    training_info_raw = json.load(f)

input_len = training_info_raw['input_len']
num_of_actions = training_info_raw['num_of_actions']
v_other_lens = training_info_raw['v_other_lens']
v_other_len = sum(v_other_lens)
training_info = {}
training_info['iteration'] = training_info_raw['iteration']
SLOW_WINDOW_END = training_info_raw['SLOW_WINDOW_END']
TRAINING_WINDOW_SIZE = training_info_raw['TRAINING_WINDOW_SIZE']

print("Num GPUs Available: ", tf.config.list_physical_devices('GPU'))


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


if os.path.exists(f'{SAVES_DIR}/iteration{training_info["iteration"] - 1}'):
    custom_objects = {"softmax_cross_entropy_with_logits": softmax_cross_entropy_with_logits,
                      "softmax_cross_entropy_with_logits_simple": softmax_cross_entropy_with_logits_simple, "mse_ignoring_out_of_bound": mse_ignoring_out_of_bound}
    with keras.utils.custom_object_scope(custom_objects):
        model = tf.keras.models.load_model(f'{SAVES_DIR}/iteration{training_info["iteration"] - 1}')


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
        target = [list(x), v[0], v[1], list(p), [v_other for v_other in v[2:]]]
        training_pool.append((iteration, target))
    if len(content) != offset:
        print(f'{len(content) - offset} bytes remaining for decoding')
        raise "agent error"


training_pool = []
start_window = 0
if training_info['iteration'] >= SLOW_WINDOW_END:
    start_window = max(SLOW_WINDOW_END, training_info['iteration'] - TRAINING_WINDOW_SIZE)
    for i in range(start_window, training_info['iteration'] - 1):
        print(f'loading data from {SAVES_DIR}/iteration{i}/training_data.bin')
        get_training_samples(training_pool, i, f'{SAVES_DIR}/iteration{i}/training_data.bin')
get_training_samples(training_pool, training_info["iteration"] - 1, f'{SAVES_DIR}/iteration{training_info["iteration"] - 1}/training_data.bin')

print(f'number of samples={len(training_pool)}')

train_iter = 10 if training_info['iteration'] < SLOW_WINDOW_END + TRAINING_WINDOW_SIZE - 1 else 5
for _i in range(train_iter):
    minibatch = training_pool
    x_train = []
    exp_health_head_train = []
    exp_win_head_train = []
    policy_head_train = []
    exp_other_heads_train = []
    for i in range(len(v_other_lens)):
        exp_other_heads_train.append([])
    for _, (x, v_win, v_health, p, v_others) in minibatch:
        x_train.append(np.asarray(x))
        exp_health_head_train.append(np.asarray(v_health).reshape(1))
        exp_win_head_train.append(np.asarray(v_win).reshape(1))
        policy_head_train.append(np.asarray(p).reshape(num_of_actions))
        for i in range(len(v_other_lens)):
            exp_other_heads_train[i].append(np.asarray(v_others[i]).reshape(v_other_lens[i]))
    x_train = np.asarray(x_train)
    exp_health_head_train = np.asarray(exp_health_head_train)
    exp_win_head_train = np.asarray(exp_win_head_train)
    policy_head_train = np.asarray(policy_head_train)
    for i in range(len(v_other_lens)):
        exp_other_heads_train[i] = np.asarray(exp_other_heads_train[i])
    target = [exp_health_head_train, exp_win_head_train, policy_head_train] + exp_other_heads_train
    fit_result = model.fit(np.asarray(x_train), target, epochs=1)
model.save(f'/kaggle/working/iteration{training_info["iteration"]}')
