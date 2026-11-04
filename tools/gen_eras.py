#!/usr/bin/env python3
# rebuilds src/eras/26.3, 1.21.1 and 1.20.1 from src/eras/26.1, run from repo root
import copy, itertools, json, os, re, shutil

MOD = "morefood"
SRC = "src/eras/26.1"
PLURAL = {"recipe": "recipes", "advancement": "advancements", "loot_table": "loot_tables",
          "tags/item": "tags/items", "tags/block": "tags/blocks"}
# 1.20.1 has no c:nuggets/iron on both loaders
OLD_TAGS = {"c:nuggets/iron": f"{MOD}:iron_nuggets"}
ERAS = {
    "1.21.1": {},
    "1.20.1": {"plural": True},
}
ANGLES = [-45, -22.5, 0, 22.5, 45]
# composite order in the 26.1 pizza item models, the legacy client picks one layered model per combination
TOPPINGS = []


def load(path):
    with open(path) as f:
        return json.load(f)


def w(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")


def tagname(era, t):
    return OLD_TAGS.get(t, t) if ERAS[era].get("plural") else t


def ing(era, x):
    if isinstance(x, list):
        return [ing(era, i) for i in x]
    if isinstance(x, dict):
        return x
    return {"tag": tagname(era, x[1:])} if x.startswith("#") else {"item": x}


def item_pred(era, p):
    p = dict(p)
    items = p.pop("items", None)
    if items is None or not ERAS[era].get("plural"):
        if items is not None: p["items"] = items
        return p
    if isinstance(items, str) and items.startswith("#"):
        p["tag"] = tagname(era, items[1:])
    else:
        p["items"] = items if isinstance(items, list) else [items]
    return p


def nbt_of(era, components):
    out = {}
    for k, v in components.items():
        if k == "minecraft:item_model":
            continue
        if k == "minecraft:use_remainder":
            out[f"{MOD}:vessel"] = v["id"]
        else:
            out[k] = v
    return out


def result(era, r, smelting=False):
    comps = nbt_of(era, r.get("components", {}))
    if not ERAS[era].get("plural"):
        out = {"id": r["id"]}
        if "count" in r: out["count"] = r["count"]
        if comps: out["components"] = comps
        return out
    if smelting:
        return r["id"]
    out = {"item": r["id"]}
    if "count" in r: out["count"] = r["count"]
    if comps: out["nbt"] = comps
    return out


def conditions(era, d):
    if "neoforge:conditions" in d and ERAS[era].get("plural"):
        conds = d.pop("neoforge:conditions")
        d["conditions"] = [{"type": "forge:mod_loaded", "modid": c["modid"]} for c in conds]
    return d


def reorder(d):
    # keep the conditions on top like the 26.1 files
    keys = [k for k in ("fabric:load_conditions", "neoforge:conditions", "conditions") if k in d]
    return {**{k: d[k] for k in keys}, **{k: v for k, v in d.items() if k not in keys}}


def recipe(era, d):
    d = copy.deepcopy(d)
    t = d["type"]
    if t == "minecraft:crafting_shaped":
        d["key"] = {k: ing(era, v) for k, v in d["key"].items()}
    elif t == "minecraft:crafting_shapeless":
        d["ingredients"] = [ing(era, i) for i in d["ingredients"]]
    elif t in ("minecraft:smelting", f"{MOD}:baking", f"{MOD}:frying"):
        d["ingredient"] = ing(era, d["ingredient"])
    elif t == f"{MOD}:boiling":
        d["ingredients"] = [ing(era, i) for i in d["ingredients"]]
        for o in d.get("other_vessels", []):
            o["result"] = result(era, o["result"])
    elif t == f"{MOD}:assembly":
        for k in ("base", "lid"):
            if k in d: d[k] = ing(era, d[k])
        d["fillings"] = [ing(era, i) for i in d.get("fillings", [])]
        if "extras" in d: d["extras"]["ingredient"] = ing(era, d["extras"]["ingredient"])
    if "result" in d:
        d["result"] = result(era, d["result"], smelting=t == "minecraft:smelting")
    if ERAS[era].get("plural") and t == "minecraft:crafting_shapeless" and d["result"].get("item") == "simplebooks:guide":
        # vanilla 1.20.1 crafting drops nbt on Fabric, Simple Books has its own type for guides
        d["type"] = "simplebooks:guide"
        d["book"] = d.pop("result")["nbt"]["simplebooks:book"]
    return reorder(conditions(era, d))


def advancement(era, d):
    d = copy.deepcopy(d)
    for c in d.get("criteria", {}).values():
        cond = c.get("conditions", {})
        if c["trigger"] == "minecraft:inventory_changed":
            cond["items"] = [item_pred(era, p) for p in cond.get("items", [])]
    return reorder(conditions(era, d))


def loot_fix(era, node):
    if isinstance(node, dict):
        if node.get("condition") == "minecraft:match_tool" and "predicate" in node:
            node["predicate"] = item_pred(era, node["predicate"])
        if node.get("function") == "minecraft:set_components":
            comps = nbt_of(era, node["components"])
            if ERAS[era].get("plural"):
                node.clear()
                node.update({"function": "minecraft:set_nbt", "tag": json.dumps(comps, separators=(",", ":"))})
            else:
                node["components"] = comps
        for v in node.values():
            loot_fix(era, v)
    elif isinstance(node, list):
        for v in node:
            loot_fix(era, v)


def loot(era, d, name):
    d = copy.deepcopy(d)
    loot_fix(era, d)
    if ERAS[era].get("plural"):
        for pool in d.get("pools", []):
            for e in pool["entries"]:
                if e.get("type") == "minecraft:item" and e.get("name") == "simplebooks:guide":
                    # an optional tag keeps the table loading when Simple Books is missing
                    e["type"] = "minecraft:tag"
                    e["name"] = f"{MOD}:{name}_book"
                    e["expand"] = False
    return reorder(conditions(era, d))


def tag(era, d):
    d = copy.deepcopy(d)
    vals = []
    for v in d["values"]:
        if isinstance(v, str) and v.startswith("#"):
            v = "#" + tagname(era, v[1:])
        vals.append(v)
    d["values"] = vals
    return d


def snap(node):
    if isinstance(node, dict):
        if "angle" in node:
            node["angle"] = min(ANGLES, key=lambda a: (abs(a - node["angle"]), -abs(a)))
        for v in node.values():
            snap(v)
    elif isinstance(node, list):
        for v in node:
            snap(v)


def swap_tags(era, node):
    if isinstance(node, dict):
        return {k: swap_tags(era, v) for k, v in node.items()}
    if isinstance(node, list):
        return [swap_tags(era, v) for v in node]
    if isinstance(node, str) and node.startswith("#"):
        return "#" + tagname(era, node[1:])
    return node


def pizza_overrides(era, A, name):
    # one layered model per topping combination, picked by the morefood:toppings item property
    combos = [c for n in (1, 2, 3) for c in itertools.combinations(range(len(TOPPINGS)), n)]
    base = load(f"{SRC}/assets/{MOD}/models/item/{name}.json")
    overrides = []
    for i, combo in enumerate(combos, 1):
        tex = {"layer0": base["textures"]["layer0"]}
        for layer, t in enumerate(combo, 1):
            tex[f"layer{layer}"] = TOPPINGS[t][1]
        model = f"{MOD}:item/pizza/{name}_{i}"
        w(f"{A}/models/item/pizza/{name}_{i}.json", {"parent": "minecraft:item/generated", "textures": tex})
        overrides.append({"predicate": {f"{MOD}:toppings": i}, "model": model})
    w(f"{A}/models/item/{name}.json", {**base, "overrides": overrides})


def read_toppings():
    d = load(f"{SRC}/assets/{MOD}/items/item_pizza_cooked.json")
    for m in d["model"]["models"][1:]:
        model = m["on_true"]["model"].split(":")[1]
        tex = load(f"src/main/resources/assets/{MOD}/models/{model}.json")["textures"]["layer0"]
        TOPPINGS.append((m["topping"], tex))


def era_build(era):
    e = ERAS[era]
    out = f"src/eras/{era}"
    shutil.rmtree(out, ignore_errors=True)
    for root, _, files in os.walk(SRC):
        for fn in files:
            src = os.path.join(root, fn)
            rel = os.path.relpath(src, SRC)
            parts = rel.split(os.sep)
            if parts[0] == "assets" and parts[2] == "items":
                continue
            if rel == "pack.mcmeta":
                os.makedirs(out, exist_ok=True)
                with open(f"{out}/pack.mcmeta", "w") as f:
                    f.write('{\n    "pack": {\n        "description": "${name}",\n        "pack_format": ${pack_format}\n    }\n}\n')
                continue
            d = load(src)
            if parts[0] == "data":
                ns, kind = parts[1], parts[2]
                if kind == "tags":
                    kind = f"tags/{parts[3]}"
                    tail = parts[4:]
                else:
                    tail = parts[3:]
                name = "/".join(tail)[:-5]
                if kind == "recipe": d = recipe(era, d)
                elif kind == "advancement": d = advancement(era, d)
                elif kind == "loot_table": d = loot(era, d, name)
                elif kind.startswith("tags/"): d = tag(era, d)
                if e.get("plural"): kind = PLURAL.get(kind, kind)
                w(os.path.join(out, "data", ns, kind, *tail), d)
                continue
            if parts[2] == "models":
                snap(d)
            elif parts[2] == "simplebooks":
                d = swap_tags(era, d)
            w(os.path.join(out, rel), d)
    A = f"{out}/assets/{MOD}"
    for block in ("block_stove", "block_assembly_board"):
        w(f"{A}/models/item/{block}.json", {"parent": f"{MOD}:block/{block}"})
    peel = load(f"{SRC}/assets/{MOD}/models/item/item_baking_peel.json")
    peel["overrides"] = [{"predicate": {f"{MOD}:loaded": 1}, "model": f"{MOD}:item/item_baking_peel_loaded"}]
    w(f"{A}/models/item/item_baking_peel.json", peel)
    for name in ("item_pizza_cooked", "item_pizza_uncooked"):
        pizza_overrides(era, A, name)
    if e.get("plural"):
        D = f"{out}/data/{MOD}/tags/items"
        w(f"{D}/iron_nuggets.json", {"values": ["minecraft:iron_nugget",
            {"id": "#forge:nuggets/iron", "required": False}, {"id": "#c:iron_nuggets", "required": False}]})
        for name in ("cookbook",):
            w(f"{D}/{name}_book.json", {"values": [{"id": "simplebooks:guide", "required": False}]})


def num(v):
    return int(v) if isinstance(v, float) and v.is_integer() else v


def one_or_all(conds):
    conds = [cond_263(c) for c in conds]
    return conds[0] if len(conds) == 1 else {"type": "minecraft:all_of", "terms": conds}


def cond_263(c):
    c = {("type" if k == "condition" else k): v for k, v in c.items()}
    if c["type"] == "minecraft:block_state_property":
        c = {"type": "minecraft:match_block", "blocks": c["block"], **({"state": c["properties"]} if "properties" in c else {})}
    if "term" in c:
        c["term"] = cond_263(c["term"])
    return c


def loot_263(node):
    # 26.3 loot uses one condition and modifier per node, typed by "type"
    if isinstance(node, list):
        return [loot_263(v) for v in node]
    if not isinstance(node, dict):
        return node
    out = {}
    for k, v in node.items():
        if k in ("fabric:load_conditions", "neoforge:conditions"):
            out[k] = v
        elif k == "conditions":
            out["condition"] = one_or_all(v)
        elif k == "functions":
            mods = [loot_263({("type" if a == "function" else a): b for a, b in f.items()}) for f in v]
            out["modifier"] = mods[0] if len(mods) == 1 else mods
        elif k in ("rolls", "count"):
            out[k] = num(v) if not isinstance(v, dict) else loot_263(v)
        elif k == "bonus_rolls" and v == 0:
            continue
        else:
            out[k] = loot_263(v)
    return out


def modern_build(era):
    # same as 26.1 apart from the data formats 26.3 changed
    out = f"src/eras/{era}"
    shutil.rmtree(out, ignore_errors=True)
    shutil.copytree(SRC, out)
    for root, _, files in os.walk(f"{out}/data"):
        for fn in files:
            path = os.path.join(root, fn)
            parts = os.path.relpath(path, f"{out}/data").split(os.sep)
            if parts[1] == "loot_table":
                w(path, loot_263(load(path)))
            elif parts[1] == "advancement":
                d = load(path)
                for c in d.get("criteria", {}).values():
                    if c["trigger"] == "minecraft:recipe_unlocked" and "recipe" in c.get("conditions", {}):
                        c["conditions"]["recipes"] = c["conditions"].pop("recipe")
                w(path, d)


read_toppings()
for era in ERAS:
    era_build(era)
modern_build("26.3")
