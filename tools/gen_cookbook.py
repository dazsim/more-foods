#!/usr/bin/env python3
# adds the vegan, tree and kitchen machine pages to the 26.1 cookbook and gates every page on the crops it needs
# run from the repo root, then run tools/gen_eras.py
import glob, json, os

MOD = "morefood"
SRC = "src/eras/26.1"
PAGES = f"{SRC}/assets/{MOD}/simplebooks/books/cookbook/pages"
BOOK = f"{SRC}/assets/{MOD}/simplebooks/books/cookbook.json"
TAGS = f"{SRC}/data/{MOD}/tags/item/unlocks"
IMG = "morefood:textures/simplebooks/cookbook/"

lang = {}
for f in ("/workspace/ref/261/assets/minecraft/lang/en_us.json", f"src/main/resources/assets/{MOD}/lang/en_us.json"):
    if os.path.exists(f):
        lang.update(json.load(open(f)))


def load(p):
    return json.load(open(p))


def w(p, d):
    os.makedirs(os.path.dirname(p), exist_ok=True)
    with open(p, "w") as f:
        json.dump(d, f, indent=2)
        f.write("\n")


def name(i):
    if isinstance(i, list):
        a, b = name(i[0]), name(i[1])
        if len(i) == 2 and b.endswith(" " + a):
            return "Any " + a
        return a + " or " + b if len(i) == 2 else a + " or other"
    if i.startswith("#"):
        return i[1:].split(":")[1].split("/")[-1].replace("_", " ").title()
    ns, p = i.split(":")
    for k in ("item", "block"):
        if f"{k}.{ns}.{p}" in lang:
            return lang[f"{k}.{ns}.{p}"]
    return p.replace("item_", "").replace("block_", "").replace("_", " ").title()


# crops and trees: unlock tag id -> (produce, entry items, kind)
CROPS = {}
for f in sorted(glob.glob(f"{SRC}/data/{MOD}/recipe/item_seed_*.json")):
    r = load(f)
    produce, seed = r["ingredients"][0], r["result"]["id"]
    CROPS[seed.split("item_seed_")[1]] = (produce, [seed, produce], "crop")
TREES = {"avocado": "in jungles", "coconut": "on beaches", "jackfruit": "in jungles", "olive": "in savannas"}
for t in TREES:
    CROPS[t] = (f"{MOD}:item_{t}", [f"{MOD}:item_{t}", f"{MOD}:block_sapling_{t}"], "tree")
PRODUCE = {v[0]: k for k, v in CROPS.items()}

RANK = {"morefood:assembly": 0, "morefood:boiling": 1, "morefood:frying": 1, "morefood:baking": 1,
        "minecraft:crafting_shapeless": 2, "minecraft:crafting_shaped": 2, "minecraft:smelting": 3,
        "morefood:grinding": 4, "morefood:pressing": 4, "morefood:washing": 4}
recipes = {}
for f in sorted(glob.glob(f"{SRC}/data/{MOD}/recipe/**/*.json", recursive=True)):
    r = load(f)
    if "result" not in r or not isinstance(r["result"], dict) or "id" not in r["result"]:
        continue
    out = r["result"]["id"]
    flat = json.dumps(r)
    if "item_pot_" in flat or "_frying_pan" in out or "item_seed_" in out:
        continue
    recipes.setdefault(out, []).append(r)
for k in recipes:
    recipes[k].sort(key=lambda r: RANK.get(r["type"], 9))


def ing(x):
    if isinstance(x, dict):
        if x.get("water_bottle"):
            return "minecraft:potion"
        if "ingredient" in x:
            return ing(x["ingredient"])
        x = x.get("item") or ("#" + x["tag"])
    if isinstance(x, list):
        return x[0] if len(x) == 1 else x
    return x


def inputs(r):
    t = r["type"]
    if t == "morefood:assembly":
        l = [r["base"]] + r.get("fillings", []) + ([r["lid"]] if "lid" in r else [])
    elif "ingredients" in r:
        l = []
        for x in r["ingredients"]:
            l += [x] * (x.get("count", 1) if isinstance(x, dict) and "ingredient" in x else 1)
    elif t == "minecraft:crafting_shaped":
        l = [r["key"][c] for row in r["pattern"] for c in row if c != " "]
    else:
        l = [r["ingredient"]]
    return [ing(x) for x in l]


# crops an item needs: union over its recipe, nothing if any recipe for it needs no crop
_crops = {}


def crops_of(i, stack=()):
    if isinstance(i, list):
        sets = [crops_of(x, stack) for x in i]
        return set() if any(not s for s in sets) else set().union(*sets)
    if not isinstance(i, str) or i.startswith("#"):
        return set()
    if i in PRODUCE:
        return {PRODUCE[i]}
    for k, (_, entry, _) in CROPS.items():
        if i in entry:
            return {k}
    if i in _crops:
        return _crops[i]
    if i not in recipes or i in stack:
        return set()
    best = None
    for r in recipes[i]:
        s = set()
        for x in inputs(r):
            s |= crops_of(x, stack + (i,))
        if not s:
            best = set()
            break
        best = s if best is None else best | s
    _crops[i] = best or set()
    return _crops[i]


TOOLS = ["minecraft:crafting_table", "morefood:item_pestle_mortar", "morefood:item_mixing_bowl", "morefood:item_mincer",
         "morefood:item_juicer", "morefood:item_knife", "morefood:item_pot", "morefood:item_frying_pan",
         "morefood:block_stove", "minecraft:furnace", "morefood:block_assembly_board", "morefood:item_baking_peel",
         "morefood:block_grinder", "morefood:block_press", "morefood:block_kitchen_sink"]
BUCKETS = {"minecraft:water_bucket", "minecraft:milk_bucket"}
STOP = {"morefood:item_vegetable_oil"}
POT = ",\nor over a\ncampfire, slower"
MACHINE = {"morefood:grinding": ("morefood:block_grinder", "crank it or\nfuel the firebox"),
           "morefood:pressing": ("morefood:block_press", "crank it or\nfuel the firebox"),
           "morefood:washing": ("morefood:block_kitchen_sink", "use it on the\nsink or hopper\nit in")}


def ref(i, **kw):
    if isinstance(i, list):
        d = {"items": i, "name": name(i)}
    elif i.startswith("#"):
        d = {"item": i, "name": name(i)}
    else:
        d = {"item": i}
    d.update({k: v for k, v in kw.items() if v is not None})
    return d if len(d) > 1 or "item" not in d else d["item"]


def secs(t):
    return f"{t // 20} seconds"


def width(n_in, extra):
    return n_in * 56 + (n_in - 1) * 28 + extra


class Page:
    def __init__(self):
        self.steps, self.raw, self.tools, self.done, self.left = [], {}, set(), set(), {}

    def need(self, i, n=1):
        if isinstance(i, str) and i in TOOLS:
            self.tools.add(i)
            return
        k = json.dumps(i)
        self.raw[k] = self.raw.get(k, 0) + n

    def make(self, item, n=1, stack=()):
        # adds the steps for item once and counts the raw inputs for n of it
        if (isinstance(item, list) or item.startswith("#") or item in TOOLS or item.startswith("minecraft:")
                or item not in recipes or item in stack or (stack and item in STOP)):
            return False
        r = recipes[item][0]
        left = self.left.get(item, 0)
        if left < n:
            per = r.get("portions", r["result"].get("count", 1))
            batches = -(-(n - left) // per)
            counts = {}
            for i in inputs(r):
                k = json.dumps(i)
                counts[k] = counts.get(k, 0) + 1
            for k, c in counts.items():
                i = json.loads(k)
                if isinstance(i, str) and (i in TOOLS or i in BUCKETS):
                    self.need(i)
                elif not self.make(i, c * batches, stack + (item,)):
                    self.need(i, c * batches)
            if r.get("water", 0) > 0:
                self.need("minecraft:water_bucket")
            if r.get("vessel"):
                self.need(r["vessel"], n - min(left, n))
            left += batches * per
            if item not in self.done:
                self.step(item, r)
                self.done.add(item)
        self.left[item] = left - n
        return True

    def step(self, item, r):
        t = r["type"]
        cnt = r["result"].get("count", 1)
        ins = inputs(r)
        out = ref(item, count=cnt if cnt > 1 else None)
        if t == "minecraft:crafting_shapeless":
            self.tools.add("minecraft:crafting_table")
            tl = [i for i in ins if isinstance(i, str) and i in TOOLS]
            order = tl + [i for i in ins if i not in tl]
            # repeated ingredients show once as a stack
            seen = {}
            for i in order:
                k = json.dumps(i)
                seen[k] = (i, seen[k][1] + 1 if k in seen else 1)
            cells = [ref(i, count=n if n > 1 else None) for i, n in seen.values()]
            if width(len(cells), 90) > 430:
                self.steps.append({"kind": "sequence", "title": name(item), "text_first": False,
                                   "text": "Crafting Table (shapeless): put these in the grid in any order.",
                                   "cells": cells + [ref(item, count=cnt if cnt > 1 else None, note="result")]})
            else:
                self.steps.append({"kind": "craft_shapeless", "inputs": cells, "output": out})
        elif t == "minecraft:crafting_shaped":
            self.tools.add("minecraft:crafting_table")
            self.steps.append({"kind": "craft_shaped", "pattern": r["pattern"],
                               "key": {k: ref(ing(v)) for k, v in r["key"].items()}, "output": out})
        elif t == "morefood:boiling":
            self.tools |= {"morefood:item_pot", "morefood:block_stove"}
            pot = list(ins)
            if r.get("water", 0) > 0:
                pot = ["minecraft:water_bucket"] + pot
            vessel = r.get("vessel")
            portions = r.get("portions", 1)
            out = ref(item, count=portions if portions > 1 else None)
            if width(len(pot), 240) <= 430:
                note = secs(r["cookingtime"]) + POT + (f".\nScoop out with\na {name(vessel)}" if vessel else "")
                self.steps.append({"kind": "process", "title": name(item), "note": note, "inputs": [ref(i) for i in pot],
                                   "appliance": "morefood:item_pot", "on": "morefood:block_stove", "output": out})
            else:
                txt = (f"Put these in the Pot on the Stove, any order, {secs(r['cookingtime'])} (slower on a campfire)."
                       + (f" Scoop out portions with a {name(vessel)}." if vessel else ""))
                self.steps.append({"kind": "sequence", "title": name(item) + " in the Pot", "text_first": False, "text": txt,
                                   "cells": [ref(i) for i in pot] + [ref(item, count=portions if portions > 1 else None, note="result")]})
        elif t == "morefood:frying":
            self.tools |= {"morefood:item_frying_pan", "morefood:block_stove"}
            self.steps.append({"kind": "process", "title": name(item), "note": secs(r["cookingtime"]) + ",\nfrom the stove\noutput",
                               "inputs": [ref(ins[0])], "appliance": "morefood:item_frying_pan", "on": "morefood:block_stove", "output": out})
        elif t == "morefood:baking":
            self.tools.add("morefood:block_stove")
            self.steps.append({"kind": "process", "title": name(item), "note": secs(r["cookingtime"]) + "\nin the oven",
                               "inputs": [ref(ins[0])], "appliance": {"item": "morefood:block_stove", "name": "Stove oven"}, "output": out})
        elif t in MACHINE:
            block, how = MACHINE[t]
            self.tools.add(block)
            if r.get("byproduct"):
                how += f",\ngives back a\n{name(r['byproduct']['id'])}"
            self.steps.append({"kind": "process", "title": name(item), "note": how, "inputs": [ref(i) for i in ins],
                               "appliance": block, "output": out})

    def need_lists(self):
        ings = []
        for k, n in self.raw.items():
            i = json.loads(k)
            if isinstance(i, str) and i in BUCKETS:
                n = 1
            ings.append(ref(i, count=n if n > 1 else None))
        return ings, [t for t in TOOLS if t in self.tools]


SUB = {"morefood:boiling": "Cooked in the pot on the stove.", "morefood:baking": "Baked in the stove oven.",
       "morefood:frying": "Fried in the pan on the stove.", "minecraft:crafting_shapeless": "Put together at the crafting table.",
       "morefood:pressing": "Squeezed in the press.", "morefood:grinding": "Ground in the grinder.",
       "morefood:washing": "Washed in the kitchen sink."}


def recipe_page(item, sort, subtitle=None, extra=None):
    p = Page()
    p.make(item)
    ings, tools = p.need_lists()
    return {"section": "recipes", "template": "morefood:recipe", "sort": sort, "title": name(item), "icon": item,
            "subtitle": subtitle or SUB.get(recipes[item][0]["type"], ""), "ingredients": ings, "tools": tools,
            "need_note": "Tools and Buckets are not consumed", "steps": p.steps + (extra or [])}


pages = {}
oil_link = {"kind": "links", "header": "Vegetable Oil comes from peanuts or avocados:", "columns": 3,
            "links": [{"item": "morefood:item_vegetable_oil", "page": "vegetable_oil"}]}
VEGAN = [
    ("item_tofu", None, None), ("item_tempeh", None, None), ("item_satay", None, None),
    ("item_soup_lentil", None, None), ("item_chickpeas_roasted", None, None), ("item_hummus", None, None),
    ("item_falafel", None, None), ("item_peanuts_roasted", None, None), ("item_peanut_butter", None, None),
    ("item_porridge", None, None), ("item_oatcake", None, None), ("item_oat_milk", None, None),
    ("item_beansprouts_fried", None, None), ("item_guacamole", None, None), ("item_coconut_milk", None, None),
    ("item_butter_vegan", None, [oil_link]), ("item_jackfruit_cooked", None, None), ("item_pulled_jackfruit", None, None),
    ("item_olive_oil", None, None), ("item_seitan_fried", None, None),
]
for n, (item, sub, extra) in enumerate(VEGAN):
    pages[item.replace("item_", "")] = recipe_page(f"{MOD}:{item}", 1200 + 2 * n, sub, extra)

# vegetable oil has two ways, show both
vo = Page()
for r in recipes[f"{MOD}:item_vegetable_oil"]:
    if r["type"] == "minecraft:crafting_shapeless":
        for i in inputs(r):
            vo.need(i)
        vo.step(f"{MOD}:item_vegetable_oil", r)
ings = [{"items": ["morefood:item_peanuts", "morefood:item_avocado"], "name": "Peanuts or Avocado"}, "minecraft:glass_bottle"]
tools = vo.need_lists()[1]
pages["vegetable_oil"] = {"section": "recipes", "template": "morefood:recipe", "sort": 1199, "title": "Vegetable Oil",
                          "icon": f"{MOD}:item_vegetable_oil", "subtitle": "From peanuts or an avocado. The press works too.",
                          "ingredients": ings, "tools": tools, "need_note": "Use either recipe",
                          "steps": vo.steps}

# milk by the bottle, both ways
mb = Page()
for out in (f"{MOD}:item_milk_bottle", "minecraft:milk_bucket"):
    mb.step(out, recipes[out][0])
pages["milk_bottle"] = {"section": "recipes", "template": "morefood:recipe", "sort": 1101, "title": name(f"{MOD}:item_milk_bottle"),
                        "icon": f"{MOD}:item_milk_bottle", "subtitle": "Drink it like a bucket, or use it where a recipe wants milk.",
                        "ingredients": ["minecraft:milk_bucket", {"item": "minecraft:glass_bottle", "count": 4}],
                        "tools": mb.need_lists()[1], "need_note": "Buckets and bottles come back", "steps": mb.steps}

# crop and tree pages for the vegan branch
NEW_CROPS = ["soy", "lentil", "chickpea", "peanut", "oats", "beansprouts"]
for n, c in enumerate(NEW_CROPS + list(TREES)):
    produce, entry, kind = CROPS[c]
    p = Page()
    if kind == "crop":
        p.step(entry[0], {"type": "minecraft:crafting_shapeless", "ingredients": [produce], "result": {"id": entry[0]}})
    pages[f"crop_{c}"] = {"section": "crops", "template": "morefood:info", "sort": 313 + n, "title": name(produce), "icon": produce}
    if kind == "crop":
        pages[f"crop_{c}"].update({
            "subtitle": "A crop. Seeds drop from grass.",
            "text": f"Break grass to find {name(entry[0])}. Plant them on farmland and harvest when fully grown; Bone Meal speeds it up. One {name(produce)} crafts back into seeds."})
    else:
        sap = entry[1]
        pages[f"crop_{c}"].update({
            "subtitle": f"A fruit tree. Grows wild {TREES[c]}.",
            "text": f"{name(produce)} hangs under the leaves like a cocoa pod. Break it when it is ripe for two, or early for one. Left alone, a ripe one drops its two after a few minutes and grows again. The leaves drop {name(sap)}s; plant one on grass or dirt{' or sand' if c == 'coconut' else ''} and Bone Meal helps it grow."})
    pages[f"crop_{c}"]["steps"] = p.steps

# kitchen machines, needed for tofu and seitan
def shaped(item, title, note):
    r = recipes[item][0]
    return {"kind": "craft_shaped", "pattern": r["pattern"], "key": {k: ref(ing(v)) for k, v in r["key"].items()},
            "output": ref(item, count=r["result"].get("count")), "title": title, "note": note,
            "number": False, "join": False, "label_width": 400}


pages["machines"] = {
    "section": "tools", "template": "morefood:info", "sort": 201, "title": "Kitchen Machines", "icon": "morefood:block_grinder",
    "subtitle": "The grinder, the press and the sink.",
    "text": "The Grinder and Press work three inputs at a time. A gearbox one is cranked with an empty hand, a firebox one burns fuel. Sneak with an empty hand to open it.",
    "steps": [
        shaped("morefood:block_kitchen_chassis", "Kitchen Chassis", "Place it first, then build the machine on it."),
        shaped("morefood:item_grinder_core", "Grinder Core", "Makes it a Grinder: flour, pastes, butters."),
        shaped("morefood:item_press_core", "Press Core", "Makes it a Press: juices, milks, oils, tofu."),
        {"kind": "sequence", "title": "Build it on the chassis", "text_first": False, "number": False,
         "text": "Use each part on the chassis in this order and turn a Screwdriver after each one.",
         "cells": [{"items": ["morefood:item_grinder_core", "morefood:item_press_core"], "name": "A core"},
                   "workshopcore:control_board",
                   {"items": ["morefood:item_kitchen_gearbox", "morefood:item_firebox"], "name": "Gearbox or Firebox"},
                   {"item": "morefood:item_kitchen_panel", "count": 4}, "morefood:item_kitchen_worktop"]},
        shaped("morefood:item_kitchen_gearbox", "Kitchen Gearbox", "Crank it with an empty hand."),
        shaped("morefood:item_firebox", "Firebox", "Burns any furnace fuel."),
        shaped("morefood:item_kitchen_panel", "Kitchen Panel", "Four go on each machine."),
        shaped("morefood:item_kitchen_worktop", "Kitchen Worktop", "The last part."),
        shaped("morefood:block_kitchen_sink", "Kitchen Sink",
               "Never runs dry. Fills buckets and bottles, washes dough into seitan. Hoppers and pipes work too."),
    ]}

pages["milk_churn"] = {
    "section": "tools", "template": "morefood:info", "sort": 202, "title": "Milk Churn", "icon": "morefood:block_milk_churn",
    "subtitle": "Keeps up to 8 buckets of milk.",
    "text": "Empty Milk Buckets and Milk Bottles into it, or fill empty ones from it. Four bottles make a bucket. The window on the front shows how full it is; use it with an empty hand for the exact amount. Break it and it keeps its milk. Hoppers and pipes put full or empty buckets and bottles in the top or sides and take what comes back from the bottom.",
    "steps": [shaped("morefood:block_milk_churn", "Milk Churn", "Two blocks tall. Planks and iron.")]}

for pid, j in pages.items():
    w(f"{PAGES}/{pid}.json", j)

# used-in links on the crop pages, 3 columns
USED_IN_MAX = 30
allpages = {os.path.basename(f)[:-5]: load(f) for f in glob.glob(f"{PAGES}/*.json")}


def needs(j):
    s = set()
    for i in j.get("ingredients", []):
        if isinstance(i, dict):
            i = i.get("items") or i.get("item")
        s |= crops_of(i)
    return s


for c, (produce, entry, kind) in CROPS.items():
    pid = f"crop_{c}" if f"crop_{c}" in allpages else None
    if pid is None:
        pid = next((k for k, j in allpages.items() if j.get("section") == "crops" and j.get("icon") == produce), None)
    if pid is None:
        continue
    j = allpages[pid]
    users = sorted((k for k, u in allpages.items() if u.get("section") in ("recipes", "toppings") and c in needs(u)
                    and not k.endswith("_2")), key=lambda k: allpages[k]["title"])
    links = next((s for s in j["steps"] if s.get("kind") == "links"), None)
    if links is None and users:
        links = {"kind": "links", "header": "Used in:", "columns": 3, "links": []}
        j["steps"].append(links)
    if links is not None:
        have = {l["page"] for l in links["links"]}
        links["links"] += [{"item": allpages[k]["icon"], "page": k} for k in users if k not in have]
        links["links"] = links["links"][:USED_IN_MAX]


ORDER = list(CROPS)


def simplify(kind, parts):
    # parts are rules: a crop name, ("all", ...) or ("any", ...); None means no crop needed
    if kind == "any" and any(p is None for p in parts):
        return None
    flat = []
    for p in parts:
        if p is None:
            continue
        sub = p[1] if isinstance(p, tuple) and p[0] == kind else [p]
        for q in sub:
            if q not in flat:
                flat.append(q)
    if kind == "all":
        # a crop already required makes any group holding it redundant
        flat = [q for q in flat if not (isinstance(q, tuple) and any(x in flat for x in q[1] if not isinstance(x, tuple)))]
    if not flat:
        return None
    if len(flat) == 1:
        return flat[0]
    key = lambda q: (isinstance(q, tuple), ORDER.index(q) if not isinstance(q, tuple) else 0)
    return (kind, tuple(sorted(flat, key=key)))


_rule = {}


def rule_of(i, stack=()):
    # what a player must have found to make i, from every recipe that makes it
    if isinstance(i, list):
        return simplify("any", [rule_of(x, stack) for x in i])
    if not isinstance(i, str) or i.startswith("#") or i in TOOLS:
        return None
    if i in PRODUCE:
        return PRODUCE[i]
    for k, (_, entry, _) in CROPS.items():
        if i in entry:
            return k
    if i in _rule:
        return _rule[i]
    if i not in recipes or i in stack:
        return None
    out = simplify("any", [simplify("all", [rule_of(x, stack + (i,)) for x in inputs(r)]) for r in recipes[i]])
    _rule[i] = out
    return out


def page_rule(j):
    icon = j.get("icon")
    if icon in recipes:
        return rule_of(icon)
    parts = []
    for x in j.get("ingredients", []):
        if isinstance(x, dict):
            x = x.get("items") or x.get("item")
        parts.append(rule_of(x))
    return simplify("all", parts)


def has_any(r):
    return isinstance(r, tuple) and (r[0] == "any" or any(has_any(q) for q in r[1]))


def to_json(r):
    if not isinstance(r, tuple):
        return {"tag": f"{MOD}:unlocks/{r}"}
    return {r[0]: [to_json(q) for q in r[1]]}


def words(r):
    if not isinstance(r, tuple):
        return name(CROPS[r][0])
    ps = [words(q) for q in r[1]]
    return " or ".join(ps) if r[0] == "any" else ", ".join(ps[:-1]) + " and " + ps[-1]


def show(r):
    if not isinstance(r, tuple):
        return r
    return r[0] + "(" + ", ".join(show(q) for q in r[1]) + ")"


# gate: a crop or tree page on itself, a recipe on the crops it needs
gated = {}
for pid, j in allpages.items():
    j.pop("unlock_on", None)
    j.pop("unlock_hint", None)
    r, hint = None, None
    if j.get("section") == "crops":
        c = next((k for k, v in CROPS.items() if v[0] == j.get("icon")), None)
        if c:
            r = c
            produce, entry, kind = CROPS[c]
            hint = (f"Break grass to find {name(entry[0])}" if kind == "crop"
                    else f"Find {name(produce)} trees {TREES[c]}")
    elif j.get("section") in ("recipes", "toppings"):
        base = pid[:-2] if pid.endswith("_2") else pid
        r = page_rule(allpages.get(base, j))
        # Simple Books names the missing tags itself, but cannot word an "or" inside a group
        if has_any(r):
            hint = f"Find {words(r)} to unlock"
    if r is not None:
        keys = list(j.keys())
        at = keys.index("subtitle") + 1 if "subtitle" in keys else len(keys)
        extra = {"unlock_on": to_json(r)}
        if hint:
            extra["unlock_hint"] = hint
        j = {**{k: j[k] for k in keys[:at]}, **extra, **{k: j[k] for k in keys[at:]}}
        gated[pid] = r
    w(f"{PAGES}/{pid}.json", j)

# tag names for the hints Simple Books builds, and for recipe viewers
LANG = f"src/main/resources/assets/{MOD}/lang/en_us.json"
lang_file = load(LANG)
for c, (produce, _, _) in CROPS.items():
    lang_file[f"tag.item.{MOD}.unlocks.{c}"] = name(produce)
w(LANG, lang_file)

for c, (_, entry, _) in CROPS.items():
    w(f"{TAGS}/{c}.json", {"values": entry})

book = load(BOOK)
book["locked"] = book.get("locked", "show")
w(BOOK, book)

for pid in sorted(gated):
    print(pid, "->", show(gated[pid]))
print(len(gated), "gated of", len(allpages))
