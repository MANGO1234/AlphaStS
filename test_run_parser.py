import subprocess
import platform
import os
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

JAVA_BASE = [
    'java',
    '--add-opens', 'java.base/java.util=ALL-UNNAMED',
    '-classpath', CLASS_PATH,
    'com.alphaStS.Main',
    '--replay-test',
]

parser = argparse.ArgumentParser(description='Replay test utilities for battle log validation.')
subparsers = parser.add_subparsers(dest='subcommand')

FILTER_HELP = (
    'comma-separated list of {run}:{battle} selectors; '
    'each part is *, N, or N-M (inclusive). '
    'e.g. "0:*,1:2-5,*:0"'
)

# --parse-historical-data (default)
parse_parser = subparsers.add_parser(
    'parse-historical-data',
    help='Parse a historical run data file and print a summary of each battle entry.',
)
parse_parser.add_argument('run_file',
                          help='Path to the historical run data file.')
parse_parser.add_argument('--filter', dest='filter', default=None,
                          metavar='SPEC', help=FILTER_HELP)

# --generate-runs
generate_parser = subparsers.add_parser(
    'generate-runs',
    help='Generate replay run files by playing random moves in STS.',
)
generate_parser.add_argument('run_file',
                             help='Path to the historical run data file.')
generate_parser.add_argument('--filter', dest='filter', default=None,
                             metavar='SPEC', help=FILTER_HELP)
generate_parser.add_argument('--ip', default=None,
                             help='IP address of the STS server.')
generate_parser.add_argument('--replay', action='store_true',
                             help='Replay each generated run log after saving.')

# --replay-run
replay_parser = subparsers.add_parser(
    'replay-run',
    help='Replay a single run log file against the simulated game state.',
)
replay_parser.add_argument('run_log',
                           help='Path to the .run log file to replay.')
replay_parser.add_argument('--verbose', action='store_true',
                           help='Print simulated and logged state at each step.')

args = parser.parse_args()

os.chdir('./agent')

if args.subcommand == 'generate-runs':
    cmd = JAVA_BASE + ['--generate-runs', args.run_file]
    if args.filter is not None:
        cmd += ['--filter', args.filter]
    if args.ip is not None:
        cmd += ['--ip', args.ip]
    if args.replay:
        cmd += ['--replay']
elif args.subcommand == 'replay-run':
    cmd = JAVA_BASE + ['--replay-run', args.run_log]
    if args.verbose:
        cmd += ['--verbose']
else:
    # Default: parse-historical-data
    cmd = JAVA_BASE + ['--parse-historical-data', args.run_file]
    if args.filter is not None:
        cmd += ['--filter', args.filter]

subprocess.run(cmd)
