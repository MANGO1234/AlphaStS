# format

import numpy as np
import matplotlib.pyplot as plt
import sys
import ast
import json

plt.style.use('seaborn-whitegrid')

if len(sys.argv) > 1:
    if sys.argv[1] == '-a':
        num_plots = (len(sys.argv) - 2) // 2
        fig, axs = plt.subplots(num_plots)
        for i in range(2, len(sys.argv), 2):
            array_name = sys.argv[i].strip()
            array_values = json.loads(sys.argv[i + 1])
            array_values = np.nan_to_num(array_values)
            ax = axs[(i - 2) // 2]
            ax.plot(array_values)
            ax.set_title(array_name)
    else:
        fig, ax = plt.subplots(4)
        for path in sys.argv[1:]:
            with open(f'{path}/training.json', 'r') as f:
                training_info = json.load(f)
            cur_iteration = int(training_info['iteration'])
            info = []
            for i in range(1, cur_iteration):
                info.append(training_info['iteration_info'][str(i)])
            x0 = np.linspace(0, cur_iteration - 1, cur_iteration - 1)
            y0 = [x['death_rate'] for x in info if 'death_rate' in x]
            ax[0].plot(x0[:len(y0)], y0, label=path)
            y1 = [x['avg_dmg_no_death'] for x in info if 'avg_dmg_no_death' in x]
            ax[1].plot(x0[:len(y1)], y1, label=path)
            y2 = [x['avg_final_q'] for x in info if 'avg_final_q' in x]
            ax[2].plot(x0[:len(y2)], y2, label=path)
            x1 = [x['accumulated_time'] for x in info if 'accumulated_time' in x]
            ax[3].plot(x1[:len(y0)], y2, label=path)

        ax[0].legend()
        ax[1].legend()
        ax[2].legend()

plt.show()
