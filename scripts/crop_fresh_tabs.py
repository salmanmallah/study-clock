from PIL import Image

img = Image.open('reverse-engineering/phase-6-comparison/original/test_drawer_fresh.png')
# drawer is y=850..1080
for idx, x in enumerate([216, 542, 869, 1168, 1400, 1650, 1900, 2150]):
    crop = img.crop((x - 80, 880, x + 80, 1040))
    crop.save(f'reverse-engineering/phase-6-comparison/original/fresh_tab_{idx}_{x}.png')
    print(f"Saved fresh_tab_{idx}_{x}.png")
