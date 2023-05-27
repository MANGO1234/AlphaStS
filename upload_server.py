# format

import subprocess
import os
from misc import getFlag, getFlagValue
from os.path import expanduser
home = expanduser("~")

# This Python script updates and deploys alphaStS to multiple linux servers. It performs the following steps:
# - Adds all changes to the git repository
# - Commits the changes with a temporary commit message
# - For each specified IP address, it creates a directory "alphaStS" if it doesn't exist already
# - Copies the .git directory to the remote server using rsync
# - Executes a sequence of commands on the remote server:
#     - Kills any running Java processes
#     - Resets the git repository to the latest commit
#     - Builds and installs a Java project using Maven
#     - Runs a Python script and redirects output to a log file
# - If any exception occurs during the deployment, the script rolls back the last commit and raises the exception again.

ips = getFlagValue('-ip', '').split(',')
print(ips)

f = open(f'{home}\\alphaStSServers.txt', 'w')

p = subprocess.Popen(f'git add -A', stdout=subprocess.PIPE, stderr=subprocess.PIPE)
while p.poll() is None:
    print(p.stdout.readline().decode('ascii'), end='')
[print(line.decode('ascii'), end='') for line in p.stderr.readlines()]

p = subprocess.Popen(f'git commit -m "tmp"', stdout=subprocess.PIPE, stderr=subprocess.PIPE)
while p.poll() is None:
    print(p.stdout.readline().decode('ascii'), end='')
[print(line.decode('ascii'), end='') for line in p.stderr.readlines()]

git_command = ["git", "rev-parse", "--abbrev-ref", "HEAD"]
process = subprocess.Popen(git_command, stdout=subprocess.PIPE)
output, _ = process.communicate()
branch_name = output.decode("utf-8").strip()

try:
    for ip in ips:
        print(f'*********** {ip} ************************************************')
        if ip.find('@192.168.1.') >= 0:
            username = ip[:ip.find('@192.168.1.')]
            ip = ip[ip.find('@192.168.1.') + 1:]
            f.write(f'{ip}:4000\n')
            p = subprocess.Popen(f'ssh -o StrictHostKeyChecking=no {username}@{ip} "F: & cd git\\alphaStS & git fetch --all & git reset --hard HEAD & git checkout origin/{branch_name} & git reset --hard origin/{branch_name} & cd agent & mvn install & cd .. & taskkill /F /IM java.exe"', stdout=subprocess.PIPE, stderr=subprocess.PIPE)
            while p.poll() is None:
                print(p.stdout.readline().decode('ascii'), end='')
            [print(line.decode('ascii'), end='') for line in p.stderr.readlines()]
        else:
            f.write(f'{ip}:4000\n')
            p = subprocess.Popen(f'ssh -o StrictHostKeyChecking=no -i {home}\\Downloads\\Test.pem ec2-user@{ip} "mkdir -p ./alphaStS"', stdout=subprocess.PIPE, stderr=subprocess.PIPE)
            while p.poll() is None:
                print(p.stdout.readline().decode('ascii'), end='')
            [print(line.decode('ascii'), end='') for line in p.stderr.readlines()]

            p = subprocess.Popen(f'bash -c "rsync -r -P --delete .git/ -e \'ssh -o StrictHostKeyChecking=no -i /root/.ssh/svr1.pem\' ec2-user@{ip}:/home/ec2-user/alphaStS/.git/"', stdout=subprocess.PIPE, stderr=subprocess.PIPE)
            while p.poll() is None:
                print(p.stdout.readline().decode('ascii'), end='')
            [print(line.decode('ascii'), end='') for line in p.stderr.readlines()]

            p = subprocess.Popen(f'ssh -o StrictHostKeyChecking=no -i {home}\\Downloads\\Test.pem ec2-user@{ip} "bash -c \'killall -9 java; cd ~/alphaStS; git reset --hard; cd agent; mvn install; cd ..; python3 test_dist.py -server > /tmp/alphaStS.log 2>&1 & disown -a; exit\'"', stdout=subprocess.PIPE, stderr=subprocess.PIPE)
            while p.poll() is None:
                print(p.stdout.readline().decode('ascii'), end='')
            [print(line.decode('ascii'), end='') for line in p.stderr.readlines()]
except e:
    p = subprocess.Popen(f'git reset HEAD^', stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    while p.poll() is None:
        print(p.stdout.readline().decode('ascii'), end='')
    [print(line.decode('ascii'), end='') for line in p.stderr.readlines()]
    raise e

p = subprocess.Popen(f'git reset HEAD^', stdout=subprocess.PIPE, stderr=subprocess.PIPE)
while p.poll() is None:
    print(p.stdout.readline().decode('ascii'), end='')
[print(line.decode('ascii'), end='') for line in p.stderr.readlines()]

f.close()
