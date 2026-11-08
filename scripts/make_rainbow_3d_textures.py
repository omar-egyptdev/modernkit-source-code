#!/usr/bin/env python3
"""Rainbow 3D armor textures: 64x64 sheets = normal armor layout (top half) plus UV islands
(bottom half) for the 3D pieces in RainbowArmorModel.java. Keep the tables in sync with the Java.
IMPORTANT: run this AFTER make_rainbow_textures.py (that script writes the old 64x32 sheets)."""
import colorsys

_r = open('scripts/make_rainbow_textures.py').read().split("print('--- rainbow icons')")[0]
exec(_r, globals())

TRIM = (232, 232, 245)   # silver

def canvas(img):
    out = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
    out.paste(img, (0, 0))
    return out

def paint_part(img, rect, face, off):
    px = img.load()
    x0, y0, x1, y1 = rect
    w, h = x1 - x0, y1 - y0
    for y in range(y0, y1):
        for x in range(x0, x1):
            hue = (((x * 2 + y * 3) % 64) / 64.0 + off) % 1.0
            sat = 0.85
            val = (0.97 - 0.2 * (y - y0) / max(1, h)) * SHADE[face]
            if (x in (x0, x1 - 1) or y in (y0, y1 - 1)) and w >= 3 and h >= 3:
                val *= 0.55
            elif x == x0 + 1 and y == y0 + 1 and w >= 3:
                val, sat = 1.0, 0.25          # glossy highlight
            r, g, b = colorsys.hsv_to_rgb(hue, sat, max(0.0, min(1.0, val)))
            px[x, y] = (int(r * 255), int(g * 255), int(b * 255), 255)

def part(img, u, v, w, h, d, off):
    for f, r in faces(u, v, w, h, d).items():
        paint_part(img, r, f, off)

# (u, v, w, h, d) -- keep in sync with RainbowArmorModel.java
OUTER = [(0, 32, 1, 5, 8),    # tall helmet crest
         (18, 32, 1, 4, 5),   # cheek guard
         (30, 32, 1, 3, 4),   # helmet wing
         (40, 32, 8, 5, 1),   # chest plate
         (18, 41, 8, 2, 1),   # brow plate
         (38, 40, 1, 4, 6),   # pauldron side
         (0, 45, 6, 8, 1),    # back plate
         (14, 45, 6, 2, 6),   # pauldron cap
         (38, 50, 6, 3, 1),   # abdomen plate
         (0, 54, 7, 3, 7),    # bracer
         (28, 54, 7, 2, 7),   # boot cuff
         (52, 50, 4, 2, 2),   # toe cap
         (56, 38, 2, 3, 2),   # shoulder spike
         (56, 43, 1, 3, 3),   # ankle fin
         (56, 16, 2, 2, 1)]   # chest gem
INNER = [(0, 32, 4, 3, 1),    # knee plate
         (12, 32, 1, 5, 4),   # thigh plate
         (24, 32, 6, 3, 1),   # belt buckle
         (40, 32, 1, 4, 4)]   # hip guard

layer1 = canvas(decorate(armor_layer1(), 1, TRIM))
for i, p in enumerate(OUTER):
    part(layer1, *p, off=(i * 0.13) % 1.0)
layer1.save(os.path.join(ARMOR, 'modernkit_rainbow_layer_1.png'))

layer2 = canvas(decorate(armor_layer2(), 2, TRIM))
for i, p in enumerate(INNER):
    part(layer2, *p, off=(i * 0.21) % 1.0)
layer2.save(os.path.join(ARMOR, 'modernkit_rainbow_layer_2.png'))
print('3D rainbow armor textures created (64x64)')
