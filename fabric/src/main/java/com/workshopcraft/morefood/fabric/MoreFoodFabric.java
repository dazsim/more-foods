package com.workshopcraft.morefood.fabric;

import com.workshopcraft.morefood.MoreFood;
import com.workshopcraft.morefood.MoreFoodBlocks;
import com.workshopcraft.morefood.MoreFoodItems;
import com.workshopcraft.morefood.MoreFoodKitchen;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.LootPool;

import java.util.function.BiConsumer;

public class MoreFoodFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        MoreFoodKitchen.registerComponents(direct(BuiltInRegistries.DATA_COMPONENT_TYPE));
        MoreFoodBlocks.register(direct(BuiltInRegistries.BLOCK));
        MoreFoodItems.register(direct(BuiltInRegistries.ITEM));
        MoreFoodItems.registerTabs(direct(BuiltInRegistries.CREATIVE_MODE_TAB));
        MoreFoodKitchen.registerBlockEntities(direct(BuiltInRegistries.BLOCK_ENTITY_TYPE));
        MoreFoodKitchen.registerMenus(direct(BuiltInRegistries.MENU));
        MoreFoodKitchen.registerRecipeTypes(direct(BuiltInRegistries.RECIPE_TYPE));
        MoreFoodKitchen.registerRecipeSerializers(direct(BuiltInRegistries.RECIPE_SERIALIZER));
        MoreFoodKitchen.registerBookCategories(direct(BuiltInRegistries.RECIPE_BOOK_CATEGORY));
        MoreFoodKitchen.registerParticles(direct(BuiltInRegistries.PARTICLE_TYPE));

        ItemStorage.SIDED.registerForBlockEntity((stove, side) -> ContainerStorage.of(stove, side), MoreFoodKitchen.stoveEntity);
        FluidStorage.SIDED.registerForBlockEntity(PotStorage::of, MoreFoodKitchen.stoveEntity);
        ItemStorage.SIDED.registerForBlockEntity((pot, side) -> ContainerStorage.of(pot, side), MoreFoodKitchen.cookingPotEntity);
        FluidStorage.SIDED.registerForBlockEntity(PotStorage::ofCampfire, MoreFoodKitchen.cookingPotEntity);
        ItemStorage.SIDED.registerForBlockEntity(BoardStorage::of, MoreFoodKitchen.boardEntity);

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
