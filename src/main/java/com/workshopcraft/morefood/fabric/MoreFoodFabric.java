package com.workshopcraft.morefood.fabric;

import com.workshopcraft.morefood.MoreFood;
import com.workshopcraft.morefood.MoreFoodBlocks;
import com.workshopcraft.morefood.MoreFoodItems;
import com.workshopcraft.morefood.MoreFoodKitchen;
import net.fabricmc.api.ModInitializer;
//? if >=1.21 {
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
//?} else {
/*import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
*///?}
//? if >=26.1 {
import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;
//?}
//? if <26.3 {
import net.fabricmc.fabric.api.registry.CompostableRegistry;
//?}
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
        //? if >=1.20.5 {
        MoreFoodKitchen.registerComponents(direct(BuiltInRegistries.DATA_COMPONENT_TYPE));
        //?}
        MoreFoodBlocks.register(direct(BuiltInRegistries.BLOCK));
        MoreFoodItems.register(direct(BuiltInRegistries.ITEM));
        MoreFoodItems.registerTabs(direct(BuiltInRegistries.CREATIVE_MODE_TAB));
        MoreFoodKitchen.registerBlockEntities(direct(BuiltInRegistries.BLOCK_ENTITY_TYPE));
        MoreFoodKitchen.registerMenus(direct(BuiltInRegistries.MENU));
        MoreFoodKitchen.registerRecipeTypes(direct(BuiltInRegistries.RECIPE_TYPE));
        MoreFoodKitchen.registerRecipeSerializers(direct(BuiltInRegistries.RECIPE_SERIALIZER));
        //? if >=26.1 {
        MoreFoodKitchen.registerBookCategories(direct(BuiltInRegistries.RECIPE_BOOK_CATEGORY));
        //?}
        MoreFoodKitchen.registerParticles(direct(BuiltInRegistries.PARTICLE_TYPE));
        MoreFoodKitchen.registerSounds(direct(BuiltInRegistries.SOUND_EVENT));
        //? if >=26.1 {
        RecipeSynchronization.synchronizeRecipeSerializer(MoreFoodKitchen.BAKING_SERIALIZER);
        RecipeSynchronization.synchronizeRecipeSerializer(MoreFoodKitchen.FRYING_SERIALIZER);
        RecipeSynchronization.synchronizeRecipeSerializer(MoreFoodKitchen.BOILING_SERIALIZER);
        RecipeSynchronization.synchronizeRecipeSerializer(MoreFoodKitchen.ASSEMBLY_SERIALIZER);
        //?}

        // 26.3 items carry compostable themselves
        //? if >=26.1 && <26.3 {
        MoreFoodItems.COMPOST.forEach((name, chance) -> CompostableRegistry.INSTANCE.add(BuiltInRegistries.ITEM.getValue(MoreFood.id(name)), chance));
        //?} else if <26.1 {
        /*MoreFoodItems.COMPOST.forEach((name, chance) -> CompostableRegistry.INSTANCE.add(BuiltInRegistries.ITEM.get(MoreFood.id(name)), chance));
        *///?}

        ItemStorage.SIDED.registerForBlockEntity((stove, side) -> ContainerStorage.of(stove, side), MoreFoodKitchen.stoveEntity);
        FluidStorage.SIDED.registerForBlockEntity(PotStorage::of, MoreFoodKitchen.stoveEntity);
        ItemStorage.SIDED.registerForBlockEntity((pot, side) -> ContainerStorage.of(pot, side), MoreFoodKitchen.cookingPotEntity);
        FluidStorage.SIDED.registerForBlockEntity(PotStorage::ofCampfire, MoreFoodKitchen.cookingPotEntity);
        ItemStorage.SIDED.registerForBlockEntity(BoardStorage::of, MoreFoodKitchen.boardEntity);

        //? if >=1.21 {
        LootTableEvents.MODIFY.register((key, table, source, registries) -> {
            final LootPool.Builder pool = MoreFood.extraLoot(key);
            if (pool != null && source.isBuiltin()) {
                table.withPool(pool);
            }
        });
        //?} else {
        /*LootTableEvents.MODIFY.register((resources, loot, id, table, source) -> {
            final LootPool.Builder pool = MoreFood.extraLoot(id);
            if (pool != null && source.isBuiltin()) {
                table.withPool(pool);
            }
        });
        *///?}
    }

    private static <T> BiConsumer<Identifier, T> direct(Registry<T> registry) {
        return (id, value) -> Registry.register(registry, id, value);
    }
}
