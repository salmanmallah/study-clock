import subprocess
import time
import os
from PIL import Image
import numpy as np

NATIVE_DIR = "reverse-engineering/phase-6-comparison/native"
os.makedirs(NATIVE_DIR, exist_ok=True)

def tap(x, y, wait=1.0):
    subprocess.run(["adb", "shell", "input", "tap", str(x), str(y)], check=True)
    time.sleep(wait)

def swipe(x1, y1, x2, y2, duration_ms=400, wait=1.0):
    subprocess.run(["adb", "shell", "input", "swipe", str(x1), str(y1), str(x2), str(y2), str(duration_ms)], check=True)
    time.sleep(wait)

def screencap(filename):
    dest = os.path.join(NATIVE_DIR, filename)
    subprocess.run(["adb", "shell", "rm", "-f", "/sdcard/sc_native_temp.png"])
    subprocess.run(["adb", "shell", "screencap", "-p", "/sdcard/sc_native_temp.png"], check=True)
    subprocess.run(["adb", "pull", "/sdcard/sc_native_temp.png", dest], check=True, stdout=subprocess.DEVNULL)
    img = Image.open(dest)
    print(f"Captured {filename}: {img.size} ({'Landscape' if img.size[0] > img.size[1] else 'Portrait'})")
    return img, dest

def measure_image(img_path):
    img = Image.open(img_path).convert('RGB')
    arr = np.array(img)
    # ignore rightmost 64px for handle
    sub = arr[:, :2300, :] if img.width > img.height else arr
    pts = np.where(np.max(sub, axis=2) > 30)
    if len(pts[0]) > 0:
        y1, y2 = pts[0].min(), pts[0].max()
        x1, x2 = pts[1].min(), pts[1].max()
        print(f"   -> Bounds: X=[{x1}..{x2}] (w={x2-x1+1}), Y=[{y1}..{y2}] (h={y2-y1+1})")
    else:
        print("   -> Bounds: all black or background")

def main():
    print("=== Starting Native App Capture Suite ===")
    
    # 1. Ensure app is in foreground
    subprocess.run(["adb", "shell", "am", "start", "-n", "com.ghost.fullscreenclock/.MainActivity"])
    time.sleep(2)
    
    # 2. Capture 01_clock12_landscape.png
    print("\n--- 01: 12-Hour Clock ---")
    tap(1182, 540) # open drawer
    tap(148, 950)  # Tab 0: DIGITAL_12
    time.sleep(1)
    # close drawer if open
    tap(1182, 300)
    screencap("01_clock12_landscape.png")
    measure_image(os.path.join(NATIVE_DIR, "01_clock12_landscape.png"))
    
    # 3. Capture 02_clock24_landscape.png
    print("\n--- 02: 24-Hour Clock ---")
    tap(1182, 540) # open drawer
    tap(395, 950)  # Tab 1: DIGITAL_24
    time.sleep(1)
    tap(1182, 300)
    screencap("02_clock24_landscape.png")
    measure_image(os.path.join(NATIVE_DIR, "02_clock24_landscape.png"))
    
    # 4. Capture 03_clock12_sec_landscape.png
    print("\n--- 03: 12-Hour Seconds Clock ---")
    tap(1182, 540)
    tap(643, 950)  # Tab 2: SECONDS_12
    time.sleep(1)
    tap(1182, 300)
    screencap("03_clock12_sec_landscape.png")
    measure_image(os.path.join(NATIVE_DIR, "03_clock12_sec_landscape.png"))
    
    # 5. Capture 04_clock24_sec_landscape.png
    print("\n--- 04: 24-Hour Seconds Clock ---")
    tap(1182, 540)
    tap(890, 950)  # Tab 3: SECONDS_24
    time.sleep(1)
    tap(1182, 300)
    screencap("04_clock24_sec_landscape.png")
    measure_image(os.path.join(NATIVE_DIR, "04_clock24_sec_landscape.png"))
    
    # 6. Capture 05_flip_12h_landscape.png and 05_flip_24h_landscape.png
    print("\n--- 05: Flip Clock ---")
    tap(1182, 540)
    tap(1138, 950) # Tab 4: FLIP_CLOCK
    time.sleep(1.5)
    tap(1182, 300)
    screencap("05_flip_12h_landscape.png")
    measure_image(os.path.join(NATIVE_DIR, "05_flip_12h_landscape.png"))
    # for 24h flip
    screencap("05_flip_24h_landscape.png")
    
    # 7. Capture 06_pomodoro_idle_landscape.png
    print("\n--- 06: Pomodoro ---")
    tap(1182, 540)
    tap(1385, 950) # Tab 5: POMODORO
    time.sleep(1.5)
    screencap("06_pomodoro_idle_landscape.png")
    measure_image(os.path.join(NATIVE_DIR, "06_pomodoro_idle_landscape.png"))
    
    # 8. Capture 08_timer_idle_landscape.png, running, paused
    print("\n--- 08: Timer ---")
    tap(1182, 540)
    tap(1880, 950) # Tab 7: TIMER
    time.sleep(1.5)
    screencap("08_timer_idle_landscape.png")
    measure_image(os.path.join(NATIVE_DIR, "08_timer_idle_landscape.png"))
    # tap play (dial center or play button in current layout)
    # native current layout has buttons on the right or dial
    tap(1800, 540) # tap start
    time.sleep(1.5)
    screencap("08_timer_running_landscape.png")
    tap(1800, 540) # tap pause
    time.sleep(1)
    screencap("08_timer_paused_landscape.png")
    
    # 9. Drawer open landscape
    print("\n--- 10: Drawer Open Landscape ---")
    tap(1182, 540)
    screencap("10_drawer_open_landscape.png")
    
    # 10. Capture 07_stopwatch_idle_portrait.png, running, laps, paused
    print("\n--- 07: Stopwatch (Portrait) ---")
    tap(1633, 950) # Tab 6: STOPWATCH (rotates to portrait!)
    time.sleep(2.5)
    screencap("07_stopwatch_idle_portrait.png")
    measure_image(os.path.join(NATIVE_DIR, "07_stopwatch_idle_portrait.png"))
    # tap start
    tap(732, 796) # Start button coordinates
    time.sleep(2)
    # tap lap twice
    tap(348, 796)
    time.sleep(1.5)
    tap(348, 796)
    time.sleep(1)
    screencap("07_stopwatch_laps_portrait.png")
    screencap("07_stopwatch_running_portrait.png")
    # tap pause
    tap(732, 796)
    time.sleep(1)
    screencap("07_stopwatch_paused_portrait.png")
    
    # 11. Capture 09_settings_portrait.png
    print("\n--- 09: Settings (Portrait) ---")
    # In stopwatch portrait, toggle drawer
    tap(540, 2200) # bottom drawer
    time.sleep(1)
    # swipe drawer left in portrait to reach settings
    swipe(900, 2200, 200, 2200, 400)
    # tap settings (last tab)
    tap(900, 2200)
    time.sleep(2)
    screencap("09_settings_portrait.png")
    measure_image(os.path.join(NATIVE_DIR, "09_settings_portrait.png"))
    
    # 12. Drawer open portrait
    screencap("10_drawer_open_portrait.png")
    
    print("\n=== Native Suite Capture Complete! ===")

if __name__ == "__main__":
    main()
