from PIL import Image
import numpy as np

img = Image.open('reverse-engineering/phase-6-comparison/original/debug_tap_542.png').convert('RGB')
arr = np.array(img)

# Let's check non-dark areas
bright = np.max(arr, axis=2) > 100
pts = np.where(bright)
if len(pts[0]) > 0:
    min_y, max_y = pts[0].min(), pts[0].max()
    min_x, max_x = pts[1].min(), pts[1].max()
    print(f"Bright content: Y=[{min_y}..{max_y}], X=[{min_x}..{max_x}]")
    crop = img.crop((min_x, min_y, max_x, max_y))
    crop.save('reverse-engineering/phase-6-comparison/original/debug_542_crop.png')
    print("Crop saved, size:", crop.size)
