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
import tf2onnx
from misc import getFlag, getFlagValue

# rand.seed(5)
# np.random.seed(5)
# tf.random.set_seed(5)

DO_TRAINING = getFlag('-training')
SKIP_TRAINING_MATCHES = getFlag('-s')
PLAY_A_GAME = getFlag('-p')
PLAY_MATCHES = getFlag('-m')
NUMBER_OF_THREADS = int(getFlagValue('-t', 1))
NUMBER_OF_THREADS_TRAINING = int(getFlagValue('-tt', 0))
ITERATION_COUNT = int(getFlagValue('-c', 5))
Z_TRAIN_WINDOW_END = int(getFlagValue('-z', -1))
NODE_COUNT = int(getFlagValue('-n', 1000))
SAVES_DIR = getFlagValue('-dir', './saves')
SKIP_FIRST = getFlag('-skip_first')
USE_KAGGLE = getFlagValue('-kaggle')
KAGGLE_USER_NAME = getFlagValue('-kaggle_user', None)
KAGGLE_DATASET_NAME = 'dataset'

if NUMBER_OF_THREADS_TRAINING > 0:
    tf.config.threading.set_intra_op_parallelism_threads(1)
    tf.config.threading.set_inter_op_parallelism_threads(1)
    os.environ["OMP_NUM_THREADS"] = f"1"
    os.environ['TF_NUM_INTEROP_THREADS'] = f"1"
    os.environ['TF_NUM_INTRAOP_THREADS'] = f"1"


def convertToOnnx(model, input_len, output_dir):
    spec = (tf.TensorSpec((None, input_len), tf.float32, name="input"),)
    output_path = output_dir + "/model.onnx"
    model_proto, _ = tf2onnx.convert.from_keras(model, input_signature=spec, opset=13, output_path=output_path)

sep = ':'
if platform.system() == 'Windows':
    sep = ';'

CLASS_PATH = f'./agent/target/classes{sep}{os.getenv("M2_HOME")}/repository/com/microsoft/onnxruntime/onnxruntime/1.10.0/onnxruntime-1.10.0.jar{sep}./agent/src/resources/mallet.jar{sep}./agent/src/resources/mallet-deps.jar{sep}{os.getenv("M2_HOME")}/repository/org/jdom/jdom/1.1/jdom-1.1.jar{sep}{os.getenv("M2_HOME")}/repository/com/fasterxml/jackson/core/jackson-databind/2.12.4/jackson-databind-2.12.4.jar{sep}{os.getenv("M2_HOME")}/repository/com/fasterxml/jackson/core/jackson-annotations/2.12.4/jackson-annotations-2.12.4.jar{sep}{os.getenv("M2_HOME")}/repository/com/fasterxml/jackson/core/jackson-core/2.12.4/jackson-core-2.12.4.jar{sep}{os.getenv("M2_HOME")}/repository/org/apache/commons/commons-compress/1.21/commons-compress-1.21.jar{sep}{os.getenv("M2_HOME")}/repository/org/apache/commons/commons-math3/3.6.1/commons-math3-3.6.1.jar'


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
        convertToOnnx(model, input_len, f'{SAVES_DIR}/iteration{training_info["iteration"] - 1}')
