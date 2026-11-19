#!/usr/bin/env python3
"""Generate vegan crop assets. Run from the repo root: python3 tools/gen_vegan.py"""
import json, os, random, shutil
from PIL import Image

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
A = os.path.join(ROOT, "src/main/resources/assets")
ERA = os.path.join(ROOT, "src/eras/26.1")
OLD = "/workspace/mods/more-foods-jar/extracted/assets/morefood/textures"

def w(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", newline="\n") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")

def wp(path, im):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    im.save(path)

def blank():
    return Image.new("RGBA", (16, 16), (0, 0, 0, 0))

def hexc(h):
    h = h.lstrip("#")
    return tuple(int(h[i:i+2], 16) for i in (0, 2, 4)) + (255,)

def put(im, x, y, c):
    if 0 <= x < 16 and 0 <= y < 16:
        if isinstance(c, str):
            c = hexc(c)
        im.putpixel((x, y), c)

def fill(im, pixels, c):
    for x, y in pixels:
        put(im, x, y, c)

def tex(rel, fn, seed=""):
    im = blank()
    px = im.load()
    rnd = random.Random(seed or rel)
    for y in range(16):
        for x in range(16):
            c = fn(x, y, rnd)
            if c:
                px[x, y] = c if len(c) == 4 else c + (255,)
    wp(os.path.join(A, "morefood/textures", rel), im)
    return im

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

def fruit_tex(base, light, dark):
    b, l, d = hexc(base), hexc(light), hexc(dark)
    def f(x, y, r):
        if x + y <= 13:
            return l if (x + y) % 5 == 0 and r.random() < 0.5 else b
        return d if (x + y) > 17 else b
    return f

def wood(base, light, dark, top=False, topc=None):
    b, l, d = hexc(base), hexc(light), hexc(dark)
    t = hexc(topc) if topc else l
    def f(x, y, r):
        if top:
            dist = max(abs(x - 7.5), abs(y - 7.5))
            if dist < 2:
                return t
            if dist > 6.5:
                return d
            return l if (x + y) % 5 == 0 else b
        if y % 4 == 0:
            return d
        return l if r.random() < 0.18 else b
    return f

T = "morefood:block/crop/"

def child(crop, name, par, textures):
    path = os.path.join(A, "morefood/models/block/crop", f"{crop}_{name}.json")
    w(path, {"parent": "morefood:block/crop_shape/" + par, "render_type": "minecraft:cutout",
             "textures": {k: T + v for k, v in textures.items()}})
    return f"morefood:block/crop/{crop}_{name}"

def crop(crop, block, shape, tx, extra=None, extra_name="fruit", shape_of=None):
    parts = []
    for s in range(8):
        sh = shape_of(s) if shape_of else s
        m = child(crop, f"s{s}", f"{shape}_s{sh}", tx if not callable(tx) else tx(s))
        parts.append({"when": {"age": str(s)}, "apply": {"model": m}})
        if extra:
            r = extra(s)
            if r:
                par, etx = r
                parts.append({"when": {"age": str(s)}, "apply": {"model": child(crop, f"{extra_name}_s{s}", par, etx)}})
    w(os.path.join(A, "morefood/blockstates", block + ".json"), {"multipart": parts})

def item_model(name, texture):
    w(os.path.join(A, "morefood/models/item", name + ".json"),
      {"parent": "minecraft:item/generated", "textures": {"layer0": texture}})
    w(os.path.join(ERA, "assets/morefood/items", name + ".json"),
      {"model": {"type": "minecraft:model", "model": "morefood:item/" + name}})

def block_item_model(name, model):
    w(os.path.join(ERA, "assets/morefood/items", name + ".json"),
      {"model": {"type": "minecraft:model", "model": model}})

def blob(im, cx, cy, points, colors):
    """Draw a small highlighted blob. colors = (base, light, dark, outline?)"""
    base, light, dark = colors[:3]
    outline = colors[3] if len(colors) > 3 else dark
    for dx, dy, kind in points:
        c = light if kind == "l" else dark if kind == "d" else outline if kind == "o" else base
        put(im, cx + dx, cy + dy, c)

# ---------- reuse old textures ----------
def clean_beansprout_seed():
    src = Image.open(os.path.join(OLD, "items/seeds/item_seed_beansprouts.png")).convert("RGBA")
    # drop near-black, downscale to 16
    out = blank()
    for y in range(32):
        for x in range(32):
            r, g, b, a = src.getpixel((x, y))
            if a == 0 or r + g + b < 30:
                continue
            out.putpixel((x // 2, y // 2), (r, g, b, 255))
    return out

shutil.copyfile(os.path.join(OLD, "items/item_peanut.png"),
                os.path.join(A, "morefood/textures/item/item_peanuts.png"))
shutil.copyfile(os.path.join(OLD, "items/seeds/item_seed_peanut.png"),
                os.path.join(A, "morefood/textures/item/seeds/item_seed_peanut.png"))
shutil.copyfile(os.path.join(OLD, "items/seeds/item_seed_lentil.png"),
                os.path.join(A, "morefood/textures/item/seeds/item_seed_lentil.png"))
wp(os.path.join(A, "morefood/textures/item/seeds/item_seed_beansprouts.png"), clean_beansprout_seed())

# ---------- crop block textures ----------
tex("block/crop/soy_leaf.png", leafy("4a8a32", "6aac44", "32681e", "c4d878"), "soy_leaf")
tex("block/crop/soy_pod.png", fruit_tex("7a9a3a", "b0c85a", "4e7020"), "soy_pod")
tex("block/crop/lentil_leaf.png", leafy("5a9a3a", "7cba52", "3e7a24", "b8d86a"), "lentil_leaf")
tex("block/crop/lentil_pod.png", fruit_tex("6b8f2e", "9ab84a", "4a6818"), "lentil_pod")
tex("block/crop/chickpea_leaf.png", leafy("5c8c38", "7eae4a", "3e6c24", "c0d88a"), "chickpea_leaf")
tex("block/crop/chickpea_pod.png", fruit_tex("c8b46a", "e0d090", "8a7a3a"), "chickpea_pod")
tex("block/crop/peanut_leaf.png", leafy("4e8a30", "6eaa40", "32621c", "b4cc70"), "peanut_leaf")
tex("block/crop/peanut_pod.png", fruit_tex("c8a45a", "e0c078", "8a6e30"), "peanut_pod")
tex("block/crop/oat_blade.png", stripes("5a9a3c", "7cba52", "3e7228", 4), "oat_blade")
tex("block/crop/oat_blade_ripe.png", stripes("c4a64a", "e0c46a", "8a7a28", 4), "oat_blade_ripe")
tex("block/crop/sprout_blade.png", stripes("d8e070", "f0f4a0", "9aaa48", 5), "sprout_blade")

# ---------- crop models ----------
LOW = [0, 1, 2, 3, 3, 4, 4, 5]
crop("soy", "block_crop_soy", "bush", {"stem": "stem", "leaf": "soy_leaf"},
     lambda s: (f"bush_fruit_s{s}", {"fruit": "soy_pod", "fruit_top": "stem"}) if s >= 4 else None)
crop("lentil", "block_crop_lentil", "bush", {"stem": "stem", "leaf": "lentil_leaf"},
     lambda s: (f"bush_fruit_s{min(s,5)}", {"fruit": "lentil_pod", "fruit_top": "stem"}) if s >= 4 else None,
     shape_of=lambda s: LOW[s])
crop("chickpea", "block_crop_chickpea", "bush", {"stem": "stem", "leaf": "chickpea_leaf"},
     lambda s: (f"bush_fruit_s{s}", {"fruit": "chickpea_pod", "fruit_top": "stem"}) if s >= 4 else None)
crop("peanut", "block_crop_peanut", "bush", {"stem": "stem", "leaf": "peanut_leaf"},
     lambda s: (f"bush_fruit_s{min(s,5)}", {"fruit": "peanut_pod", "fruit_top": "stem"}) if s >= 4 else None,
     shape_of=lambda s: LOW[s])
crop("oats", "block_crop_oats", "tuft",
     lambda s: {"blade": "oat_blade_ripe" if s >= 5 else "oat_blade"})
crop("beansprouts", "block_crop_beansprouts", "tuft", {"blade": "sprout_blade"})

# ---------- item textures ----------
def seeds(name, colors):
    """Scattered small seed clusters, matching existing seed style."""
    im = blank()
    rnd = random.Random(name)
    # 4 clusters, similar to haricot/lentil
    spots = [(4, 4), (10, 3), (6, 9), (11, 10)]
    base, light, dark = colors
    for i, (cx, cy) in enumerate(spots):
        shape = [(0, 0, "b"), (1, 0, "l"), (0, 1, "d")]
        if rnd.random() < 0.5:
            shape.append((1, 1, "b"))
        blob(im, cx, cy, shape, (base, light, dark))
    wp(os.path.join(A, "morefood/textures/item/seeds", name + ".png"), im)

def pile(name, colors, offsets):
    """A few loose beans/nuts."""
    im = blank()
    base, light, dark, outline = colors
    for cx, cy, pts in offsets:
        blob(im, cx, cy, pts, (base, light, dark, outline))
    wp(os.path.join(A, "morefood/textures/item", name if name.endswith(".png") else name + ".png"), im)

# soybeans: small yellow-green round beans
bean = [(0,0,"o"),(1,0,"o"),(2,0,"o"),(0,1,"o"),(1,1,"l"),(2,1,"b"),(0,2,"o"),(1,2,"d"),(2,2,"o")]
pile("item_soybeans.png", ("c8c050", "e8e078", "8a8828", "5a5820"),
     [(3,4,bean),(8,3,bean),(5,8,bean),(10,9,bean)])
# lentils are already the seed texture; crop item is a small pile of orange-red lentils
lentil = [(0,0,"o"),(1,0,"l"),(0,1,"d"),(1,1,"b")]
pile("item_lentils.png", ("c45a28", "e07840", "8a3414", "5a200c"),
     [(4,5,lentil),(8,4,lentil),(6,8,lentil),(11,7,lentil),(3,9,lentil)])
# chickpeas: beige round
chick = [(0,0,"o"),(1,0,"o"),(2,0,"o"),(0,1,"o"),(1,1,"l"),(2,1,"b"),(0,2,"o"),(1,2,"d"),(2,2,"o")]
pile("item_chickpeas.png", ("dcc48a", "f0e0b0", "a89050", "6a5830"),
     [(4,4,chick),(9,3,chick),(6,8,chick),(11,9,chick)])
# oats: loose pale grain spikelets
im = blank()
for cx, cy, flipped in ((3,4,False),(8,3,True),(5,9,False),(10,8,True)):
    for i, col in enumerate(("e8d888", "d0c060", "a89840")):
        x = cx + (i if not flipped else -i)
        put(im, x, cy, col)
        put(im, x, cy+1, "8a7a30")
    put(im, cx, cy-1, "c8b858")
wp(os.path.join(A, "morefood/textures/item/item_oats.png"), im)
# beansprouts: pale shoots
im = blank()
# three curved sprouts
for sx, sy, color, tip in ((4,3,"e8e8b0","9ccc54"),(8,2,"f0f0c4","b4dc68"),(6,7,"dcdca0","88b848")):
    put(im, sx, sy+3, "c8c888")
    put(im, sx, sy+2, color)
    put(im, sx, sy+1, color)
    put(im, sx+1, sy, color)
    put(im, sx+1, sy-1, tip)
    put(im, sx+2, sy-1, tip)
wp(os.path.join(A, "morefood/textures/item/item_beansprouts.png"), im)

# distinctive soy / chickpea / oat seed shapes are generated by gen_fruit_pods.py

# fruits
def draw_jackfruit(im, ripe=True):
    # large studded oval
    body = "c8c04a" if ripe else "7a9a3a"
    light = "e0dc70" if ripe else "9cbc52"
    dark = "8a8828" if ripe else "4e7020"
    outline = "5a5820" if ripe else "2e4a14"
    # oval 11x13
    for y in range(2, 15):
        for x in range(3, 13):
            cx, cy = 7.5, 8
            if ((x-cx)/5.2)**2 + ((y-cy)/6.4)**2 <= 1:
                edge = ((x-cx)/5.2)**2 + ((y-cy)/6.4)**2 > 0.72
                put(im, x, y, outline if edge else (light if x+y < 14 else dark if x+y > 18 else body))
    # studs
    for x, y in ((5,5),(8,4),(10,6),(6,8),(9,8),(5,11),(8,11),(10,10)):
        put(im, x, y, dark)
    # stem
    put(im, 7, 2, "4a6a28")
    put(im, 8, 2, "4a6a28")
    put(im, 7, 1, "3a521c")

im = blank(); draw_jackfruit(im, True)
wp(os.path.join(A, "morefood/textures/item/item_jackfruit.png"), im)
im = blank(); draw_jackfruit(im, False)
# cooked: more yellow/brown, slightly flatter
im = blank()
for y in range(4, 14):
    for x in range(3, 13):
        if ((x-7.5)/5)**2 + ((y-8.5)/5)**2 <= 1:
            put(im, x, y, "e0b45a" if (x+y)%3==0 else "c89038" if x+y>18 else "d4a448")
for x, y in ((5,7),(8,6),(6,10),(9,9),(4,11)):
    put(im, x, y, "a07028")
wp(os.path.join(A, "morefood/textures/item/item_jackfruit_cooked.png"), im)

# coconut item texture is generated by gen_fruit_pods.py (fibrous brown husk)

# olives: mostly ripe dark purple to black, one still green
im = blank()
def olive_at(im, cx, cy, ripe):
    body, light, dark = (("2a1a28","4a2a3c","140c18") if ripe else ("6a8a38","8aaa50","3e5e1c"))
    for dx, dy, c in ((0,0,dark),(1,0,light),(2,0,dark),(0,1,dark),(1,1,body),(2,1,dark),(1,2,dark)):
        put(im, cx+dx, cy+dy, c)
olive_at(im, 3, 5, True)
olive_at(im, 8, 4, True)
olive_at(im, 5, 9, True)
olive_at(im, 10, 8, False)
wp(os.path.join(A, "morefood/textures/item/item_olive.png"), im)

# avocado: pear shape, ripe dark green to near black
im = blank()
for y in range(2, 15):
    for x in range(4, 12):
        # narrower at top
        rx = 2.6 if y < 7 else 3.6
        if ((x-7.5)/rx)**2 + ((y-8.5)/6.2)**2 <= 1:
            edge = ((x-7.5)/rx)**2 + ((y-8.5)/6.2)**2 > 0.72
            put(im, x, y, "0a1408" if edge else ("2a4a1c" if x+y < 14 else "0c1c0a" if x+y>20 else "14240e"))
put(im, 7, 2, "1a3010")
put(im, 8, 2, "243818")
# hint of stone dip
put(im, 7, 9, "0a1408")
put(im, 8, 9, "2a4a1c")
wp(os.path.join(A, "morefood/textures/item/item_avocado.png"), im)

# roasted peanuts: darker version of old peanut layout, two nuts
im = Image.open(os.path.join(A, "morefood/textures/item/item_peanuts.png")).convert("RGBA")
px = im.load()
for y in range(16):
    for x in range(16):
        r,g,b,a = px[x,y]
        if a == 0: continue
        px[x,y] = (max(0,int(r*0.7)), max(0,int(g*0.55)), max(0,int(b*0.35)), a)
wp(os.path.join(A, "morefood/textures/item/item_peanuts_roasted.png"), im)

# roasted chickpeas: darker pile
im = Image.open(os.path.join(A, "morefood/textures/item/item_chickpeas.png")).convert("RGBA")
px = im.load()
for y in range(16):
    for x in range(16):
        r,g,b,a = px[x,y]
        if a == 0: continue
        px[x,y] = (max(0,int(r*0.85)), max(0,int(g*0.7)), max(0,int(b*0.4)), a)
wp(os.path.join(A, "morefood/textures/item/item_chickpeas_roasted.png"), im)

# fried beansprouts: same sprouts a bit browned
im = Image.open(os.path.join(A, "morefood/textures/item/item_beansprouts.png")).convert("RGBA")
px = im.load()
for y in range(16):
    for x in range(16):
        r,g,b,a = px[x,y]
        if a == 0: continue
        if g > r:  # tip greens become browned
            px[x,y] = (min(255,int(r*1.1+20)), max(0,int(g*0.7)), max(0,int(b*0.3)), a)
        else:
            px[x,y] = (min(255,int(r*0.95)), max(0,int(g*0.8)), max(0,int(b*0.5)), a)
wp(os.path.join(A, "morefood/textures/item/item_beansprouts_fried.png"), im)

# bowls: recolor tomato soup filling
def bowl(name, mapping):
    src = Image.open(os.path.join(A, "morefood/textures/item/item_soup_tomato.png")).convert("RGBA")
    out = src.copy()
    px = out.load()
    for y in range(16):
        for x in range(16):
            r,g,b,a = px[x,y]
            if a == 0: continue
            key = f"{r:02x}{g:02x}{b:02x}"
            if key in mapping:
                px[x,y] = hexc(mapping[key])
    wp(os.path.join(A, "morefood/textures/item", name), out)

# soup filling colors: d8402a, f06a4a, c4321e, a82818
bowl("item_porridge.png", {
    "d8402a": "e8d49a", "f06a4a": "f4e8b8", "c4321e": "d0c07a", "a82818": "b8a45c",
})
bowl("item_soup_lentil.png", {
    "d8402a": "c86a28", "f06a4a": "e09040", "c4321e": "a85018", "a82818": "8a3c10",
})

# ---------- tree textures ----------
# leaves (untinted, already coloured)
def leaves_tex(name, base, light, dark, fruit_color=None, fruit_light=None):
    def f(x, y, r, fruit=False):
        n = (x * 3 + y * 5 + (1 if fruit else 0))
        rnd = random.Random(name + str(n))
        c = hexc(light) if rnd.random() < 0.28 else hexc(dark) if rnd.random() < 0.45 else hexc(base)
        # slight blotches
        if (x + y * 3) % 7 == 0:
            c = hexc(dark)
        return c
    tex(f"block/{name}.png", lambda x,y,r: f(x,y,r,False), name)
    # fruiting-leaf textures removed; pods are generated by gen_fruit_pods.py

leaves_tex("leaves_jackfruit", "3d7a28", "5a9a38", "2a5a18", "c8c04a", "e0dc70")
leaves_tex("leaves_coconut", "5a9a30", "78b848", "3a7218", "7a4e24", "8a5a2c")
leaves_tex("leaves_olive", "6a8a58", "8aaa70", "4a6a38", "6a8a38", "8aaa50")
leaves_tex("leaves_avocado", "2e6a24", "4a8a38", "1e4a14", "4e8230", "6a9a38")

# palm log
tex("block/log_palm.png", wood("8a6230", "a07840", "5a3e18"), "log_palm")
tex("block/log_palm_top.png", wood("c4a06a", "d8b880", "8a6a38", top=True, topc="e8d0a0"), "log_palm_top")

# saplings: little plants in cross
def sapling_tex(name, leaf, bark, fruit=None):
    im = blank()
    # trunk
    for y in range(8, 16):
        put(im, 7, y, bark)
        put(im, 8, y, bark)
    # foliage blob
    for y in range(1, 10):
        for x in range(3, 13):
            if ((x-7.5)/4.4)**2 + ((y-5.2)/4.2)**2 <= 1:
                put(im, x, y, leaf[0] if (x+y)%3 else leaf[1])
    if fruit:
        for x,y in ((5,5),(10,6),(7,3)):
            put(im, x, y, fruit)
    wp(os.path.join(A, "morefood/textures/block", name + ".png"), im)

sapling_tex("sapling_jackfruit", ("3d7a28","5a9a38"), "6a4a22", "c8c04a")
sapling_tex("sapling_coconut", ("5a9a30","78b848"), "8a6230")
# coconut sapling as a small palm: overwrite with a more palm-like shape
im = blank()
for y in range(7, 16):
    put(im, 7, y, "8a6230")
    put(im, 8, y, "6a4a22")
# fronds
for dx, dy in ((-1,6),(-2,5),(-3,5),(-4,6),(1,6),(2,5),(3,5),(4,6),(0,4),(1,3),(-1,3),(0,2),(2,4),(-2,4)):
    put(im, 7+dx, dy, "5a9a30" if (dx+dy)%2 else "78b848")
    put(im, 8+dx, dy, "3a7218")
wp(os.path.join(A, "morefood/textures/block/sapling_coconut.png"), im)
sapling_tex("sapling_olive", ("6a8a58","8aaa70"), "7a6a48", "6a8a38")
sapling_tex("sapling_avocado", ("2e6a24","4a8a38"), "6a4a22", "4e8230")

# ---------- tree block models / states ----------
for name, leaves, fruit in (
    ("jackfruit", "leaves_jackfruit", "leaves_jackfruit_fruit"),
    ("coconut", "leaves_coconut", "leaves_coconut_fruit"),
    ("olive", "leaves_olive", "leaves_olive_fruit"),
    ("avocado", "leaves_avocado", "leaves_avocado_fruit"),
):
    w(os.path.join(A, "morefood/models/block", f"leaves_{name}.json"), {
        "parent": "minecraft:block/leaves", "render_type": "minecraft:cutout_mipped",
        "textures": {"all": f"morefood:block/leaves_{name}"}
    })
    w(os.path.join(A, "morefood/blockstates", f"block_leaves_{name}.json"), {
        "variants": {"": {"model": f"morefood:block/leaves_{name}"}}
    })
    w(os.path.join(A, "morefood/models/block", f"sapling_{name}.json"), {
        "parent": "minecraft:block/cross", "render_type": "minecraft:cutout",
        "textures": {"cross": f"morefood:block/sapling_{name}"}
    })
    w(os.path.join(A, "morefood/blockstates", f"block_sapling_{name}.json"), {
        "variants": {"": {"model": f"morefood:block/sapling_{name}"}}
    })
    w(os.path.join(A, "morefood/models/item", f"block_sapling_{name}.json"), {
        "parent": "minecraft:item/generated",
        "textures": {"layer0": f"morefood:block/sapling_{name}"}
    })
    block_item_model(f"block_sapling_{name}", f"morefood:item/block_sapling_{name}")
    block_item_model(f"block_leaves_{name}", f"morefood:block/leaves_{name}")

w(os.path.join(A, "morefood/models/block/block_log_palm.json"), {
    "parent": "minecraft:block/cube_column",
    "textures": {"end": "morefood:block/log_palm_top", "side": "morefood:block/log_palm"}
})
w(os.path.join(A, "morefood/models/block/block_log_palm_horizontal.json"), {
    "parent": "minecraft:block/cube_column_horizontal",
    "textures": {"end": "morefood:block/log_palm_top", "side": "morefood:block/log_palm"}
})
w(os.path.join(A, "morefood/blockstates/block_log_palm.json"), {
    "variants": {
        "axis=y": {"model": "morefood:block/block_log_palm"},
        "axis=z": {"model": "morefood:block/block_log_palm_horizontal"},
        "axis=x": {"model": "morefood:block/block_log_palm_horizontal", "x": 90, "y": 90}
    }
})
w(os.path.join(A, "morefood/models/item/block_log_palm.json"), {"parent": "morefood:block/block_log_palm"})
block_item_model("block_log_palm", "morefood:block/block_log_palm")

# item models for all items
ITEMS = [
    ("item_soybeans", "morefood:item/item_soybeans"),
    ("item_lentils", "morefood:item/item_lentils"),
    ("item_chickpeas", "morefood:item/item_chickpeas"),
    ("item_peanuts", "morefood:item/item_peanuts"),
    ("item_oats", "morefood:item/item_oats"),
    ("item_beansprouts", "morefood:item/item_beansprouts"),
    ("item_seed_soy", "morefood:item/seeds/item_seed_soy"),
    ("item_seed_lentil", "morefood:item/seeds/item_seed_lentil"),
    ("item_seed_chickpea", "morefood:item/seeds/item_seed_chickpea"),
    ("item_seed_peanut", "morefood:item/seeds/item_seed_peanut"),
    ("item_seed_oats", "morefood:item/seeds/item_seed_oats"),
    ("item_seed_beansprouts", "morefood:item/seeds/item_seed_beansprouts"),
    ("item_jackfruit", "morefood:item/item_jackfruit"),
    ("item_coconut", "morefood:item/item_coconut"),
    ("item_olive", "morefood:item/item_olive"),
    ("item_avocado", "morefood:item/item_avocado"),
    ("item_peanuts_roasted", "morefood:item/item_peanuts_roasted"),
    ("item_chickpeas_roasted", "morefood:item/item_chickpeas_roasted"),
    ("item_beansprouts_fried", "morefood:item/item_beansprouts_fried"),
    ("item_porridge", "morefood:item/item_porridge"),
    ("item_soup_lentil", "morefood:item/item_soup_lentil"),
    ("item_jackfruit_cooked", "morefood:item/item_jackfruit_cooked"),
]
for name, texname in ITEMS:
    item_model(name, texname)

# ---------- data ----------
def crop_loot(block, crop_item, seed, count=2.0):
    return {
      "type": "minecraft:block",
      "functions": [{"function": "minecraft:explosion_decay"}],
      "pools": [
        {"rolls": 1.0, "bonus_rolls": 0.0, "entries": [
          {"type": "minecraft:alternatives", "children": [
            {"type": "minecraft:item", "name": crop_item,
             "conditions": [{"condition": "minecraft:block_state_property", "block": block, "properties": {"age": "7"}}],
             "functions": [{"function": "minecraft:set_count", "count": count}]},
            {"type": "minecraft:item", "name": seed}
          ]}
        ]},
        {"rolls": 1.0, "bonus_rolls": 0.0,
         "conditions": [{"condition": "minecraft:block_state_property", "block": block, "properties": {"age": "7"}}],
         "entries": [{"type": "minecraft:item", "name": seed,
           "functions": [{"function": "minecraft:apply_bonus", "enchantment": "minecraft:fortune",
             "formula": "minecraft:binomial_with_bonus_count",
             "parameters": {"extra": 4, "probability": 0.5333333}}]}]}
      ],
      "random_sequence": "morefood:blocks/" + block.split(":")[1]
    }

CROP_ITEMS = {
    "soy": ("morefood:item_soybeans", "morefood:item_seed_soy"),
    "lentil": ("morefood:item_lentils", "morefood:item_seed_lentil"),
    "chickpea": ("morefood:item_chickpeas", "morefood:item_seed_chickpea"),
    "peanut": ("morefood:item_peanuts", "morefood:item_seed_peanut"),
    "oats": ("morefood:item_oats", "morefood:item_seed_oats"),
    "beansprouts": ("morefood:item_beansprouts", "morefood:item_seed_beansprouts"),
}
for name, (crop_item, seed) in CROP_ITEMS.items():
    w(os.path.join(ERA, f"data/morefood/loot_table/blocks/block_crop_{name}.json"),
      crop_loot(f"morefood:block_crop_{name}", crop_item, seed, 2.0 if name != "oats" else 3.0))

SILK = {
  "condition": "minecraft:any_of",
  "terms": [
    {"condition": "minecraft:match_tool", "predicate": {"items": "minecraft:shears"}},
    {"condition": "minecraft:match_tool", "predicate": {"predicates": {"minecraft:enchantments": [
      {"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]}}}
  ]
}

def leaves_loot(name, fruit):
    block = f"morefood:block_leaves_{name}"
    sapling = f"morefood:block_sapling_{name}"
    return {
      "type": "minecraft:block",
      "pools": [
        {"rolls": 1.0, "bonus_rolls": 0.0, "entries": [
          {"type": "minecraft:alternatives", "children": [
            {"type": "minecraft:item", "conditions": [SILK], "name": block},
            {"type": "minecraft:item", "name": sapling,
             "conditions": [
               {"condition": "minecraft:survives_explosion"},
               {"chances": [0.05, 0.0625, 0.083333336, 0.1],
                "condition": "minecraft:table_bonus", "enchantment": "minecraft:fortune"}
             ]}
          ]}
        ]},
        {"rolls": 1.0, "bonus_rolls": 0.0,
         "conditions": [{"condition": "minecraft:inverted", "term": SILK}],
         "entries": [{"type": "minecraft:item", "name": "minecraft:stick",
           "conditions": [{"chances": [0.02, 0.022222223, 0.025, 0.033333335, 0.1],
             "condition": "minecraft:table_bonus", "enchantment": "minecraft:fortune"}],
           "functions": [
             {"add": False, "count": {"type": "minecraft:uniform", "max": 2.0, "min": 1.0}, "function": "minecraft:set_count"},
             {"function": "minecraft:explosion_decay"}]}]}
      ],
      "random_sequence": f"morefood:blocks/block_leaves_{name}"
    }

def simple_loot(name):
    return {
      "type": "minecraft:block",
      "pools": [{"rolls": 1.0, "bonus_rolls": 0.0, "entries": [
        {"type": "minecraft:item", "name": name, "conditions": [{"condition": "minecraft:survives_explosion"}]}
      ]}],
      "random_sequence": "morefood:blocks/" + name.split(":")[1]
    }

for name, fruit in (("jackfruit","morefood:item_jackfruit"),("coconut","morefood:item_coconut"),
                    ("olive","morefood:item_olive"),("avocado","morefood:item_avocado")):
    w(os.path.join(ERA, f"data/morefood/loot_table/blocks/block_leaves_{name}.json"), leaves_loot(name, fruit))
    w(os.path.join(ERA, f"data/morefood/loot_table/blocks/block_sapling_{name}.json"), simple_loot(f"morefood:block_sapling_{name}"))
w(os.path.join(ERA, "data/morefood/loot_table/blocks/block_log_palm.json"), simple_loot("morefood:block_log_palm"))

# seed recipes
for crop_item, seed in CROP_ITEMS.values():
    w(os.path.join(ERA, "data/morefood/recipe", seed.split(":")[1] + ".json"), {
      "type": "minecraft:crafting_shapeless", "category": "misc",
      "ingredients": [crop_item],
      "result": {"id": seed}
    })

# simple cooked recipes
def baking(ing, result, time=200, xp=0.1):
    w(os.path.join(ERA, f"data/morefood/recipe/{result.split(':')[1]}_from_baking.json"), {
      "type": "morefood:baking", "category": "food", "cookingtime": time, "experience": xp,
      "ingredient": ing, "result": {"id": result}
    })
baking("morefood:item_peanuts", "morefood:item_peanuts_roasted")
baking("morefood:item_chickpeas", "morefood:item_chickpeas_roasted")
baking("morefood:item_jackfruit", "morefood:item_jackfruit_cooked", 300, 0.2)
w(os.path.join(ERA, "data/morefood/recipe/item_beansprouts_fried_from_frying.json"), {
  "type": "morefood:frying", "category": "food", "cookingtime": 100, "experience": 0.1,
  "ingredient": "morefood:item_beansprouts", "result": {"id": "morefood:item_beansprouts_fried"}
})
w(os.path.join(ERA, "data/morefood/recipe/item_porridge_from_boiling.json"), {
  "type": "morefood:boiling",
  "ingredients": ["morefood:item_oats", "minecraft:milk_bucket"],
  "water": 0, "result": {"id": "morefood:item_porridge"},
  "portions": 2, "cookingtime": 300, "colour": "#e8d49a", "vessel": "minecraft:bowl"
})
w(os.path.join(ERA, "data/morefood/recipe/item_soup_lentil_from_boiling.json"), {
  "type": "morefood:boiling",
  "ingredients": ["morefood:item_lentils", "morefood:item_lentils", "morefood:item_onion_chopped", "morefood:item_stock"],
  "water": 250, "result": {"id": "morefood:item_soup_lentil"},
  "portions": 2, "cookingtime": 400, "colour": "#c86a28", "vessel": "minecraft:bowl"
})

# worldgen
def placed(name, sapling, rarity=None, count=None):
    w(os.path.join(ERA, f"data/morefood/worldgen/configured_feature/tree_{name}.json"), {
      "type": f"morefood:{name}_tree", "config": {}
    })
    filters = []
    if count is not None:
        filters.append({"type": "minecraft:count", "count": count})
    if rarity is not None:
        filters.append({"type": "minecraft:rarity_filter", "chance": rarity})
    filters.extend([
        {"type": "minecraft:in_square"},
        {"type": "minecraft:surface_water_depth_filter", "max_water_depth": 0},
        {"type": "minecraft:heightmap", "heightmap": "OCEAN_FLOOR"},
        {"type": "minecraft:biome"},
        {"type": "minecraft:block_predicate_filter",
         "predicate": {"type": "minecraft:would_survive", "state": {"Name": sapling}}}
    ])
    w(os.path.join(ERA, f"data/morefood/worldgen/placed_feature/tree_{name}.json"), {
      "feature": f"morefood:tree_{name}",
      "placement": filters
    })

# attempts per chunk: olive is easy to find, jungle trees retry and skip vanilla canopy
placed("jackfruit", "morefood:block_sapling_jackfruit", count=2)
placed("coconut", "morefood:block_sapling_coconut", rarity=3)
placed("olive", "morefood:block_sapling_olive", count=1)
placed("avocado", "morefood:block_sapling_avocado", count=1)

for name, tag in (("jackfruit","#minecraft:is_jungle"),("coconut","#minecraft:is_beach"),
                  ("olive","#minecraft:is_savanna"),("avocado","#minecraft:is_jungle")):
    w(os.path.join(ERA, f"data/morefood/neoforge/biome_modifier/tree_{name}.json"), {
      "type": "neoforge:add_features", "biomes": tag,
      "features": f"morefood:tree_{name}", "step": "vegetal_decoration"
    })

# tags helpers
def read(path):
    with open(path) as f:
        return json.load(f)

def add_values(path, extra):
    d = read(path)
    vals = d["values"]
    for v in extra:
        if v not in vals:
            vals.append(v)
    w(path, d)

for name, crop_item, seed in (
    ("soy","morefood:item_soybeans","morefood:item_seed_soy"),
    ("lentil","morefood:item_lentils","morefood:item_seed_lentil"),
    ("chickpea","morefood:item_chickpeas","morefood:item_seed_chickpea"),
    ("peanut","morefood:item_peanuts","morefood:item_seed_peanut"),
    ("oats","morefood:item_oats","morefood:item_seed_oats"),
    ("beansprouts","morefood:item_beansprouts","morefood:item_seed_beansprouts"),
):
    w(os.path.join(ERA, f"data/c/tags/item/crops/{name}.json"), {"values": [crop_item]})
    w(os.path.join(ERA, f"data/c/tags/item/seeds/{name}.json"), {"values": [seed]})
    add_values(os.path.join(ERA, "data/c/tags/item/crops.json"), [f"#c:crops/{name}"])
    add_values(os.path.join(ERA, "data/c/tags/item/seeds.json"), [f"#c:seeds/{name}"])
    add_values(os.path.join(ERA, "data/morefood/tags/item/crops.json"), [crop_item])
    add_values(os.path.join(ERA, "data/morefood/tags/item/seeds.json"), [seed])
    add_values(os.path.join(ERA, "data/minecraft/tags/item/villager_plantable_seeds.json"), [seed])
    add_values(os.path.join(ERA, "data/minecraft/tags/block/crops.json"), [f"morefood:block_crop_{name}"])
    add_values(os.path.join(ERA, "data/minecraft/tags/block/maintains_farmland.json"), [f"morefood:block_crop_{name}"])

# fruits as crops too
for fruit in ("morefood:item_jackfruit","morefood:item_coconut","morefood:item_olive","morefood:item_avocado"):
    add_values(os.path.join(ERA, "data/morefood/tags/item/crops.json"), [fruit])
for name, fruit in (("jackfruit","morefood:item_jackfruit"),("coconut","morefood:item_coconut"),
                    ("olive","morefood:item_olive"),("avocado","morefood:item_avocado")):
    w(os.path.join(ERA, f"data/c/tags/item/crops/{name}.json"), {"values": [fruit]})
    add_values(os.path.join(ERA, "data/c/tags/item/crops.json"), [f"#c:crops/{name}"])

w(os.path.join(ERA, "data/c/tags/item/foods/fruit.json"), {"values": [
    "morefood:item_jackfruit", "morefood:item_coconut", "morefood:item_olive", "morefood:item_avocado"
]})
add_values(os.path.join(ERA, "data/c/tags/item/foods/vegetable.json"), [
    "morefood:item_beansprouts", "morefood:item_lentils", "morefood:item_chickpeas"
])

add_values(os.path.join(ERA, "data/minecraft/tags/block/mineable/axe.json"), ["morefood:block_log_palm"])
w(os.path.join(ERA, "data/minecraft/tags/block/mineable/hoe.json"), {"values": [
    "morefood:block_leaves_jackfruit","morefood:block_leaves_coconut",
    "morefood:block_leaves_olive","morefood:block_leaves_avocado"
]})
w(os.path.join(ERA, "data/minecraft/tags/block/leaves.json"), {"values": [
    "morefood:block_leaves_jackfruit","morefood:block_leaves_coconut",
    "morefood:block_leaves_olive","morefood:block_leaves_avocado"
]})
w(os.path.join(ERA, "data/minecraft/tags/block/saplings.json"), {"values": [
    "morefood:block_sapling_jackfruit","morefood:block_sapling_coconut",
    "morefood:block_sapling_olive","morefood:block_sapling_avocado"
]})
w(os.path.join(ERA, "data/minecraft/tags/item/saplings.json"), {"values": [
    "morefood:block_sapling_jackfruit","morefood:block_sapling_coconut",
    "morefood:block_sapling_olive","morefood:block_sapling_avocado"
]})
w(os.path.join(ERA, "data/minecraft/tags/block/logs.json"), {"values": ["morefood:block_log_palm"]})
w(os.path.join(ERA, "data/minecraft/tags/block/logs_that_burn.json"), {"values": ["morefood:block_log_palm"]})
w(os.path.join(ERA, "data/minecraft/tags/item/logs.json"), {"values": ["morefood:block_log_palm"]})
w(os.path.join(ERA, "data/minecraft/tags/item/logs_that_burn.json"), {"values": ["morefood:block_log_palm"]})

# grass loot
grass = read(os.path.join(ERA, "data/morefood/loot_table/inject/grass.json"))
names = [e["name"] for e in grass["pools"][0]["entries"]]
for seed in ("morefood:item_seed_soy","morefood:item_seed_lentil","morefood:item_seed_chickpea",
             "morefood:item_seed_peanut","morefood:item_seed_oats","morefood:item_seed_beansprouts"):
    if seed not in names:
        grass["pools"][0]["entries"].append({"type": "minecraft:item", "name": seed})
w(os.path.join(ERA, "data/morefood/loot_table/inject/grass.json"), grass)

# compost data map
compost = read(os.path.join(ROOT, "src/main/resources/data/neoforge/data_maps/item/compostables.json"))
def add_compost(item, chance):
    compost["values"][item] = {"chance": chance}
for name in ("soy","lentil","chickpea","peanut","oats","beansprouts"):
    add_compost(f"morefood:item_seed_{name}", 0.3)
for name in ("jackfruit","coconut","olive","avocado"):
    add_compost(f"morefood:block_sapling_{name}", 0.3)
    add_compost(f"morefood:block_leaves_{name}", 0.3)
for name in ("item_soybeans","item_lentils","item_chickpeas","item_peanuts","item_oats","item_beansprouts",
             "item_jackfruit","item_coconut","item_olive","item_avocado"):
    add_compost(f"morefood:{name}", 0.65)
w(os.path.join(ROOT, "src/main/resources/data/neoforge/data_maps/item/compostables.json"), compost)

# lang
lang_path = os.path.join(A, "morefood/lang/en_us.json")
lang = read(lang_path)
lang.update({
  "item.morefood.item_soybeans": "Soybeans",
  "item.morefood.item_seed_soy": "Soybean Seeds",
  "block.morefood.block_crop_soy": "Soybeans",
  "item.morefood.item_lentils": "Lentils",
  "item.morefood.item_seed_lentil": "Lentil Seeds",
  "block.morefood.block_crop_lentil": "Lentils",
  "item.morefood.item_chickpeas": "Chickpeas",
  "item.morefood.item_seed_chickpea": "Chickpea Seeds",
  "block.morefood.block_crop_chickpea": "Chickpeas",
  "item.morefood.item_peanuts": "Peanuts",
  "item.morefood.item_seed_peanut": "Peanut Seeds",
  "block.morefood.block_crop_peanut": "Peanuts",
  "item.morefood.item_oats": "Oats",
  "item.morefood.item_seed_oats": "Oat Seeds",
  "block.morefood.block_crop_oats": "Oats",
  "item.morefood.item_beansprouts": "Beansprouts",
  "item.morefood.item_seed_beansprouts": "Beansprout Seeds",
  "block.morefood.block_crop_beansprouts": "Beansprouts",
  "item.morefood.item_jackfruit": "Jackfruit",
  "item.morefood.item_coconut": "Coconut",
  "item.morefood.item_olive": "Olives",
  "item.morefood.item_avocado": "Avocado",
  "block.morefood.block_sapling_jackfruit": "Jackfruit Sapling",
  "block.morefood.block_sapling_coconut": "Coconut Sapling",
  "block.morefood.block_sapling_olive": "Olive Sapling",
  "block.morefood.block_sapling_avocado": "Avocado Sapling",
  "block.morefood.block_leaves_jackfruit": "Jackfruit Leaves",
  "block.morefood.block_leaves_coconut": "Palm Leaves",
  "block.morefood.block_leaves_olive": "Olive Leaves",
  "block.morefood.block_leaves_avocado": "Avocado Leaves",
  "block.morefood.block_log_palm": "Palm Log",
  "item.morefood.item_peanuts_roasted": "Roasted Peanuts",
  "item.morefood.item_chickpeas_roasted": "Roasted Chickpeas",
  "item.morefood.item_beansprouts_fried": "Fried Beansprouts",
  "item.morefood.item_porridge": "Porridge",
  "item.morefood.item_soup_lentil": "Lentil Soup",
  "item.morefood.item_jackfruit_cooked": "Cooked Jackfruit",
})
w(lang_path, lang)
print("vegan assets written")
