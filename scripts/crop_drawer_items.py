from PIL import Image
import os

img = Image.open('reverse-engineering/phase-6-comparison/original/test_drawer.png')
crops_dir = 'reverse-engineering/phase-6-comparison/original/debug_drawer_items'
os.makedirs(crops_dir, exist_ok=True)

# Let's crop full drawer: y 800 to 1080
for idx, (x1, x2) in enumerate([(0, 360), (360, 730), (730, 1160), (1160, 1590), (1590, 2020), (2020, 2364)]):
    c = img.crop((x1, 800, x2, 1080))
    c.save(f'{crops_dir}/crop_{idx}_{x1}_{x2}.png')

print("Saved debug crops to", crops_dir)
