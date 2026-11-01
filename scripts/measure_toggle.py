from PIL import Image
import numpy as np

img = Image.open('reverse-engineering/phase-6-comparison/original/test_ad_dismissed.png').convert('RGB')
top_r = img.crop((1700, 0, 2364, 250))
arr = np.array(top_r)
# Find where the toggle pill is:
# Background of the pill is brighter than 0
pts = np.where(arr > 15)
min_y, max_y = pts[0].min(), pts[0].max()
min_x, max_x = pts[1].min(), pts[1].max()
print(f"Toggle relative bounds: X=[{min_x}..{max_x}], Y=[{min_y}..{max_y}]")
print(f"Screen bounds: X=[{1700+min_x}..{1700+max_x}], Y=[{min_y}..{max_y}]")
print(f"Center: ({1700 + (min_x+max_x)//2}, {(min_y+max_y)//2})")

# Crop and save stretched
crop = top_r.crop((min_x - 10, min_y - 10, max_x + 10, max_y + 10))
crop_arr = np.array(crop)
stretched = ((crop_arr / crop_arr.max()) * 255).astype(np.uint8)
Image.fromarray(stretched).save('reverse-engineering/phase-6-comparison/original/debug_toggle_exact.png')
print("Exact toggle saved!")
