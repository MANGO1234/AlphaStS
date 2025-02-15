# format
import subprocess
import platform
import os

sep = ':'
if platform.system() == 'Windows':
    sep = ';'


CLASS_PATH = f'./target/classes{sep}{os.getenv("M2_HOME")}/repository/com/microsoft/onnxruntime/onnxruntime/1.10.0/onnxruntime-1.10.0.jar{sep}./src/resources/mallet.jar{sep}./src/resources/mallet-deps.jar{sep}{os.getenv("M2_HOME")}/repository/org/jdom/jdom/1.1/jdom-1.1.jar{sep}{os.getenv("M2_HOME")}/repository/com/fasterxml/jackson/core/jackson-databind/2.12.4/jackson-databind-2.12.4.jar{sep}{os.getenv("M2_HOME")}/repository/com/fasterxml/jackson/core/jackson-annotations/2.12.4/jackson-annotations-2.12.4.jar{sep}{os.getenv("M2_HOME")}/repository/com/fasterxml/jackson/core/jackson-core/2.12.4/jackson-core-2.12.4.jar{sep}{os.getenv("M2_HOME")}/repository/org/apache/commons/commons-compress/1.21/commons-compress-1.21.jar{sep}{os.getenv("M2_HOME")}/repository/org/apache/commons/commons-math3/3.6.1/commons-math3-3.6.1.jar{sep}{os.getenv("M2_HOME")}/repository/one/util/streamex/0.8.3/streamex-0.8.3.jar'

os.chdir('agent')
agent_args = ['java', '--add-opens', 'java.base/java.util=ALL-UNNAMED', '-classpath', CLASS_PATH, 'com.alphaStS.Main', '-i']
subprocess.run(agent_args)
