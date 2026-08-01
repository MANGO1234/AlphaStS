# format

import platform
import subprocess
import os
import time
from misc import getFlag, getFlagValue

RUN_SERVER = getFlag('-server')
PLAY_MATCHES = getFlag('-matches')
THREAD_COUNT = int(getFlagValue('-t', 1))
BATCH_PER_THREAD = int(getFlagValue('-b', 1))
NUMBER_OF_GAMES = int(getFlagValue('-c', 200))
NUMBER_OF_GAMES_PER_SCENARIO = getFlagValue('-c_scenario')
NUMBER_OF_NODES = int(getFlagValue('-n', 100))
AGENT_JAR = getFlagValue('-jar')
SAVES_DIR = getFlagValue('-dir', './saves')
USE_KAGGLE = getFlag('-kaggle')
USE_GPU = getFlag('-gpu')
USE_GPU_AGENT_ONLY = getFlag('-gpu_agent')

start = time.time()

sep = ':'
if platform.system() == 'Windows':
    sep = ';'

if not USE_GPU:
    import tf2onnx
    onnx_jar = "onnxruntime_gpu/1.10.0/onnxruntime_gpu-1.10.0.jar" if USE_GPU_AGENT_ONLY else "onnxruntime/1.10.0/onnxruntime-1.10.0.jar"
else:
    onnx_jar = "onnxruntime_gpu/1.10.0/onnxruntime_gpu-1.10.0.jar"

CLASS_PATH = f'./agent/target/classes{sep}{os.getenv("M2_HOME")}/repository/com/microsoft/onnxruntime/{onnx_jar}{sep}./agent/src/resources/mallet.jar{sep}./agent/src/resources/mallet-deps.jar{sep}{os.getenv("M2_HOME")}/repository/org/jdom/jdom/1.1/jdom-1.1.jar{sep}{os.getenv("M2_HOME")}/repository/com/fasterxml/jackson/core/jackson-databind/2.12.4/jackson-databind-2.12.4.jar{sep}{os.getenv("M2_HOME")}/repository/com/fasterxml/jackson/core/jackson-annotations/2.12.4/jackson-annotations-2.12.4.jar{sep}{os.getenv("M2_HOME")}/repository/com/fasterxml/jackson/core/jackson-core/2.12.4/jackson-core-2.12.4.jar{sep}{os.getenv("M2_HOME")}/repository/org/apache/commons/commons-compress/1.21/commons-compress-1.21.jar{sep}{os.getenv("M2_HOME")}/repository/org/apache/commons/commons-math3/3.6.1/commons-math3-3.6.1.jar{sep}{os.getenv("M2_HOME")}/repository/one/util/streamex/0.8.3/streamex-0.8.3.jar'


def make_agent_args(args):
    agent_args = ['java']
    agent_args += ['--add-opens', 'java.base/java.util=ALL-UNNAMED']
    if AGENT_JAR is not None:
        agent_args += ['-jar', AGENT_JAR]
    else:
        agent_args += ['-classpath', CLASS_PATH, 'com.alphaStS.Main']
    return agent_args + args

if RUN_SERVER:
    if not USE_KAGGLE:
        os.chdir('./agent')
    args = ['--server', '-t', str(THREAD_COUNT), '-b', str(BATCH_PER_THREAD)]
    if platform.system() != 'Windows':
        args = ['-Xmx10G'] + args
    p = subprocess.Popen(make_agent_args(args), stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    while p.poll() is None:
        print(p.stdout.readline().decode('ascii'), end='', flush=True)
    [print(line.decode('ascii'), end='', flush=True) for line in p.stderr.readlines()]
    p.terminate()
    print(time.time() - start)

if PLAY_MATCHES:
    if not USE_KAGGLE:
        os.chdir('./agent')
    args = ['--play', '-c', str(NUMBER_OF_GAMES), '-n', str(NUMBER_OF_NODES), '-t', str(THREAD_COUNT), '-b', str(BATCH_PER_THREAD), '-dir', SAVES_DIR]
    if NUMBER_OF_GAMES_PER_SCENARIO is not None:
        args += ['-c_scenario', NUMBER_OF_GAMES_PER_SCENARIO]
    if platform.system() != 'Windows':
        args = ['-Xmx10G'] + args
    p = subprocess.Popen(make_agent_args(args), stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    while p.poll() is None:
        print(p.stdout.readline().decode('ascii'), end='', flush=True)
    [print(line.decode('ascii'), end='', flush=True) for line in p.stderr.readlines()]
    print(time.time() - start)
