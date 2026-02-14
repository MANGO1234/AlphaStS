#!/usr/bin/env python3
"""
Interactive Server Management Script

This script provides commands to start and stop the AlphaStS interactive server.
The server provides an HTTP JSON API for executing interactive mode commands.

Usage:
    python interactive_server.py start [--port PORT] [--model DIR]  - Start the server in the background
    python interactive_server.py stop                               - Stop the running server
    python interactive_server.py status                             - Check if server is running

See interactive_cli_commands.md for available commands to send to the server.
"""

import subprocess
import platform
import os
import sys
import signal
import time
import json
import urllib.request
import urllib.error

# Path separator for classpath
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

DEFAULT_PORT = 7999
PID_FILE = os.path.join(os.path.dirname(os.path.abspath(__file__)), '.interactive_server.pid')
LOG_FILE = os.path.join(os.path.dirname(os.path.abspath(__file__)), '.interactive_server.log')


def get_agent_dir():
    """Get the agent directory path."""
    return os.path.join(os.path.dirname(os.path.abspath(__file__)), 'agent')


def is_server_running(port=DEFAULT_PORT):
    """Check if the server is running by trying to connect to it."""
    try:
        url = f'http://localhost:{port}/execute'
        req = urllib.request.Request(
            url,
            data=json.dumps({"commands": []}).encode('utf-8'),
            headers={'Content-Type': 'application/json'},
            method='POST'
        )
        with urllib.request.urlopen(req, timeout=2) as response:
            return True
    except (urllib.error.URLError, urllib.error.HTTPError, TimeoutError, ConnectionRefusedError):
        return False
    except Exception:
        return False


def read_pid():
    """Read the PID and port from the PID file."""
    if os.path.exists(PID_FILE):
        try:
            with open(PID_FILE, 'r') as f:
                data = json.loads(f.read().strip())
                return data.get('pid'), data.get('port', DEFAULT_PORT)
        except (ValueError, IOError, json.JSONDecodeError):
            pass
    return None, None


def write_pid(pid, port):
    """Write the PID and port to the PID file."""
    with open(PID_FILE, 'w') as f:
        json.dump({'pid': pid, 'port': port}, f)


def remove_pid():
    """Remove the PID file."""
    if os.path.exists(PID_FILE):
        os.remove(PID_FILE)


def start_server(port=DEFAULT_PORT, model_dir=None):
    """Start the interactive server in the background."""
    if is_server_running(port):
        print(f"Server is already running on port {port}")
        return False

    agent_dir = get_agent_dir()
    if not os.path.exists(os.path.join(agent_dir, 'target', 'classes')):
        print("Error: Java classes not found. Run 'mvn compile' in the agent/ directory first.")
        return False

    print(f"Starting interactive server on port {port}...")

    # Build the command
    agent_args = [
        'java',
        '--add-opens', 'java.base/java.util=ALL-UNNAMED',
        '-classpath', CLASS_PATH,
        'com.alphaStS.Main',
        '--interactive-server',
        '--port', str(port)
    ]
    if model_dir:
        agent_args.extend(['-dir', model_dir])

    # Start the server process in the background
    with open(LOG_FILE, 'w') as log:
        process = subprocess.Popen(
            agent_args,
            cwd=agent_dir,
            stdout=log,
            stderr=subprocess.STDOUT,
            start_new_session=True
        )

    write_pid(process.pid, port)

    # Wait for server to be ready
    print("Waiting for server to start...")
    for i in range(30):  # Wait up to 30 seconds
        time.sleep(1)
        if is_server_running(port):
            print(f"Server started successfully (PID: {process.pid})")
            print(f"API endpoint: POST http://localhost:{port}/execute")
            print(f"Log file: {LOG_FILE}")
            return True

    # Server didn't start
    print("Error: Server failed to start within 30 seconds")
    print(f"Check log file for details: {LOG_FILE}")
    stop_server()
    return False


def stop_server():
    """Stop the running interactive server."""
    pid, _ = read_pid()

    if pid is None:
        print("No PID file found. Server may not be running.")
        return False

    try:
        # Check if process exists
        os.kill(pid, 0)
    except OSError:
        print(f"Process {pid} not found. Cleaning up PID file.")
        remove_pid()
        return False

    print(f"Stopping server (PID: {pid})...")

    try:
        if platform.system() == 'Windows':
            subprocess.run(['taskkill', '/F', '/PID', str(pid)], check=True)
        else:
            os.kill(pid, signal.SIGTERM)

            # Wait for process to terminate
            for _ in range(10):
                time.sleep(0.5)
                try:
                    os.kill(pid, 0)
                except OSError:
                    break
            else:
                # Force kill if still running
                print("Process didn't terminate gracefully, force killing...")
                os.kill(pid, signal.SIGKILL)

        print("Server stopped successfully")
        remove_pid()
        return True

    except Exception as e:
        print(f"Error stopping server: {e}")
        remove_pid()
        return False


def status_server():
    """Check the server status."""
    pid, port = read_pid()

    if pid is None:
        print("Server is NOT RUNNING (no PID file)")
        return False

    if is_server_running(port):
        print(f"Server is RUNNING on port {port}")
        print(f"PID: {pid}")
        return True
    else:
        print(f"Server is NOT RUNNING on port {port}")
        print(f"Stale PID file found: {pid}")
        return False


def print_usage():
    """Print usage information."""
    print(__doc__)
    print("\nExamples:")
    print("  python interactive_server.py start")
    print("  python interactive_server.py start --port 8000")
    print("  python interactive_server.py start --model ../saves")
    print("  python interactive_server.py status")
    print("  python interactive_server.py stop")


def main():
    if len(sys.argv) < 2:
        print_usage()
        sys.exit(1)

    command = sys.argv[1].lower()

    # Parse port and model arguments
    port = DEFAULT_PORT
    model_dir = None
    for i, arg in enumerate(sys.argv):
        if arg == '--port' and i + 1 < len(sys.argv):
            try:
                port = int(sys.argv[i + 1])
            except ValueError:
                print(f"Error: Invalid port number: {sys.argv[i + 1]}")
                sys.exit(1)
        elif arg == '--model' and i + 1 < len(sys.argv):
            model_dir = sys.argv[i + 1]

    if command == 'start':
        success = start_server(port, model_dir)
        sys.exit(0 if success else 1)

    elif command == 'stop':
        success = stop_server()
        sys.exit(0 if success else 1)

    elif command == 'status':
        running = status_server()
        sys.exit(0 if running else 1)

    elif command in ('help', '-h', '--help'):
        print_usage()
        sys.exit(0)

    else:
        print(f"Unknown command: {command}")
        print_usage()
        sys.exit(1)


if __name__ == '__main__':
    main()
