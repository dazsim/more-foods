"""Generates the 3D crop models, blockstates and textures. Run from the repo root: python3 tools/cropmodels.py"""
import json, math, os, random
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

def rings(top, base, light, dark, crown=0):
    t, b, l, d = hexc(top), hexc(base), hexc(light), hexc(dark)
    def f(x, y, r):
        if y < crown or (y == crown and r.random() < 0.5):
            return t
        if y % 4 == 3 and r.random() < 0.7:
            return d
        return l if r.random() < 0.15 else b
    return f

def crown(outer, inner, centre):
    o, i, c = hexc(outer), hexc(inner), hexc(centre)
    def f(x, y, r):
        d = max(abs(x - 7.5), abs(y - 7.5))
        if d < 2:
            return c
        return i if d < 4.5 and r.random() < 0.8 else o
    return f

def speckle(base, light, dark, amount=0.3):
    b, l, d = hexc(base), hexc(light), hexc(dark)
    def f(x, y, r):
        n = r.random()
        return l if n < amount / 2 else d if n < amount else b
    return f

def feather(base, light, dark):
    b, l, d = hexc(base), hexc(light), hexc(dark)
    def f(x, y, r):
        if x % 4 == 1:
            return d
        if (x + y) % 3 == 0:
            return l
        return d if r.random() < 0.15 else b
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
tex("cabbage_leaf", leafy("6f9f74", "8dbb8c", "507e58", "b4d8ac"))
tex("cabbage_heart", leafy("c4e2a2", "dff0c4", "a6c888"))
tex("spinach_leaf", leafy("2f7a2a", "4a983a", "205a1c", "64ae4c"))
tex("spinach_heart", leafy("47963a", "62b04c", "34782a"))
tex("kale_leaf", leafy("3a6338", "557f4c", "263f25", "6c9660"))
tex("garlic_blade", stripes("7f9a6a", "9ab484", "62804e", 5))
tex("garlic_bulb", stripes("e4e8d6", "f4f6ec", "c49286", 3))
tex("leek_blade", stripes("4f8257", "6a9e6a", "3a6640", 4))
tex("leek_shaft", stripes("e6efd4", "f8fcf0", "c8dcb0", 3))
tex("pea_leaf", leafy("6fa118", "89a746", "518313", "96b255"))
tex("pea_pod", fruit("8ec83c", "b4e070", "5e9a20"))
tex("pea_flower", lambda x, y, r: hexc("f4f4ee") if (x + y) % 3 else hexc("d8d8d0"))
tex("bean_leaf", leafy("588918", "74a71e", "4a7a12", "83ae28"))
tex("bean_pod", stripes("7cb83a", "a0d45c", "5a8a24", 5))
tex("bean_pod_dry", stripes("d8c890", "ece0b0", "b0a070", 5))
tex("parsnip_stalk", stripes("8ab05a", "a6c878", "6a8e40", 4))
tex("parsnip_leaf", feather("6fa03a", "9cc864", "4e7a26"))
tex("parsnip_root", rings("e2d6a0", "eee4bc", "f8f0d4", "cfc088"))
tex("parsnip_root_top", crown("e8dcae", "d6c890", "8a9a4a"))
tex("turnip_stalk", stripes("6fa118", "89b33a", "518313", 4))
tex("turnip_leaf", leafy("5f9a2a", "7fb544", "467a1a", "9cc860"))
tex("turnip_root", rings("b14b7f", "eee8d0", "faf6e8", "d6ceb0", 4))
tex("turnip_root_top", crown("b14b7f", "d98db4", "6a7a2a"))
tex("swede_stalk", stripes("5f8a6a", "7aa484", "466e50", 4))
tex("swede_leaf", leafy("5a8a78", "78a894", "406a5a", "9cc4b0"))
tex("swede_root", rings("b92d74", "d7b35a", "e8c878", "a8843a", 5))
tex("swede_root_top", crown("b92d74", "d76fa3", "6a6a3a"))

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

def corners(e):
    f, t = e["from"], e["to"]
    pts = [[x, y, z] for x in (f[0], t[0]) for y in (f[1], t[1]) for z in (f[2], t[2])]
    r = e.get("rotation")
    if not r:
        return pts
    a = math.radians(r["angle"])
    c, s = math.cos(a), math.sin(a)
    o = r["origin"]
    out = []
    for p in pts:
        x, y, z = (p[i] - o[i] for i in range(3))
        if r["axis"] == "x":
            y, z = y * c - z * s, y * s + z * c
        elif r["axis"] == "y":
            z, x = z * c - x * s, z * s + x * c
        else:
            x, y = x * c - y * s, x * s + y * c
        out.append([x + o[0], y + o[1], z + o[2]])
    return out

def parent(name, elements, particle="leaf"):
    for e in elements:
        for p in corners(e):
            assert -1e-6 <= p[0] <= 16 + 1e-6 and -1e-6 <= p[2] <= 16 + 1e-6, (name, e["from"], e["to"])
            assert G - 1 <= p[1] <= 16 + 1e-6, (name, e["from"], e["to"])
    w(os.path.join(A, "morefood/models/block/crop_shape", name + ".json"),
      {"ambientocclusion": False, "textures": {"particle": "#" + particle}, "elements": elements})

G = -1  # crops sit one pixel into farmland, like vanilla block/crop

# low leafy rosette: #heart, #leaf
def rosette(name, scale=1, tilt=22.5, grow=0, heart=True, cap=True):
    k = (lambda v: v) if scale == 1 else (lambda v: max(0.5, round(v * scale * 2) / 2))
    for s in range(8):
        hw = k([1, 2, 2, 3, 4, 4, 5, 6][s])
        hh = k([1, 1, 2, 2, 3, 4, 4, 5][s])
        L = k([1, 2, 3, 3, 4, 5, 5, 6][s]) + (grow if s >= 2 else 0)
        W = k([1, 2, 2, 3, 3, 4, 5, 5][s])
        el = [cbox(8, G, 8, hw, hh, hw, "heart")] if heart else []
        a = tilt
        h2 = hw / 2 if heart else 0.5
        # outer leaves stop short of the block edge so they never poke into neighbours
        L = min(L, math.floor((7.9 - h2) / math.cos(math.radians(a)) * 10) / 10)
        el.append(box([8 - W / 2, G, 8 - h2 - L], [8 + W / 2, G + 1, 8 - h2], "leaf", {"origin": [8, G, 8 - h2], "axis": "x", "angle": a}))
        el.append(box([8 - W / 2, G, 8 + h2], [8 + W / 2, G + 1, 8 + h2 + L], "leaf", {"origin": [8, G, 8 + h2], "axis": "x", "angle": -a}))
        el.append(box([8 - h2 - L, G, 8 - W / 2], [8 - h2, G + 1, 8 + W / 2], "leaf", {"origin": [8 - h2, G, 8], "axis": "z", "angle": -a}))
        el.append(box([8 + h2, G, 8 - W / 2], [8 + h2 + L, G + 1, 8 + W / 2], "leaf", {"origin": [8 + h2, G, 8], "axis": "z", "angle": a}))
        if s >= 5:
            d = L + h2 - 1
            S = max(0.5, W - 1)
            r2 = math.sqrt(2)
            # corner leaves run along the diagonal, outer tip unchanged, inner end tucked under the head
            r_out = min(d * 0.7 * r2 + S / 2, 8 * r2 - S / 2)
            r_in = max(0, h2 * r2 - S / 2 - 0.5)
            rc, ln = (r_out + r_in) / 2, round(r_out - r_in, 2)
            for sx, sz in ((-1, -1), (1, -1), (-1, 1), (1, 1)):
                cx, cz = round(8 + sx * rc / r2, 2), round(8 + sz * rc / r2, 2)
                lx, lz = (S, ln) if sx == sz else (ln, S)
                el.append(cbox(cx, G, cz, lx, 1, lz, "leaf", {"origin": [cx, G, cz], "axis": "y", "angle": 45}))
        if heart and cap and s >= 6:
            el.append(cbox(8, G + hh, 8, hw - 2, 1, hw - 2, "heart"))
        if not heart and s >= 1:
            # young leaves standing up in the middle
            c = [0, 1, 1, 1, 1.5, 1.5, 2, 2][s]
            el.append(cbox(8, G, 8, c, c * 2 + 1, c, "leaf"))
        parent(f"{name}_s{s}", el)

rosette("leafy")
rosette("leafy_small", scale=0.7, cap=False)
rosette("leafy_tall", tilt=50, grow=2, heart=False)

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

# leek: white shaft with flat blades fanned along x: #blade, #shaft
for s in range(8):
    top = G + [0, 0, 2, 3, 4, 5, 6, 7][s]
    el = []
    if s >= 2:
        el.append(cbox(8, G, 8, 2, top - G, 2, "shaft"))
    n = [2, 2, 3, 3, 4, 4, 5, 5][s]
    L = [2, 3, 4, 5, 6, 7, 8, 9][s]
    for i, a in enumerate([0, -20, 20, -38, 38][:n]):
        h = L - (1 if i >= 3 else 0)
        rot = {"origin": [8, top, 8], "axis": "z", "angle": a} if a else None
        el.append(box([7.25, top, 7.75], [8.75, top + h, 8.25], "blade", rot))
    parent(f"tuft_leek_s{s}", el, "blade")

# trellis: two posts and three rails, leaves climbing between them: #stake, #leaf
TRELLIS = {}
for s in range(8):
    h = [1, 3, 4, 6, 8, 10, 12, 13][s]
    el = [box([2, G, 7.5], [3, 15, 8.5], "stake"), box([13, G, 7.5], [14, 15, 8.5], "stake")]
    for y in (3, 8, 13):
        el.append(box([3, y, 7.5], [13, y + 1, 8.5], "stake"))
    leaves = []
    xs = [5, 9, 11, 7]
    y, i = G, 0
    while y <= G + h - 2:
        x = xs[i % 4]
        side = 1 if i % 2 else -1
        leaves.append((x, y, 8 + side * 1.5, 4, 3, 2))
        if s >= 5:
            leaves.append((x + 2, y + 1, 8 - side * 1.5, 3, 3, 2))
        y += 2
        i += 1
    if s == 0:
        leaves = [(6, G, 8, 2, 1, 2), (10, G, 8, 2, 1, 2)]
    TRELLIS[s] = leaves
    for lx, ly, lz, sx, sy, sz in leaves:
        el.append(cbox(lx, ly, lz, sx, sy, sz, "leaf"))
    parent(f"trellis_s{s}", el, "stake")

# flowers then pods hanging off the trellis leaves: #fruit
for s in range(4, 8):
    fl = [1, 3, 4, 5][s - 4]
    el = []
    for j, (lx, ly, lz, sx, sy, sz) in enumerate(TRELLIS[s]):
        if j % 2 or ly < G + 2:
            continue
        fz = lz + (2 if lz > 8 else -2)
        t = 2 if s > 4 else 1
        el.append(cbox(lx, max(G, ly - fl + 1), fz, t, fl, t, "fruit"))
    parent(f"trellis_fruit_s{s}", el, "fruit")

# bush: stem and leaf clusters: #stem, #leaf
BUSH = {}
for s in range(8):
    h = [1, 2, 3, 4, 5, 6, 7, 7][s]
    el = [cbox(8, G, 8, 2, h, 2, "stem")]
    if s <= 1:
        leaves = [(8, G + h, 8, 3, 1, 3)]
    else:
        c = [0, 0, 4, 5, 5, 6, 7, 8][s]
        leaves = [(8, G + h, 8, c, max(2, c - 2), c)]
        if s >= 3:
            o = [0, 0, 0, 3, 3, 4, 5, 5][s]
            sc = [0, 0, 0, 3, 3, 4, 4, 4][s]
            y = G + max(1, h - 2)
            for dx, dz in ((-o, 0), (o, 0), (0, -o), (0, o)):
                leaves.append((8 + dx, y, 8 + dz, sc, sc - 1, sc))
    BUSH[s] = leaves
    for lx, ly, lz, sx, sy, sz in leaves:
        el.append(cbox(lx, ly, lz, sx, sy, sz, "leaf"))
    parent(f"bush_s{s}", el)

# pods hanging under the outer leaf clusters: #fruit, #fruit_top
for s in range(4, 8):
    fl = [1, 2, 2, 3][s - 4]
    el = []
    for lx, ly, lz, sx, sy, sz in BUSH[s][1:]:
        dx = 1 if lx > 8 else -1 if lx < 8 else 0
        dz = 1 if lz > 8 else -1 if lz < 8 else 0
        fx, fz = lx + dx * (sx / 2 + 0.5), lz + dz * (sz / 2 + 0.5)
        y = max(G, ly - fl)
        for off in ((-1, 1) if s >= 6 else (0,)):
            ox, oz = (off, 0) if dx == 0 else (0, off)
            el.append(cbox(fx + ox, y, fz + oz, 1, fl if s > 4 else 1, 1, "fruit"))
            if s >= 6:
                el.append(cbox(fx + ox, y + fl, fz + oz, 1, 1, 1, "fruit_top"))
    parent(f"bush_fruit_s{s}", el, "fruit")

# root crop leaves: stalks leaning out with a leaf on top: #stalk, #leaf
def root_top(name, tall=1.0, leaf=1.0):
    spots = [(8, 8, None), (8, 7, ("x", -22.5)), (8, 9, ("x", 22.5)), (7, 8, ("z", 22.5)), (9, 8, ("z", -22.5))]
    for s in range(8):
        n = [2, 2, 3, 3, 4, 4, 5, 5][s]
        H = round([1, 2, 3, 4, 5, 6, 7, 8][s] * tall * 2) / 2
        lw = round([1, 1.5, 2, 2, 2.5, 3, 3, 3.5][s] * leaf * 2) / 2
        lh = round([1, 1, 1.5, 2, 2, 2.5, 2.5, 3][s] * tall * 2) / 2
        el = []
        for i, (x, z, r) in enumerate(spots[:n]):
            h = H - (i % 2)
            rot = {"origin": [x, G, z], "axis": r[0], "angle": r[1]} if r else None
            el.append(box([x - 0.5, G, z - 0.5], [x + 0.5, G + h, z + 0.5], "stalk", rot))
            el.append(cbox(x, G + h - lh / 2, z, lw, lh, lw, "leaf", rot))
        parent(f"{name}_s{s}", el)

root_top("root_top")
root_top("root_top_tall", tall=1.3, leaf=0.7)
root_top("root_top_wide", tall=0.7, leaf=1.3)

# root shoulder showing through the soil: #root, #root_top
ROOTS = {"small": ([2, 2, 2.5, 3, 3], [1, 1, 1.5, 2, 2]),
         "medium": ([3, 3.5, 4, 4.5, 5], [1, 1.5, 2, 2.5, 3]),
         "large": ([3, 4, 4.5, 5, 6], [1, 2, 2.5, 3, 4])}
for size, (ws, ups) in ROOTS.items():
    for s in range(3, 8):
        wd, up = ws[s - 3], ups[s - 3]
        e = cbox(8, G, 8, wd, up - G, wd, "root")
        x0 = (16 - wd) / 2
        for k in ("north", "south", "east", "west"):
            e["faces"][k]["uv"] = [x0, 0, x0 + wd, up - G]
        e["faces"]["up"]["texture"] = "#root_top"
        parent(f"root_{size}_s{s}", [e], "root")

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
crop("cabbage", "morefood", "block_crop_cabbage", "leafy", {"heart": "cabbage_heart", "leaf": "cabbage_leaf"})
crop("spinach", "morefood", "block_crop_spinach", "leafy_small", {"heart": "spinach_heart", "leaf": "spinach_leaf"})
crop("kale", "morefood", "block_crop_kale", "leafy_tall", {"leaf": "kale_leaf"})
crop("garlic", "morefood", "block_crop_garlic", "tuft", {"blade": "garlic_blade"},
     lambda s: (f"tuft_bulb_s{s}", {"bulb": "garlic_bulb"}) if s >= 3 else None, "bulb")
crop("leek", "morefood", "block_crop_leek", "tuft_leek", {"blade": "leek_blade", "shaft": "leek_shaft"})
crop("peas", "morefood", "block_crop_peas", "trellis", {"stake": "stake", "leaf": "pea_leaf"},
     lambda s: (f"trellis_fruit_s{s}", {"fruit": "pea_flower" if s == 4 else "pea_pod"}) if s >= 4 else None)
crop("haricot", "morefood", "block_crop_haricot", "bush", {"stem": "stem", "leaf": "bean_leaf"},
     lambda s: (f"bush_fruit_s{s}", {"fruit": "bean_pod_dry" if s == 7 else "bean_pod", "fruit_top": "stem"}) if s >= 4 else None)

def root(size, name):
    return lambda s: (f"root_{size}_s{s}", {"root": name + "_root", "root_top": name + "_root_top"}) if s >= 3 else None

crop("parsnip", "morefood", "block_crop_parsnip", "root_top_tall", {"stalk": "parsnip_stalk", "leaf": "parsnip_leaf"}, root("small", "parsnip"), "root")
crop("turnip", "morefood", "block_crop_turnip", "root_top_wide", {"stalk": "turnip_stalk", "leaf": "turnip_leaf"}, root("medium", "turnip"), "root")
crop("swede", "morefood", "block_crop_swede", "root_top", {"stalk": "swede_stalk", "leaf": "swede_leaf"}, root("large", "swede"), "root")
