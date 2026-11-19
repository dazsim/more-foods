package com.workshopcraft.morefood.worldgen;

import com.workshopcraft.morefood.MoreFoodBlocks;
import com.workshopcraft.morefood.blocks.FruitLeavesBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
//? if <26.3 {
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
//?} else {
/*import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
*///?}

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

//? if >=26.3 {
/*public class FruitTreeFeature implements Feature {
*///?} else {
public class FruitTreeFeature extends Feature<NoneFeatureConfiguration> {
//?}
    public enum Type {
        JACKFRUIT, COCONUT, OLIVE, AVOCADO
    }

    private static final Map<Type, FruitTreeFeature> BY_TYPE = new EnumMap<>(Type.class);
    private final Type type;

    public FruitTreeFeature(Type type) {
        //? if <26.3
        super(NoneFeatureConfiguration.CODEC);
        this.type = type;
        BY_TYPE.put(type, this);
    }

    public static FruitTreeFeature of(Type type) {
        final FruitTreeFeature existing = BY_TYPE.get(type);
        return existing != null ? existing : new FruitTreeFeature(type);
    }

    //? if >=26.3 {
    /*public static com.mojang.serialization.MapCodec<FruitTreeFeature> codecOf(Type type) {
        return com.mojang.serialization.MapCodec.unit(() -> of(type));
    }

    @Override
    public com.mojang.serialization.MapCodec<? extends Feature> codec() {
        return codecOf(type);
    }
    *///?}

    public Type treeType() {
        return type;
    }

    public static boolean grow(Type type, WorldGenLevel level, RandomSource random, BlockPos origin) {
        return of(type).grow(level, random, origin);
    }

    public boolean grow(WorldGenLevel level, RandomSource random, BlockPos origin) {
        final BlockState sapling = MoreFoodBlocks.SAPLINGS.get(type).defaultBlockState();
        if (!sapling.canSurvive(level, origin)) return false;
        return type == Type.COCONUT ? palm(level, random, origin) : canopy(level, random, origin);
    }

    //? if <26.3 {
    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        return grow(context.level(), context.random(), context.origin());
    }
    //?} else {
    /*@Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        return grow(level, random, origin);
    }
    *///?}

    private boolean canopy(WorldGenLevel level, RandomSource random, BlockPos origin) {
        final int height = switch (type) {
            case OLIVE -> 4 + random.nextInt(3);
            case AVOCADO -> 5 + random.nextInt(3);
            default -> 5 + random.nextInt(3);
        };
        final int radius = type == Type.OLIVE ? 2 : 2 + random.nextInt(2);
        if (!clearTrunk(level, origin, height)) return false;

        final BlockState log = log();
        final List<BlockPos> logs = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            final BlockPos pos = origin.above(y);
            setLog(level, pos, log);
            logs.add(pos.immutable());
        }
        // olive trees lean a little
        if (type == Type.OLIVE && random.nextBoolean()) {
            final Direction lean = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            final BlockPos side = origin.above(height - 1).relative(lean);
            if (replaceable(level, side)) {
                setLog(level, side, log.setValue(RotatedPillarBlock.AXIS, lean.getAxis()));
                logs.add(side.immutable());
            }
        }

        final int crown = origin.getY() + height - 1;
        final BlockPos tip = logs.get(logs.size() - 1);
        final List<Leaf> leaves = new ArrayList<>();
        for (int dx = -radius - 1; dx <= radius + 1; dx++) {
            for (int dz = -radius - 1; dz <= radius + 1; dz++) {
                for (int dy = -1; dy <= 2; dy++) {
                    final int dist = Math.abs(dx) + Math.abs(dz) + Math.max(0, dy);
                    final int limit = radius + (dy == 0 ? 1 : dy == 1 ? 0 : -1);
                    if (dist > limit + (type == Type.OLIVE && (dx + dz) % 2 == 0 ? -1 : 0)) continue;
                    if (dy == 2 && (Math.abs(dx) > 1 || Math.abs(dz) > 1)) continue;
                    final BlockPos pos = new BlockPos(tip.getX() + dx, crown + dy, tip.getZ() + dz);
                    if (logs.contains(pos)) continue;
                    if (!replaceable(level, pos)) continue;
                    final boolean fruit = dist >= 1 && random.nextInt(type == Type.JACKFRUIT ? 3 : 4) == 0;
                    leaves.add(new Leaf(pos, fruit));
                }
            }
        }
        placeLeaves(level, logs, leaves);
        return true;
    }

    private boolean palm(WorldGenLevel level, RandomSource random, BlockPos origin) {
        final int height = 6 + random.nextInt(3);
        final Direction lean = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        final int leanAt = 3 + random.nextInt(2);
        if (!clearPalm(level, origin, height, lean, leanAt)) return false;

        final BlockState log = log();
        final List<BlockPos> logs = new ArrayList<>();
        int x = origin.getX();
        int z = origin.getZ();
        for (int y = 0; y < height; y++) {
            if (y == leanAt) {
                x += lean.getStepX();
                z += lean.getStepZ();
            }
            final BlockPos pos = new BlockPos(x, origin.getY() + y, z);
            final BlockState placed = y == leanAt ? log.setValue(RotatedPillarBlock.AXIS, lean.getAxis()) : log;
            setLog(level, pos, placed);
            logs.add(pos.immutable());
        }
        final BlockPos tip = logs.get(logs.size() - 1);
        final List<Leaf> leaves = new ArrayList<>();
        // crown around the top
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = 0; dy <= 1; dy++) {
                    if (dx == 0 && dz == 0 && dy == 0) continue;
                    final BlockPos pos = tip.offset(dx, dy, dz);
                    if (replaceable(level, pos)) leaves.add(new Leaf(pos, dy == 0 && (dx == 0 || dz == 0) && random.nextBoolean()));
                }
            }
        }
        // fronds
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            final int length = 2 + random.nextInt(2);
            for (int i = 1; i <= length; i++) {
                final BlockPos pos = tip.relative(dir, i).above(i == length ? 0 : (i == 1 ? 1 : 0));
                if (replaceable(level, pos)) leaves.add(new Leaf(pos, i == 1 && random.nextInt(3) == 0));
                if (i > 1) {
                    final BlockPos drop = pos.below();
                    if (replaceable(level, drop)) leaves.add(new Leaf(drop, random.nextInt(2) == 0));
                }
            }
        }
        placeLeaves(level, logs, leaves);
        return true;
    }

    private BlockState log() {
        return switch (type) {
            case COCONUT -> MoreFoodBlocks.logPalm.defaultBlockState();
            case JACKFRUIT -> Blocks.JUNGLE_LOG.defaultBlockState();
            case OLIVE, AVOCADO -> Blocks.OAK_LOG.defaultBlockState();
        };
    }

    private BlockState leaves() {
        return MoreFoodBlocks.LEAVES.get(type).defaultBlockState()
                .setValue(LeavesBlock.PERSISTENT, false)
                .setValue(LeavesBlock.DISTANCE, 1);
    }

    private void setLog(WorldGenLevel level, BlockPos pos, BlockState log) {
        level.setBlock(pos, log, 2);
    }

    private void placeLeaves(WorldGenLevel level, List<BlockPos> logs, List<Leaf> leaves) {
        final BlockState base = leaves();
        for (Leaf leaf : leaves) {
            int dist = 7;
            for (BlockPos log : logs) {
                dist = Math.min(dist, Math.abs(log.getX() - leaf.pos.getX()) + Math.abs(log.getY() - leaf.pos.getY()) + Math.abs(log.getZ() - leaf.pos.getZ()));
            }
            dist = Math.max(1, Math.min(7, dist));
            level.setBlock(leaf.pos, base.setValue(LeavesBlock.DISTANCE, dist).setValue(FruitLeavesBlock.FRUIT, leaf.fruit), 2);
        }
    }

    private boolean clearTrunk(WorldGenLevel level, BlockPos origin, int height) {
        for (int y = 0; y < height; y++) {
            if (!replaceable(level, origin.above(y))) return false;
        }
        return true;
    }

    private boolean clearPalm(WorldGenLevel level, BlockPos origin, int height, Direction lean, int leanAt) {
        int x = origin.getX();
        int z = origin.getZ();
        for (int y = 0; y < height; y++) {
            if (y == leanAt) {
                x += lean.getStepX();
                z += lean.getStepZ();
            }
            if (!replaceable(level, new BlockPos(x, origin.getY() + y, z))) return false;
        }
        return true;
    }

    private static boolean replaceable(WorldGenLevel level, BlockPos pos) {
        final BlockState state = level.getBlockState(pos);
        return state.canBeReplaced() || state.getBlock() instanceof com.workshopcraft.morefood.blocks.FruitSaplingBlock;
    }

    private record Leaf(BlockPos pos, boolean fruit) {}
}
