#!/usr/bin/env python3
"""Generate cocoa-like fruit pod assets, fix coconut and seed textures, and render previews."""
import json, math, os, shutil
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

ROOT = Path("/workspace/mods/morefoods-261")
A = ROOT / "src/main/resources/assets"
D26 = ROOT / "src/eras/26.1/data"
TEX_BLOCK = A / "morefood/textures/block"
TEX_ITEM = A / "morefood/textures/item"
PREV = Path("/workspace/mods/morefoods-dist/vegan-preview")

def w(path, obj):
    path = Path(path)
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")

def hexc(s):
    s = s.lstrip("#")
    return (int(s[0:2], 16), int(s[2:4], 16), int(s[4:6], 16), 255)

def blank(n=16):
    return Image.new("RGBA", (n, n), (0, 0, 0, 0))

def put(im, x, y, c):
    if 0 <= x < im.size[0] and 0 <= y < im.size[1]:
        im.putpixel((int(x), int(y)), hexc(c) if isinstance(c, str) else c)

def wp(path, im):
    Path(path).parent.mkdir(parents=True, exist_ok=True)
    im.save(path)

# ---------- seeds ----------
def draw_soy_seeds():
    """Pale yellow-green oval beans with a brown hilum."""
    im = blank()
    # 3x2 ovals, slightly larger than the old dots
    beans = [(3, 3), (9, 2), (5, 8), (10, 9)]
    for i, (cx, cy) in enumerate(beans):
        body, light, dark, out, hilum = "d8d06a", "f0e888", "9a9828", "5a5820", "6a4a18"
        for dx, dy, col in (
            (0, 0, out), (1, 0, out), (2, 0, out),
            (0, 1, out), (1, 1, light), (2, 1, body),
            (0, 2, out), (1, 2, dark), (2, 2, out),
        ):
            put(im, cx + dx, cy + dy, col)
        # hilum on the side
        put(im, cx + (0 if i % 2 == 0 else 2), cy + 1, hilum)
    return im

def draw_chickpea_seeds():
    """Beige beaked chickpeas, distinct from round beans."""
    im = blank()
    # teardrop / beak pointing up-right or down-left
    def pea(cx, cy, flip):
        out, light, body, dark = "6a5830", "f0e0b0", "dcc48a", "a89050"
        # 4x3 body with a one-pixel beak
        coords = {
            (1, 0): out, (2, 0): out,
            (0, 1): out, (1, 1): light, (2, 1): body, (3, 1): out,
            (0, 2): out, (1, 2): body, (2, 2): dark, (3, 2): out,
            (1, 3): out, (2, 3): out,
        }
        # beak
        if not flip:
            coords[(3, 0)] = out
            coords[(4, 1)] = out
        else:
            coords[(0, 0)] = out
            coords[(-1, 1)] = out
        for (dx, dy), col in coords.items():
            put(im, cx + dx, cy + dy, col)
        # little crease
        put(im, cx + 2, cy + 1, "c4a86e")
    pea(2, 2, False)
    pea(8, 1, True)
    pea(4, 8, True)
    pea(9, 9, False)
    return im

def draw_oat_seeds():
    """Elongated pale grains with a husk point, not round dots."""
    im = blank()
    grains = [(2, 3, False), (8, 2, True), (4, 8, False), (10, 9, True)]
    for cx, cy, flip in grains:
        # 2x5 slanted grain
        cols = ["c8b870", "e8d888", "f4e8a8", "d0c060", "8a7a30"]
        for i, col in enumerate(("6a5a28", "e8d888", "f4e8a8", "c8b858", "6a5a28")):
            x = cx + (i if not flip else 4 - i) // 2
            y = cy + i
            put(im, x, y, col)
            if 0 < i < 4:
                put(im, x + (0 if not flip else 0), y, col)
                put(im, x + 1, y, "d0c060" if i < 3 else "8a7a30")
        # awn / point
        tipx = cx + (2 if not flip else 0)
        put(im, tipx, cy - 1, "c8b858")
        # dark hull line
        put(im, cx + (0 if not flip else 2), cy + 2, "8a7a30")
    return im

wp(TEX_ITEM / "seeds/item_seed_soy.png", draw_soy_seeds())
wp(TEX_ITEM / "seeds/item_seed_chickpea.png", draw_chickpea_seeds())
wp(TEX_ITEM / "seeds/item_seed_oats.png", draw_oat_seeds())

# ---------- coconut item: cooler fibrous husk, not reddish ----------
def draw_coconut_item():
    im = blank()
    for y in range(3, 14):
        for x in range(3, 13):
            rx, ry = (x - 7.5) / 5.0, (y - 8.0) / 5.5
            r = rx * rx + ry * ry
            if r > 1:
                continue
            edge = r > 0.78
            # fibre streaks: slightly vertical, cooler brown / tan
            fibre = ((x * 3 + y) % 5 == 0) or ((x + y * 2) % 7 == 0)
            if edge:
                col = "3e2c14"
            elif fibre:
                col = "5a4020" if (x + y) % 2 == 0 else "6a4e28"
            elif x + y < 14:
                col = "c4a06a"  # highlight
            elif x + y > 19:
                col = "6a5030"
            else:
                col = "8a6a3c"
            put(im, x, y, col)
    # three eyes near the top
    for x, y in ((6, 5), (8, 5), (7, 7)):
        put(im, x, y, "2a1c0c")
    # a couple of longer husk fibres on the edge
    put(im, 4, 8, "4a3820")
    put(im, 11, 9, "4a3820")
    put(im, 5, 12, "4a3820")
    return im

wp(TEX_ITEM / "item_coconut.png", draw_coconut_item())

# ---------- pod textures (cocoa-style packed 16x16) ----------
# Layout: [0,0-8,8] top/bottom  [8,0-16,4] stem  [8,4-16,16] sides (8x12)
def fill_rect(im, x0, y0, x1, y1, color_fn):
    for y in range(y0, y1):
        for x in range(x0, x1):
            put(im, x, y, color_fn(x, y, x0, y0, x1, y1))

def jackfruit_tex(age):
    im = blank()
    if age == 0:
        body, light, dark, out, stem, stem_d = "7a9a3a", "9cbc52", "4e7020", "2e4a14", "4a6a28", "3a521c"
    elif age == 1:
        body, light, dark, out, stem, stem_d = "a8b844", "c8d060", "6a7a24", "3e4e14", "5a6a28", "3a4a18"
    else:
        body, light, dark, out, stem, stem_d = "c8c04a", "e0dc70", "8a8828", "5a5820", "6a5a24", "3a3a14"
    # top 8x8 warty
    def top(x, y, *a):
        if x in (0, 7) or y in (0, 7):
            return out
        if (x + y * 3) % 5 == 0:
            return dark
        if x + y < 6:
            return light
        return body
    fill_rect(im, 0, 0, 8, 8, top)
    # stem 8x4
    def st(x, y, *a):
        if y <= 1:
            return stem_d if x in (8, 15) else stem
        return stem_d if (x + y) % 3 == 0 else stem
    fill_rect(im, 8, 0, 16, 4, st)
    # sides 8x12
    def side(x, y, *a):
        lx, ly = x - 8, y - 4
        if lx in (0, 7) or ly in (0, 11):
            return out
        if (lx * 2 + ly) % 4 == 0:
            return dark  # studs
        if lx + ly < 6:
            return light
        if lx + ly > 14:
            return dark
        return body
    fill_rect(im, 8, 4, 16, 16, side)
    return im

def coconut_tex(age):
    im = blank()
    if age < 2:
        body, light, dark, out, stem, stem_d = "5a8a38", "7aaa50", "3e6a20", "2a4a14", "4a6a28", "2e4a14"
        fibre = False
    else:
        body, light, dark, out, stem, stem_d = "8a6a3c", "c4a06a", "5a4020", "3e2c14", "6a5a30", "3a2c14"
        fibre = True
    def top(x, y, *a):
        if x in (0, 7) or y in (0, 7):
            return out
        if fibre and (x % 2 == 0):
            return dark
        return light if x + y < 6 else body
    fill_rect(im, 0, 0, 8, 8, top)
    def st(x, y, *a):
        return stem_d if x in (8, 15) or y == 0 else stem
    fill_rect(im, 8, 0, 16, 4, st)
    def side(x, y, *a):
        lx, ly = x - 8, y - 4
        if lx in (0, 7) or ly in (0, 11):
            return out
        if fibre and ((lx + ly * 2) % 4 == 0):
            return dark
        if lx + ly < 6:
            return light
        if lx + ly > 15:
            return dark
        return body
    fill_rect(im, 8, 4, 16, 16, side)
    # eyes on ripe coconut sides
    if age == 2:
        put(im, 10, 8, "2a1c0c")
        put(im, 12, 8, "2a1c0c")
        put(im, 11, 10, "2a1c0c")
    return im

def olive_tex(age):
    im = blank()
    if age == 0:
        body, light, dark, out, stem, stem_d = "6a8a38", "8aaa50", "3e5e1c", "2a3e10", "4a6a28", "2e4a14"
    elif age == 1:
        # blushing toward purple
        body, light, dark, out, stem, stem_d = "4a4a30", "6a5a40", "2e2030", "1c141c", "4a6a28", "2e4a14"
    else:
        # ripe: dark purple to black
        body, light, dark, out, stem, stem_d = "2a1a28", "4a2a3c", "140c18", "0a060c", "3a4a20", "243014"
    def top(x, y, *a):
        if x in (0, 7) or y in (0, 7):
            return out
        return light if x + y < 7 else body
    fill_rect(im, 0, 0, 8, 8, top)
    fill_rect(im, 8, 0, 16, 4, lambda x, y, *a: "2e4a14" if x in (8, 15) or y == 0 else "4a6a28")
    def side(x, y, *a):
        lx, ly = x - 8, y - 4
        if lx in (0, 7) or ly in (0, 11):
            return out
        if lx + ly < 6:
            return light
        if lx + ly > 15:
            return dark
        return body
    fill_rect(im, 8, 4, 16, 16, side)
    # highlight glint
    put(im, 10, 7, light if age else "9cbc62")
    return im

def avocado_tex(age):
    im = blank()
    if age == 0:
        body, light, dark, out = "5a8a38", "7aaa50", "3e6a20", "2a4a14"
    elif age == 1:
        body, light, dark, out = "2e5a20", "3e7228", "1e3e14", "14280c"
    else:
        # ripe: dark green to near black, still a little highlight so the shape reads
        body, light, dark, out = "14240e", "1e3a16", "0a1408", "060a04"
    def top(x, y, *a):
        if x in (0, 7) or y in (0, 7):
            return out
        # bumpy
        if (x + y * 2) % 5 == 0:
            return dark
        return light if x + y < 6 else body
    fill_rect(im, 0, 0, 8, 8, top)
    fill_rect(im, 8, 0, 16, 4, lambda x, y, *a: "2e4a14" if x in (8, 15) or y == 0 else "4a6a28")
    def side(x, y, *a):
        lx, ly = x - 8, y - 4
        if lx in (0, 7) or ly in (0, 11):
            return out
        if (lx * 3 + ly) % 5 == 0:
            return dark
        if lx + ly < 6:
            return light
        if lx + ly > 15:
            return dark
        return body
    fill_rect(im, 8, 4, 16, 16, side)
    return im

TEX_FN = {
    "jackfruit": jackfruit_tex,
    "coconut": coconut_tex,
    "olive": olive_tex,
    "avocado": avocado_tex,
}
for name, fn in TEX_FN.items():
    for age in range(3):
        wp(TEX_BLOCK / f"fruit_{name}_stage{age}.png", fn(age))

# ---------- models ----------
def face(uv, tex="#fruit"):
    return {"uv": uv, "texture": tex}

def box(frm, to, faces):
    return {"from": frm, "to": to, "faces": faces}

def pod_faces():
    # sides use the side strip, ends use the top square
    return {
        "up": face([0, 0, 8, 8]),
        "down": face([0, 0, 8, 8]),
        "north": face([8, 4, 16, 16]),
        "south": face([8, 4, 16, 16]),
        "west": face([8, 4, 16, 16]),
        "east": face([8, 4, 16, 16]),
    }

def stem_faces():
    return {
        "up": face([8, 0, 16, 4]),
        "down": face([8, 0, 16, 4]),
        "north": face([8, 0, 16, 4]),
        "south": face([8, 0, 16, 4]),
        "west": face([8, 0, 16, 4]),
        "east": face([8, 0, 16, 4]),
    }

def model(name, age, elements):
    tex = f"morefood:block/fruit_{name}_stage{age}"
    return {
        "ambientocclusion": False,
        "render_type": "minecraft:cutout",
        "textures": {"particle": tex, "fruit": tex},
        "elements": elements,
    }

# hanging (from the ceiling of the block)
HANG = {
    "jackfruit": {
        0: [
            box([7, 13, 7], [9, 16, 9], stem_faces()),
            box([5, 9, 5], [11, 14, 11], pod_faces()),
        ],
        1: [
            box([7, 13, 7], [9, 16, 9], stem_faces()),
            box([4, 5, 4], [12, 14, 12], pod_faces()),
        ],
        2: [
            box([7, 13, 7], [9, 16, 9], stem_faces()),
            box([3, 1, 3], [13, 14, 13], pod_faces()),
        ],
    },
    "coconut": {
        0: [
            box([7, 14, 7], [9, 16, 9], stem_faces()),
            box([6, 11, 6], [10, 15, 10], pod_faces()),
        ],
        1: [
            box([6, 14, 6], [8, 16, 8], stem_faces()),
            box([9, 14, 8], [11, 16, 10], stem_faces()),
            box([4, 9, 5], [9, 14, 10], pod_faces()),
            box([8, 10, 6], [13, 15, 11], pod_faces()),
        ],
        2: [
            box([6, 14, 5], [8, 16, 7], stem_faces()),
            box([9, 14, 7], [11, 16, 9], stem_faces()),
            box([7, 14, 10], [9, 16, 12], stem_faces()),
            box([4, 7, 4], [9, 12, 9], pod_faces()),
            box([8, 6, 6], [13, 11, 11], pod_faces()),
            box([6, 9, 8], [11, 14, 13], pod_faces()),
        ],
    },
    "olive": {
        0: [
            box([7, 14, 7], [9, 16, 9], stem_faces()),
            box([6, 11, 6], [10, 15, 10], pod_faces()),
        ],
        1: [
            box([7, 14, 7], [9, 16, 9], stem_faces()),
            box([6, 9, 6], [10, 15, 10], pod_faces()),
        ],
        2: [
            box([6, 14, 6], [8, 16, 8], stem_faces()),
            box([9, 14, 8], [11, 16, 10], stem_faces()),
            box([5, 9, 5], [9, 14, 9], pod_faces()),
            box([8, 8, 8], [12, 13, 12], pod_faces()),
        ],
    },
    "avocado": {
        0: [
            box([7, 14, 7], [9, 16, 9], stem_faces()),
            box([6, 11, 6], [10, 15, 10], pod_faces()),
        ],
        1: [
            box([7, 14, 7], [9, 16, 9], stem_faces()),
            box([6, 11, 6], [10, 14, 10], pod_faces()),  # narrower neck
            box([5, 7, 5], [11, 12, 11], pod_faces()),
        ],
        2: [
            box([7, 14, 7], [9, 16, 9], stem_faces()),
            box([6, 11, 6], [10, 15, 10], pod_faces()),
            box([5, 5, 5], [11, 12, 11], pod_faces()),
        ],
    },
}

# side-attached (south: pod against z=16, toward the leaf)
SIDE = {
    "jackfruit": {
        0: [
            box([7, 13, 12], [9, 16, 16], stem_faces()),
            box([5, 8, 10], [11, 14, 16], pod_faces()),
        ],
        1: [
            box([7, 13, 12], [9, 16, 16], stem_faces()),
            box([4, 5, 8], [12, 14, 16], pod_faces()),
        ],
        2: [
            box([7, 13, 12], [9, 16, 16], stem_faces()),
            box([3, 2, 6], [13, 14, 16], pod_faces()),
        ],
    },
    "coconut": {
        0: [
            box([7, 13, 13], [9, 16, 16], stem_faces()),
            box([6, 9, 12], [10, 14, 16], pod_faces()),
        ],
        1: [
            box([5, 13, 13], [7, 16, 16], stem_faces()),
            box([9, 13, 13], [11, 16, 16], stem_faces()),
            box([4, 8, 11], [9, 14, 16], pod_faces()),
            box([8, 7, 10], [13, 13, 16], pod_faces()),
        ],
        2: [
            box([5, 13, 13], [7, 16, 16], stem_faces()),
            box([9, 13, 13], [11, 16, 16], stem_faces()),
            box([7, 13, 12], [9, 16, 15], stem_faces()),
            box([3, 6, 9], [8, 12, 15], pod_faces()),
            box([8, 5, 8], [13, 11, 14], pod_faces()),
            box([6, 8, 10], [11, 14, 16], pod_faces()),
        ],
    },
    "olive": {
        0: [
            box([7, 13, 13], [9, 16, 16], stem_faces()),
            box([6, 9, 12], [10, 13, 16], pod_faces()),
        ],
        1: [
            box([7, 13, 13], [9, 16, 16], stem_faces()),
            box([6, 8, 11], [10, 13, 16], pod_faces()),
        ],
        2: [
            box([5, 13, 13], [7, 16, 16], stem_faces()),
            box([9, 13, 13], [11, 16, 16], stem_faces()),
            box([5, 7, 11], [9, 12, 16], pod_faces()),
            box([8, 6, 10], [12, 11, 16], pod_faces()),
        ],
    },
    "avocado": {
        0: [
            box([7, 13, 13], [9, 16, 16], stem_faces()),
            box([6, 9, 12], [10, 13, 16], pod_faces()),
        ],
        1: [
            box([7, 13, 13], [9, 16, 16], stem_faces()),
            box([6, 9, 12], [10, 13, 15], pod_faces()),
            box([5, 6, 10], [11, 11, 16], pod_faces()),
        ],
        2: [
            box([7, 13, 13], [9, 16, 16], stem_faces()),
            box([6, 9, 12], [10, 14, 16], pod_faces()),
            box([5, 4, 10], [11, 11, 16], pod_faces()),
        ],
    },
}

MODELS_DIR = A / "morefood/models/block"
STATES_DIR = A / "morefood/blockstates"
for name in TEX_FN:
    for age in range(3):
        w(MODELS_DIR / f"fruit_{name}_hang_stage{age}.json", model(name, age, HANG[name][age]))
        w(MODELS_DIR / f"fruit_{name}_side_stage{age}.json", model(name, age, SIDE[name][age]))
    variants = {}
    for age in range(3):
        hang = f"morefood:block/fruit_{name}_hang_stage{age}"
        side = f"morefood:block/fruit_{name}_side_stage{age}"
        variants[f"age={age},facing=up"] = {"model": hang}
        variants[f"age={age},facing=down"] = {"model": hang, "x": 180}
        variants[f"age={age},facing=south"] = {"model": side}
        variants[f"age={age},facing=north"] = {"model": side, "y": 180}
        variants[f"age={age},facing=west"] = {"model": side, "y": 270}
        variants[f"age={age},facing=east"] = {"model": side, "y": 90}
    w(STATES_DIR / f"block_fruit_{name}.json", {"variants": variants})

# ---------- plain leaves (drop fruiting state) ----------
for name in TEX_FN:
    w(A / "morefood/models/block" / f"leaves_{name}.json", {
        "parent": "minecraft:block/leaves",
        "render_type": "minecraft:cutout_mipped",
        "textures": {"all": f"morefood:block/leaves_{name}"},
    })
    w(STATES_DIR / f"block_leaves_{name}.json", {
        "variants": {"": {"model": f"morefood:block/leaves_{name}"}}
    })
    for extra in (
        A / "morefood/models/block" / f"leaves_{name}_fruit.json",
        TEX_BLOCK / f"leaves_{name}_fruit.png",
    ):
        if extra.exists():
            extra.unlink()
            print("removed", extra)

# ---------- loot: cocoa-like (1 if young, 2 if mature) + strip fruit from leaves ----------
def pod_loot(name, count=2):
    return {
        "type": "minecraft:block",
        "pools": [
            {
                "bonus_rolls": 0.0,
                "entries": [
                    {
                        "type": "minecraft:item",
                        "functions": [
                            {
                                "add": False,
                                "conditions": [
                                    {
                                        "block": f"morefood:block_fruit_{name}",
                                        "condition": "minecraft:block_state_property",
                                        "properties": {"age": "2"},
                                    }
                                ],
                                "count": float(count),
                                "function": "minecraft:set_count",
                            },
                            {"function": "minecraft:explosion_decay"},
                        ],
                        "name": f"morefood:item_{name}",
                    }
                ],
                "rolls": 1.0,
            }
        ],
        "random_sequence": f"morefood:blocks/block_fruit_{name}",
    }

for name in TEX_FN:
    w(D26 / "morefood/loot_table/blocks" / f"block_fruit_{name}.json", pod_loot(name, 3 if name == "olive" else 2))
    leaf = D26 / "morefood/loot_table/blocks" / f"block_leaves_{name}.json"
    data = json.loads(leaf.read_text())
    # drop the fruit=true pool
    data["pools"] = [
        p for p in data["pools"]
        if not any(
            (c.get("condition") == "minecraft:block_state_property" and c.get("properties", {}).get("fruit"))
            for c in p.get("conditions", [])
        )
    ]
    w(leaf, data)

# ---------- lang ----------
lang_path = A / "morefood/lang/en_us.json"
lang = json.loads(lang_path.read_text())
for name, label in (("jackfruit", "Jackfruit"), ("coconut", "Coconut"), ("olive", "Olives"), ("avocado", "Avocado")):
    lang[f"block.morefood.block_fruit_{name}"] = label
w(lang_path, lang)

print("assets written")
