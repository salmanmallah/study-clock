from PIL import Image
import numpy as np

img = Image.open('reverse-engineering/phase-6-comparison/original/07_stopwatch_idle_portrait.png').convert('RGB')
arr = np.array(img)
print("Stopwatch image size:", img.size)

# Measure top clock display: y=100..500
# Measure stopwatch digits: y=500..800
# Measure buttons: y=750..1100

# Stopwatch main digits:
sw_region = arr[500:800, :, :]
pts = np.where(np.max(sw_region, axis=2) > 100)
if len(pts[0]) > 0:
    min_y = 500 + pts[0].min()
    max_y = 500 + pts[0].max()
    min_x = pts[1].min()
    max_x = pts[1].max()
    print(f"Stopwatch Main Digits: X=[{min_x}..{max_x}] (width={max_x-min_x+1}), Y=[{min_y}..{max_y}] (height={max_y-min_y+1})")
    crop = img.crop((min_x - 10, min_y - 10, max_x + 10, max_y + 10))
    crop.save('reverse-engineering/phase-6-comparison/original/debug_sw_digits.png')

# Stopwatch buttons: y=750..1100
btn_region = arr[750:1100, :, :]
# find circular buttons
pts_b = np.where(np.max(btn_region, axis=2) > 40)
if len(pts_b[0]) > 0:
    b_min_y = 750 + pts_b[0].min()
    b_max_y = 750 + pts_b[0].max()
    b_min_x = pts_b[1].min()
    b_max_x = pts_b[1].max()
    print(f"Buttons Y=[{b_min_y}..{b_max_y}] (height={b_max_y-b_min_y+1})")
    # find centers of the two buttons
    # Button 0 is on the left, Button 1 is on the right
    row = np.max(arr[(b_min_y+b_max_y)//2, :, :], axis=1) > 40
    # find intervals
    in_btn = False
    btns = []
    s = 0
    for x in range(img.width):
        if row[x] and not in_btn:
            in_btn = True
            s = x
        elif not row[x] and in_btn:
            in_btn = False
            btns.append((s, x-1))
    if in_btn:
        btns.append((s, img.width-1))
    for i, (x1, x2) in enumerate(btns):
        print(f"Button {i}: X=[{x1}..{x2}] (width={x2-x1+1}), Center=({(x1+x2)//2}, {(b_min_y+b_max_y)//2})")
