#!/usr/bin/env python3
# milk churn: placeholder textures, models, blockstates, data. run from repo root, then gen_cookbook.py and gen_eras.py
import json, os, random
from gen_machines import RES, ERA, TEX, MOD, w, new, rect, outline, noise, shade, hexc, OAK, IRON

MILK = hexc(0xF6F6F2)


def staves(seed):
    im = new(); r = random.Random(seed); px = im.load()
    for x in range(16):
        base = shade(OAK[1], 1 + r.uniform(-.06, .06))
        for y in range(16):
            px[x, y] = shade(base, 1 + r.uniform(-.03, .03))
    for x in (0, 4, 8, 12):
        rect(im, x, 0, x, 15, OAK[3])
    return im


def side():
    im = staves(121)
    for y in (2, 12):
        rect(im, 0, y, 15, y, IRON[2]); rect(im, 0, y + 1, 15, y + 1, IRON[3])
    return im


def top():
    im = staves(122)
    outline(im, 0, 0, 15, 15, IRON[3])
    return im


def lid():
    im = new(); noise(im, 0, 0, 15, 15, IRON[1], .04, 123)
    outline(im, 0, 0, 15, 15, IRON[3])
    return im


def handle():
    im = new(); noise(im, 0, 0, 15, 15, OAK[3], .05, 124)
    return im


def window():
    im = new(); noise(im, 0, 0, 15, 15, hexc(0x2B3138), .05, 125)
    return im


def milk():
    im = new(); noise(im, 0, 0, 15, 15, MILK, .015, 126)
    return im


def item_icon():
    im = new()
    rect(im, 4, 1, 11, 1, IRON[2])
    rect(im, 7, 0, 8, 0, OAK[3])
    rect(im, 5, 2, 10, 3, IRON[1])
    rect(im, 3, 4, 12, 15, OAK[1])
    for x in (3, 6, 9, 12):
        rect(im, x, 4, x, 15, OAK[3])
    for y in (6, 13):
        rect(im, 3, y, 12, y, IRON[3])
    rect(im, 7, 7, 8, 12, hexc(0x2B3138)); rect(im, 7, 10, 8, 12, MILK)
    outline(im, 3, 4, 12, 15, OAK[4])
    return im


TEXTURES = {"block/milk_churn_side": side, "block/milk_churn_top": top, "block/milk_churn_lid": lid,
            "block/milk_churn_handle": handle, "block/milk_churn_window": window, "block/milk_churn_milk": milk,
            "item/block_milk_churn": item_icon}

WINDOW = (14, 8)  # pixels of window in the lower and upper half


def box(a, b, tex, faces=("north", "south", "east", "west", "up", "down")):
    f = {}
    for d in faces:
        if d in ("up", "down"):
            uv = [a[0], a[2], b[0], b[2]]
        elif d in ("north", "south"):
            uv = [a[0], 16 - b[1], b[0], 16 - a[1]]
        else:
            uv = [a[2], 16 - b[1], b[2], 16 - a[1]]
        f[d] = {"uv": uv, "texture": tex}
    return {"from": a, "to": b, "faces": f}


def model(half, level):
    t = {"particle": f"{MOD}:block/milk_churn_side", "side": f"{MOD}:block/milk_churn_side", "top": f"{MOD}:block/milk_churn_top",
         "lid": f"{MOD}:block/milk_churn_lid", "handle": f"{MOD}:block/milk_churn_handle",
         "window": f"{MOD}:block/milk_churn_window", "milk": f"{MOD}:block/milk_churn_milk"}
    filled = round(sum(WINDOW) * level / 8)
    if half == "lower":
        els = [box([3, 0, 3], [13, 16, 13], "#side", ("north", "south", "east", "west")),
               box([3, 0, 3], [13, 16, 13], "#top", ("down",)),
               box([7, 2, 2.95], [9, 16, 3], "#window", ("north",))]
        h = min(WINDOW[0], filled)
        if h: els.append(box([7, 2, 2.9], [9, 2 + h, 2.95], "#milk", ("north",)))
    else:
        els = [box([3, 0, 3], [13, 10, 13], "#side", ("north", "south", "east", "west")),
               box([3, 10, 3], [13, 10, 13], "#top", ("up",)),
               box([5, 10, 5], [11, 13, 11], "#lid"),
               box([4, 13, 4], [12, 14, 12], "#lid"),
               box([7.5, 14, 7.5], [8.5, 16, 8.5], "#handle"),
               box([7, 0, 2.95], [9, 8, 3], "#window", ("north",))]
        h = max(0, filled - WINDOW[0])
        if h: els.append(box([7, 0, 2.9], [9, h, 2.95], "#milk", ("north",)))
    return {"parent": "minecraft:block/block", "textures": t, "elements": els}


def assets():
    for name, fn in TEXTURES.items():
        fn().save(f"{TEX}/{name}.png")
    seen = {}
    variants = {}
    rot = {"north": 0, "east": 90, "south": 180, "west": 270}
    for half in ("lower", "upper"):
        for level in range(9):
            m = model(half, level)
            key = json.dumps(m)
            if key not in seen:
                seen[key] = f"{MOD}:block/milk_churn_{half}_{level}"
                w(f"{RES}/models/block/milk_churn_{half}_{level}.json", m)
            for f, y in rot.items():
                v = {"model": seen[key]}
                if y: v["y"] = y
                variants[f"facing={f},half={half},milk={level}"] = v
    w(f"{RES}/blockstates/block_milk_churn.json", {"variants": variants})
    w(f"{RES}/models/item/block_milk_churn.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{MOD}:item/block_milk_churn"}})
    w(f"{ERA}/assets/{MOD}/items/block_milk_churn.json", {"model": {"type": "minecraft:model", "model": f"{MOD}:item/block_milk_churn"}})


def data():
    w(f"{ERA}/data/{MOD}/loot_table/blocks/block_milk_churn.json", {
        "type": "minecraft:block", "random_sequence": f"{MOD}:blocks/block_milk_churn",
        "pools": [{"rolls": 1, "bonus_rolls": 0, "entries": [{"type": "minecraft:item", "name": f"{MOD}:block_milk_churn",
                                                              # keeps the milk; gen_eras turns this into copy_nbt for 1.20.1
                                                              "functions": [{"function": "minecraft:copy_components", "source": "block_entity", "include": [f"{MOD}:milk"]}]}],
                   "conditions": [{"condition": "minecraft:block_state_property", "block": f"{MOD}:block_milk_churn", "properties": {"half": "lower"}},
                                  {"condition": "minecraft:survives_explosion"}]}]})
    w(f"{ERA}/data/{MOD}/recipe/block_milk_churn.json", {
        "type": "minecraft:crafting_shaped", "category": "misc", "pattern": ["PIP", "P P", "PIP"],
        "key": {"P": "#minecraft:planks", "I": "minecraft:iron_ingot"}, "result": {"id": f"{MOD}:block_milk_churn"}})
    p = f"{ERA}/data/minecraft/tags/block/mineable/axe.json"
    tag = json.load(open(p))
    if f"{MOD}:block_milk_churn" not in tag["values"]:
        tag["values"].append(f"{MOD}:block_milk_churn")
    w(p, tag)


def lang():
    p = f"{RES}/lang/en_us.json"
    text = open(p).read()
    add = {"block.morefood.block_milk_churn": "Milk Churn", "message.morefood.churn.milk": "Milk: %s / %s mB"}
    lines = text.split("\n")
    at = max(i for i, l in enumerate(lines) if l.strip().startswith('"block.morefood.block_kitchen_sink"'))
    new_lines = [f'  "{k}": {json.dumps(v)},' for k, v in add.items() if f'"{k}"' not in text]
    lines[at + 1:at + 1] = new_lines
    open(p, "w").write("\n".join(lines))
    json.load(open(p))


if __name__ == "__main__":
    assets(); data(); lang()
