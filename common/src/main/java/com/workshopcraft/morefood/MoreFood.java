package com.workshopcraft.morefood;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import org.jspecify.annotations.Nullable;

import java.util.Map;

public class MoreFood {
    public static final String MODID = "morefood";

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    //vanilla loot tables that get one of ours added on, for the grass seeds and the extra mob drops
    private static final Map<Identifier, String> INJECT = Map.of(
            Identifier.withDefaultNamespace("blocks/short_grass"), "inject/grass",
            Identifier.withDefaultNamespace("blocks/fern"), "inject/grass",
            Identifier.withDefaultNamespace("entities/cow"), "inject/cow",
            Identifier.withDefaultNamespace("entities/mooshroom"), "inject/cow",
            Identifier.withDefaultNamespace("entities/pig"), "inject/pig",
            Identifier.withDefaultNamespace("entities/chicken"), "inject/chicken",
            Identifier.withDefaultNamespace("entities/sheep"), "inject/sheep");

    public static LootPool.@Nullable Builder extraLoot(ResourceKey<LootTable> table) {
        final String inject = INJECT.get(table.identifier());
        if (inject == null) return null;
        return LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, id(inject))));
    }
}
