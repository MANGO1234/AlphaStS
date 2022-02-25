# format

import sys

args = [a for a in sys.argv]


def getFlag(flag, default=False):
    new_args = []
    for arg in args:
        if arg == flag:
            return True
        new_args.append(arg)
    return default


def getFlagValue(flag, default=None):
    new_args = []
    v = default
    for i, arg in enumerate(args):
        if arg == flag:
            v = args[i + 1]
            i += 1
        else:
            new_args.append(arg)
    return v
