#!/usr/bin/env python3
# kitchen machines and vegan foods: textures, models, blockstates, data. run from repo root, then gen_eras.py
import json, os, random
from PIL import Image

MOD = "morefood"
RES = "src/main/resources/assets/morefood"
ERA = "src/eras/26.1"
TEX = RES + "/textures"


def w(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")


def hexc(h, a=255):
    return ((h >> 16) & 255, (h >> 8) & 255, h & 255, a)


def shade(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c[:3]) + (c[3] if len(c) > 3 else 255,)


def mix(a, b, t):
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3)) + (255,)


def new(n=16):
    return Image.new("RGBA", (n, n), (0, 0, 0, 0))


def rect(im, x0, y0, x1, y1, c):
    px = im.load()
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            if 0 <= x < im.width and 0 <= y < im.height:
                px[x, y] = c


def outline(im, x0, y0, x1, y1, c):
    rect(im, x0, y0, x1, y0, c); rect(im, x0, y1, x1, y1, c)
    rect(im, x0, y0, x0, y1, c); rect(im, x1, y0, x1, y1, c)


def noise(im, x0, y0, x1, y1, base, amt, seed):
    r = random.Random(seed); px = im.load()
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            px[x, y] = shade(base, 1 + r.uniform(-amt, amt))


def disc(im, cx, cy, rad, c, edge=None):
    px = im.load()
    for y in range(im.height):
        for x in range(im.width):
            d = ((x + .5 - cx) ** 2 + (y + .5 - cy) ** 2) ** .5
            if d <= rad:
                px[x, y] = edge if edge and d > rad - 1 else c


# palettes
OAK = [hexc(0xC29D62), hexc(0xAF8F55), hexc(0x9A7A48), hexc(0x7E6237), hexc(0x5E4826)]
STONE = [hexc(0xB0B0B0), hexc(0x9C9C9C), hexc(0x868686), hexc(0x6E6E6E), hexc(0x4E4E4E)]
IRON = [hexc(0xF0F0F0), hexc(0xD0D0D0), hexc(0xAAAAAA), hexc(0x7E7E7E), hexc(0x505050)]
BRICK = [hexc(0xB5614A), hexc(0x965040), hexc(0x7A3E31), hexc(0xC9C2B0)]


def planks(seed, dark=0):
    im = new(); r = random.Random(seed); px = im.load()
    for y in range(16):
        for x in range(16):
            px[x, y] = shade(OAK[1 + dark], 1 + r.uniform(-.05, .05))
    for y in (3, 7, 11, 15):
        rect(im, 0, y, 15, y, OAK[3 + dark])
    for y in range(0, 16, 4):
        for k in range(2):
            gx = r.randrange(16); rect(im, gx, y + 1, min(15, gx + r.randrange(2, 5)), y + 1, OAK[2 + dark])
        bx = r.randrange(2, 14); rect(im, bx, y, bx, y + 2, OAK[3 + dark])
    return im


def frame_tex():
    im = new(); r = random.Random(3); px = im.load()
    for y in range(16):
        for x in range(16):
            px[x, y] = shade(OAK[2], 1 + r.uniform(-.06, .06))
    for x in range(16):
        if r.random() < .3: rect(im, x, r.randrange(16), x, r.randrange(16), OAK[3])
    outline(im, 0, 0, 15, 15, OAK[4])
    for x, y in ((2, 2), (13, 2), (2, 13), (13, 13)):
        px[x, y] = IRON[3]
    return im


def panel_tex():
    im = planks(11)
    outline(im, 0, 0, 15, 15, OAK[4]); outline(im, 1, 1, 14, 14, OAK[0])
    rect(im, 2, 14, 14, 14, OAK[3]); rect(im, 14, 2, 14, 14, OAK[3])
    for x, y in ((2, 2), (13, 2), (2, 13), (13, 13)):
        im.putpixel((x, y), IRON[2])
    return im


def worktop_tex():
    im = new(); noise(im, 0, 0, 15, 15, STONE[0], .04, 21)
    outline(im, 0, 0, 15, 15, STONE[2])
    return im


def worktop_side():
    im = new(); noise(im, 0, 0, 15, 15, STONE[1], .04, 22)
    rect(im, 0, 0, 15, 0, STONE[0]); rect(im, 0, 15, 15, 15, STONE[3])
    return im


def core_tex():
    im = new(); noise(im, 0, 0, 15, 15, STONE[2], .06, 31)
    outline(im, 0, 0, 15, 15, STONE[4])
    disc(im, 8, 8, 6, STONE[1], STONE[3])
    disc(im, 8, 8, 2, OAK[3], OAK[4])
    return im


def gearbox_tex():
    im = planks(41, 1)
    outline(im, 0, 0, 15, 15, OAK[4])
    gear(im, 8, 8, 6, OAK[0], OAK[3])
    return im


def gear(im, cx, cy, rad, c, edge):
    disc(im, cx, cy, rad - 1.5, c, edge)
    px = im.load()
    for dx, dy in ((0, -1), (0, 1), (-1, 0), (1, 0), (.7, .7), (-.7, .7), (.7, -.7), (-.7, -.7)):
        for k in (rad - 1.5, rad - .5):
            x, y = int(cx + dx * k), int(cy + dy * k)
            if 0 <= x < 16 and 0 <= y < 16: px[x, y] = c
    disc(im, cx, cy, 1.5, IRON[3])


def grinder_front():
    im = panel_tex()
    rect(im, 4, 9, 11, 12, OAK[4]); outline(im, 3, 8, 12, 13, IRON[3])
    rect(im, 5, 10, 10, 12, hexc(0xE8DDBE))
    rect(im, 5, 3, 10, 5, OAK[3]); outline(im, 4, 2, 11, 6, OAK[4])
    return im


def millstone_side():
    im = new(); noise(im, 0, 0, 15, 15, STONE[2], .08, 51)
    rect(im, 0, 7, 15, 8, STONE[4])
    return im


def millstone_top():
    im = new(); noise(im, 0, 0, 15, 15, STONE[1], .06, 52)
    disc(im, 8, 8, 8, STONE[1], STONE[3])
    for k in range(4):
        rect(im, 8 + k, 7, 8 + k, 7, STONE[3]); rect(im, 7 - k, 8, 7 - k, 8, STONE[3])
    disc(im, 8, 8, 2.2, OAK[4])
    return im


def hopper_tex():
    im = planks(61, 1); outline(im, 0, 0, 15, 15, OAK[4])
    return im


def hopper_top():
    im = hopper_tex(); rect(im, 3, 3, 12, 12, OAK[4]); rect(im, 5, 5, 10, 10, hexc(0x2C2014))
    return im


def press_front():
    im = panel_tex()
    rect(im, 3, 3, 12, 12, IRON[3]); rect(im, 4, 4, 11, 11, IRON[2])
    rect(im, 4, 9, 11, 11, hexc(0xD8C070))
    rect(im, 6, 12, 9, 13, IRON[4]); im.putpixel((7, 14), hexc(0xD8C070))
    return im


def iron_tex(seed=71):
    im = new(); noise(im, 0, 0, 15, 15, IRON[2], .05, seed)
    outline(im, 0, 0, 15, 15, IRON[3])
    return im


def screw_tex():
    im = new(); px = im.load()
    for y in range(16):
        for x in range(16):
            px[x, y] = IRON[1] if (x + y) % 4 < 2 else IRON[3]
    return im


def crank_tex():
    im = new(); rect(im, 0, 0, 15, 15, IRON[3]); rect(im, 0, 0, 15, 7, IRON[2])
    rect(im, 0, 8, 15, 15, OAK[3])
    return im


def sink_side():
    im = planks(81)
    outline(im, 0, 0, 15, 15, OAK[4])
    return im


def sink_front():
    im = planks(82)
    outline(im, 0, 0, 15, 15, OAK[4])
    rect(im, 7, 1, 8, 14, OAK[4])
    outline(im, 2, 2, 5, 13, OAK[3]); outline(im, 10, 2, 13, 13, OAK[3])
    rect(im, 6, 6, 6, 8, IRON[1]); rect(im, 9, 6, 9, 8, IRON[1])
    return im


def counter_tex():
    im = new(); noise(im, 0, 0, 15, 15, hexc(0xE4E1D8), .03, 91)
    r = random.Random(92)
    for k in range(6):
        im.putpixel((r.randrange(16), r.randrange(16)), hexc(0xBDB8AA))
    return im


def basin_tex():
    im = new(); noise(im, 0, 0, 15, 15, IRON[1], .03, 93)
    for x in range(0, 16, 3):
        rect(im, x, 0, x, 15, IRON[2])
    return im


def water_tex():
    im = new(); noise(im, 0, 0, 15, 15, hexc(0x3F76E4, 200), .08, 94)
    px = im.load()
    for y in range(16):
        for x in range(16):
            px[x, y] = px[x, y][:3] + (190,)
    rect(im, 2, 3, 6, 3, hexc(0x9CC0FF, 200)); rect(im, 9, 10, 12, 10, hexc(0x9CC0FF, 200))
    return im


# item icons

def item_core_grinder():
    im = new(); disc(im, 8, 9, 6.5, STONE[1], STONE[4])
    disc(im, 8, 9, 4.2, STONE[2])
    disc(im, 8, 9, 1.6, OAK[4])
    rect(im, 7, 1, 8, 6, IRON[3]); rect(im, 7, 1, 8, 1, IRON[1])
    return im


def item_core_press():
    im = new()
    for y in range(1, 11):
        rect(im, 7, y, 8, y, IRON[1] if y % 2 else IRON[3])
    rect(im, 3, 0, 12, 1, OAK[2]); rect(im, 3, 0, 12, 0, OAK[1])
    rect(im, 2, 11, 13, 12, IRON[2]); rect(im, 2, 13, 13, 13, IRON[4])
    rect(im, 1, 14, 14, 15, OAK[3]); rect(im, 1, 14, 14, 14, OAK[2])
    return im


def item_gearbox():
    im = new(); gear(im, 8, 8, 7.5, OAK[1], OAK[4])
    gear(im, 8, 8, 3.5, OAK[2], OAK[4])
    return im


def item_panel():
    im = new(); p = panel_tex()
    im.paste(p.crop((0, 0, 12, 14)), (2, 1))
    outline(im, 2, 1, 13, 14, OAK[4])
    return im


def item_worktop():
    im = new()
    rect(im, 0, 5, 15, 9, STONE[0]); rect(im, 0, 10, 15, 11, STONE[2])
    outline(im, 0, 5, 15, 11, STONE[4])
    noise(im, 1, 6, 14, 8, STONE[0], .04, 101)
    return im


def item_firebox():
    im = new()
    for y in range(3, 15):
        for x in range(1, 15):
            c = BRICK[0] if ((y - 3) // 3 + (x + ((y - 3) // 3) * 2) // 4) % 3 else BRICK[1]
            im.putpixel((x, y), BRICK[3] if (y - 3) % 3 == 2 or (x + ((y - 3) // 3) * 2) % 4 == 3 else c)
    outline(im, 1, 3, 14, 14, BRICK[2])
    rect(im, 4, 8, 11, 14, hexc(0x2A1A10))
    rect(im, 5, 11, 10, 14, hexc(0xE07A1F)); rect(im, 6, 12, 9, 14, hexc(0xFFD24A))
    return im


def pile(base, seed, speck=None):
    im = new(); r = random.Random(seed); px = im.load()
    for y in range(6, 15):
        half = int((y - 5) * 0.9) + 1
        for x in range(8 - half, 8 + half):
            if 0 <= x < 16:
                t = .85 + .3 * (1 - (y - 6) / 9) - .2 * abs(x - 8) / 8
                px[x, y] = shade(base, t + r.uniform(-.06, .06))
    for x in range(16):
        if px[x, 14][3]: px[x, 14] = shade(base, .65)
    if speck:
        for k in range(8):
            x, y = r.randrange(4, 12), r.randrange(8, 14)
            if px[x, y][3]: px[x, y] = speck
    return im


def jar(content, lid, seed):
    im = new(); r = random.Random(seed)
    rect(im, 4, 1, 11, 3, lid); rect(im, 4, 3, 11, 3, shade(lid, .7)); rect(im, 5, 1, 10, 1, shade(lid, 1.2))
    rect(im, 3, 4, 12, 14, hexc(0xDCE8EC)); outline(im, 3, 4, 12, 14, hexc(0x9FB2B8))
    noise(im, 4, 6, 11, 13, content, .06, seed)
    rect(im, 4, 13, 11, 13, shade(content, .75))
    rect(im, 5, 6, 5, 11, shade(content, 1.25))
    return im


def recolor(src, test, ramp_from):
    im = Image.open(f"{TEX}/item/{src}.png").convert("RGBA"); px = im.load()
    for y in range(16):
        for x in range(16):
            c = px[x, y]
            if c[3] and test(c):
                px[x, y] = ramp_from(c)
    return im


def bottle(color):
    return recolor("item_juice_apple", lambda c: c[:3] == (0x99, 0xcb, 0x21), lambda c: hexc(color))


def bowl(ramp, garnish=None, seed=0):
    keys = {(0xe0, 0x90, 0x40): 0, (0xc8, 0x6a, 0x28): 1, (0xa8, 0x50, 0x18): 2, (0x8a, 0x3c, 0x10): 3}
    im = recolor("item_soup_lentil", lambda c: c[:3] in keys, lambda c: hexc(ramp[keys[c[:3]]]))
    if garnish:
        r = random.Random(seed)
        for k in range(4):
            im.putpixel((r.randrange(5, 11), r.randrange(6, 8)), hexc(garnish))
    return im


def cube(top, side, front, spots=None, seed=0):
    im = new(); px = im.load(); r = random.Random(seed)
    for y in range(16):
        for x in range(16):
            # simple isometric block 2..13
            pass
    rect(im, 2, 4, 13, 7, hexc(top)); rect(im, 2, 8, 13, 13, hexc(side))
    rect(im, 9, 8, 13, 13, hexc(front))
    rect(im, 3, 3, 12, 3, hexc(top))
    outline(im, 2, 3, 13, 13, shade(hexc(side), .7))
    if spots:
        for k in range(10):
            x, y = r.randrange(3, 13), r.randrange(4, 13)
            px[x, y] = hexc(spots)
    return im


def balls(color, n_seed):
    im = new()
    for cx, cy in ((5, 10), (11, 10), (8, 6)):
        disc(im, cx, cy, 3.4, hexc(color), shade(hexc(color), .65))
        im.putpixel((int(cx) - 1, int(cy) - 1), shade(hexc(color), 1.25))
    r = random.Random(n_seed); px = im.load()
    for k in range(10):
        x, y = r.randrange(16), r.randrange(16)
        if px[x, y][3]: px[x, y] = shade(px[x, y], .8)
    return im


def lump(src_ramp, seed):
    keys = {(0xef, 0xd2, 0x94): 0, (0xea, 0xca, 0x85): 1, (0xdb, 0xb8, 0x74): 2, (0xea, 0xcd, 0x8f): 0}
    return recolor("item_dough", lambda c: c[:3] in keys, lambda c: hexc(src_ramp[keys[c[:3]]]))


def oatcake():
    im = new(); disc(im, 8, 8, 6.5, hexc(0xD7B57A), hexc(0x9C7A45))
    r = random.Random(7); px = im.load()
    for k in range(14):
        x, y = r.randrange(3, 13), r.randrange(3, 13)
        if px[x, y][3]: px[x, y] = hexc(0xB8925A)
    return im


def gui():
    im = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    W, H = 176, 166
    rect(im, 3, 0, W - 4, H - 1, (0, 0, 0, 255)); rect(im, 0, 3, W - 1, H - 4, (0, 0, 0, 255))
    rect(im, 1, 2, W - 2, H - 3, (0, 0, 0, 255)); rect(im, 2, 1, W - 3, H - 2, (0, 0, 0, 255))
    rect(im, 3, 1, W - 4, H - 2, hexc(0xC6C6C6)); rect(im, 1, 3, W - 2, H - 4, hexc(0xC6C6C6))
    rect(im, 2, 2, W - 3, H - 3, hexc(0xC6C6C6))
    rect(im, 3, 1, W - 5, 2, hexc(0xFFFFFF)); rect(im, 1, 3, 2, H - 5, hexc(0xFFFFFF)); im.putpixel((3, 3), hexc(0xFFFFFF))
    rect(im, 4, H - 3, W - 4, H - 2, hexc(0x555555)); rect(im, W - 3, 4, W - 2, H - 4, hexc(0x555555))
    im.putpixel((W - 4, H - 4), hexc(0x555555))

    def slot(x, y, s=18):
        rect(im, x, y, x + s - 1, y + s - 1, hexc(0x8B8B8B))
        rect(im, x, y, x + s - 2, y, hexc(0x373737)); rect(im, x, y, x, y + s - 2, hexc(0x373737))
        rect(im, x + 1, y + s - 1, x + s - 1, y + s - 1, hexc(0xFFFFFF)); rect(im, x + s - 1, y + 1, x + s - 1, y + s - 1, hexc(0xFFFFFF))
    for x in (26, 44, 62):
        slot(x - 1, 16)
    slot(111, 30, 26)
    slot(141, 34)
    slot(43, 52)
    for r_ in range(3):
        for c in range(9):
            slot(7 + c * 18, 83 + r_ * 18)
    for c in range(9):
        slot(7 + c * 18, 141)
    # empty arrow at 85,35 (24x17) and flame at 45,36 (14x14)
    for x in range(22):
        h = 4 if x < 15 else 8 - (x - 15)
        if x < 15:
            rect(im, 85 + x, 35 + 8 - 2, 85 + x, 35 + 8 + 1, hexc(0x8B8B8B))
        else:
            rect(im, 85 + x, 35 + 8 - h, 85 + x, 35 + 8 + h - 1, hexc(0x8B8B8B))
    for y in range(14):
        half = int((y / 13) * 5) + 1 if y > 3 else 1
        rect(im, 51 - half, 36 + y, 51 + half, 36 + y, hexc(0x8B8B8B))
    # lit sprite at 176,0 (14x14) and progress arrow at 176,14 (24x17), same layout as the stove screen
    for y in range(14):
        half = int((y / 13) * 5) + 1 if y > 3 else 1
        for x in range(51 - half, 52 + half):
            t = y / 13
            im.putpixel((176 + x - 45, y), mix(hexc(0xFFE26A), hexc(0xE0441C), t))
    for x in range(22):
        h = 4 if x < 15 else 8 - (x - 15)
        if x < 15:
            rect(im, 176 + x, 14 + 8 - 2, 176 + x, 14 + 8 + 1, hexc(0xFFFFFF))
        else:
            rect(im, 176 + x, 14 + 8 - h, 176 + x, 14 + 8 + h - 1, hexc(0xFFFFFF))
    return im


BLOCK_TEX = {
    "kitchen_frame": frame_tex, "kitchen_panel": panel_tex, "kitchen_worktop": worktop_tex,
    "kitchen_worktop_side": worktop_side, "kitchen_core": core_tex, "kitchen_gearbox": gearbox_tex,
    "grinder_front": grinder_front, "millstone_side": millstone_side, "millstone_top": millstone_top,
    "grinder_hopper": hopper_tex, "grinder_hopper_top": hopper_top, "press_front": press_front,
    "press_iron": iron_tex, "press_screw": screw_tex, "machine_crank": crank_tex, "press_frame": lambda: planks(77, 1),
    "sink_side": sink_side, "sink_front": sink_front, "sink_counter": counter_tex, "sink_basin": basin_tex,
    "sink_water": water_tex,
}

ITEM_TEX = {
    "item_grinder_core": item_core_grinder, "item_press_core": item_core_press, "item_kitchen_gearbox": item_gearbox,
    "item_kitchen_panel": item_panel, "item_kitchen_worktop": item_worktop, "item_firebox": item_firebox,
    "item_ground_soy": lambda: pile(hexc(0xE6D49A), 1, hexc(0xC9B26E)),
    "item_chickpea_paste": lambda: pile(hexc(0xD9C08A), 2, hexc(0xB89C62)),
    "item_oat_flour": lambda: recolor("item_flour", lambda c: c[0] > 0xE8, lambda c: shade(hexc(0xE3CFA0), c[0] / 0xF8)),
    "item_peanut_butter": lambda: jar(hexc(0xB07A3A), hexc(0xC0392B), 3),
    "item_soy_milk": lambda: bottle(0xF2EEDC), "item_oat_milk": lambda: bottle(0xE9DDBF),
    "item_coconut_milk": lambda: bottle(0xFAFAF4), "item_olive_oil": lambda: bottle(0xB5B530),
    "item_vegetable_oil": lambda: bottle(0xF0C93A),
    "item_tofu": lambda: cube(0xF4F1E6, 0xE2DDCB, 0xD3CDB8),
    "item_tempeh": lambda: cube(0xD8BE86, 0xC4A66C, 0xB39558, 0xF1E6C8, 5),
    "item_seitan": lambda: lump([0xC8A27A, 0xB38D63, 0x977250], 1),
    "item_seitan_fried": lambda: lump([0xA9703A, 0x8E5A2C, 0x6E4220], 2),
    "item_falafel_uncooked": lambda: balls(0x9DAA5B, 3), "item_falafel": lambda: balls(0x9A5E2A, 4),
    "item_hummus": lambda: bowl([0xEEDCAE, 0xDCC690, 0xC9AF78, 0xAE935E], 0xB5B530, 1),
    "item_guacamole": lambda: bowl([0xA8C95A, 0x8DB043, 0x739233, 0x5A7526], 0xD8382A, 2),
    "item_satay": lambda: bowl([0xD79A52, 0xBF8040, 0xA16830, 0x825224], 0xF4F1E6, 3),
    "item_pulled_jackfruit": lambda: bowl([0xC27242, 0xA65A30, 0x8B4524, 0x6E3418], 0xE6C27A, 4),
    "item_butter_vegan": lambda: recolor("item_butter", lambda c: True, lambda c: mix(c, (250, 246, 225, 255), .5)),
    "item_oatcake": oatcake,
}


def textures():
    for n, f in BLOCK_TEX.items():
        im = f(); os.makedirs(f"{TEX}/block/machine", exist_ok=True); im.save(f"{TEX}/block/machine/{n}.png")
    for n, f in ITEM_TEX.items():
        f().save(f"{TEX}/item/{n}.png")
    os.makedirs(f"{TEX}/gui", exist_ok=True)
    gui().save(f"{TEX}/gui/kitchen_machine.png")


# models

def faces(tex, uv=None, skip=(), cull=False, **over):
    out = {}
    for d in ("north", "south", "east", "west", "up", "down"):
        if d in skip: continue
        f = {"texture": over.get(d, tex)}
        if uv: f["uv"] = uv(d)
        out[d] = f
    return out


def el(fr, to, tex, skip=(), **over):
    def uv(d):
        x0, y0, z0 = fr; x1, y1, z1 = to
        if d in ("up", "down"): return [x0, z0, x1, z1]
        if d in ("north", "south"): return [x0, 16 - y1, x1, 16 - y0]
        return [z0, 16 - y1, z1, 16 - y0]
    e = {"from": fr, "to": to, "faces": faces(tex, uv, skip, **over)}
    for d in e["faces"].values():
        d["uv"] = [max(0, min(16, v)) for v in d["uv"]]
        if d["uv"][0] == d["uv"][2]: d["uv"][2] = min(16, d["uv"][0] + 1)
        if d["uv"][1] == d["uv"][3]: d["uv"][3] = min(16, d["uv"][1] + 1)
    return e


def model(name, textures_, elements, parent="block/block"):
    m = {"parent": parent, "textures": textures_, "elements": elements}
    w(f"{RES}/models/block/{name}.json", m)


def full_up(e):
    e["faces"]["up"]["uv"] = [0, 0, 16, 16]
    return e


T = lambda n: f"{MOD}:block/machine/{n}"


def frame_elements():
    els = []
    for x, z in ((0, 0), (14, 0), (0, 14), (14, 14)):
        els.append(el([x, 0, z], [x + 2, 16, z + 2], "#frame"))
    for y in (0, 14):
        els.append(el([2, y, 0], [14, y + 2, 2], "#frame"))
        els.append(el([2, y, 14], [14, y + 2, 16], "#frame"))
        els.append(el([0, y, 2], [2, y + 2, 14], "#frame"))
        els.append(el([14, y, 2], [16, y + 2, 14], "#frame"))
    els.append(el([2, 1, 2], [14, 2, 14], "#floor"))
    return els


def models():
    model("kitchen_chassis", {"particle": T("kitchen_frame"), "frame": T("kitchen_frame"), "floor": "minecraft:block/oak_planks"}, frame_elements())
    for loose, dy in (("", 0), ("_loose", 1.5)):
        model("kitchen_chassis_core" + loose, {"particle": T("kitchen_core"), "c": T("kitchen_core")},
              [el([4, 2 + dy, 4], [12, 9 + dy, 12], "#c")])
        model("kitchen_chassis_gearbox" + loose, {"particle": T("kitchen_gearbox"), "g": T("kitchen_gearbox")},
              [el([3, 3 + dy, 12], [13, 13 + dy, 14], "#g")])
        o = .75 if loose else 0
        model("kitchen_chassis_panels" + loose, {"particle": T("kitchen_panel"), "p": T("kitchen_panel")},
              [el([2, 2, 1 - o], [14, 14, 2 - o], "#p"), el([2, 2, 14 + o], [14, 14, 15 + o], "#p"),
               el([1 - o, 2, 2], [2 - o, 14, 14], "#p"), el([14 + o, 2, 2], [15 + o, 14, 14], "#p")])
    model("kitchen_chassis_worktop_loose", {"particle": T("kitchen_worktop"), "t": T("kitchen_worktop"), "s": T("kitchen_worktop_side")},
          [el([0, 16.5, 0], [16, 18.5, 16], "#s", up="#t", down="#t")])

    body = [el([0, 0, 0], [16, 14, 16], "#side", north="#front", up="#top", down="#bottom"),
            el([-.5, 14, -.5], [16.5, 16, 16.5], "#wside", up="#wtop", down="#wtop")]
    base = {"side": T("kitchen_panel"), "bottom": "minecraft:block/oak_planks", "wtop": T("kitchen_worktop"),
            "wside": T("kitchen_worktop_side"), "top": T("kitchen_worktop"), "crank": T("machine_crank")}
    crank = [el([16, 7, 7], [18.5, 9, 9], "#crank"), el([18.5, 3, 7], [19.5, 9, 9], "#crank"),
             el([19.5, 3, 7], [21.5, 5, 9], "#crank")]
    model("block_grinder", dict(base, particle=T("kitchen_panel"), front=T("grinder_front"), ms=T("millstone_side"),
                                mt=T("millstone_top"), h=T("grinder_hopper"), ht=T("grinder_hopper_top")),
          body + crank + [el([3, 16, 3], [13, 19, 13], "#ms", up="#mt", down="#mt"),
                          full_up(el([5, 19, 5], [11, 23, 11], "#h", up="#ht"))])
    model("block_press", dict(base, particle=T("kitchen_panel"), front=T("press_front"), i=T("press_iron"),
                              s=T("press_screw"), f=T("press_frame")),
          body + [el([3, 16, 3], [13, 17, 13], "#i"),
                  el([2, 16, 6], [4, 28, 10], "#f"), el([12, 16, 6], [14, 28, 10], "#f"), el([2, 26, 6], [14, 28, 10], "#f"),
                  el([7, 19, 7], [9, 30, 9], "#s"), el([4, 18, 4], [12, 19, 12], "#i"),
                  el([3, 30, 7.5], [13, 31, 8.5], "#crank"), el([2, 29, 7], [3.5, 32, 9], "#crank")])
    model("block_kitchen_sink", {"particle": T("sink_side"), "side": T("sink_side"), "front": T("sink_front"),
                                 "c": T("sink_counter"), "b": T("sink_basin"), "wa": T("sink_water"), "i": T("press_iron")},
          [el([0, 0, 0], [16, 13, 16], "#side", north="#front", up="#b", down="#side"),
           el([0, 13, 0], [16, 16, 3], "#c", south="#b"), el([0, 13, 13], [16, 16, 16], "#c", north="#b"),
           el([0, 13, 3], [3, 16, 13], "#c", east="#b"), el([13, 13, 3], [16, 16, 13], "#c", west="#b"),
           {"from": [3, 14.5, 3], "to": [13, 14.5, 13], "faces": {"up": {"texture": "#wa", "uv": [3, 3, 13, 13]}}},
           el([7, 16, 13], [9, 20, 15], "#i"), el([7, 19, 9], [9, 20, 13], "#i"), el([7, 18, 9], [9, 19, 10], "#i")])


def blockstates():
    rot = {"north": 0, "east": 90, "south": 180, "west": 270}

    def ap(m, f):
        a = {"model": f"{MOD}:block/{m}"}
        if rot[f]: a["y"] = rot[f]
        return a
    parts = [{"apply": {"model": f"{MOD}:block/kitchen_chassis"}}]
    for f in rot:
        for m, steps in (("kitchen_chassis_core_loose", "1"), ("kitchen_chassis_core", "2|3|4|5|6|7"),
                         ("kitchen_chassis_gearbox_loose", "3"), ("kitchen_chassis_gearbox", "4|5|6|7"),
                         ("kitchen_chassis_panels_loose", "5"), ("kitchen_chassis_panels", "6|7"),
                         ("kitchen_chassis_worktop_loose", "7")):
            parts.append({"when": {"facing": f, "step": steps}, "apply": ap(m, f)})
    w(f"{RES}/blockstates/block_kitchen_chassis.json", {"multipart": parts})
    for b in ("block_grinder", "block_press", "block_kitchen_sink"):
        w(f"{RES}/blockstates/{b}.json", {"variants": {f"facing={f}": ap(b, f) for f in rot}})


FOODS = ["item_ground_soy", "item_chickpea_paste", "item_oat_flour", "item_peanut_butter", "item_soy_milk",
         "item_oat_milk", "item_olive_oil", "item_vegetable_oil", "item_coconut_milk", "item_tofu", "item_tempeh",
         "item_seitan", "item_seitan_fried", "item_falafel_uncooked", "item_falafel", "item_hummus", "item_guacamole",
         "item_satay", "item_pulled_jackfruit", "item_butter_vegan", "item_oatcake"]
PARTS = ["item_grinder_core", "item_press_core", "item_kitchen_gearbox", "item_kitchen_panel", "item_kitchen_worktop", "item_firebox"]
BLOCKS = {"block_kitchen_chassis": "kitchen_chassis", "block_grinder": "block_grinder", "block_press": "block_press",
          "block_kitchen_sink": "block_kitchen_sink"}


def items():
    for n in FOODS + PARTS:
        w(f"{RES}/models/item/{n}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{MOD}:item/{n}"}})
        w(f"{ERA}/assets/{MOD}/items/{n}.json", {"model": {"type": "minecraft:model", "model": f"{MOD}:item/{n}"}})
    for n, m in BLOCKS.items():
        w(f"{RES}/models/item/{n}.json", {"parent": f"{MOD}:block/{m}"})
        w(f"{ERA}/assets/{MOD}/items/{n}.json", {"model": {"type": "minecraft:model", "model": f"{MOD}:block/{m}"}})


def loot(n, drop=True):
    pools = []
    if drop:
        pools = [{"rolls": 1, "bonus_rolls": 0, "entries": [{"type": "minecraft:item", "name": f"{MOD}:{n}"}],
                  "conditions": [{"condition": "minecraft:survives_explosion"}]}]
    w(f"{ERA}/data/{MOD}/loot_table/blocks/{n}.json", {"type": "minecraft:block", "pools": pools, "random_sequence": f"{MOD}:blocks/{n}"})


def tag(path, values):
    p = f"{ERA}/data/{path}.json"
    old = json.load(open(p))["values"] if os.path.exists(p) else []
    w(p, {"values": old + [v for v in values if v not in old]})


def shaped(name, pattern, key, result, count=1):
    r = {"id": f"{MOD}:{result}"}
    if count > 1: r["count"] = count
    w(f"{ERA}/data/{MOD}/recipe/{name}.json", {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": pattern, "key": key, "result": r})


def shapeless(name, ings, result, count=1):
    r = {"id": f"{MOD}:{result}"}
    if count > 1: r["count"] = count
    w(f"{ERA}/data/{MOD}/recipe/{name}.json", {"type": "minecraft:crafting_shapeless", "category": "misc", "ingredients": ings, "result": r})


def cooked(kind, name, ing, result, time=100, xp=.1):
    w(f"{ERA}/data/{MOD}/recipe/{name}.json", {"type": f"{MOD}:{kind}", "category": "food", "cookingtime": time, "experience": xp,
                                                "ingredient": ing, "result": {"id": f"{MOD}:{result}"}})


def boiled(name, ings, result, colour, portions=2, time=400):
    w(f"{ERA}/data/{MOD}/recipe/{name}.json", {"type": f"{MOD}:boiling", "ingredients": ings, "water": 250, "result": {"id": f"{MOD}:{result}"},
                                                "portions": portions, "cookingtime": time, "colour": colour, "vessel": "minecraft:bowl"})


M = lambda n: f"{MOD}:{n}"


def data():
    loot("block_kitchen_chassis"); loot("block_kitchen_sink")
    loot("block_grinder", False); loot("block_press", False)
    tag("workshopcore/tags/item/control_boards", [M("item_kitchen_gearbox")])
    tag("workshopcore/tags/item/machine_panels", [M("item_kitchen_panel")])
    tag("c/tags/item/worktops", [M("item_kitchen_worktop")])
    tag("minecraft/tags/block/mineable/axe", [M("block_kitchen_chassis"), M("block_grinder"), M("block_press")])
    tag("minecraft/tags/block/mineable/pickaxe", [M("block_kitchen_sink")])

    shaped("block_kitchen_chassis", ["PPP", "S S", "PPP"], {"P": "#minecraft:planks", "S": "minecraft:stick"}, "block_kitchen_chassis")
    shaped("item_grinder_core", [" F ", "SMS", " S "], {"F": "minecraft:flint", "S": "minecraft:cobblestone", "M": M("item_pestle_mortar")}, "item_grinder_core")
    shaped("item_press_core", [" S ", "PJP", "PPP"], {"S": "minecraft:stick", "P": "#minecraft:planks", "J": M("item_juicer")}, "item_press_core")
    shaped("item_kitchen_gearbox", [" S ", "SPS", " S "], {"S": "minecraft:stick", "P": "#minecraft:planks"}, "item_kitchen_gearbox")
    shaped("item_kitchen_panel", ["S", "S"], {"S": "#minecraft:wooden_slabs"}, "item_kitchen_panel", 2)
    shaped("item_kitchen_worktop", ["CCC"], {"C": "minecraft:smooth_stone_slab"}, "item_kitchen_worktop")
    shaped("item_firebox", ["B B", "BBB"], {"B": "minecraft:brick"}, "item_firebox")
    shaped("block_kitchen_sink", ["I I", "PIP"], {"I": "minecraft:iron_ingot", "P": "#minecraft:planks"}, "block_kitchen_sink")

    pm, ju = M("item_pestle_mortar"), M("item_juicer")
    shapeless("item_ground_soy", [pm, M("item_soybeans")], "item_ground_soy")
    shapeless("item_chickpea_paste", [pm, M("item_chickpeas")], "item_chickpea_paste")
    shapeless("item_peanut_butter", [pm, M("item_peanuts"), M("item_peanuts")], "item_peanut_butter")
    shapeless("item_oat_flour", [pm, M("item_oats")], "item_oat_flour")
    shapeless("item_soy_milk", [ju, M("item_ground_soy"), "minecraft:water_bucket", "minecraft:glass_bottle"], "item_soy_milk")
    shapeless("item_oat_milk", [ju, M("item_oats"), "minecraft:water_bucket", "minecraft:glass_bottle"], "item_oat_milk")
    shapeless("item_olive_oil", [ju, M("item_olive"), M("item_olive"), "minecraft:glass_bottle"], "item_olive_oil")
    shapeless("item_vegetable_oil", [ju, M("item_peanuts"), M("item_peanuts"), "minecraft:glass_bottle"], "item_vegetable_oil")
    shapeless("item_vegetable_oil_from_avocado", [ju, M("item_avocado"), "minecraft:glass_bottle"], "item_vegetable_oil")
    shapeless("item_coconut_milk", [ju, M("item_coconut"), "minecraft:glass_bottle"], "item_coconut_milk")
    shapeless("item_tempeh", [M("item_soybeans"), M("item_soybeans"), M("item_soybeans"), "minecraft:brown_mushroom"], "item_tempeh")
    shapeless("item_hummus", [M("item_chickpea_paste"), M("item_olive_oil"), M("item_garlic_chopped"), "minecraft:bowl"], "item_hummus")
    shapeless("item_falafel_uncooked", [M("item_chickpea_paste"), M("item_onion_chopped"), M("item_garlic_chopped")], "item_falafel_uncooked", 2)
    shapeless("item_guacamole", [M("item_knife"), M("item_avocado"), M("item_tomato_chopped"), M("item_onion_chopped"), "minecraft:bowl"], "item_guacamole")
    shapeless("item_butter_vegan", [M("item_mixing_bowl"), M("item_vegetable_oil"), M("item_coconut_milk")], "item_butter_vegan")
    cooked("frying", "item_falafel_from_frying", M("item_falafel_uncooked"), "item_falafel")
    cooked("frying", "item_seitan_fried_from_frying", M("item_seitan"), "item_seitan_fried")
    cooked("baking", "item_oatcake_from_baking", M("item_oat_flour"), "item_oatcake", 200)
    boiled("item_satay_from_boiling", [M("item_tofu"), M("item_tofu"), M("item_peanut_butter"), M("item_coconut_milk")], "item_satay", "#d79a52")
    boiled("item_pulled_jackfruit_from_boiling", [M("item_jackfruit"), M("item_jackfruit"), M("item_sauce_tomato"), M("item_onion_chopped")], "item_pulled_jackfruit", "#a65a30")


LANG = {
    "block.morefood.block_kitchen_chassis": "Kitchen Chassis",
    "block.morefood.block_grinder": "Grinder",
    "block.morefood.block_press": "Press",
    "block.morefood.block_kitchen_sink": "Kitchen Sink",
    "item.morefood.item_grinder_core": "Grinder Core",
    "item.morefood.item_press_core": "Press Core",
    "item.morefood.item_kitchen_gearbox": "Kitchen Gearbox",
    "item.morefood.item_kitchen_panel": "Kitchen Panel",
    "item.morefood.item_kitchen_worktop": "Kitchen Worktop",
    "item.morefood.item_firebox": "Firebox",
    "item.morefood.item_ground_soy": "Ground Soy",
    "item.morefood.item_chickpea_paste": "Hummus Base",
    "item.morefood.item_oat_flour": "Oat Flour",
    "item.morefood.item_peanut_butter": "Peanut Butter",
    "item.morefood.item_soy_milk": "Soy Milk",
    "item.morefood.item_oat_milk": "Oat Milk",
    "item.morefood.item_olive_oil": "Olive Oil",
    "item.morefood.item_vegetable_oil": "Vegetable Oil",
    "item.morefood.item_coconut_milk": "Coconut Milk",
    "item.morefood.item_tofu": "Tofu",
    "item.morefood.item_tempeh": "Tempeh",
    "item.morefood.item_seitan": "Seitan",
    "item.morefood.item_seitan_fried": "Fried Seitan",
    "item.morefood.item_falafel_uncooked": "Raw Falafel",
    "item.morefood.item_falafel": "Falafel",
    "item.morefood.item_hummus": "Hummus",
    "item.morefood.item_guacamole": "Guacamole",
    "item.morefood.item_satay": "Tofu Satay",
    "item.morefood.item_pulled_jackfruit": "Pulled Jackfruit",
    "item.morefood.item_butter_vegan": "Vegan Butter",
    "item.morefood.item_oatcake": "Oatcake",
    "jei.morefood.grinding": "Grinder",
    "jei.morefood.pressing": "Press",
    "jei.morefood.washing": "Kitchen Sink",
    "jei.morefood.cranked": "Crank or fuel, %s",
    "jei.morefood.sink": "Wash in a sink, %s",
}


def lang():
    p = f"{RES}/lang/en_us.json"
    lines = open(p).read().split("\n")
    text = "\n".join(lines)
    add = [f'  "{k}": {json.dumps(v)},' for k, v in LANG.items() if f'"{k}"' not in text]
    if not add: return
    at = max(i for i, l in enumerate(lines) if l.strip().startswith('"jei.morefood.'))
    if not lines[at].rstrip().endswith(","): lines[at] += ","
    lines[at + 1:at + 1] = add
    open(p, "w").write("\n".join(lines))
    json.load(open(p))


if __name__ == "__main__":
    textures(); models(); blockstates(); items(); data(); lang()
