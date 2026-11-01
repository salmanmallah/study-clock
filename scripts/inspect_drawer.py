from PIL import Image
import numpy as np

img = Image.open('reverse-engineering/phase-6-comparison/original/test_drawer.png')
print("Image size:", img.size)
# inspect bottom region y=700..1080
bottom = img.crop((0, 700, 2364, 1080))
bottom.save('reverse-engineering/phase-6-comparison/original/drawer_crop.png')
print("Saved drawer_crop.png")
