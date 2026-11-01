import os
import glob
from PIL import Image
import numpy as np

asset_files = sorted(glob.glob('reverse-engineering/phase-3/extracted-assets/*.png'))

def compare_icons():
    for i in range(6):
        tab_path = f'reverse-engineering/phase-6-comparison/original/tab_icon_{i}.png'
        if not os.path.exists(tab_path):
            continue
        tab_img = Image.open(tab_path).convert('L').resize((64, 64))
        tab_arr = np.array(tab_img, dtype=float)
        
        best_match = None
        best_diff = float('inf')
        
        for af in asset_files:
            asset_img = Image.open(af).convert('L').resize((64, 64))
            asset_arr = np.array(asset_img, dtype=float)
            diff = np.mean(np.abs(tab_arr - asset_arr))
            if diff < best_diff:
                best_diff = diff
                best_match = os.path.basename(af)
        
        print(f"Tab {i} best match: {best_match} (diff: {best_diff:.2f})")

if __name__ == '__main__':
    compare_icons()
