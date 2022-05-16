# format

import numpy as np
import matplotlib.pyplot as plt
import sys
import json

plt.style.use('seaborn-whitegrid')
fig, ax = plt.subplots(4)
# ax[0].set_yscale("log")
# ax[1].set_yscale("log")
# ax[2].set_yscale("log")

if len(sys.argv) > 1:
    with open(f'{sys.argv[1]}/training.json', 'r') as f:
        training_info = json.load(f)
    cur_iteration = int(training_info['iteration'])
    info = []
    for i in range(1, cur_iteration):
        info.append(training_info['iteration_info'][str(i)])
    print(f'x0 = np.linspace(0, {cur_iteration - 1}, {cur_iteration - 1})')
    print(f'y0 = [{", ".join([str(x["death_rate"]) for x in info if "death_rate" in x])}]')
    print(f'ax[0].plot(x0, y0, label=\'???\')')
    print(f'y1 = [{", ".join([str(x["avg_dmg_no_death"]) for x in info if "avg_dmg_no_death" in x])}]')
    print(f'ax[1].plot(x0, y1, label=\'???\')')
    print(f'y2 = [{", ".join([str(x["avg_final_q"]) for x in info if "avg_final_q" in x])}]')
    print(f'ax[2].plot(x0, y2, label=\'???\')')
    print(f'x1 = [{", ".join([str(x["accumulated_time"]) for x in info if "accumulated_time" in x])}]')
    print(f'ax[3].plot(x1, y0, label=\'???\')')
    print(f'dagger = [{", ".join([str(x["dagger_killed_per"]) for x in info if "dagger_killed_per" in x])}]')
    exit(0)

ax[0].legend()
ax[1].legend()
ax[2].legend()

plt.show()
