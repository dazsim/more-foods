#!/bin/sh
# writes the grinder and press loot tables with Workshop Core's MachineLoot for every era; run from repo root
# machines drop with any tool (--tool none); run after gen_eras.py, which would write the same tables
set -e
v=$(sed -n 's/^deps.workshopcore = "\(.*\)"/\1/p' stonecutter.properties.toml)
jar=../workshopcore-maven/com/workshopcraft/workshopcore/workshopcore-neoforge-26.1.2/$v/workshopcore-neoforge-26.1.2-$v.jar
java=${JAVA_HOME:-/home/box/.gradle/jdks/eclipse_adoptium-25-amd64-linux.2}/bin/java
for era in 26.1:1.21:loot_table 1.21.1:1.21:loot_table 26.3:26.3:loot_table 1.20.1:1.20.1:loot_tables; do
    dir=src/eras/${era%%:*}/data/morefood/${era##*:}/blocks
    fmt=$(echo "$era" | cut -d: -f2)
    for m in grinder press; do
        extra=""; [ $m = press ] && extra="--height2"
        "$java" -cp "$jar" com.workshopcraft.workshopcore.machine.MachineLoot morefood:block_$m --tool none --format "$fmt" $extra > /tmp/mf_loot.json
        python3 -c "import json,sys; d=json.load(open('/tmp/mf_loot.json')); d['random_sequence']='morefood:blocks/block_$m'; open('$dir/block_$m.json','w').write(json.dumps(d,indent=2)+'\n')"
    done
done
