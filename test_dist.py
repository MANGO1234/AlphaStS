# format

import platform
import subprocess
import os
import time
from misc import getFlag, getFlagValue

PLAY_A_GAME = getFlag('-p')
PLAY_MATCHES = getFlag('-m')

sep = ':'
if platform.system() == 'Windows':
    sep = ';'

CLASS_PATH_AGENT = f'./target/classes{sep}{os.getenv("M2_HOME")}/repository/com/microsoft/onnxruntime/onnxruntime/1.10.0/onnxruntime-1.10.0.jar{sep}./src/resources/mallet.jar{sep}./src/resources/mallet-deps.jar{sep}{os.getenv("M2_HOME")}/repository/org/jdom/jdom/1.1/jdom-1.1.jar{sep}{os.getenv("M2_HOME")}/repository/com/fasterxml/jackson/core/jackson-databind/2.12.4/jackson-databind-2.12.4.jar{sep}{os.getenv("M2_HOME")}/repository/com/fasterxml/jackson/core/jackson-annotations/2.12.4/jackson-annotations-2.12.4.jar{sep}{os.getenv("M2_HOME")}/repository/com/fasterxml/jackson/core/jackson-core/2.12.4/jackson-core-2.12.4.jar{sep}{os.getenv("M2_HOME")}/repository/org/apache/commons/commons-compress/1.21/commons-compress-1.21.jar{sep}{os.getenv("M2_HOME")}/repository/org/apache/commons/commons-math3/3.6.1/commons-math3-3.6.1.jar'

start = time.time()

if PLAY_A_GAME:
    os.chdir('./agent')
    agent_args = ['java', '--add-opens', 'java.base/java.util=ALL-UNNAMED', '-classpath', CLASS_PATH_AGENT, 'com.alphaStS.Main', '--server']
    if platform.system() != 'Windows':
        agent_args = agent_args[:1] + ['-Xmx700m'] + agent_args[1:]
    p = subprocess.Popen(agent_args, stdout=subprocess.PIPE)
    while p.poll() is None:
        print(p.stdout.readline().decode('ascii'), end='', flush=True)
    [print(line.decode('ascii'), end='', flush=True) for line in p.stderr.readlines()]
    p.terminate()
    print(time.time() - start)

if PLAY_MATCHES:
    os.chdir('./agent')
    agent_args = ['java', '--add-opens', 'java.base/java.util=ALL-UNNAMED', '-classpath', CLASS_PATH_AGENT, 'com.alphaStS.Main', '-p', '-g', '-c', 200, '-n', 100]
    if platform.system() != 'Windows':
        agent_args = agent_args[:1] + ['-Xmx700m'] + agent_args[1:]
    p = subprocess.Popen(agent_args, stdout=subprocess.PIPE)
    while p.poll() is None:
        print(p.stdout.readline().decode('ascii'), end='', flush=True)
    [print(line.decode('ascii'), end='', flush=True) for line in p.stderr.readlines()]
    print(time.time() - start)
