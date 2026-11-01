from PIL import Image
import numpy as np

img = Image.open('reverse-engineering/phase-6-comparison/original/debug_tap_542.png').convert('RGB')
arr = np.array(img)
print("debug_tap_542 size:", img.size)
print("Mean RGB:", arr.mean(axis=(0, 1)))

# Let's check center:
center = arr[300:700, 500:1800, :]
print("Center mean RGB:", center.mean(axis=(0, 1)))

# Save a scaled preview
img.resize((1182, 540)).save('reverse-engineering/phase-6-comparison/original/debug_tap_542_preview.png')
print("Preview saved as PNG")
