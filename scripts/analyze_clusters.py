from PIL import Image
import numpy as np

img = Image.open('reverse-engineering/phase-6-comparison/original/debug_flip_tap_crop.png').convert('RGB')
arr = np.array(img)
print("Crop size:", img.size)
# inspect row at middle of digits (around y=350 inside crop)
y_mid = 350
# check where bright pixels are
bright = np.max(arr, axis=2) > 150
cols_with_bright = np.where(np.any(bright, axis=0))[0]
print(f"Columns with bright digits: {cols_with_bright.min()} to {cols_with_bright.max()}")

# Find clusters of bright columns (the digits)
in_cluster = False
start = 0
clusters = []
for x in range(img.width):
    has_bright = np.any(bright[:, x])
    if has_bright and not in_cluster:
        in_cluster = True
        start = x
    elif not has_bright and in_cluster:
        in_cluster = False
        clusters.append((start, x))
if in_cluster:
    clusters.append((start, img.width - 1))

print(f"Detected digit clusters: {len(clusters)}")
for idx, (s, e) in enumerate(clusters):
    print(f"Cluster {idx}: x=[{s}..{e}], width={e-s+1}")
