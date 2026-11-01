from PIL import Image
import numpy as np

img = Image.open('reverse-engineering/phase-6-comparison/original/test_pomodoro_tap.png').convert('RGB')
arr = np.array(img)
# Let's save a crop of the middle y=400..900, x=300..2100
crop = img.crop((300, 300, 2100, 850))
crop.save('reverse-engineering/phase-6-comparison/original/debug_pomodoro_controls.png')
print("Saved debug_pomodoro_controls.png")
