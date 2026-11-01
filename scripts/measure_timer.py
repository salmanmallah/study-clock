from PIL import Image
import numpy as np

img = Image.open('reverse-engineering/phase-6-comparison/original/08_timer_idle_landscape.png').convert('RGB')
arr = np.array(img)

# Find play button on the right:
# In the right region x=1800..2300, y=300..700
play_region = arr[300:700, 1800:2300, :]
pts = np.where(np.max(play_region, axis=2) > 50)
if len(pts[0]) > 0:
    min_y = 300 + pts[0].min()
    max_y = 300 + pts[0].max()
    min_x = 1800 + pts[1].min()
    max_x = 1800 + pts[1].max()
    print(f"Play Button: X=[{min_x}..{max_x}] (width={max_x-min_x+1}), Y=[{min_y}..{max_y}] (height={max_y-min_y+1})")
    print(f"Play Button Center: ({ (min_x+max_x)//2 }, { (min_y+max_y)//2 })")
    play_crop = img.crop((min_x - 10, min_y - 10, max_x + 10, max_y + 10))
    play_crop.save('reverse-engineering/phase-6-comparison/original/debug_timer_play_btn.png')

# Find wheel pickers:
# Middle region x=200..1800, y=100..700
wheel_region = arr[100:700, 200:1800, :]
# Find columns of text
bright_cols = np.where(np.any(np.max(wheel_region, axis=2) > 50, axis=0))[0]
print(f"Wheel Picker bounds along X: {200 + bright_cols.min()} to {200 + bright_cols.max()}")
