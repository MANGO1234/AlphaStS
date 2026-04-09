import subprocess
import platform
import os
import sys
import argparse

sep = ':'
if platform.system() == 'Windows':
    sep = ';'

M2_REPO = os.getenv("M2_REPO", os.path.expanduser("~/.m2/repository"))

CLASS_PATH = sep.join([
    './target/classes',
    f'{M2_REPO}/com/microsoft/onnxruntime/onnxruntime_gpu/1.10.0/onnxruntime_gpu-1.10.0.jar',
    f'{M2_REPO}/org/jdom/jdom/1.1/jdom-1.1.jar',
    f'{M2_REPO}/com/fasterxml/jackson/core/jackson-databind/2.18.4/jackson-databind-2.18.4.jar',
    f'{M2_REPO}/com/fasterxml/jackson/core/jackson-annotations/2.18.4/jackson-annotations-2.18.4.jar',
    f'{M2_REPO}/com/fasterxml/jackson/core/jackson-core/2.18.4/jackson-core-2.18.4.jar',
    f'{M2_REPO}/org/apache/commons/commons-compress/1.26.0/commons-compress-1.26.0.jar',
    f'{M2_REPO}/org/apache/commons/commons-math3/3.6.1/commons-math3-3.6.1.jar',
    f'{M2_REPO}/one/util/streamex/0.8.3/streamex-0.8.3.jar',
])

parser = argparse.ArgumentParser()
parser.add_argument('--generate', action='store_true', help='Use --test-run-generate instead of --test-run-parser')
parser.add_argument('--ip', default=None, help='IP address for sendBattleDefinition (only used with --generate)')
parser.add_argument('run_file', nargs='?', default='../../b.run')
parser.add_argument('run_idx', nargs='?', default=None)
args = parser.parse_args()

os.chdir('./agent')

if args.generate:
    cmd = [
        'java',
        '--add-opens', 'java.base/java.util=ALL-UNNAMED',
        '-classpath', CLASS_PATH,
        'com.alphaStS.Main',
        '--test-run-generate', args.run_file,
    ]
    if args.run_idx is not None:
        cmd.append(args.run_idx)
    if args.ip is not None:
        cmd.extend(['--ip', args.ip])
else:
    cmd = [
        'java',
        '--add-opens', 'java.base/java.util=ALL-UNNAMED',
        '-classpath', CLASS_PATH,
        'com.alphaStS.Main',
        '--test-run-parser', args.run_file,
    ]
    if args.run_idx is not None:
        cmd.append(args.run_idx)

subprocess.run(cmd)
