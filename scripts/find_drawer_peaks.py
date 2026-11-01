from PIL import Image
import numpy as np

img = Image.open('reverse-engineering/phase-6-comparison/original/debug_drawer_m0.png').convert('RGB')
arr = np.array(img)

mid_rows = arr[60:140, :, :]
col_std = np.std(mid_rows, axis=(0, 2))

peaks = []
for x in range(100, img.width - 100, 30):
    val = np.mean(col_std[x-15:x+15])
    if val > 15:
        peaks.append((x, val))

clusters = []
curr = []
for p, v in peaks:
    if not curr or p - curr[-1][0] <= 60:
        curr.append((p, v))
    else:
        avg_x = int(sum(x for x, _ in curr) / len(curr))
        clusters.append(avg_x)
        curr = [(p, v)]
if curr:
    clusters.append(int(sum(x for x, _ in curr) / len(curr)))

print("Detected item centers along X:")
for i, c in enumerate(clusters):
    print(f"Item {i}: center X = {c}")
