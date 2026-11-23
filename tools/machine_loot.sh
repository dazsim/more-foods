#!/bin/sh
# writes the grinder and press loot tables with Workshop Core's MachineLoot, then run gen_eras.py; run from repo root
set -e
v=$(sed -n 's/^deps.workshopcore = "\(.*\)"/\1/p' stonecutter.properties.toml)
jar=../workshopcore-maven/com/workshopcraft/workshopcore/workshopcore-neoforge-26.1.2/$v/workshopcore-neoforge-26.1.2-$v.jar
java=${JAVA_HOME:-/home/box/.gradle/jdks/eclipse_adoptium-25-amd64-linux.2}/bin/java
out=src/eras/26.1/data/morefood/loot_table/blocks
"$java" -cp "$jar" com.workshopcraft.workshopcore.machine.MachineLoot morefood:block_grinder --keep-parts > /tmp/mf_grinder.json
"$java" -cp "$jar" com.workshopcraft.workshopcore.machine.MachineLoot morefood:block_press --keep-parts --height2 > /tmp/mf_press.json
python3 - "$out" <<'PY'
import json, sys
for m in ("grinder", "press"):
    d = json.load(open(f"/tmp/mf_{m}.json"))
    d["random_sequence"] = f"morefood:blocks/block_{m}"
    with open(f"{sys.argv[1]}/block_{m}.json", "w") as f:
        f.write(json.dumps(d, indent=2) + "\n")
PY
