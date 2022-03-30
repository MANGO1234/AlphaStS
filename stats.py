# format

import math

# https://www.itl.nist.gov/div898/handbook/prc/section3/prc33.htm
n_win1 = 6771
n1 = 10000

n_win2 = 6655
n2 = 10000

p1 = n_win1 / n1
p2 = n_win2 / n2
p = (n_win1 + n_win2) / (n1 + n2)

z = (p1 - p2) / math.sqrt(p * (1 - p) * (1 / n1 + 1 / n2))
print(z)
