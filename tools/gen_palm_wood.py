#!/usr/bin/env python3
"""Generate palm wood textures, models, recipes, tags, loot and item models.
Run from the repo root. Then run tools/gen_eras.py to rebuild 1.20.1, 1.21.1 and 26.3."""
from __future__ import annotations

import json
import os
import random
import shutil
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
MC = Path("/workspace/mods/mc261src/assets/minecraft")
MAIN = ROOT / "src/main/resources/assets/morefood"
ERA = ROOT / "src/eras/26.1"
MOD = "morefood"

PALM_LOGS = [
    "block_log_palm",
    "block_stripped_log_palm",
    "block_wood_palm",
    "block_stripped_wood_palm",
]
WOOD_BLOCKS = PALM_LOGS + [
    "block_planks_palm",
    "block_stairs_palm",
    "block_slab_palm",
    "block_fence_palm",
    "block_fence_gate_palm",
    "block_door_palm",
    "block_trapdoor_palm",
    "block_button_palm",
    "block_pressure_plate_palm",
]
PLANKS_STYLE = [
    "block_planks_palm",
    "block_stairs_palm",
    "block_slab_palm",
    "block_fence_palm",
    "block_fence_gate_palm",
    "block_door_palm",
    "block_trapdoor_palm",
    "block_button_palm",
    "block_pressure_plate_palm",
]


def w(path: Path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")


def load(path: Path):
    with path.open() as f:
        return json.load(f)


def rewrite(obj, mapping: dict[str, str]):
    if isinstance(obj, dict):
        return {k: rewrite(v, mapping) for k, v in obj.items()}
    if isinstance(obj, list):
        return [rewrite(v, mapping) for v in obj]
    if isinstance(obj, str):
        for a, b in mapping.items():
            obj = obj.replace(a, b)
        return obj
    return obj


# ---------------------------------------------------------------------------
# Textures — fibrous cocowood, no growth rings, hairy bark, stripey planks
# ---------------------------------------------------------------------------

def px(im: Image.Image, x, y, color):
    if 0 <= x < 16 and 0 <= y < 16:
        im.putpixel((x, y), color)


def lerp(a, b, t):
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3)) + (255,)


def jitter(rng: random.Random, color, amt=8):
    return tuple(max(0, min(255, c + rng.randint(-amt, amt))) for c in color[:3]) + (255,)


def make_bark(seed=46) -> Image.Image:
    """Stringy, hairy husk. Vertical coir fibres, no rings."""
    rng = random.Random(seed)
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 255))
    dark = (58, 34, 18)
    mid = (92, 58, 30)
    light = (138, 92, 48)
    hair = (196, 158, 98)
    # base vertical fibre bundles of uneven width
    x = 0
    while x < 16:
        width = rng.choice([1, 1, 2, 2, 3])
        base = rng.choice([dark, mid, mid, light])
        wave = rng.uniform(-0.6, 0.6)
        for dx in range(width):
            for y in range(16):
                shift = int(round((y / 5.0) * wave))
                tone = lerp(base, light if (dx == 0 or dx == width - 1) else dark, 0.15 if dx else 0.0)
                # slight vertical grit
                tone = jitter(rng, tone, 10)
                px(im, x + dx, (y + shift) % 16, tone)
        x += width
    # crossing coir threads (hairy)
    for _ in range(14):
        x0 = rng.randint(0, 15)
        y0 = rng.randint(0, 15)
        length = rng.randint(3, 8)
        color = hair if rng.random() < 0.45 else light
        slant = rng.choice([-1, 0, 0, 1, 1])
        for i in range(length):
            px(im, x0 + (i * slant) // 3, (y0 + i) % 16, jitter(rng, color, 12))
    # stray 1px hairs sticking out of the "surface"
    for _ in range(18):
        x = rng.randint(0, 15)
        y = rng.randint(0, 15)
        color = hair if rng.random() < 0.5 else light
        px(im, x, y, jitter(rng, color, 8))
        if rng.random() < 0.5:
            px(im, (x + rng.choice([-1, 1])) % 16, y, jitter(rng, mid, 6))
    return im


def make_bark_top(seed=47) -> Image.Image:
    """Cut end of fibre bundles: speckled dots, fibrous rim, no growth rings."""
    rng = random.Random(seed)
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 255))
    pith = (168, 118, 70)
    mid = (122, 74, 38)
    dark = (72, 42, 22)
    bark = (90, 56, 30)
    hair = (186, 146, 90)
    # fill with packed fibre-end dots
    for y in range(16):
        for x in range(16):
            # slightly denser toward the middle
            cx, cy = x - 7.5, y - 7.5
            r = (cx * cx + cy * cy) ** 0.5
            base = lerp(pith, mid, min(1.0, r / 9.0))
            if rng.random() < 0.35:
                base = dark if rng.random() < 0.4 else pith
            px(im, x, y, jitter(rng, base, 14))
    # fibre-end clusters (2x2 ovals)
    for _ in range(22):
        x, y = rng.randint(1, 14), rng.randint(1, 14)
        color = rng.choice([pith, mid, dark, hair])
        for dx, dy in ((0, 0), (1, 0), (0, 1), (1, 1)):
            if rng.random() < 0.8:
                px(im, x + dx, y + dy, jitter(rng, color, 10))
    # hairy bark rim, irregular, not a clean ring
    for i in range(16):
        for edge, j in (("x0", i), ("x15", i), ("y0", i), ("y15", i)):
            if edge.startswith("x"):
                x = 0 if edge == "x0" else 15
                y = j
            else:
                y = 0 if edge == "y0" else 15
                x = j
            px(im, x, y, jitter(rng, bark if rng.random() < 0.7 else hair, 12))
            # second pixel inward, broken
            if rng.random() < 0.65:
                if edge == "x0":
                    px(im, 1, y, jitter(rng, dark, 10))
                elif edge == "x15":
                    px(im, 14, y, jitter(rng, dark, 10))
                elif edge == "y0":
                    px(im, x, 1, jitter(rng, dark, 10))
                else:
                    px(im, x, 14, jitter(rng, dark, 10))
    return im


def make_stripped_side(seed=48) -> Image.Image:
    """Cleaner interior: vertical fibre bundles, warm red-brown, speckled."""
    rng = random.Random(seed)
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 255))
    deep = (90, 36, 20)
    mid = (154, 74, 40)
    light = (204, 130, 72)
    speck = (232, 196, 140)
    x = 0
    while x < 16:
        width = rng.choice([1, 2, 2, 3])
        bundle = rng.choice([deep, mid, mid, light])
        for dx in range(width):
            for y in range(16):
                # faint wave so bundles aren't dead-straight
                yoff = int(round(0.4 * (1 if (y // 4 + x) % 2 == 0 else -1)))
                tone = bundle
                if dx == 0:
                    tone = lerp(bundle, deep, 0.35)
                elif dx == width - 1:
                    tone = lerp(bundle, light, 0.25)
                if rng.random() < 0.08:
                    tone = speck
                px(im, x + dx, (y + yoff) % 16, jitter(rng, tone, 8))
        x += width
    # occasional darker fibre flecks
    for _ in range(16):
        px(im, rng.randint(0, 15), rng.randint(0, 15), jitter(rng, deep, 6))
    return im


def make_stripped_top(seed=49) -> Image.Image:
    """Stripped end: same fibre-bundle dots, no bark rim, no rings."""
    rng = random.Random(seed)
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 255))
    pith = (210, 150, 90)
    mid = (154, 74, 40)
    deep = (90, 36, 20)
    for y in range(16):
        for x in range(16):
            # packed ends, slightly warmer in the middle
            cx, cy = x - 7.5, y - 7.5
            r = (cx * cx + cy * cy) ** 0.5
            base = lerp(pith, mid, min(1.0, r / 10.0))
            if rng.random() < 0.3:
                base = deep if rng.random() < 0.35 else pith
            px(im, x, y, jitter(rng, base, 12))
    for _ in range(20):
        x, y = rng.randint(0, 14), rng.randint(0, 14)
        color = rng.choice([pith, mid, deep])
        for dx, dy in ((0, 0), (1, 0), (0, 1)):
            if rng.random() < 0.85:
                px(im, x + dx, y + dy, jitter(rng, color, 8))
    return im


def make_planks(seed=50) -> Image.Image:
    """Warm red-brown boards with stripey fibre grain and speckles."""
    rng = random.Random(seed)
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 255))
    deep = (86, 32, 18)
    mid = (166, 78, 42)
    light = (210, 132, 74)
    pale = (232, 192, 132)
    # four boards like vanilla planks, but grain is vertical fibre, no rings
    # vanilla oak planks: roughly 3-4 horizontal boards. Keep that board layout
    # so it reads as planks, with stripey grain inside each board.
    seams = [0, 4, 8, 12]
    for i, y0 in enumerate(seams):
        y1 = seams[i + 1] if i + 1 < len(seams) else 16
        # seam line
        for x in range(16):
            px(im, x, y0, jitter(rng, deep, 6))
        for y in range(y0 + 1, y1):
            # vertical fibre in this board, slightly offset per board
            off = rng.randint(0, 2)
            for x in range(16):
                stripe = (x + off + i) % 3
                if stripe == 0:
                    tone = deep
                elif stripe == 1:
                    tone = mid
                else:
                    tone = light
                if rng.random() < 0.07:
                    tone = pale
                px(im, x, y, jitter(rng, tone, 7))
    return im


def make_door_bottom(planks: Image.Image, seed=51) -> Image.Image:
    """Plank door bottom with a simple frame and handle, matching vanilla doors."""
    rng = random.Random(seed)
    im = planks.copy()
    frame = (86, 32, 18, 255)
    mid = (166, 78, 42, 255)
    pale = (232, 192, 132, 255)
    # frame
    for i in range(16):
        px(im, 0, i, frame)
        px(im, 15, i, frame)
        px(im, i, 15, frame)
        px(im, i, 0, mid)  # joins the top half
    # inner panel inset
    for y in range(2, 15):
        px(im, 1, y, frame)
        px(im, 14, y, frame)
    for x in range(1, 15):
        px(im, x, 14, frame)
        px(im, x, 1, mid)
    # handle on the right, mid-height (continues to top half)
    px(im, 12, 2, pale)
    px(im, 13, 2, pale)
    px(im, 12, 3, frame)
    return im


def make_door_top(planks: Image.Image, seed=52) -> Image.Image:
    rng = random.Random(seed)
    im = planks.copy()
    frame = (86, 32, 18, 255)
    mid = (166, 78, 42, 255)
    pale = (232, 192, 132, 255)
    dark = (54, 22, 12, 255)
    for i in range(16):
        px(im, 0, i, frame)
        px(im, 15, i, frame)
        px(im, i, 0, frame)
        px(im, i, 15, mid)
    for y in range(1, 15):
        px(im, 1, y, frame)
        px(im, 14, y, frame)
    for x in range(1, 15):
        px(im, x, 1, frame)
        px(im, x, 14, mid)
    # small window (4x5) like many vanilla wooden doors
    for y in range(4, 9):
        for x in range(6, 10):
            px(im, x, y, dark if (x in (6, 9) or y in (4, 8)) else (40, 16, 10, 255))
    # handle continuation
    px(im, 12, 14, pale)
    px(im, 13, 14, pale)
    px(im, 12, 13, frame)
    return im


def make_trapdoor(planks: Image.Image, seed=53) -> Image.Image:
    im = planks.copy()
    frame = (86, 32, 18, 255)
    pale = (232, 192, 132, 255)
    for i in range(16):
        px(im, 0, i, frame)
        px(im, 15, i, frame)
        px(im, i, 0, frame)
        px(im, i, 15, frame)
    for i in range(1, 15):
        px(im, 1, i, frame)
        px(im, 14, i, frame)
        px(im, i, 1, frame)
        px(im, i, 14, frame)
    # cross brace
    for i in range(3, 13):
        px(im, i, i, frame)
        px(im, i, 15 - i, frame)
    px(im, 8, 8, pale)
    return im


def make_door_item(top: Image.Image, bottom: Image.Image) -> Image.Image:
    """16x16 item icon: shrink the two halves into a door silhouette."""
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    # vanilla door items are a 6-8px wide door standing in the sprite
    top_s = top.resize((10, 8), Image.Resampling.NEAREST)
    bot_s = bottom.resize((10, 8), Image.Resampling.NEAREST)
    im.paste(top_s, (3, 0))
    im.paste(bot_s, (3, 8))
    # clear a little margin already present
    return im


def write_textures():
    tex = MAIN / "textures/block"
    item = MAIN / "textures/item"
    item.mkdir(parents=True, exist_ok=True)
    bark = make_bark()
    bark_top = make_bark_top()
    stripped = make_stripped_side()
    stripped_top = make_stripped_top()
    planks = make_planks()
    door_bottom = make_door_bottom(planks)
    door_top = make_door_top(planks)
    trap = make_trapdoor(planks)
    door_item = make_door_item(door_top, door_bottom)
    bark.save(tex / "log_palm.png")
    bark_top.save(tex / "log_palm_top.png")
    stripped.save(tex / "log_palm_stripped.png")
    stripped_top.save(tex / "log_palm_stripped_top.png")
    planks.save(tex / "planks_palm.png")
    door_bottom.save(tex / "door_palm_bottom.png")
    door_top.save(tex / "door_palm_top.png")
    trap.save(tex / "trapdoor_palm.png")
    door_item.save(item / "door_palm.png")
    print("wrote textures")


# ---------------------------------------------------------------------------
# Blockstates and models
# ---------------------------------------------------------------------------

def log_blockstate(model: str):
    return {
        "variants": {
            "axis=y": {"model": f"{MOD}:block/{model}"},
            "axis=z": {"model": f"{MOD}:block/{model}_horizontal"},
            "axis=x": {"model": f"{MOD}:block/{model}_horizontal", "x": 90, "y": 90},
        }
    }


def cube_column(end: str, side: str, horizontal=False):
    return {
        "parent": "minecraft:block/cube_column_horizontal" if horizontal else "minecraft:block/cube_column",
        "textures": {"end": end, "side": side},
    }


def copy_cherry_set():
    """Copy cherry stairs/slab/fence/gate/door/trapdoor/button/plate models and blockstates."""
    mapping = {
        "minecraft:block/cherry_planks": f"{MOD}:block/planks_palm",
        "minecraft:block/cherry_door_bottom": f"{MOD}:block/door_palm_bottom",
        "minecraft:block/cherry_door_top": f"{MOD}:block/door_palm_top",
        "minecraft:block/cherry_trapdoor": f"{MOD}:block/trapdoor_palm",
        "minecraft:block/cherry_": f"{MOD}:block/block_",
        "_cherry": "_palm",
        "cherry_": "",
        "minecraft:block/block_": f"{MOD}:block/block_",
    }
    # The naive replace above is messy. Do explicit file copies instead.
    model_renames = {
        "cherry_stairs": "block_stairs_palm",
        "cherry_stairs_inner": "block_stairs_palm_inner",
        "cherry_stairs_outer": "block_stairs_palm_outer",
        "cherry_slab": "block_slab_palm",
        "cherry_slab_top": "block_slab_palm_top",
        "cherry_fence_post": "block_fence_palm_post",
        "cherry_fence_side": "block_fence_palm_side",
        "cherry_fence_inventory": "block_fence_palm_inventory",
        "cherry_fence_gate": "block_fence_gate_palm",
        "cherry_fence_gate_open": "block_fence_gate_palm_open",
        "cherry_fence_gate_wall": "block_fence_gate_palm_wall",
        "cherry_fence_gate_wall_open": "block_fence_gate_palm_wall_open",
        "cherry_door_bottom_left": "block_door_palm_bottom_left",
        "cherry_door_bottom_left_open": "block_door_palm_bottom_left_open",
        "cherry_door_bottom_right": "block_door_palm_bottom_right",
        "cherry_door_bottom_right_open": "block_door_palm_bottom_right_open",
        "cherry_door_top_left": "block_door_palm_top_left",
        "cherry_door_top_left_open": "block_door_palm_top_left_open",
        "cherry_door_top_right": "block_door_palm_top_right",
        "cherry_door_top_right_open": "block_door_palm_top_right_open",
        "cherry_trapdoor_bottom": "block_trapdoor_palm_bottom",
        "cherry_trapdoor_top": "block_trapdoor_palm_top",
        "cherry_trapdoor_open": "block_trapdoor_palm_open",
        "cherry_button": "block_button_palm",
        "cherry_button_pressed": "block_button_palm_pressed",
        "cherry_button_inventory": "block_button_palm_inventory",
        "cherry_pressure_plate": "block_pressure_plate_palm",
        "cherry_pressure_plate_down": "block_pressure_plate_palm_down",
    }
    tex_map = {
        "minecraft:block/cherry_planks": f"{MOD}:block/planks_palm",
        "minecraft:block/cherry_door_bottom": f"{MOD}:block/door_palm_bottom",
        "minecraft:block/cherry_door_top": f"{MOD}:block/door_palm_top",
        "minecraft:block/cherry_trapdoor": f"{MOD}:block/trapdoor_palm",
    }
    model_id_map = {f"minecraft:block/{src}": f"{MOD}:block/{dst}" for src, dst in model_renames.items()}

    for src, dst in model_renames.items():
        data = load(MC / "models/block" / f"{src}.json")
        data = rewrite(data, {**tex_map, **model_id_map})
        w(MAIN / "models/block" / f"{dst}.json", data)

    state_renames = {
        "cherry_stairs": "block_stairs_palm",
        "cherry_slab": "block_slab_palm",
        "cherry_fence": "block_fence_palm",
        "cherry_fence_gate": "block_fence_gate_palm",
        "cherry_door": "block_door_palm",
        "cherry_trapdoor": "block_trapdoor_palm",
        "cherry_button": "block_button_palm",
        "cherry_pressure_plate": "block_pressure_plate_palm",
    }
    for src, dst in state_renames.items():
        data = load(MC / "blockstates" / f"{src}.json")
        data = rewrite(data, {**model_id_map, "minecraft:block/cherry_planks": f"{MOD}:block/block_planks_palm"})
        w(MAIN / "blockstates" / f"{dst}.json", data)


def write_log_models():
    # existing palm log models stay in the same files; rewrite textures already updated
    w(MAIN / "models/block/block_log_palm.json", cube_column(f"{MOD}:block/log_palm_top", f"{MOD}:block/log_palm"))
    w(MAIN / "models/block/block_log_palm_horizontal.json", cube_column(f"{MOD}:block/log_palm_top", f"{MOD}:block/log_palm", True))
    w(MAIN / "blockstates/block_log_palm.json", log_blockstate("block_log_palm"))

    w(MAIN / "models/block/block_stripped_log_palm.json", cube_column(f"{MOD}:block/log_palm_stripped_top", f"{MOD}:block/log_palm_stripped"))
    w(MAIN / "models/block/block_stripped_log_palm_horizontal.json", cube_column(f"{MOD}:block/log_palm_stripped_top", f"{MOD}:block/log_palm_stripped", True))
    w(MAIN / "blockstates/block_stripped_log_palm.json", log_blockstate("block_stripped_log_palm"))

    # wood: bark (or stripped side) on all faces
    w(MAIN / "models/block/block_wood_palm.json", cube_column(f"{MOD}:block/log_palm", f"{MOD}:block/log_palm"))
    w(MAIN / "models/block/block_wood_palm_horizontal.json", cube_column(f"{MOD}:block/log_palm", f"{MOD}:block/log_palm", True))
    w(MAIN / "blockstates/block_wood_palm.json", log_blockstate("block_wood_palm"))

    w(MAIN / "models/block/block_stripped_wood_palm.json", cube_column(f"{MOD}:block/log_palm_stripped", f"{MOD}:block/log_palm_stripped"))
    w(MAIN / "models/block/block_stripped_wood_palm_horizontal.json", cube_column(f"{MOD}:block/log_palm_stripped", f"{MOD}:block/log_palm_stripped", True))
    w(MAIN / "blockstates/block_stripped_wood_palm.json", log_blockstate("block_stripped_wood_palm"))

    w(MAIN / "models/block/block_planks_palm.json", {
        "parent": "minecraft:block/cube_all",
        "textures": {"all": f"{MOD}:block/planks_palm"},
    })
    w(MAIN / "blockstates/block_planks_palm.json", {
        "variants": {"": {"model": f"{MOD}:block/block_planks_palm"}}
    })


def write_item_models():
    # block-parent item models in main (used by all eras)
    block_parents = {
        "block_log_palm": "block_log_palm",
        "block_stripped_log_palm": "block_stripped_log_palm",
        "block_wood_palm": "block_wood_palm",
        "block_stripped_wood_palm": "block_stripped_wood_palm",
        "block_planks_palm": "block_planks_palm",
        "block_stairs_palm": "block_stairs_palm",
        "block_slab_palm": "block_slab_palm",
        "block_fence_palm": "block_fence_palm_inventory",
        "block_fence_gate_palm": "block_fence_gate_palm",
        "block_trapdoor_palm": "block_trapdoor_palm_bottom",
        "block_button_palm": "block_button_palm_inventory",
        "block_pressure_plate_palm": "block_pressure_plate_palm",
    }
    for item, model in block_parents.items():
        w(MAIN / "models/item" / f"{item}.json", {"parent": f"{MOD}:block/{model}"})
    w(MAIN / "models/item/block_door_palm.json", {
        "parent": "minecraft:item/generated",
        "textures": {"layer0": f"{MOD}:item/door_palm"},
    })

    # 26.1+ item model definitions
    items = ERA / "assets/morefood/items"
    for name in WOOD_BLOCKS:
        w(items / f"{name}.json", {
            "model": {
                "type": "minecraft:model",
                "model": f"{MOD}:item/{name}" if name == "block_door_palm" else f"{MOD}:block/{block_parents.get(name, name) if name != 'block_door_palm' else 'item/block_door_palm'}",
            }
        })
    # fix door pointer (uses item model, not block)
    w(items / "block_door_palm.json", {
        "model": {"type": "minecraft:model", "model": f"{MOD}:item/block_door_palm"}
    })
    # fences/buttons already pointed at inventory models via block_parents used above — but
    # non-door items used f"{MOD}:block/{block_parents[name]}" which is correct.


# ---------------------------------------------------------------------------
# Recipes, loot, tags, advancements
# ---------------------------------------------------------------------------

def shaped(result, count, pattern, key, category, group=None):
    d = {
        "type": "minecraft:crafting_shaped",
        "category": category,
        "pattern": pattern,
        "key": key,
        "result": {"id": f"{MOD}:{result}", **({"count": count} if count != 1 else {})},
    }
    if group:
        d["group"] = group
    return d


def shapeless(result, count, ingredients, category, group=None):
    d = {
        "type": "minecraft:crafting_shapeless",
        "category": category,
        "ingredients": ingredients,
        "result": {"id": f"{MOD}:{result}", **({"count": count} if count != 1 else {})},
    }
    if group:
        d["group"] = group
    return d


def stonecutting(result, count, ingredient):
    return {
        "type": "minecraft:stonecutting",
        "ingredient": ingredient,
        "result": {"count": count, "id": f"{MOD}:{result}"},
    }


def write_recipes():
    rec = ERA / "data/morefood/recipe"
    planks = f"{MOD}:block_planks_palm"
    logs = "#morefood:palm_logs"
    w(rec / "block_planks_palm.json", shapeless("block_planks_palm", 4, [logs], "building", "planks"))
    w(rec / "block_wood_palm.json", shaped("block_wood_palm", 3, ["##", "##"], {"#": f"{MOD}:block_log_palm"}, "building", "bark"))
    w(rec / "block_stripped_wood_palm.json", shaped("block_stripped_wood_palm", 3, ["##", "##"], {"#": f"{MOD}:block_stripped_log_palm"}, "building", "bark"))
    w(rec / "block_stairs_palm.json", shaped("block_stairs_palm", 4, ["#  ", "## ", "###"], {"#": planks}, "building", "wooden_stairs"))
    w(rec / "block_slab_palm.json", shaped("block_slab_palm", 6, ["###"], {"#": planks}, "building", "wooden_slab"))
    w(rec / "block_fence_palm.json", shaped("block_fence_palm", 3, ["W#W", "W#W"], {"#": "minecraft:stick", "W": planks}, "misc", "wooden_fence"))
    w(rec / "block_fence_gate_palm.json", shaped("block_fence_gate_palm", 1, ["#W#", "#W#"], {"#": "minecraft:stick", "W": planks}, "redstone", "wooden_fence_gate"))
    w(rec / "block_door_palm.json", shaped("block_door_palm", 3, ["##", "##", "##"], {"#": planks}, "redstone", "wooden_door"))
    w(rec / "block_trapdoor_palm.json", shaped("block_trapdoor_palm", 2, ["###", "###"], {"#": planks}, "redstone", "wooden_trapdoor"))
    w(rec / "block_button_palm.json", shapeless("block_button_palm", 1, [planks], "redstone", "wooden_button"))
    w(rec / "block_pressure_plate_palm.json", shaped("block_pressure_plate_palm", 1, ["##"], {"#": planks}, "redstone", "wooden_pressure_plate"))
    # stonecutter QoL (vanilla has none for wood; stairs and slabs only)
    w(rec / "block_stairs_palm_from_planks_stonecutting.json", stonecutting("block_stairs_palm", 1, planks))
    w(rec / "block_slab_palm_from_planks_stonecutting.json", stonecutting("block_slab_palm", 2, planks))


def simple_loot(name: str):
    return {
        "type": "minecraft:block",
        "pools": [{
            "rolls": 1.0,
            "bonus_rolls": 0.0,
            "entries": [{
                "type": "minecraft:item",
                "name": f"{MOD}:{name}",
                "conditions": [{"condition": "minecraft:survives_explosion"}],
            }],
        }],
        "random_sequence": f"{MOD}:blocks/{name}",
    }


def slab_loot(name: str):
    return {
        "type": "minecraft:block",
        "pools": [{
            "bonus_rolls": 0.0,
            "entries": [{
                "type": "minecraft:item",
                "functions": [
                    {
                        "add": False,
                        "conditions": [{
                            "block": f"{MOD}:{name}",
                            "condition": "minecraft:block_state_property",
                            "properties": {"type": "double"},
                        }],
                        "count": 2.0,
                        "function": "minecraft:set_count",
                    },
                    {"function": "minecraft:explosion_decay"},
                ],
                "name": f"{MOD}:{name}",
            }],
            "rolls": 1.0,
        }],
        "random_sequence": f"{MOD}:blocks/{name}",
    }


def door_loot(name: str):
    return {
        "type": "minecraft:block",
        "pools": [{
            "bonus_rolls": 0.0,
            "conditions": [{"condition": "minecraft:survives_explosion"}],
            "entries": [{
                "type": "minecraft:item",
                "conditions": [{
                    "block": f"{MOD}:{name}",
                    "condition": "minecraft:block_state_property",
                    "properties": {"half": "lower"},
                }],
                "name": f"{MOD}:{name}",
            }],
            "rolls": 1.0,
        }],
        "random_sequence": f"{MOD}:blocks/{name}",
    }


def write_loot():
    loot = ERA / "data/morefood/loot_table/blocks"
    for name in WOOD_BLOCKS:
        if name == "block_slab_palm":
            w(loot / f"{name}.json", slab_loot(name))
        elif name == "block_door_palm":
            w(loot / f"{name}.json", door_loot(name))
        else:
            w(loot / f"{name}.json", simple_loot(name))


def tag_file(path: Path, values, replace=False):
    d = {"values": values}
    if replace:
        d["replace"] = True
    w(path, d)


def merge_tag(path: Path, extra: list[str]):
    if path.exists():
        d = load(path)
        vals = list(d.get("values", []))
        for v in extra:
            if v not in vals:
                vals.append(v)
        d["values"] = vals
        w(path, d)
    else:
        tag_file(path, extra)


def write_tags():
    # morefood palm_logs
    for kind in ("block", "item"):
        tag_file(ERA / f"data/morefood/tags/{kind}/palm_logs.json", [f"{MOD}:{n}" for n in PALM_LOGS])

    # minecraft logs: replace the bare log with the family tag
    for kind in ("block", "item"):
        for name, values in {
            "logs": ["#morefood:palm_logs"],
            "logs_that_burn": ["#morefood:palm_logs"],
        }.items():
            tag_file(ERA / f"data/minecraft/tags/{kind}/{name}.json", values)

    merge_tag(ERA / "data/minecraft/tags/block/mineable/axe.json", [f"{MOD}:{n}" for n in WOOD_BLOCKS if n != "block_log_palm"])
    # log already listed

    def both(tag, values):
        for kind in ("block", "item"):
            merge_tag(ERA / f"data/minecraft/tags/{kind}/{tag}.json", values)

    both("planks", [f"{MOD}:block_planks_palm"])
    both("wooden_stairs", [f"{MOD}:block_stairs_palm"])
    both("wooden_slabs", [f"{MOD}:block_slab_palm"])
    both("wooden_fences", [f"{MOD}:block_fence_palm"])
    both("fence_gates", [f"{MOD}:block_fence_gate_palm"])
    both("wooden_doors", [f"{MOD}:block_door_palm"])
    both("wooden_trapdoors", [f"{MOD}:block_trapdoor_palm"])
    both("wooden_buttons", [f"{MOD}:block_button_palm"])
    both("wooden_pressure_plates", [f"{MOD}:block_pressure_plate_palm"])
    merge_tag(ERA / "data/minecraft/tags/block/overworld_natural_logs.json", [f"{MOD}:block_log_palm"])

    # NeoForge axe stripping (1.21.1 and 26.1 / 26.2). Harmless if ignored on 26.3.
    w(ERA / "data/neoforge/data_maps/block/strippables.json", {
        "values": {
            f"{MOD}:block_log_palm": {"stripped_block": f"{MOD}:block_stripped_log_palm"},
            f"{MOD}:block_wood_palm": {"stripped_block": f"{MOD}:block_stripped_wood_palm"},
        }
    })


def recipe_adv(name: str, unlock_item, recipe_id=None):
    rid = recipe_id or name
    criterion_name = "has_item"
    item_cond = {"items": unlock_item}
    return {
        "parent": "minecraft:recipes/root",
        "criteria": {
            criterion_name: {
                "conditions": {"items": [item_cond]},
                "trigger": "minecraft:inventory_changed",
            },
            "has_the_recipe": {
                "conditions": {"recipe": f"{MOD}:{rid}"},
                "trigger": "minecraft:recipe_unlocked",
            },
        },
        "requirements": [[ "has_the_recipe", criterion_name ]],
        "rewards": {"recipes": [f"{MOD}:{rid}"]},
    }


def write_advancements():
    adv = ERA / "data/morefood/advancement/recipes/building"
    red = ERA / "data/morefood/advancement/recipes/redstone"
    pairs = [
        (adv, "block_planks_palm", "#morefood:palm_logs"),
        (adv, "block_wood_palm", f"{MOD}:block_log_palm"),
        (adv, "block_stripped_wood_palm", f"{MOD}:block_stripped_log_palm"),
        (adv, "block_stairs_palm", f"{MOD}:block_planks_palm"),
        (adv, "block_slab_palm", f"{MOD}:block_planks_palm"),
        (adv, "block_fence_palm", f"{MOD}:block_planks_palm"),
        (adv, "block_stairs_palm_from_planks_stonecutting", f"{MOD}:block_planks_palm"),
        (adv, "block_slab_palm_from_planks_stonecutting", f"{MOD}:block_planks_palm"),
        (red, "block_fence_gate_palm", f"{MOD}:block_planks_palm"),
        (red, "block_door_palm", f"{MOD}:block_planks_palm"),
        (red, "block_trapdoor_palm", f"{MOD}:block_planks_palm"),
        (red, "block_button_palm", f"{MOD}:block_planks_palm"),
        (red, "block_pressure_plate_palm", f"{MOD}:block_planks_palm"),
    ]
    for folder, name, unlock in pairs:
        w(folder / f"{name}.json", recipe_adv(name, unlock))


def main():
    write_textures()
    write_log_models()
    copy_cherry_set()
    write_item_models()
    write_recipes()
    write_loot()
    write_tags()
    write_advancements()
    print("palm wood assets written")


if __name__ == "__main__":
    main()
