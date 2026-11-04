package com.workshopcraft.morefood;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
//? if >=1.21 {
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
//?} else {
/*import net.minecraft.world.level.storage.loot.entries.LootTableReference;
*///?}
import org.jspecify.annotations.Nullable;

import java.util.Map;

public class MoreFood {
    public static final String MODID = "morefood";

    public static Identifier id(String path) {
        //? if >=1.21 {
        return Identifier.fromNamespaceAndPath(MODID, path);
        //?} else {
        /*return new Identifier(MODID, path);
        *///?}
    }

    private static Identifier mc(String path) {
        //? if >=1.21 {
        return Identifier.withDefaultNamespace(path);
        //?} else {
        /*return new Identifier("minecraft", path);
        *///?}
    }

    //vanilla loot tables that get one of ours added on, for the grass seeds and the extra mob drops
    private static final Map<Identifier, String> INJECT = Map.of(
            //? if >=1.20.3 {
            mc("blocks/short_grass"), "inject/grass",
            //?} else {
            /*mc("blocks/grass"), "inject/grass",
            *///?}
            mc("blocks/fern"), "inject/grass",
            mc("entities/cow"), "inject/cow",
            mc("entities/mooshroom"), "inject/cow",
            mc("entities/pig"), "inject/pig",
            mc("entities/chicken"), "inject/chicken",
            mc("entities/sheep"), "inject/sheep");

    //? if >=26.3 {
    /*public static LootPool.@Nullable Builder extraLoot(ResourceKey<LootTable> table, net.minecraft.core.HolderGetter.Provider registries) {
        final String inject = INJECT.get(table.identifier());
        if (inject == null) return null;
        return registries.get(ResourceKey.create(Registries.LOOT_TABLE, id(inject)))
                .map(holder -> LootPool.lootPool().add(NestedLootTable.lootTableReference(holder))).orElse(null);
    }
    *///?} else if >=26.1 {
    public static LootPool.@Nullable Builder extraLoot(ResourceKey<LootTable> table) {
        final String inject = INJECT.get(table.identifier());
        if (inject == null) return null;
        return LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, id(inject))));
    }
    //?} else if >=1.21 {
    /*public static LootPool.@Nullable Builder extraLoot(ResourceKey<LootTable> table) {
        final String inject = INJECT.get(table.location());
        if (inject == null) return null;
        return LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, id(inject))));
    }
    *///?} else {
    /*public static LootPool.@Nullable Builder extraLoot(Identifier table) {
        final String inject = INJECT.get(table);
        if (inject == null) return null;
        return LootPool.lootPool().add(LootTableReference.lootTableReference(id(inject)));
    }
    *///?}
}
