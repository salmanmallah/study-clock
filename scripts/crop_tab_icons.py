from PIL import Image

img = Image.open('reverse-engineering/phase-6-comparison/original/debug_after_back.png')
# Tab centers at y=950: 602, 849, 1096, 1343, 1590, 1837
centers = [602, 849, 1096, 1343, 1590, 1837]

for i, c in enumerate(centers):
    icon = img.crop((c - 80, 880, c + 80, 1040))
    icon.save(f'reverse-engineering/phase-6-comparison/original/tab_icon_{i}.png')
    print(f"Saved tab_icon_{i}.png (center={c})")
