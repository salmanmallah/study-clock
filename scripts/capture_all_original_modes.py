import subprocess
import time
import os
from PIL import Image

def run_cmd(cmd):
    p = subprocess.run(cmd, shell=True, capture_output=True, text=True)
    return p.stdout

def capture(dest):
    os.makedirs(os.path.dirname(dest), exist_ok=True)
    run_cmd("adb shell screencap -p /sdcard/sc_temp.png")
    run_cmd(f"adb pull /sdcard/sc_temp.png {dest}")
    img = Image.open(dest)
    print(f"Captured: {dest} -> Size: {img.size}")
    return img

def dismiss_dialog_if_any():
    # If a dialog is up, tapping back arrow at (180, 120) dismisses it
    run_cmd("adb shell input tap 180 120")
    time.sleep(0.5)

print("Capturing modes systematically...")
