package com.workshopcraft.morefood.worldgen;

import com.workshopcraft.morefood.MoreFoodBlocks;
import com.workshopcraft.morefood.blocks.FruitPodBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
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
        return growAt(level, random, origin);
    }

    private boolean placeFromWorldgen(WorldGenLevel level, RandomSource random, BlockPos origin) {
        if (growAt(level, random, origin)) return true;
        if (type != Type.JACKFRUIT && type != Type.AVOCADO) return false;
        for (int i = 0; i < 10; i++) {
            final int dx = random.nextInt(9) - 4;
            final int dz = random.nextInt(9) - 4;
            if (dx == 0 && dz == 0) continue;
            final BlockPos at = findGround(level, origin.offset(dx, 0, dz));
            if (at != null && growAt(level, random, at)) return true;
        }
        return false;
    }

    private boolean growAt(WorldGenLevel level, RandomSource random, BlockPos origin) {
        final BlockState sapling = MoreFoodBlocks.SAPLINGS.get(type).defaultBlockState();
        if (!sapling.canSurvive(level, origin)) return false;
        return type == Type.COCONUT ? palm(level, random, origin) : canopy(level, random, origin);
    }

    //? if <26.3 {
    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        return placeFromWorldgen(context.level(), context.random(), context.origin());
    }
    //?} else {
    /*@Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        return placeFromWorldgen(level, random, origin);
    }
    *///?}

    private boolean canopy(WorldGenLevel level, RandomSource random, BlockPos origin) {
        final int height = switch (type) {
            case OLIVE -> 6 + random.nextInt(4);
            case AVOCADO -> 8 + random.nextInt(5);
            default -> 9 + random.nextInt(6);
        };
        final int radius = switch (type) {
            case OLIVE -> 3 + random.nextInt(2);
            case AVOCADO -> 3 + random.nextInt(2);
            default -> 4 + random.nextInt(2);
        };
        if (!clearTrunk(level, origin, height)) return false;
        if (overlapsOtherTree(level, origin, height, radius)) return false;

        final BlockState log = log();
        final List<BlockPos> logs = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            final BlockPos pos = origin.above(y);
            setLog(level, pos, log);
            logs.add(pos.immutable());
        }
        // olive trees lean a little, keep the branch log upright so no end grain shows
        if (type == Type.OLIVE && random.nextBoolean()) {
            final Direction lean = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            final BlockPos side = origin.above(Math.max(2, height - 2)).relative(lean);
            if (replaceable(level, side) && !isOtherTree(level.getBlockState(side))) {
                setLog(level, side, log);
                logs.add(side.immutable());
            }
        }

        final int crown = origin.getY() + height - 1;
        final BlockPos tip = logs.get(logs.size() - 1);
        final int top = type == Type.OLIVE ? 1 : 2;
        final List<Leaf> leaves = new ArrayList<>();
        for (int dx = -radius - 1; dx <= radius + 1; dx++) {
            for (int dz = -radius - 1; dz <= radius + 1; dz++) {
                for (int dy = -1; dy <= top; dy++) {
                    final int dist = Math.abs(dx) + Math.abs(dz) + Math.max(0, dy);
                    final int limit = radius + (dy == 0 ? 1 : dy == 1 ? 0 : -1);
                    if (dist > limit + (type == Type.OLIVE && (dx + dz) % 2 == 0 ? -1 : 0)) continue;
                    if (dy == top && (Math.abs(dx) > 1 || Math.abs(dz) > 1)) continue;
                    final BlockPos pos = new BlockPos(tip.getX() + dx, crown + dy, tip.getZ() + dz);
                    if (logs.contains(pos)) continue;
                    if (!replaceable(level, pos) || isOtherTree(level.getBlockState(pos))) continue;
                    final boolean edge = Math.abs(dx) >= radius - 1 || Math.abs(dz) >= radius - 1 || dy == -1;
                    final boolean fruit = switch (type) {
                        case AVOCADO -> edge && random.nextInt(3) == 0;
                        case JACKFRUIT -> dist >= 1 && random.nextInt(3) == 0;
                        default -> dist >= 1 && random.nextInt(4) == 0;
                    };
                    leaves.add(new Leaf(pos, fruit));
                }
            }
        }
        placeLeaves(level, logs, leaves, random);
        return true;
    }

    private boolean palm(WorldGenLevel level, RandomSource random, BlockPos origin) {
        final int height = 9 + random.nextInt(5);
        final Direction lean = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        final int leanAt = 4 + random.nextInt(3);
        final int leanAgain = Math.min(height - 3, leanAt + 2 + random.nextInt(2));
        if (!clearPalm(level, origin, height, lean, leanAt, leanAgain)) return false;

        final BlockState log = log();
        final List<BlockPos> logs = new ArrayList<>();
        int x = origin.getX();
        int z = origin.getZ();
        for (int y = 0; y < height; y++) {
            final boolean step = y == leanAt || y == leanAgain;
            if (step) {
                final BlockPos knuckle = new BlockPos(x, origin.getY() + y, z);
                if (replaceable(level, knuckle)) {
                    setLog(level, knuckle, log);
                    logs.add(knuckle.immutable());
                }
                x += lean.getStepX();
                z += lean.getStepZ();
            }
            final BlockPos pos = new BlockPos(x, origin.getY() + y, z);
            setLog(level, pos, log);
            logs.add(pos.immutable());
        }
        final BlockPos tip = logs.get(logs.size() - 1);
        final List<Leaf> leaves = new ArrayList<>();
        // crown around the top
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = 0; dy <= 1; dy++) {
                    if (dx == 0 && dz == 0 && dy == 0) continue;
                    if (Math.abs(dx) == 2 && Math.abs(dz) == 2) continue;
                    if (dy == 1 && (Math.abs(dx) > 1 || Math.abs(dz) > 1)) continue;
                    final BlockPos pos = tip.offset(dx, dy, dz);
                    final boolean fruit = dy == 0 && (Math.abs(dx) + Math.abs(dz) == 1);
                    if (replaceable(level, pos)) leaves.add(new Leaf(pos, fruit));
                }
            }
        }
        // fronds
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            final int length = 3 + random.nextInt(3);
            for (int i = 1; i <= length; i++) {
                final int lift = i == 1 || i < length - 1 ? 1 : 0;
                final BlockPos pos = tip.relative(dir, i).above(lift);
                if (replaceable(level, pos)) leaves.add(new Leaf(pos, i == 1));
                if (i > 1) {
                    final BlockPos drop = pos.below();
                    if (replaceable(level, drop)) leaves.add(new Leaf(drop, false));
                }
            }
        }
        placeLeaves(level, logs, leaves, random);
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

    private void placeLeaves(WorldGenLevel level, List<BlockPos> logs, List<Leaf> leaves, RandomSource random) {
        final BlockState base = leaves();
        for (Leaf leaf : leaves) {
            int dist = 7;
            for (BlockPos log : logs) {
                dist = Math.min(dist, Math.abs(log.getX() - leaf.pos.getX()) + Math.abs(log.getY() - leaf.pos.getY()) + Math.abs(log.getZ() - leaf.pos.getZ()));
            }
            dist = Math.max(1, Math.min(7, dist));
            level.setBlock(leaf.pos, base.setValue(LeavesBlock.DISTANCE, dist), 2);
        }
        for (Leaf leaf : leaves) {
            if (leaf.fruit) FruitPodBlock.placeOnTree(type, level, leaf.pos, random);
        }
    }

    private boolean clearTrunk(WorldGenLevel level, BlockPos origin, int height) {
        for (int y = 0; y < height; y++) {
            if (!replaceable(level, origin.above(y))) return false;
        }
        return true;
    }

    private boolean clearPalm(WorldGenLevel level, BlockPos origin, int height, Direction lean, int leanAt, int leanAgain) {
        int x = origin.getX();
        int z = origin.getZ();
        for (int y = 0; y < height; y++) {
            if (y == leanAt || y == leanAgain) {
                if (!replaceable(level, new BlockPos(x, origin.getY() + y, z))) return false;
                x += lean.getStepX();
                z += lean.getStepZ();
            }
            if (!replaceable(level, new BlockPos(x, origin.getY() + y, z))) return false;
        }
        return true;
    }

    private boolean overlapsOtherTree(WorldGenLevel level, BlockPos origin, int height, int radius) {
        if (type != Type.JACKFRUIT && type != Type.AVOCADO) return false;
        for (int y = 1; y <= height + 2; y++) {
            final int r = y < height - 1 ? 1 : radius;
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (dx == 0 && dz == 0 && y < height) continue;
                    if (isOtherTree(level.getBlockState(origin.offset(dx, y, dz)))) return true;
                }
            }
        }
        return false;
    }

    private static boolean isOtherTree(BlockState state) {
        return state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES);
    }

    private BlockPos findGround(WorldGenLevel level, BlockPos approx) {
        for (int dy = 3; dy >= -4; dy--) {
            final BlockPos at = approx.above(dy);
            if (replaceable(level, at) && MoreFoodBlocks.SAPLINGS.get(type).defaultBlockState().canSurvive(level, at)) {
                return at;
            }
        }
        return null;
    }

    private static boolean replaceable(WorldGenLevel level, BlockPos pos) {
        final BlockState state = level.getBlockState(pos);
        return state.canBeReplaced() || state.getBlock() instanceof com.workshopcraft.morefood.blocks.FruitSaplingBlock;
    }

    private record Leaf(BlockPos pos, boolean fruit) {}
}
