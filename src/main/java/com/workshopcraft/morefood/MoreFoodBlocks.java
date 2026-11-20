package com.workshopcraft.morefood;

import com.workshopcraft.morefood.blocks.BlockCropMoreFood;
import com.workshopcraft.morefood.blocks.FruitLeavesBlock;
import com.workshopcraft.morefood.blocks.FruitPodBlock;
import com.workshopcraft.morefood.blocks.FruitSaplingBlock;
import com.workshopcraft.morefood.blocks.PalmWood;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.SlabBlock;
import org.jspecify.annotations.Nullable;
import com.workshopcraft.morefood.board.AssemblyBoardBlock;
import com.workshopcraft.morefood.stove.CookingPotBlock;
import com.workshopcraft.morefood.stove.StoveBlock;
import com.workshopcraft.morefood.worldgen.FruitTreeFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
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
    public static final Map<FruitTreeFeature.Type, FruitSaplingBlock> SAPLINGS = new LinkedHashMap<>();
    public static final Map<FruitTreeFeature.Type, FruitLeavesBlock> LEAVES = new LinkedHashMap<>();
    public static final Map<FruitTreeFeature.Type, FruitPodBlock> PODS = new LinkedHashMap<>();
    public static Block logPalm;
    public static Block strippedLogPalm;
    public static Block woodPalm;
    public static Block strippedWoodPalm;
    public static Block planksPalm;
    public static Block stairsPalm;
    public static Block slabPalm;
    public static Block fencePalm;
    public static Block fenceGatePalm;
    public static Block doorPalm;
    public static Block trapdoorPalm;
    public static Block buttonPalm;
    public static Block pressurePlatePalm;


    public static void register(BiConsumer<Identifier, Block> registry) {
        cropTomato = crop(registry, "block_crop_tomato", () -> MoreFoodItems.seedTomato);
        cropLettuce = crop(registry, "block_crop_lettuce", () -> MoreFoodItems.seedLettuce);
        cropOnion = crop(registry, "block_crop_onion", () -> MoreFoodItems.seedOnion);
        for (String name : MoreFoodItems.NEW_CROPS) {
            CROPS.put(name, crop(registry, "block_crop_" + name, () -> MoreFoodItems.SEEDS.get(name)));
        }
        for (String name : MoreFoodItems.VEGAN_CROPS) {
            CROPS.put(name, crop(registry, "block_crop_" + name, () -> MoreFoodItems.SEEDS.get(name)));
        }
        logPalm = pillar(registry, "block_log_palm", MapColor.WOOD);
        strippedLogPalm = pillar(registry, "block_stripped_log_palm", MapColor.TERRACOTTA_ORANGE);
        woodPalm = pillar(registry, "block_wood_palm", MapColor.WOOD);
        strippedWoodPalm = pillar(registry, "block_stripped_wood_palm", MapColor.TERRACOTTA_ORANGE);
        planksPalm = register(registry, "block_planks_palm", new Block(plankProps("block_planks_palm", MapColor.TERRACOTTA_ORANGE)));
        stairsPalm = register(registry, "block_stairs_palm", new PalmWood.Stairs(planksPalm.defaultBlockState(), plankProps("block_stairs_palm", MapColor.TERRACOTTA_ORANGE)));
        slabPalm = register(registry, "block_slab_palm", new SlabBlock(plankProps("block_slab_palm", MapColor.TERRACOTTA_ORANGE)));
        fencePalm = register(registry, "block_fence_palm", new FenceBlock(plankProps("block_fence_palm", MapColor.TERRACOTTA_ORANGE)));
        fenceGatePalm = register(registry, "block_fence_gate_palm", new PalmWood.Gate(plankProps("block_fence_gate_palm", MapColor.TERRACOTTA_ORANGE)));
        doorPalm = register(registry, "block_door_palm", new PalmWood.Door(doorProps("block_door_palm")));
        trapdoorPalm = register(registry, "block_trapdoor_palm", new PalmWood.Trapdoor(doorProps("block_trapdoor_palm")));
        buttonPalm = register(registry, "block_button_palm", new PalmWood.Button(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, MoreFood.id("block_button_palm")))
                .noCollision()
                .strength(0.5F)
                .pushReaction(PushReaction.DESTROY)));
        pressurePlatePalm = register(registry, "block_pressure_plate_palm", new PalmWood.Plate(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, MoreFood.id("block_pressure_plate_palm")))
                .mapColor(MapColor.TERRACOTTA_ORANGE)
                .forceSolidOn()
                .noCollision()
                .strength(0.5F)
                .pushReaction(PushReaction.DESTROY)));
        for (FruitTreeFeature.Type type : FruitTreeFeature.Type.values()) {
            final String id = type.name().toLowerCase();
            final FruitLeavesBlock leaves = new FruitLeavesBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, MoreFood.id("block_leaves_" + id)))
                    .mapColor(MapColor.PLANT)
                    .strength(0.2F)
                    .randomTicks()
                    .sound(SoundType.GRASS)
                    .noOcclusion()
                    .isSuffocating((state, level, pos) -> false)
                    //? if <26.3 {
                    .isViewBlocking((state, level, pos) -> false)
                    //?}
                    .isRedstoneConductor((state, level, pos) -> false)
                    .ignitedByLava()
                    .pushReaction(PushReaction.DESTROY),
                    type);
            registry.accept(MoreFood.id("block_leaves_" + id), leaves);
            LEAVES.put(type, leaves);
            final FruitPodBlock pod = new FruitPodBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, MoreFood.id("block_fruit_" + id)))
                    .mapColor(MapColor.PLANT)
                    .randomTicks()
                    .strength(0.2F, 3.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
                    .pushReaction(PushReaction.DESTROY),
                    type);
            registry.accept(MoreFood.id("block_fruit_" + id), pod);
            PODS.put(type, pod);
            final boolean sand = type == FruitTreeFeature.Type.COCONUT;
            final FruitSaplingBlock sapling = new FruitSaplingBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, MoreFood.id("block_sapling_" + id)))
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .randomTicks()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY), type, sand);
            registry.accept(MoreFood.id("block_sapling_" + id), sapling);
            SAPLINGS.put(type, sapling);
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
        MoreFoodMachines.registerBlocks(registry);
    }


    private static Block pillar(BiConsumer<Identifier, Block> registry, String name, MapColor color) {
        return register(registry, name, new RotatedPillarBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, MoreFood.id(name)))
                .mapColor(color)
                .instrument(NoteBlockInstrument.BASS)
                .strength(2.0F)
                .sound(SoundType.WOOD)
                .ignitedByLava()));
    }

    private static BlockBehaviour.Properties plankProps(String name, MapColor color) {
        return BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, MoreFood.id(name)))
                .mapColor(color)
                .instrument(NoteBlockInstrument.BASS)
                .strength(2.0F, 3.0F)
                .sound(SoundType.WOOD)
                .ignitedByLava();
    }

    private static BlockBehaviour.Properties doorProps(String name) {
        return BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, MoreFood.id(name)))
                .mapColor(MapColor.TERRACOTTA_ORANGE)
                .instrument(NoteBlockInstrument.BASS)
                .strength(3.0F)
                .sound(SoundType.WOOD)
                .noOcclusion()
                .ignitedByLava()
                .pushReaction(PushReaction.DESTROY);
    }

    private static Block register(BiConsumer<Identifier, Block> registry, String name, Block block) {
        registry.accept(MoreFood.id(name), block);
        return block;
    }

    /** Result of axe-stripping a palm log or wood block, keeping the axis. */
    public static @Nullable BlockState stripped(BlockState state) {
        final Block next;
        if (state.getBlock() == logPalm) next = strippedLogPalm;
        else if (state.getBlock() == woodPalm) next = strippedWoodPalm;
        else return null;
        BlockState out = next.defaultBlockState();
        if (state.hasProperty(RotatedPillarBlock.AXIS) && out.hasProperty(RotatedPillarBlock.AXIS)) {
            out = out.setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS));
        }
        return out;
    }

    public static void flammable(FlammableConsumer consumer) {
        for (Block block : new Block[]{logPalm, strippedLogPalm, woodPalm, strippedWoodPalm}) {
            consumer.accept(block, 5, 5);
        }
        for (Block block : new Block[]{planksPalm, stairsPalm, slabPalm, fencePalm, fenceGatePalm}) {
            consumer.accept(block, 5, 20);
        }
    }

    /** Forge / NeoForge have no public fire registry; setFlammable stays private without an AT. */
    public static void applyForgeFlammability() {
        try {
            final net.minecraft.world.level.block.FireBlock fire =
                    (net.minecraft.world.level.block.FireBlock) net.minecraft.world.level.block.Blocks.FIRE;
            final java.lang.reflect.Method method = net.minecraft.world.level.block.FireBlock.class
                    .getDeclaredMethod("setFlammable", Block.class, int.class, int.class);
            method.setAccessible(true);
            flammable((block, ignite, burn) -> {
                try {
                    method.invoke(fire, block, ignite, burn);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Could not register palm wood flammability", e);
        }
    }

    @FunctionalInterface
    public interface FlammableConsumer {
        void accept(Block block, int igniteOdds, int burnOdds);
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
