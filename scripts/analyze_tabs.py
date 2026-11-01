from PIL import Image
import numpy as np

strip = Image.open('reverse-engineering/phase-3/screenshots/bottom_strip.png')
print("Strip size:", strip.size)

# The drawer in landscape spans across the screen
# Let's inspect each slice:
# We know the 9 modes from the APK strings:
# 1. 12-Hour Clock (clockpage12)
# 2. 24-Hour Clock (clockpage24)
# 3. 12h Seconds Clock (secpage12)
# 4. 24h Seconds Clock (secpage24)
# 5. Flip Clock (flipclock)
# 6. Pomodoro (pomodoro)
# 7. Stopwatch (stopwatch)
# 8. Timer (timer)
# 9. Settings (settings)
