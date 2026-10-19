"""Generates the 3D crop models, blockstates and textures. Run from the repo root: python3 tools/cropmodels.py"""
import json, os, random
from PIL import Image

A = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "common", "src", "main", "resources", "assets")

def w(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", newline="\n") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")

def tex(name, fn):
    im = Image.new("RGBA", (16, 16))
    px = im.load()
    rnd = random.Random(name)
    for y in range(16):
        for x in range(16):
            px[x, y] = fn(x, y, rnd)
    p = os.path.join(A, "morefood/textures/block/crop", name + ".png")
    os.makedirs(os.path.dirname(p), exist_ok=True)
    im.save(p)

def hexc(h):
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4)) + (255,)

def leafy(base, light, dark, vein=None):
    b, l, d = hexc(base), hexc(light), hexc(dark)
    v = hexc(vein) if vein else None
    def f(x, y, r):
        if v and (x == 7 or (y - x) % 6 == 0 and x < 7 or (x + y) % 6 == 2 and x > 7):
            return v
        n = r.random()
        return l if n < 0.22 else d if n < 0.40 else b
    return f

def stripes(base, light, dark, period=3):
    b, l, d = hexc(base), hexc(light), hexc(dark)
    def f(x, y, r):
        if x % period == 0:
            return d
        return l if r.random() < 0.15 else b
    return f

def fruit(base, light, dark):
    b, l, d = hexc(base), hexc(light), hexc(dark)
    def f(x, y, r):
        if x + y <= 13:
            return l if (x + y) % 5 == 0 and r.random() < 0.5 else b
        return d if (x + y) > 17 else b
    return f

def wood(base, light, dark):
    b, l, d = hexc(base), hexc(light), hexc(dark)
    def f(x, y, r):
        if x % 4 == 1:
            return d
        return l if r.random() < 0.18 else b
    return f

tex("lettuce_leaf", leafy("7ccf3e", "a6e65a", "5aa82c", "c8f08a"))
tex("lettuce_heart", leafy("b8e46a", "d8f5a0", "98c84c"))
tex("tomato_leaf", leafy("3f8f2a", "5cae38", "2c6a1e"))
tex("stem", stripes("4f8a2a", "6aa63a", "3a6a1e", 4))
tex("onion_blade", stripes("5e9e5a", "7cbc72", "467e44", 5))
tex("onion_bulb", stripes("d9b26a", "f0d494", "a67c3c", 3))
tex("stake", wood("9a7a4a", "b8955e", "6e5530"))
tex("tomato_red", fruit("d8281e", "ff6a50", "a01410"))
tex("tomato_green", fruit("8cbf3a", "b4dc64", "62922a"))
tex("flower", lambda x, y, r: hexc("f4d23a") if (x + y) % 3 else hexc("d0a020"))

def face_uv(a, b):
    w_, h_ = min(a, 16), min(b, 16)
    x0, y0 = (16 - w_) // 2, (16 - h_) // 2
    return [x0, y0, x0 + w_, y0 + h_]

def box(f, t, txt, rot=None):
    dx, dy, dz = (t[i] - f[i] for i in range(3))
    faces = {
        "north": face_uv(dx, dy), "south": face_uv(dx, dy),
        "east": face_uv(dz, dy), "west": face_uv(dz, dy),
        "up": face_uv(dx, dz), "down": face_uv(dx, dz),
    }
    e = {"from": f, "to": t, "faces": {k: {"uv": v, "texture": "#" + txt} for k, v in faces.items()}}
    if rot:
        e["rotation"] = rot
    return e

def cbox(cx, y0, cz, sx, sy, sz, txt, rot=None):
    return box([cx - sx / 2, y0, cz - sz / 2], [cx + sx / 2, y0 + sy, cz + sz / 2], txt, rot)

def parent(name, elements, particle="leaf"):
    w(os.path.join(A, "morefood/models/block/crop_shape", name + ".json"),
      {"ambientocclusion": False, "textures": {"particle": "#" + particle}, "elements": elements})

G = -1  # crops sit one pixel into farmland, like vanilla block/crop

# low leafy rosette: #heart, #leaf
for s in range(8):
    hw = [1, 2, 2, 3, 4, 4, 5, 6][s]
    hh = [1, 1, 2, 2, 3, 4, 4, 5][s]
    L = [1, 2, 3, 3, 4, 5, 5, 6][s]
    W = [1, 2, 2, 3, 3, 4, 5, 5][s]
    el = [cbox(8, G, 8, hw, hh, hw, "heart")]
    a = 22.5
    h2 = hw / 2
    el.append(box([8 - W / 2, G, 8 - h2 - L], [8 + W / 2, G + 1, 8 - h2], "leaf", {"origin": [8, G, 8 - h2], "axis": "x", "angle": a}))
    el.append(box([8 - W / 2, G, 8 + h2], [8 + W / 2, G + 1, 8 + h2 + L], "leaf", {"origin": [8, G, 8 + h2], "axis": "x", "angle": -a}))
    el.append(box([8 - h2 - L, G, 8 - W / 2], [8 - h2, G + 1, 8 + W / 2], "leaf", {"origin": [8 - h2, G, 8], "axis": "z", "angle": -a}))
    el.append(box([8 + h2, G, 8 - W / 2], [8 + h2 + L, G + 1, 8 + W / 2], "leaf", {"origin": [8 + h2, G, 8], "axis": "z", "angle": a}))
    if s >= 5:
        d = L + h2 - 1
        for sx, sz in ((-1, -1), (1, -1), (-1, 1), (1, 1)):
            el.append(cbox(8 + sx * d * 0.7, G, 8 + sz * d * 0.7, W - 1, 1, W - 1, "leaf",
                           {"origin": [8, G, 8], "axis": "y", "angle": 45}))
    if s >= 6:
        el.append(cbox(8, G + hh, 8, hw - 2, 1, hw - 2, "heart"))
    parent(f"leafy_s{s}", el)

# grassy tuft: #blade
for s in range(8):
    H = [2, 3, 4, 6, 7, 8, 9, 10][s]
    base = G + [0, 0, 0, 1, 2, 2, 3, 3][s]
    spots = [(8, 8, None), (7, 9, ("x", 22.5)), (9, 7, ("x", -22.5)), (7, 7, ("z", 22.5)), (9, 9, ("z", -22.5))]
    n = [2, 2, 3, 3, 4, 5, 5, 5][s]
    el = []
    for i, (x, z, r) in enumerate(spots[:n]):
        h = H - (i % 2) * 2
        rot = {"origin": [x, base, z], "axis": r[0], "angle": r[1]} if r else None
        el.append(box([x - 0.5, base, z - 0.5], [x + 0.5, base + h, z + 0.5], "blade", rot))
    parent(f"tuft_s{s}", el, "blade")

# bulb overlay for the tuft: #bulb
for s in range(3, 8):
    b = [0, 0, 0, 2, 3, 3, 4, 5][s]
    hb = [0, 0, 0, 2, 3, 3, 4, 4][s]
    el = [cbox(8, G, 8, b, hb, b, "bulb")]
    if b >= 4:
        el.append(cbox(8, G + hb, 8, b - 2, 1, b - 2, "bulb"))
    parent(f"tuft_bulb_s{s}", el, "bulb")

# staked vine: #stake, #leaf
VINE = {}
for s in range(8):
    h = [2, 3, 5, 7, 9, 11, 13, 15][s]
    el = [box([7, G, 7], [9, 15, 9], "stake")]
    leaves = [(8, G, 8, 3, 2, 3)] if s == 0 else []
    dirs = [(-2, 0), (0, -2), (2, 0), (0, 2)]
    y, i = G, 0
    while y + 3 <= G + h and s > 0:
        dx, dz = dirs[i % 4]
        sz = 4 if (s >= 5 and i < 3) else 3
        leaves.append((8 + dx, y, 8 + dz, sz, 3, sz))
        y += 2
        i += 1
    VINE[s] = leaves
    for lx, ly, lz, sx, sy, sz in leaves:
        el.append(cbox(lx, ly, lz, sx, sy, sz, "leaf"))
    parent(f"vine_s{s}", el, "stake")

# fruit on the vine: #fruit, #calyx
for s in range(4, 8):
    leaves = VINE[s]
    sz = [1, 2, 3, 3][s - 4]
    el = []
    for j, (lx, ly, lz, sx, sy, szz) in enumerate(leaves):
        if (s < 6 and j % 2) or ly < G + 1:
            continue
        dx = 1 if lx > 8 else -1 if lx < 8 else 0
        dz = 1 if lz > 8 else -1 if lz < 8 else 0
        push = sx / 2 + sz / 2 - 1
        fx = lx + dx * push if dx else lx + (1.5 if j % 4 < 2 else -1.5)
        fz = lz + dz * push if dz else lz + (1.5 if j % 4 >= 2 else -1.5)
        y = max(G, ly - sz + 1)
        el.append(cbox(fx, y, fz, sz, sz, sz, "fruit"))
        if s >= 5:
            el.append(cbox(fx, y + sz, fz, 1, 1, 1, "calyx"))
    parent(f"vine_fruit_s{s}", el, "fruit")

T = "morefood:block/crop/"

def child(crop, name, par, textures):
    w(os.path.join(A, "morefood/models/block/crop", f"{crop}_{name}.json"),
      {"parent": "morefood:block/crop_shape/" + par, "textures": {k: T + v for k, v in textures.items()}})
    return f"morefood:block/crop/{crop}_{name}"

def crop(crop, ns, block, shape, tx, extra=None, extra_name="fruit"):
    parts = []
    for s in range(8):
        m = child(crop, f"s{s}", f"{shape}_s{s}", tx)
        parts.append({"when": {"age": str(s)}, "apply": {"model": m}})
        if extra:
            r = extra(s)
            if r:
                par, etx = r
                parts.append({"when": {"age": str(s)}, "apply": {"model": child(crop, f"{extra_name}_s{s}", par, etx)}})
    w(os.path.join(A, ns, "blockstates", block + ".json"), {"multipart": parts})

def vine_fruit(ripe, unripe):
    def f(s):
        if s == 4:
            return "vine_fruit_s4", {"fruit": "flower"}
        if s >= 5:
            return f"vine_fruit_s{s}", {"fruit": unripe if s == 5 else ripe, "calyx": "stem"}
    return f

crop("tomato", "morefood", "block_crop_tomato", "vine", {"stake": "stake", "leaf": "tomato_leaf"},
     vine_fruit("tomato_red", "tomato_green"))
crop("lettuce", "morefood", "block_crop_lettuce", "leafy", {"heart": "lettuce_heart", "leaf": "lettuce_leaf"})
crop("onion", "morefood", "block_crop_onion", "tuft", {"blade": "onion_blade"},
     lambda s: (f"tuft_bulb_s{s}", {"bulb": "onion_bulb"}) if s >= 3 else None, "bulb")
