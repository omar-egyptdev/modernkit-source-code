#!/usr/bin/env python3
"""Generates door item icons that match the PLACED door textures.

The previous version of this script drew the icons from an invented flat
palette, which made the held/hotbar icon look completely different from the
door you actually place in the world. This version instead samples the real
door block textures used by the placed door:

  assets/minecraft/textures/block/obsidian_door_top.png    (placed door, upper half)
  assets/minecraft/textures/block/obsidian_door_bottom.png (placed door, lower half)
  assets/minecraft/textures/block/rainbow_door_top.png     (animated, 10 frames)
  assets/minecraft/textures/block/rainbow_door_bottom.png  (animated, 10 frames)

The icon layout is the vanilla oak door item icon structure (windows as
transparent holes, hinges, handle, recessed panels) so it still looks like a
real Minecraft door icon, but every pixel takes its color from the actual
placed-door texture, keeping the obsidian swirls / rainbow shimmer of the
block the player sees in the world.

Output:
  assets/modernkit/textures/item/obsidian_door.png   16x16 static
  assets/modernkit/textures/item/rainbow_door.png    16x160 = 10 frames
                                                     (existing .mcmeta kept:
                                                     frametime 3, interpolate)

Run from repo root:  python3 scripts/make_door_item_icons.py
"""
from pathlib import Path

from PIL import Image

BLOCK = Path('src/main/resources/assets/minecraft/textures/block')
ITEM = Path('src/main/resources/assets/modernkit/textures/item')

# Vanilla oak door item sprite structure, shade-by-shade:
# '.' = transparent window hole, 'L' = recessed panel highlight,
# 'o' = door body, 'd' = dark edge, '#' = near-black hinge/handle.
TEMPLATE = [
    "................",
    "................",
    "...dooooooooo...",
    "...do...o...o...",
    "...#o...o...o...",
    "...dooooooooo...",
    "...do...o...o...",
    "...do...o...o...",
    "...doooooo##o...",
    "...doLLLoLLLo...",
    "...doLLLoLLLo...",
    "...dooooooooo...",
    "...#oLLLoLLLo...",
    "...doLLLoLLLo...",
    "...dooooooooo...",
    "...dddddddddd...",
]

# Brightness multipliers applied on top of the sampled texture colors so the
# recessed panels and edges stay readable at hotbar size.
SHADE_FACTOR = {'L': 1.45, 'o': 1.15, 'd': 0.85}


def luminance(color):
    r, g, b = color[:3]
    return 0.2126 * r + 0.7152 * g + 0.0722 * b


def darkest_color(*textures):
    """Darkest visible color across the sampled textures (for the outline)."""
    best = (255, 255, 255)
    best_lum = 1e9
    for tex in textures:
        px = tex.load()
        for y in range(tex.height):
            for x in range(tex.width):
                r, g, b, a = px[x, y]
                if a == 0:
                    continue
                lum = luminance((r, g, b))
                if lum < best_lum:
                    best_lum = lum
                    best = (r, g, b)
    return best


def sample(tex, tx, ty):
    tx = max(0, min(tex.width - 1, tx))
    ty = max(0, min(tex.height - 1, ty))
    return tex.getpixel((tx, ty))


def render(top_tex, bottom_tex, outline):
    """Builds one 16x16 icon from the top/bottom door textures."""
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    px = img.load()

    for y, row in enumerate(TEMPLATE):
        for x, ch in enumerate(row):
            if ch == '.':
                continue

            if ch == '#':
                px[x, y] = (*outline, 255)
                continue

            # Upper door half -> top texture, lower half -> bottom texture.
            if y <= 8:
                tex = top_tex
                ty = round((y - 2) / 6.0 * (tex.height - 1))
            else:
                tex = bottom_tex
                ty = round((y - 9) / 6.0 * (tex.height - 1))
            tx = round((x - 3) / 9.0 * (tex.width - 1))

            r, g, b, a = sample(tex, tx, ty)
            if a == 0:
                # Texture window hole — fall back to the nearest opaque row.
                for ty2 in (ty + 1, ty - 1, ty + 2, ty - 2):
                    r, g, b, a = sample(tex, tx, ty2)
                    if a > 0:
                        break
            if a == 0:
                r, g, b = 32, 21, 48

            factor = SHADE_FACTOR.get(ch, 1.0)
            px[x, y] = (
                min(255, round(r * factor)),
                min(255, round(g * factor)),
                min(255, round(b * factor)),
                255,
            )
    return img


def load_frames(path):
    """Returns the texture as a list of 16x16 frames (static = 1 frame)."""
    tex = Image.open(path).convert('RGBA')
    frames = []
    for k in range(tex.height // 16):
        frames.append(tex.crop((0, k * 16, 16, (k + 1) * 16)))
    return frames


def main():
    obs_top = load_frames(BLOCK / 'obsidian_door_top.png')[0]
    obs_bottom = load_frames(BLOCK / 'obsidian_door_bottom.png')[0]
    outline = darkest_color(obs_top, obs_bottom)
    render(obs_top, obs_bottom, outline).save(ITEM / 'obsidian_door.png')
    print(f'{ITEM / "obsidian_door.png"}  16x16 static '
          f'(outline #{outline[0]:02x}{outline[1]:02x}{outline[2]:02x})')

    rb_top = load_frames(BLOCK / 'rainbow_door_top.png')
    rb_bottom = load_frames(BLOCK / 'rainbow_door_bottom.png')
    count = max(len(rb_top), len(rb_bottom))
    frames = Image.new('RGBA', (16, 16 * count), (0, 0, 0, 0))
    for k in range(count):
        t = rb_top[k % len(rb_top)]
        b = rb_bottom[k % len(rb_bottom)]
        frames.paste(render(t, b, darkest_color(t, b)), (0, k * 16))
    frames.save(ITEM / 'rainbow_door.png')
    print(f'{ITEM / "rainbow_door.png"}  16x{16 * count} ({count} frames)')


if __name__ == '__main__':
    main()
