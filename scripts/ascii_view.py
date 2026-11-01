from PIL import Image
import sys

def ascii_view(image_path, width=80):
    im = Image.open(image_path).convert('L')
    aspect = im.height / im.width
    height = int(width * aspect * 0.45)
    im = im.resize((width, max(height, 1)))
    chars = " .:-=+*#%@"
    for y in range(im.height):
        line = ""
        for x in range(im.width):
            val = im.getpixel((x, y))
            idx = int(val / 256 * len(chars))
            line += chars[idx]
        print(line)

if __name__ == "__main__":
    path = sys.argv[1] if len(sys.argv) > 1 else 'reverse-engineering/phase-6-comparison/original/debug_542_center.png'
    ascii_view(path)
