package com.workshopcraft.morefood.fabric;

import com.workshopcraft.morefood.MoreFood;
import com.workshopcraft.morefood.MoreFoodBlocks;
import com.workshopcraft.morefood.MoreFoodItems;
import com.workshopcraft.morefood.MoreFoodKitchen;
import com.workshopcraft.morefood.worldgen.MoreFoodWorldGen;
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
        //? if >=26.3 {
        /*MoreFoodWorldGen.register(direct(BuiltInRegistries.FEATURE_TYPE));
        *///?} else {
        MoreFoodWorldGen.register(direct(BuiltInRegistries.FEATURE));
        //?}
        addTrees();
        //? if >=26.1 {
        RecipeSynchronization.synchronizeRecipeSerializer(MoreFoodKitchen.BAKING_SERIALIZER);
        RecipeSynchronization.synchronizeRecipeSerializer(MoreFoodKitchen.FRYING_SERIALIZER);
        RecipeSynchronization.synchronizeRecipeSerializer(MoreFoodKitchen.BOILING_SERIALIZER);
        RecipeSynchronization.synchronizeRecipeSerializer(MoreFoodKitchen.ASSEMBLY_SERIALIZER);
        RecipeSynchronization.synchronizeRecipeSerializer(MoreFoodKitchen.GRINDING_SERIALIZER);
        RecipeSynchronization.synchronizeRecipeSerializer(MoreFoodKitchen.PRESSING_SERIALIZER);
        RecipeSynchronization.synchronizeRecipeSerializer(MoreFoodKitchen.WASHING_SERIALIZER);
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
        ItemStorage.SIDED.registerForBlockEntity((sink, side) -> ContainerStorage.of(sink, side), com.workshopcraft.morefood.MoreFoodMachines.sinkEntity);
        ItemStorage.SIDED.registerForBlockEntity((churn, side) -> ContainerStorage.of(churn, side), com.workshopcraft.morefood.MoreFoodMachines.churnEntity);

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

    private static void addTrees() {
        net.fabricmc.fabric.api.biome.v1.BiomeModifications.addFeature(
                net.fabricmc.fabric.api.biome.v1.BiomeSelectors.tag(net.minecraft.tags.BiomeTags.IS_JUNGLE),
                net.minecraft.world.level.levelgen.GenerationStep.Decoration.VEGETAL_DECORATION,
                MoreFoodWorldGen.JACKFRUIT);
        net.fabricmc.fabric.api.biome.v1.BiomeModifications.addFeature(
                net.fabricmc.fabric.api.biome.v1.BiomeSelectors.tag(net.minecraft.tags.BiomeTags.IS_BEACH),
                net.minecraft.world.level.levelgen.GenerationStep.Decoration.VEGETAL_DECORATION,
                MoreFoodWorldGen.COCONUT);
        net.fabricmc.fabric.api.biome.v1.BiomeModifications.addFeature(
                net.fabricmc.fabric.api.biome.v1.BiomeSelectors.tag(net.minecraft.tags.BiomeTags.IS_SAVANNA),
                net.minecraft.world.level.levelgen.GenerationStep.Decoration.VEGETAL_DECORATION,
                MoreFoodWorldGen.OLIVE);
        net.fabricmc.fabric.api.biome.v1.BiomeModifications.addFeature(
                net.fabricmc.fabric.api.biome.v1.BiomeSelectors.tag(net.minecraft.tags.BiomeTags.IS_JUNGLE),
                net.minecraft.world.level.levelgen.GenerationStep.Decoration.VEGETAL_DECORATION,
                MoreFoodWorldGen.AVOCADO);
        final net.fabricmc.fabric.api.registry.FlammableBlockRegistry fire = net.fabricmc.fabric.api.registry.FlammableBlockRegistry.getDefaultInstance();
        MoreFoodBlocks.flammable((block, ignite, burn) -> fire.add(block, ignite, burn));
        for (net.minecraft.world.level.block.Block leaves : com.workshopcraft.morefood.MoreFoodBlocks.LEAVES.values()) fire.add(leaves, 30, 60);
        for (net.minecraft.world.level.block.Block sapling : com.workshopcraft.morefood.MoreFoodBlocks.SAPLINGS.values()) fire.add(sapling, 20, 60);
        //? if <26.3 {
        net.fabricmc.fabric.api.registry.StrippableBlockRegistry.register(MoreFoodBlocks.logPalm, MoreFoodBlocks.strippedLogPalm);
        net.fabricmc.fabric.api.registry.StrippableBlockRegistry.register(MoreFoodBlocks.woodPalm, MoreFoodBlocks.strippedWoodPalm);
        //?} else {
        /*net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            final net.minecraft.world.item.ItemStack stack = player.getItemInHand(hand);
            if (!stack.is(net.minecraft.tags.ItemTags.AXES)) return net.minecraft.world.InteractionResult.PASS;
            final net.minecraft.world.level.block.state.BlockState stripped = MoreFoodBlocks.stripped(world.getBlockState(hit.getBlockPos()));
            if (stripped == null) return net.minecraft.world.InteractionResult.PASS;
            if (!world.isClientSide()) {
                world.setBlock(hit.getBlockPos(), stripped, 11);
                world.playSound(null, hit.getBlockPos(), net.minecraft.sounds.SoundEvents.AXE_STRIP.value(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                stack.hurtAndBreak(1, player, hand);
            }
            return net.minecraft.world.InteractionResult.SUCCESS;
        });
        *///?}
    }

    private static <T> BiConsumer<Identifier, T> direct(Registry<T> registry) {
        return (id, value) -> Registry.register(registry, id, value);
    }
}
