package com.workshopcraft.morefood;

import com.workshopcraft.morefood.blocks.BlockCropMoreFood;
import com.workshopcraft.morefood.board.AssemblyBoardBlock;
import com.workshopcraft.morefood.stove.CookingPotBlock;
import com.workshopcraft.morefood.stove.StoveBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class MoreFoodBlocks {
    public static Block cropTomato;
    public static Block cropLettuce;
    public static Block cropOnion;
    public static Block stove;
    public static Block cookingPot;
    public static Block assemblyBoard;
    public static final Map<String, Block> CROPS = new LinkedHashMap<>();

    public static void register(BiConsumer<Identifier, Block> registry) {
        cropTomato = crop(registry, "block_crop_tomato", () -> MoreFoodItems.seedTomato);
        cropLettuce = crop(registry, "block_crop_lettuce", () -> MoreFoodItems.seedLettuce);
        cropOnion = crop(registry, "block_crop_onion", () -> MoreFoodItems.seedOnion);
        for (String name : MoreFoodItems.NEW_CROPS) {
            CROPS.put(name, crop(registry, "block_crop_" + name, () -> MoreFoodItems.SEEDS.get(name)));
        }
        stove = new StoveBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, MoreFood.id("block_stove")))
                .mapColor(MapColor.COLOR_GRAY)
                .instrument(NoteBlockInstrument.BASEDRUM)
                .requiresCorrectToolForDrops()
                .strength(3.5F)
                .sound(SoundType.METAL)
                .lightLevel(state -> state.getValue(StoveBlock.LIT) ? 13 : 0));
        registry.accept(MoreFood.id("block_stove"), stove);
        cookingPot = new CookingPotBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, MoreFood.id("block_cooking_pot")))
                .mapColor(MapColor.METAL)
                .strength(1.0F)
                .sound(SoundType.LANTERN)
                .noOcclusion()
                .pushReaction(PushReaction.DESTROY));
        registry.accept(MoreFood.id("block_cooking_pot"), cookingPot);
        assemblyBoard = new AssemblyBoardBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, MoreFood.id("block_assembly_board")))
                .mapColor(MapColor.WOOD)
                .instrument(NoteBlockInstrument.BASS)
                .strength(0.5F)
                .sound(SoundType.WOOD)
                .noOcclusion()
                .forceSolidOff()
                .pushReaction(PushReaction.DESTROY));
        registry.accept(MoreFood.id("block_assembly_board"), assemblyBoard);
    }

    private static Block crop(BiConsumer<Identifier, Block> registry, String name, Supplier<Item> seed) {
        final Block block = new BlockCropMoreFood(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, MoreFood.id(name)))
                .mapColor(MapColor.PLANT)
                .noCollision()
                .randomTicks()
                .instabreak()
                .sound(SoundType.CROP)
                .pushReaction(PushReaction.DESTROY), seed);
        registry.accept(MoreFood.id(name), block);
        return block;
    }
}
