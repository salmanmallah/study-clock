from PIL import Image
import numpy as np

img = Image.open('reverse-engineering/phase-3/screenshots/screen_flipclock.png').convert('RGB')
arr = np.array(img)

# Look for dark grey cards on black background: card color is around RGB (20..40, 20..40, 20..40) or (15..35)
# Let's inspect horizontal profile at y = 500
y_mid = 500
row = arr[y_mid, :, :]
# Print intervals where color is > 10 (card background)
in_card = False
start = 0
cards = []
for x in range(img.width):
    # card background is typically >= 15
    val = np.mean(arr[y_mid, x, :])
    if val >= 15 and not in_card:
        in_card = True
        start = x
    elif val < 15 and in_card:
        in_card = False
        cards.append((start, x))
if in_card:
    cards.append((start, img.width - 1))

print(f"Cards detected at y={y_mid}: {len(cards)}")
for i, (s, e) in enumerate(cards):
    print(f"Card {i}: x=[{s}..{e}], width={e-s+1}")

# Check card height for each card:
for i, (s, e) in enumerate(cards):
    c_mid = (s + e) // 2
    col = arr[:, c_mid, :]
    col_vals = np.mean(col, axis=1)
    card_ys = np.where(col_vals >= 15)[0]
    if len(card_ys) > 0:
        print(f"Card {i} y=[{card_ys.min()}..{card_ys.max()}], height={card_ys.max() - card_ys.min() + 1}")

# Check top right for 12h/24h toggle:
top_right = arr[:150, 1800:, :]
pts = np.where(top_right > 30)
if len(pts[0]) > 0:
    print(f"Top right element detected at y=[{pts[0].min()}..{pts[0].max()}], x=[{1800+pts[1].min()}..{1800+pts[1].max()}]")
