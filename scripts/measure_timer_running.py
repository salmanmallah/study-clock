from PIL import Image
import numpy as np

img = Image.open('reverse-engineering/phase-6-comparison/original/08_timer_running_landscape.png').convert('RGB')
arr = np.array(img)

# Find digits bounding box:
digits_region = arr[200:700, :, :]
pts = np.where(np.max(digits_region, axis=2) > 30)
if len(pts[0]) > 0:
    min_y = 200 + pts[0].min()
    max_y = 200 + pts[0].max()
    min_x = pts[1].min()
    max_x = pts[1].max()
    print(f"Running Digits: X=[{min_x}..{max_x}] (width={max_x-min_x+1}), Y=[{min_y}..{max_y}] (height={max_y-min_y+1})")

# Find buttons in bottom area y=700..1050:
btn_region = arr[700:1050, :, :]
pts_b = np.where(np.max(btn_region, axis=2) > 50)
if len(pts_b[0]) > 0:
    b_min_y = 700 + pts_b[0].min()
    b_max_y = 700 + pts_b[0].max()
    b_min_x = pts_b[1].min()
    b_max_x = pts_b[1].max()
    print(f"Buttons area: X=[{b_min_x}..{b_max_x}], Y=[{b_min_y}..{b_max_y}] (height={b_max_y-b_min_y+1})")
    # let's crop each button
    crop = img.crop((b_min_x - 10, b_min_y - 10, b_max_x + 10, b_max_y + 10))
    crop.save('reverse-engineering/phase-6-comparison/original/debug_timer_running_btns.png')
    print("Buttons crop saved!")
