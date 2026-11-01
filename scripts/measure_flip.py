from PIL import Image
import numpy as np

img = Image.open('reverse-engineering/phase-6-comparison/original/test_ad_dismissed.png').convert('RGB')
arr = np.array(img)

# Card background is dark grey ~ [24 26 27] or [20..40]
# Card height: inspect vertical profile at center of hour card
# Hour card is around x=300..1000, minute card is around x=1300..2000
# Let's find card columns at y=540 (screen center):
mid_row = arr[540, :, :]
card_cols = np.where(np.max(mid_row, axis=1) > 15)[0]

in_c = False
cards = []
s = 0
for x in range(img.width):
    if x in card_cols and not in_c:
        in_c = True
        s = x
    elif x not in card_cols and in_c:
        in_c = False
        cards.append((s, x-1))
if in_c:
    cards.append((s, img.width-1))

print(f"Cards detected at y=540: {len(cards)}")
for i, (x1, x2) in enumerate(cards):
    # find Y bounds of this card:
    col = arr[:, (x1+x2)//2, :]
    card_ys = np.where(np.max(col, axis=1) > 15)[0]
    y1, y2 = card_ys.min(), card_ys.max()
    print(f"Card {i}: X=[{x1}..{x2}] (width={x2-x1+1}), Y=[{y1}..{y2}] (height={y2-y1+1})")

# Measure gap between cards:
if len(cards) >= 2:
    gap = cards[1][0] - cards[0][1] - 1
    print(f"Gap between Hour and Minute cards: {gap} px")

# Find seam line (horizontal split):
# Look along Y inside card 0 around middle of card
c0_x = (cards[0][0] + cards[0][1]) // 2
c0_y1, c0_y2 = y1, y2
col_vals = arr[c0_y1:c0_y2, c0_x, :]
# seam is a black horizontal line where color drops to near 0!
seam_ys = np.where(np.max(col_vals, axis=1) <= 10)[0]
if len(seam_ys) > 0:
    seam_top = c0_y1 + seam_ys.min()
    seam_bottom = c0_y1 + seam_ys.max()
    print(f"Seam line: Y=[{seam_top}..{seam_bottom}] (height={seam_bottom-seam_top+1} px)")

# Save clean screenshot as 05_flip_12h_landscape.png
img.save('reverse-engineering/phase-6-comparison/original/05_flip_12h_landscape.png')
print("Saved 05_flip_12h_landscape.png!")
