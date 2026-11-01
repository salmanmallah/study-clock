from PIL import Image
import numpy as np

img = Image.open('reverse-engineering/phase-6-comparison/original/06_pomodoro_idle_landscape.png').convert('RGB')
arr = np.array(img)

# Background color at (50, 50):
bg_color = arr[50, 50]
print(f"Pomodoro Background Color: RGB={bg_color} Hex=#{bg_color[0]:02X}{bg_color[1]:02X}{bg_color[2]:02X}")

# Measure digits y=100..400:
# Digits are darker than background
diff = np.linalg.norm(arr[100:400, :, :] - bg_color, axis=2)
pts = np.where(diff > 50)
if len(pts[0]) > 0:
    min_y = 100 + pts[0].min()
    max_y = 100 + pts[0].max()
    min_x = pts[1].min()
    max_x = pts[1].max()
    print(f"Pomodoro Digits: X=[{min_x}..{max_x}] (width={max_x-min_x+1}), Y=[{min_y}..{max_y}] (height={max_y-min_y+1})")

# Measure buttons y=400..750:
btn_diff = np.linalg.norm(arr[400:750, :, :] - bg_color, axis=2)
pts_b = np.where(btn_diff > 50)
if len(pts_b[0]) > 0:
    b_y1 = 400 + pts_b[0].min()
    b_y2 = 400 + pts_b[0].max()
    b_x1 = pts_b[1].min()
    b_x2 = pts_b[1].max()
    print(f"Buttons area: X=[{b_x1}..{b_x2}], Y=[{b_y1}..{b_y2}] (height={b_y2-b_y1+1})")
    # find left button and right button
    mid_row = np.max(btn_diff[b_y1-400 : b_y2-400, :], axis=0) > 50
    # find intervals
    in_b = False
    btns = []
    s = 0
    for x in range(img.width):
        if mid_row[x] and not in_b:
            in_b = True
            s = x
        elif not mid_row[x] and in_b:
            in_b = False
            btns.append((s, x-1))
    if in_b:
        btns.append((s, img.width-1))
    for i, (x1, x2) in enumerate(btns):
        print(f"Button {i}: X=[{x1}..{x2}] (width={x2-x1+1}), Center=({(x1+x2)//2}, {(b_y1+b_y2)//2})")

# Measure session pill top-right y=0..200, x=1800..2364:
tr = np.linalg.norm(arr[:200, 1800:, :] - bg_color, axis=2)
pts_tr = np.where(tr > 40)
if len(pts_tr[0]) > 0:
    tr_y1 = pts_tr[0].min()
    tr_y2 = pts_tr[0].max()
    tr_x1 = 1800 + pts_tr[1].min()
    tr_x2 = 1800 + pts_tr[1].max()
    print(f"Session Pill: X=[{tr_x1}..{tr_x2}] (width={tr_x2-tr_x1+1}), Y=[{tr_y1}..{tr_y2}] (height={tr_y2-tr_y1+1})")
