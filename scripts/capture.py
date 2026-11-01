import subprocess
import sys
import os
import time
from PIL import Image

def capture(dest_path):
    os.makedirs(os.path.dirname(dest_path), exist_ok=True)
    subprocess.run(["adb", "shell", "rm", "-f", "/sdcard/screencap_temp.png"])
    subprocess.run(["adb", "shell", "screencap", "-p", "/sdcard/screencap_temp.png"], check=True)
    subprocess.run(["adb", "pull", "/sdcard/screencap_temp.png", dest_path], check=True, stdout=subprocess.DEVNULL)
    img = Image.open(dest_path)
    print(f"Captured: {dest_path} -> Size: {img.size} ({'Landscape' if img.size[0] > img.size[1] else 'Portrait'})")
    return img

if __name__ == "__main__":
    if len(sys.argv) > 1:
        capture(sys.argv[1])
    else:
        print("Usage: python capture.py <dest_path>")
