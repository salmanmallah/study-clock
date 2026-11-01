import subprocess
import time
from PIL import Image

centers = [602, 849, 1096, 1343, 1590, 1837]

for idx, c in enumerate(centers):
    print(f"\n--- Tapping Tab {idx} at ({c}, 950) ---")
    subprocess.run(["adb", "shell", "input", "tap", str(c), "950"])
    time.sleep(1.5)
    dest = f"reverse-engineering/phase-6-comparison/original/test_tab_{idx}_{c}.png"
    subprocess.run(["adb", "shell", "screencap", "-p", "/sdcard/screencap_temp.png"])
    subprocess.run(["adb", "pull", "/sdcard/screencap_temp.png", dest], stdout=subprocess.DEVNULL)
    img = Image.open(dest)
    print(f"Captured Tab {idx}: size={img.size} ({'Landscape' if img.size[0] > img.size[1] else 'Portrait'})")
    # if it rotated or changed screen, let's reopen drawer if needed
    time.sleep(0.5)
