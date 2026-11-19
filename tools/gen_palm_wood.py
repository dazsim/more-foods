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



# 5 warm red-brown shades, low contrast. Recolour vanilla layouts so planks
# and doors read like oak, without the busy checkered grain.
PALETTE = [
    (198, 120, 74),  # highlight
    (182, 106, 64),  # main
    (166,  94, 56),  # mid
    (132,  72, 44),  # dark fibre / seam mid
    (102,  54, 34),  # seam dark
]
STRIPPED_PALETTE = [
    (202, 128, 80),
    (190, 116, 72),
    (178, 104, 64),
    (166,  94, 56),
    (150,  82, 48),
]
MC_TEX = Path("/workspace/mods/mc261src/assets/minecraft/textures")

# Oak plank colours, light to dark, merged onto the 5-shade palm palette.
OAK_PLANK_MAP = {
    (194, 157, 98): PALETTE[0],
    (184, 148, 95): PALETTE[1],
    (175, 143, 85): PALETTE[2],
    (159, 132, 77): PALETTE[2],
    (150, 116, 65): PALETTE[3],
    (126,  98, 55): PALETTE[3],
    (103,  80, 44): PALETTE[4],
}


def _lum(c):
    return 0.299 * c[0] + 0.587 * c[1] + 0.114 * c[2]


def _is_hardware(c):
    r, g, b = c[:3]
    mx, mn = max(r, g, b), min(r, g, b)
    return mx <= 160 and (mx - mn) <= 24 and not (r == 0 and g == 0 and b == 0)


def _is_black(c):
    return c[0] <= 8 and c[1] <= 8 and c[2] <= 8


def _map_by_luminance(src_colors, palette):
    woods = [c for c in src_colors if not _is_black(c) and not _is_hardware(c)]
    mapping = {}
    if not woods:
        return mapping
    lo, hi = min(_lum(c) for c in woods), max(_lum(c) for c in woods)
    for c in src_colors:
        if _is_black(c):
            mapping[c] = (0, 0, 0)
        elif _is_hardware(c):
            mapping[c] = c[:3]
        else:
            t = 0.5 if hi == lo else (_lum(c) - lo) / (hi - lo)
            idx = max(0, min(len(palette) - 1, int(round((1.0 - t) * (len(palette) - 1)))))
            mapping[c] = palette[idx]
    return mapping


def recolor(im: Image.Image, mapping=None, palette=None) -> Image.Image:
    im = im.convert("RGBA")
    palette = palette or PALETTE
    out = Image.new("RGBA", im.size, (0, 0, 0, 0))
    colors = {im.getpixel((x, y))[:3] for y in range(im.size[1]) for x in range(im.size[0])}
    if mapping is None:
        mapping = _map_by_luminance(colors, palette)
    for y in range(im.size[1]):
        for x in range(im.size[0]):
            px = im.getpixel((x, y))
            rgb, a = px[:3], px[3] if len(px) == 4 else 255
            if a == 0:
                out.putpixel((x, y), (0, 0, 0, 0))
                continue
            m = mapping.get(rgb)
            if m is None:
                if _is_black(rgb):
                    m = (0, 0, 0)
                elif _is_hardware(rgb):
                    m = rgb
                else:
                    m = palette[2]
            out.putpixel((x, y), tuple(m) + (a,))
    return out


def make_planks(seed=50) -> Image.Image:
    # exact vanilla oak board layout and seams, in the calmer palm palette
    return recolor(Image.open(MC_TEX / "block/oak_planks.png"), OAK_PLANK_MAP, PALETTE)


def make_stripped_side(seed=48) -> Image.Image:
    # jungle's calm vertical fibre bands, shifted to warm red-brown
    return recolor(Image.open(MC_TEX / "block/stripped_jungle_log.png"), palette=STRIPPED_PALETTE)


def make_stripped_top(seed=49) -> Image.Image:
    """Cut fibre-bundle end: low-contrast clusters, no growth rings."""
    rng = random.Random(seed)
    sp = STRIPPED_PALETTE
    im = Image.new("RGBA", (16, 16), sp[2] + (255,))
    for cy in range(0, 16, 2):
        for cx in range(0, 16, 2):
            shade = sp[rng.choice([1, 2, 2, 2, 3])]
            idx = sp.index(shade)
            for dy in range(2):
                for dx in range(2):
                    x, y = cx + dx, cy + dy
                    if x < 16 and y < 16:
                        c = shade if rng.random() > 0.18 else sp[max(0, min(4, idx + rng.choice([-1, 0, 0, 1])))]
                        im.putpixel((x, y), c + (255,))
    for i in range(16):
        for x, y in ((0, i), (15, i), (i, 0), (i, 15)):
            im.putpixel((x, y), sp[4] + (255,))
        if rng.random() < 0.4:
            im.putpixel((1, i), sp[3] + (255,))
        if rng.random() < 0.4:
            im.putpixel((14, i), sp[3] + (255,))
        if rng.random() < 0.4:
            im.putpixel((i, 1), sp[3] + (255,))
        if rng.random() < 0.4:
            im.putpixel((i, 14), sp[3] + (255,))
    return im


def make_door_bottom(planks: Image.Image, seed=51) -> Image.Image:
    return recolor(Image.open(MC_TEX / "block/oak_door_bottom.png"), palette=PALETTE)


def make_door_top(planks: Image.Image, seed=52) -> Image.Image:
    return recolor(Image.open(MC_TEX / "block/oak_door_top.png"), palette=PALETTE)


def make_trapdoor(planks: Image.Image, seed=53) -> Image.Image:
    return recolor(Image.open(MC_TEX / "block/oak_trapdoor.png"), palette=PALETTE)


def make_door_item(top: Image.Image, bottom: Image.Image) -> Image.Image:
    return recolor(Image.open(MC_TEX / "item/oak_door.png"), palette=PALETTE)


def write_textures():
    tex = MAIN / "textures/block"
    item = MAIN / "textures/item"
    item.mkdir(parents=True, exist_ok=True)
    # Hairy bark is left alone. Darren liked it, and the generator's older bark
    # pass is not a match for the committed fibres.
    make_stripped_side().save(tex / "log_palm_stripped.png")
    make_stripped_top().save(tex / "log_palm_stripped_top.png")
    planks = make_planks()
    planks.save(tex / "planks_palm.png")
    door_bottom = make_door_bottom(planks)
    door_top = make_door_top(planks)
    door_bottom.save(tex / "door_palm_bottom.png")
    door_top.save(tex / "door_palm_top.png")
    make_trapdoor(planks).save(tex / "trapdoor_palm.png")
    make_door_item(door_top, door_bottom).save(item / "door_palm.png")
    print("wrote calmer plank, stripped, door and trapdoor textures")


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
