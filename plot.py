# format

import matplotlib.pyplot as plt
plt.style.use('seaborn-whitegrid')
import numpy as np

fig, ax = plt.subplots(3)
ax[0].set_yscale("log")
ax[1].set_yscale("log")
ax[2].set_yscale("log")

# CC Chance Correction
# PCap Policy Cap Randomization

# x = np.linspace(0, 35, 35)
# y = [98.90, 96.20, 66.70, 49.00, 56.90, 31.90, 36.40, 32.60, 26.50, 21.80, 20.50, 18.10, 17.10, 16.10, 13.45, 15.70, 15.60,
#      15.06, 13.73, 13.03, 13.16, 11.03, 11.28, 11.27, 12.15, 11.58, 11.53, 12.71, 11.00, 11.82, 10.40, 9.88, 11.33, 9.84, 10.98]
# ax[0].plot(x, y, label='max MCGS (200*100+CC)')
# y = [34.18, 33.08, 29.41, 27.57, 30.54, 26.48, 26.89, 25.27, 25.11, 24.12, 23.43, 22.82, 22.99, 23.08, 21.67, 21.7, 22.13,
#      22.48, 22.03, 21.11, 20.82, 20.67, 20.91, 20.54, 20.43, 20.76, 20.85, 20.27, 20.46, 20.32, 19.96, 19.71, 19.71, 19.69, 19.73]
# ax[1].plot(x, y, label='max MCGS (200*100+CC)')
# x = [18.42, 36.55, 56.77, 76.47, 100.94, 128.52, 160.94, 195.16, 251.37, 311.28, 373.8, 444.69, 512.6, 583.83, 651.48, 722.59, 799.26, 876.5, 955.9,
#      1038.5, 1119.9, 1208.87, 1299.86, 1394.25, 1490.57, 1588.31, 1674.61, 1762.43, 1860.9, 1955.9, 2045.87, 2136.98, 2231.62, 2327.16, 2418.57]
# y = [98.90, 96.20, 66.70, 49.00, 56.90, 31.90, 36.40, 32.60, 26.50, 21.80, 20.50, 18.10, 17.10, 16.10, 13.45, 15.70, 15.60,
#      15.06, 13.73, 13.03, 13.16, 11.03, 11.28, 11.27, 12.15, 11.58, 11.53, 12.71, 11.00, 11.82, 10.40, 9.88, 11.33, 9.84, 10.98]
# ax[2].plot(x, y, label='max MCGS (200*100+CC)')

# x0 = np.linspace(0, 35, 35)
# y0 = [96.60, 71.90, 47.80, 45.30, 44.70, 39.00, 37.80, 34.30, 28.30, 24.90, 23.40, 21.40, 18.60, 19.30, 15.40, 17.40, 17.13,
#       15.60, 13.58, 13.44, 13.48, 14.09, 13.05, 12.98, 12.64, 12.64, 12.41, 12.40, 12.69, 12.05, 11.98, 11.29, 11.13, 11.04, 10.42]
# ax[0].plot(x0, y0, label='max MCGS (200*100)')
# y1 = [33.79, 28.11, 26.57, 25.26, 24.67, 24.27, 23.68, 23.86, 24.34, 24.02, 22.59, 22.51, 21.99, 22.88, 21.06, 22.08, 22.49,
#       21.11, 20.45, 20.62, 20.3, 20.3, 20.28, 20.49, 20.1, 19.84, 20.49, 19.28, 20.14, 19.61, 19.9, 19.46, 19.5, 19.2, 19.46]
# ax[1].plot(x0, y1, label='max MCGS (200*100)')
# x2 = [20.1, 38.49, 57.41, 78.13, 101.56, 127.71, 158.21, 193.02, 240.9, 293.46, 349.19, 405.6, 465.39, 524.69, 587.22, 652.62, 719.53, 792.43, 861.9,
#       933.09, 1004.48, 1093.86, 1180.58, 1268.23, 1354.9, 1439.48, 1522.26, 1611.5, 1704.64, 1789.03, 1869.55, 1950.01, 2031.67, 2116.52, 2200.98]
# ax[2].plot(x2, y0, label='max MCGS (200*100)')

# x0 = np.linspace(0, 35, 35)
# y0 = [99.70, 94.90, 58.30, 40.60, 37.90, 35.00, 36.50, 32.50, 30.20, 28.20, 24.60, 27.00, 22.20, 22.00, 21.00, 19.53, 17.08,
#       17.00, 17.95, 15.19, 15.43, 16.68, 14.53, 13.90, 13.29, 13.03, 12.17, 12.56, 11.44, 12.48, 11.74, 11.72, 11.15, 11.21, 11.79]
# ax[0].plot(x0, y0, label='max MCGS (200*100+CC #2)')
# y1 = [32.0, 26.31, 25.52, 25.71, 24.82, 25.09, 25.18, 24.56, 24.4, 25.4, 23.77, 24.24, 23.68, 23.76, 23.49, 23.64, 22.37,
#       21.74, 22.4, 21.79, 21.96, 21.44, 21.66, 21.21, 20.5, 19.85, 20.26, 20.39, 19.75, 19.89, 19.84, 19.46, 19.28, 19.73, 20.17]
# ax[1].plot(x0, y1, label='max MCGS (200*100+CC #2)')
# x2 = [19.53, 39.62, 56.45, 78.99, 103.9, 134.88, 168.71, 205.1, 260.04, 318.44, 378.16, 439.62, 504.85, 572.59, 641.29, 712.34, 786.31, 865.76, 948.35,
#       1027.9, 1113.59, 1207.34, 1294.84, 1384.59, 1471.69, 1561.34, 1653.09, 1741.16, 1827.92, 1920.11, 2010.92, 2099.22, 2184.07, 2275.3, 2362.99]
# ax[2].plot(x2, y0, label='max MCGS (200*100+CC #2)')

x0 = np.linspace(0, 35, 35)
y0 = [99.90, 81.50, 47.10, 40.60, 36.20, 27.70, 25.90, 28.50, 27.10, 24.60, 22.60, 18.30, 16.30, 18.60, 18.15, 15.20, 15.05,
      14.94, 14.32, 14.23, 13.59, 13.09, 13.63, 12.34, 12.31, 11.13, 11.64, 11.09, 11.02, 11.74, 11.01, 10.46, 10.73, 10.45, 10.26]
ax[0].plot(x0, y0, label='max MCGS (200*100+CC #3)')
y1 = [25.0, 30.11, 27.11, 25.62, 25.67, 24.96, 23.5, 24.32, 24.43, 23.9, 22.98, 22.35, 22.4, 21.93, 22.54, 20.79, 20.98,
      21.1, 20.95, 20.51, 20.08, 19.67, 20.17, 20.05, 19.9, 19.64, 19.71, 18.86, 19.14, 19.4, 19.77, 18.98, 19.3, 19.32, 18.65]
ax[1].plot(x0, y1, label='max MCGS (200*100+CC #3)')
x2 = [15.82, 33.59, 53.68, 73.89, 97.98, 124.93, 160.01, 197.56, 249.0, 302.27, 361.48, 423.07, 495.63, 563.93, 631.78, 699.9, 768.86, 839.47, 911.12,
      986.73, 1069.04, 1150.74, 1230.64, 1314.65, 1401.58, 1482.28, 1568.85, 1652.56, 1737.72, 1818.69, 1898.44, 1979.08, 2059.7, 2138.16, 2217.66]
ax[2].plot(x2, y0, label='max MCGS (200*100+CC #3)')

x0 = np.linspace(0, 35, 35)
y0 = [94.80, 43.00, 37.00, 29.40, 23.00, 23.20, 20.30, 21.20, 18.20, 18.30, 15.40, 18.00, 14.90, 13.60, 13.45, 14.17, 12.88,
      12.02, 11.90, 11.81, 10.70, 12.07, 12.46, 10.85, 11.86, 10.69, 10.15, 10.26, 10.53, 9.71, 10.56, 10.48, 10.89, 9.77, 9.62]
ax[0].plot(x0, y0, label='max MCGS (200*100+CC+0.1)')
y1 = [31.77, 26.02, 24.78, 23.56, 22.39, 21.38, 20.42, 21.11, 20.59, 19.97, 20.41, 19.73, 18.77, 18.4, 18.79, 17.95, 18.01,
      17.92, 18.12, 18.55, 17.73, 18.06, 17.66, 17.84, 17.76, 16.95, 17.91, 17.27, 18.06, 17.51, 17.98, 17.83, 18.15, 17.39, 17.26]
ax[1].plot(x0, y1, label='max MCGS (200*100+CC+0.1)')
x2 = [19.75, 38.07, 57.82, 78.65, 103.59, 132.7, 163.47, 199.58, 250.99, 307.09, 364.62, 425.08, 492.17, 561.05, 627.87, 695.66, 764.25, 838.41,
      921.78, 1000.48, 1076.39, 1156.68, 1239.62, 1324.6, 1404.13, 1485.01, 1568.7, 1651.73, 1733.54, 1815.95, 1898.61, 1979.98, 2064.41, 2147.1, 2233.3]
ax[2].plot(x2, y0, label='max MCGS (200*100+CC+0.1)')

x0 = np.linspace(0, 35, 35)
y0 = [100.00, 90.80, 61.20, 41.80, 36.50, 31.10, 29.20, 29.80, 22.40, 24.50, 18.70, 18.20, 16.10, 17.60, 13.65,
      16.40, 15.18, 14.06, 13.12, 13.03, 13.06, 14.49, 14.54, 12.28, 12.79, 11.73, 11.81, 11.24, 11.14, 10.86, 10.04, 10.41, 10.31, 10.71, 10.74]
ax[0].plot(x0, y0, label='max MCGS (200*100+CC+0.1+noise+no clone)')
y1 = [41.0, 33.16, 30.1, 22.6, 22.36, 21.31, 21.6, 20.83, 20.01, 21.16, 19.82, 18.91, 19.82, 19.65, 18.88, 18.75, 18.93,
      18.25, 18.3, 17.94, 18.5, 18.19, 17.88, 17.33, 17.45, 17.3, 17.63, 17.51, 17.95, 17.39, 17.18, 17.56, 17.66, 17.61, 17.75]
ax[1].plot(x0, y1, label='max MCGS (200*100+CC+0.1+noise+no clone)')
x2 = [15.36, 28.68, 42.82, 59.53, 81.43, 107.36, 139.22, 170.58, 214.45, 261.41, 312.77, 365.91, 424.03, 485.85, 549.65, 614.09, 685.15,
      749.59, 818.02, 887.52, 963.38, 1045.44, 1125.38, 1206.26, 1286.07, 1362.79, 1440.13, 1515.69, 1595.3, 1674.73, 1753.08, 1833.8, 1907.06, 1982.7, 2063.09]
ax[2].plot(x2, y0, label='max MCGS (200*100+CC+0.1+noise+no clone)')

x0 = np.linspace(0, 35, 35)
y0 = [99.9, 76.4, 38.2, 30.4, 33.8, 25.5, 25.8, 18.4, 18.0, 16.6, 17.2, 16.1, 17.9, 15.6, 13.05, 14.47, 13.43, 13.42,
      12.12, 12.19, 11.56, 11.21, 11.57, 10.06, 11.62, 11.77, 10.79, 9.97, 8.99, 9.48, 8.85, 8.92, 9.09, 9.21, 9.23]
ax[0].plot(x0, y0, label='max MCGS (200*100+CC+0.1+noise+no clone #2)')
y1 = [35.0, 32.58, 27.84, 25.05, 24.89, 23.81, 22.67, 21.72, 20.04, 20.58, 20.14, 19.41, 19.88, 19.34, 18.74, 19.6, 19.34,
      19.02, 18.96, 18.58, 18.2, 18.34, 18.32, 18.6, 18.14, 19.09, 18.54, 18.04, 17.8, 17.71, 17.88, 17.69, 17.81, 18.54, 17.94]
ax[1].plot(x0, y1, label='max MCGS (200*100+CC+0.1+noise+no clone #2)')
x2 = [17.22, 32.73, 48.59, 67.2, 88.78, 114.01, 142.92, 175.72, 223.49, 271.02, 322.8, 380.11, 438.62, 500.24, 559.95, 621.64, 686.46, 752.08, 822.48,
      894.08, 963.39, 1032.15, 1109.35, 1187.26, 1266.87, 1342.21, 1419.72, 1498.85, 1582.38, 1657.91, 1732.39, 1810.17, 1883.39, 1958.27, 2033.59]
ax[2].plot(x2, y0, label='max MCGS (200*100+CC+0.1+noise+no clone #2)')


x0 = np.linspace(0, 35, 35)
y0 = [97.00, 99.40, 35.40, 49.00, 46.30, 31.20, 34.10, 28.00, 20.40, 23.40, 15.50, 15.00, 13.20, 13.30, 16.15, 11.87, 11.65,
      12.00, 11.00, 11.33, 11.79, 11.19, 10.71, 11.40, 11.75, 10.69, 10.97, 9.97, 11.14, 9.63, 10.00, 9.73, 9.47, 8.81, 10.08]
ax[0].plot(x0, y0, label='max MCGS (200*100+CC+0.1+all noise+no clone #2)')
y1 = [31.83, 29.5, 26.54, 22.88, 22.2, 21.82, 22.48, 21.49, 21.39, 21.65, 21.46, 20.42, 20.34, 19.46, 19.91, 19.21, 19.22,
      19.73, 19.15, 18.54, 18.96, 18.92, 18.88, 18.86, 19.26, 18.63, 18.61, 18.29, 18.92, 18.63, 18.66, 18.38, 18.62, 17.73, 18.2]
ax[1].plot(x0, y1, label='max MCGS (200*100+CC+0.1+all noise+no clone #2)')
x2 = [15.45, 30.89, 47.62, 66.34, 88.89, 116.0, 144.67, 176.7, 222.47, 272.19, 330.55, 384.12, 440.46, 499.37, 558.84, 620.39, 683.0, 742.47,
      802.98, 865.03, 932.52, 1001.9, 1076.25, 1147.18, 1221.63, 1298.34, 1372.99, 1443.7, 1516.87, 1587.49, 1656.9, 1727.58, 1799.14, 1872.86, 1945.7]
ax[2].plot(x2, y0, label='max MCGS (200*100+CC+0.1+all noise+no clone #2)')

# x0 = np.linspace(0, 35, 35)
# y0 = [99.90, 99.60, 80.30, 55.50, 51.40, 47.60, 43.70, 35.20, 38.40, 31.70, 30.40, 23.80, 21.50, 20.70, 19.45, 18.17, 17.77,
#       17.00, 15.08, 16.26, 13.93, 13.47, 14.61, 12.92, 13.21, 12.94, 11.94, 12.45, 12.73, 12.79, 12.83, 12.03, 12.24, 11.29, 13.13]
# ax[0].plot(x0, y0, label='max MCGS (500*100+PCap+CC #2)')
# y1 = [38.0, 30.5, 29.17, 27.78, 28.14, 27.43, 26.13, 26.41, 26.36, 25.62, 24.79, 24.24, 24.96, 23.55, 23.64, 22.69, 22.68,
#       21.53, 21.09, 22.04, 20.61, 20.52, 21.49, 20.55, 20.24, 20.82, 19.67, 20.17, 19.7, 19.38, 19.13, 19.76, 19.81, 19.36, 20.01]
# ax[1].plot(x0, y1, label='max MCGS (500*100+PCap+CC #2)')
# x2 = [14.39, 28.24, 43.0, 64.04, 88.83, 114.52, 147.22, 179.28, 228.63, 278.57, 332.02, 388.9, 446.54, 504.63, 562.01, 621.12, 684.23, 749.85, 815.68,
#       885.52, 958.12, 1036.33, 1113.69, 1191.37, 1268.08, 1346.33, 1425.41, 1513.63, 1597.35, 1679.92, 1758.55, 1839.7, 1917.47, 1997.35, 2078.9]
# ax[2].plot(x2, y0, label='max MCGS (500*100+PCap+CC #2)')

# x0 = np.linspace(0, 35, 35)
# y0 = [98.80, 71.70, 37.50, 35.10, 33.90, 26.30, 23.50, 24.60, 23.00, 17.30, 18.30, 15.80, 17.20, 19.70, 16.00, 15.47, 15.60,
#       14.70, 14.82, 13.90, 13.80, 13.98, 12.72, 12.23, 12.48, 12.78, 12.48, 12.79, 11.84, 10.83, 11.35, 11.50, 11.62, 10.03, 10.28]
# ax[0].plot(x0, y0, label='max MCGS (400*50+CC)')
# y1 = [32.33, 27.3, 27.15, 25.96, 26.7, 25.23, 24.83, 24.54, 24.24, 23.78, 23.71, 23.0, 23.7, 22.86, 21.27, 22.53, 21.86,
#       22.44, 21.77, 21.73, 21.36, 21.18, 21.73, 21.2, 21.55, 21.08, 20.84, 20.83, 21.44, 20.6, 20.8, 20.14, 20.7, 19.83, 20.35]
# ax[1].plot(x0, y1, label='max MCGS (400*50+CC)')
# x2 = [19.21, 39.25, 60.38, 86.71, 117.91, 154.77, 203.99, 261.66, 340.14, 420.78, 509.62, 604.03, 704.08, 807.01, 912.23, 1020.64, 1127.64, 1235.69,
#       1343.08, 1455.36, 1568.9, 1683.63, 1799.78, 1914.89, 2031.03, 2145.89, 2260.87, 2376.86, 2493.02, 2607.5, 2723.27, 2839.12, 2955.93, 3072.58, 3188.01]
# ax[2].plot(x2, y0, label='max MCGS (400*50+CC)')

# x0 = np.linspace(0, 35, 35)
# y0 = [97.50, 83.40, 56.20, 54.80, 52.70, 43.30, 40.40, 36.20, 31.50, 34.50, 27.30, 26.40, 30.00, 24.50, 24.80, 18.23, 21.02,
#       20.72, 18.58, 17.54, 16.99, 16.32, 16.42, 13.98, 15.29, 14.94, 14.23, 15.23, 14.34, 13.50, 13.39, 13.24, 11.03, 13.28, 12.79]
# ax[0].plot(x0, y0, label='max MCGS (100*200+CC)')
# y1 = [34.88, 30.56, 28.31, 28.31, 28.33, 25.95, 25.14, 24.63, 23.67, 24.14, 23.6, 23.3, 23.48, 21.63, 22.61, 21.69, 21.79,
#       22.26, 20.74, 21.33, 21.21, 20.85, 20.96, 20.08, 20.51, 20.08, 20.67, 20.31, 20.13, 20.26, 19.76, 20.19, 19.71, 20.5, 20.14]
# ax[1].plot(x0, y1, label='max MCGS (100*200+CC)')
# x2 = [16.03, 32.28, 48.67, 66.11, 83.72, 105.0, 125.46, 150.84, 185.84, 226.95, 269.3, 314.28, 359.07, 404.45, 450.48, 498.47, 547.31, 597.14,
#       649.86, 707.54, 764.19, 822.25, 883.07, 944.48, 1005.46, 1068.17, 1129.38, 1190.51, 1251.17, 1312.38, 1373.43, 1436.12, 1496.26, 1560.9, 1621.1]
# ax[2].plot(x2, y0, label='max MCGS (100*200+CC)')

# x0 = np.linspace(0, 35, 35)
# y0 = [99.80, 97.90, 55.40, 46.90, 48.60, 42.60, 35.90, 36.10, 32.30, 30.50, 24.30, 22.50, 24.40, 21.00, 17.50, 18.97, 15.18,
#       14.30, 14.02, 13.59, 13.64, 12.19, 14.86, 12.41, 11.03, 14.27, 12.02, 11.42, 10.71, 11.06, 11.26, 10.75, 10.19, 12.04, 10.60]
# ax[0].plot(x0, y0, label='max MCGS (200*100+CC+PI)')
# y1 = [30.0, 30.9, 27.54, 28.37, 28.42, 27.4, 26.06, 25.97, 26.32, 24.56, 24.56, 24.32, 23.62, 23.67, 23.14, 22.93, 22.24,
#       21.63, 21.94, 21.6, 21.15, 21.63, 21.94, 20.89, 20.38, 21.5, 20.85, 20.33, 20.17, 20.13, 20.5, 20.06, 19.6, 20.27, 19.36]
# ax[1].plot(x0, y1, label='max MCGS (200*100+CC+PI)')
# x2 = [15.72, 32.53, 51.61, 73.13, 98.64, 125.68, 157.76, 192.53, 249.44, 307.87, 364.82, 424.94, 488.44, 553.16, 617.44, 683.24, 751.69, 821.09,
#       892.8, 967.82, 1043.32, 1119.05, 1198.3, 1277.48, 1362.51, 1443.9, 1533.88, 1617.45, 1704.18, 1794.65, 1878.6, 1963.91, 2049.74, 2137.73, 2226.03]
# ax[2].plot(x2, y0, label='max MCGS (200*100+CC+PI)')


ax[0].legend()
ax[1].legend()
ax[2].legend()

plt.show()
