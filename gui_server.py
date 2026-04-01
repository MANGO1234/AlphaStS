import subprocess
import platform
import os
import sys

sep = ':'
if platform.system() == 'Windows':
    sep = ';'

# Use M2_REPO env var or default to ~/.m2/repository
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

os.chdir('./agent')

# Forward any extra args (e.g. --port 8080 or --sts-parse-path /some/path)
extra_args = sys.argv[1:]

agent_args = [
    'java',
    '--add-opens', 'java.base/java.util=ALL-UNNAMED',
    '-classpath', CLASS_PATH,
    'com.alphaStS.Main',
    '--gui-server',
] + extra_args

subprocess.run(agent_args)
