package com.workshopcraft.morefood.fabric;

import com.workshopcraft.morefood.MoreFood;
import com.workshopcraft.morefood.MoreFoodBlocks;
import com.workshopcraft.morefood.MoreFoodItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.LootPool;

import java.util.function.BiConsumer;

public class MoreFoodFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        MoreFoodBlocks.register(direct(BuiltInRegistries.BLOCK));
        MoreFoodItems.register(direct(BuiltInRegistries.ITEM));
        MoreFoodItems.registerTabs(direct(BuiltInRegistries.CREATIVE_MODE_TAB));

        LootTableEvents.MODIFY.register((key, table, source, registries) -> {
            final LootPool.Builder pool = MoreFood.extraLoot(key);
            if (pool != null && source.isBuiltin()) {
                table.withPool(pool);
            }
        });
    }

    private static <T> BiConsumer<Identifier, T> direct(Registry<T> registry) {
        return (id, value) -> Registry.register(registry, id, value);
    }
}
