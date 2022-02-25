import subprocess
import os
import numpy
import struct
import sys
print(sys.byteorder)

lens_str = subprocess.run(['java', '-classpath', f'F:/git/alphaStS/agent/target/classes;{os.getenv("M2_HOME")}/repository/com/microsoft/onnxruntime/onnxruntime/1.10.0/onnxruntime-1.10.0.jar',
                           'com.alphaStS.Main', '--get-lengths'], capture_output=True).stdout.decode('ascii').split(',')
input_len = int(lens_str[0])
num_of_actions = int(lens_str[1])
agent_output = subprocess.run(['java', '-classpath', f'F:/git/alphaStS/agent/target/classes;{os.getenv("M2_HOME")}/repository/com/microsoft/onnxruntime/onnxruntime/1.10.0/onnxruntime-1.10.0.jar',
                           'com.alphaStS.Main', '-tm', '-c', '1', '-t', '-dir', './tmp'], capture_output=True).stdout
split1 = agent_output.find(b'--------------------')
print(agent_output[0:split1+20].decode('ascii'))
split2 = agent_output.find(b'--------------------', split1 + 20)
print(agent_output[split1+20:split2+20].decode('ascii'))
agent_output = agent_output[split2+20:]
print(agent_output[split2+20:])
print(len(agent_output))

agent_output = subprocess.run(['java', '-classpath', f'./agent/target/classes;{os.getenv("M2_HOME")}/repository/com/microsoft/onnxruntime/onnxruntime/1.10.0/onnxruntime-1.10.0.jar;./agent/src/resources/mallet.jar;./agent/src/resources/mallet-deps.jar', 'com.alphaStS.Main', '-t', '-dir', './tmp'], capture_output=True)
if len(agent_output.stderr) > 0:
    print(agent_output.stderr.decode('ascii'))
    raise "agent error"
agent_output = agent_output.stdout

split = agent_output.find(b'--------------------')
# print(agent_output[0:split + 20].decode('ascii'))
agent_output = agent_output[split + 20:]

training_pool = []
offset = 0
# technically N^2
while len(agent_output) > 0:
    x_fmt = '>' + ('f' * input_len)
    x = struct.unpack(x_fmt, agent_output[0:4 * input_len])
    [v_health, v_win] = struct.unpack('>ff', agent_output[4 * input_len:4 * (input_len + 2)])
    p_fmt = '>' + ('f' * num_of_actions)
    p = struct.unpack(p_fmt, agent_output[4 * (input_len + 2):4 * (input_len + 2 + num_of_actions)])
    agent_output = agent_output[4 * (input_len + 2 + num_of_actions):]
    training_pool.append([list(x), [v_health], [v_win], list(p)])
if len(agent_output) != 0:
    print(f'{len(agent_output)} bytes remaining for decoding')
    raise "agent error"


agent_output = subprocess.run(['java', '-classpath', f'./agent/target/classes;{os.getenv("M2_HOME")}/repository/com/microsoft/onnxruntime/onnxruntime/1.10.0/onnxruntime-1.10.0.jar;./agent/src/resources/mallet.jar;./agent/src/resources/mallet-deps.jar', 'com.alphaStS.Main', '-i', '-dir', './tmp'], capture_output=True)

# print(training_pool)
# x_fmt = '>' + ('f' * input_len)
# x = struct.unpack(x_fmt, agent_output[0:4*input_len])
# [v_health, v_win]  = struct.unpack('>ff', agent_output[4*input_len:4*(input_len+2)])
# p_fmt = '>' + ('f' * num_of_actions)
# p  = struct.unpack(p_fmt, agent_output[4*(input_len+2):4*(input_len+2+num_of_actions)])
# i  = struct.unpack('>i', agent_output[4*(input_len+2+num_of_actions):])
# print(list(x), v_health, v_win, list(p))
# print(struct.pack('f', 0.1))
# print(struct.pack('f', 0.2))
# print(struct.pack('f', 0.3))
# print(struct.pack('f', 0))
# t = numpy.asarray(2, dtype='f') / numpy.asarray(10, dtype='f')
# print(f'{t[0]:.10f})