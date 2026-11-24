package com.workshopcraft.morefood.blocks;

import com.mojang.serialization.MapCodec;
import com.workshopcraft.morefood.MoreFoodBlocks;
import com.workshopcraft.morefood.worldgen.FruitTreeFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
//? if >=26.1 {
import net.minecraft.world.level.ScheduledTickAccess;
//?} else {
/*import net.minecraft.world.level.LevelAccessor;
*///?}
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class FruitPodBlock extends Block implements BonemealableBlock {
    public static final int MAX_AGE = 2;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_2;
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    private final FruitTreeFeature.Type type;

    //? if >=1.20.5 && <26.3 {
    public static final MapCodec<FruitPodBlock> CODEC = simpleCodec(p -> new FruitPodBlock(p, FruitTreeFeature.Type.JACKFRUIT));

    @Override
    public MapCodec<? extends FruitPodBlock> codec() {
        return CODEC;
    }
    //?}

    public FruitPodBlock(Properties properties, FruitTreeFeature.Type type) {
        super(properties);
        this.type = type;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP).setValue(AGE, 0));
    }

    public FruitTreeFeature.Type treeType() {
        return type;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        final Direction facing = state.getValue(FACING);
        if (!hangsFromLeaf(level, pos, facing)) return false;
        return level.getBlockState(pos.relative(facing)).is(MoreFoodBlocks.LEAVES.get(type));
    }

    /** Pods hang under a leaf, or off a side face that has air below, never sitting on the canopy. */
    public static boolean hangsFromLeaf(LevelReader level, BlockPos pos, Direction facing) {
        if (facing == Direction.DOWN) return false;
        if (facing == Direction.UP) return true;
        return level.getBlockState(pos.below()).canBeReplaced();
    }

    private static boolean shouldDrop(BlockState state, Direction directionToNeighbour) {
        final Direction facing = state.getValue(FACING);
        if (directionToNeighbour == facing) return true;
        return facing != Direction.UP && directionToNeighbour == Direction.DOWN;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState();
        final LevelReader level = context.getLevel();
        final BlockPos pos = context.getClickedPos();
        for (Direction direction : context.getNearestLookingDirections()) {
            if (direction == Direction.DOWN) continue;
            state = state.setValue(FACING, direction);
            if (state.canSurvive(level, pos)) return state;
        }
        return null;
    }

    //? if >=26.1 {
    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour,
                                     BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        return shouldDrop(state, directionToNeighbour) && !state.canSurvive(level, pos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
    }
    //?} else {
    /*@Override
    public BlockState updateShape(BlockState state, Direction directionToNeighbour, BlockState neighbourState, LevelAccessor level, BlockPos pos,
                                  BlockPos neighbourPos) {
        return shouldDrop(state, directionToNeighbour) && !state.canSurvive(level, pos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, directionToNeighbour, neighbourState, level, pos, neighbourPos);
    }
    *///?}

    /** One in this many random ticks drops a ripe pod's fruit. A block gets a random tick about once a minute, so about 3 minutes. */
    public static final int DROP_CHANCE = 3;

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    /** Grows, and once ripe drops its fruit and starts again, so a tree keeps fruiting with nobody there. */
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        final int age = state.getValue(AGE);
        if (age < MAX_AGE) {
            if (random.nextInt(5) == 0) level.setBlock(pos, state.setValue(AGE, age + 1), 2);
        } else if (random.nextInt(DROP_CHANCE) == 0) {
            dropResources(state, level, pos);
            level.setBlock(pos, state.setValue(AGE, 0), 2);
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape(type, state.getValue(AGE), state.getValue(FACING));
    }

    //? if >=26.3 {
    /*@Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, net.minecraft.world.level.block.BonemealSource source) {
        return state.getValue(AGE) < MAX_AGE;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state, net.minecraft.world.level.block.BonemealSource source) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, net.minecraft.world.level.block.BonemealSource source) {
        level.setBlock(pos, state.setValue(AGE, Math.min(MAX_AGE, state.getValue(AGE) + 1)), 2);
    }
    *///?} else if >=1.20.2 {
    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return state.getValue(AGE) < MAX_AGE;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        level.setBlock(pos, state.setValue(AGE, Math.min(MAX_AGE, state.getValue(AGE) + 1)), 2);
    }
    //?} else {
    /*@Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
        return state.getValue(AGE) < MAX_AGE;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        level.setBlock(pos, state.setValue(AGE, Math.min(MAX_AGE, state.getValue(AGE) + 1)), 2);
    }
    *///?}

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, AGE);
    }

    public static boolean trySpawn(FruitTreeFeature.Type type, ServerLevel level, BlockPos leaf, RandomSource random) {
        final FruitPodBlock pod = MoreFoodBlocks.PODS.get(type);
        if (pod == null) return false;
        if (hasPod(level, leaf, pod)) return false;
        return place(pod, level, leaf, 0, type, random);
    }

    public static boolean placeOnTree(FruitTreeFeature.Type type, net.minecraft.world.level.WorldGenLevel level, BlockPos leaf, RandomSource random) {
        final FruitPodBlock pod = MoreFoodBlocks.PODS.get(type);
        if (pod == null) return false;
        return place(pod, level, leaf, random.nextInt(MAX_AGE + 1), type, random);
    }

    private static boolean place(FruitPodBlock pod, net.minecraft.world.level.LevelAccessor level, BlockPos leaf, int age,
                                 FruitTreeFeature.Type type, RandomSource random) {
        for (Direction outward : hangOrder(type, level, leaf, random)) {
            final BlockPos at = leaf.relative(outward);
            final BlockState want = pod.defaultBlockState().setValue(FACING, outward.getOpposite()).setValue(AGE, age);
            if (level.getBlockState(at).canBeReplaced() && want.canSurvive(level, at)) {
                level.setBlock(at, want, 2);
                return true;
            }
        }
        return false;
    }

    private static boolean hasPod(LevelReader level, BlockPos leaf, FruitPodBlock pod) {
        for (Direction outward : Direction.values()) {
            if (outward == Direction.UP) continue;
            final BlockState state = level.getBlockState(leaf.relative(outward));
            if (state.is(pod) && state.getValue(FACING) == outward.getOpposite()) return true;
        }
        return false;
    }

    private static Direction[] hangOrder(FruitTreeFeature.Type type, net.minecraft.world.level.BlockGetter level, BlockPos leaf, RandomSource random) {
        final Direction[] sides = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        for (int i = sides.length - 1; i > 0; i--) {
            final int j = random.nextInt(i + 1);
            final Direction tmp = sides[i];
            sides[i] = sides[j];
            sides[j] = tmp;
        }
        final java.util.List<Direction> out = new java.util.ArrayList<>();
        // avocados hang off the outer sides first so they can be seen from outside the canopy
        if (type == FruitTreeFeature.Type.AVOCADO) {
            for (Direction side : sides) {
                if (level.getBlockState(leaf.relative(side)).canBeReplaced()) out.add(side);
            }
            out.add(Direction.DOWN);
        } else {
            out.add(Direction.DOWN);
            for (Direction side : sides) out.add(side);
        }
        return out.toArray(new Direction[0]);
    }

    private static VoxelShape shape(FruitTreeFeature.Type type, int age, Direction facing) {
        if (facing == Direction.UP) return hang(type, age);
        return side(type, age, facing);
    }

    private static VoxelShape hang(FruitTreeFeature.Type type, int age) {
        if (type == FruitTreeFeature.Type.JACKFRUIT) {
            return switch (age) {
                case 0 -> Block.box(5, 9, 5, 11, 16, 11);
                case 1 -> Block.box(4, 5, 4, 12, 16, 12);
                default -> Block.box(3, 1, 3, 13, 16, 13);
            };
        }
        if (type == FruitTreeFeature.Type.COCONUT) {
            return switch (age) {
                case 0 -> Block.box(6, 11, 6, 10, 16, 10);
                case 1 -> Block.box(5, 8, 5, 11, 16, 11);
                default -> Block.box(4, 5, 4, 12, 16, 12);
            };
        }
        // olive, avocado
        return switch (age) {
            case 0 -> Block.box(6, 11, 6, 10, 16, 10);
            case 1 -> Block.box(6, 9, 6, 10, 16, 10);
            default -> Block.box(5, 7, 5, 11, 16, 11);
        };
    }

    private static VoxelShape side(FruitTreeFeature.Type type, int age, Direction facing) {
        // south-attached boxes, then rotated
        int x0, y0, z0, x1, y1, z1;
        if (type == FruitTreeFeature.Type.JACKFRUIT) {
            switch (age) {
                case 0 -> { x0 = 5; y0 = 8; z0 = 10; x1 = 11; y1 = 14; z1 = 16; }
                case 1 -> { x0 = 4; y0 = 5; z0 = 8; x1 = 12; y1 = 14; z1 = 16; }
                default -> { x0 = 3; y0 = 2; z0 = 6; x1 = 13; y1 = 14; z1 = 16; }
            }
        } else if (type == FruitTreeFeature.Type.COCONUT) {
            switch (age) {
                case 0 -> { x0 = 6; y0 = 9; z0 = 12; x1 = 10; y1 = 14; z1 = 16; }
                case 1 -> { x0 = 5; y0 = 7; z0 = 10; x1 = 11; y1 = 14; z1 = 16; }
                default -> { x0 = 4; y0 = 5; z0 = 8; x1 = 12; y1 = 14; z1 = 16; }
            }
        } else {
            switch (age) {
                case 0 -> { x0 = 6; y0 = 9; z0 = 12; x1 = 10; y1 = 13; z1 = 16; }
                case 1 -> { x0 = 6; y0 = 8; z0 = 11; x1 = 10; y1 = 13; z1 = 16; }
                default -> { x0 = 5; y0 = 6; z0 = 10; x1 = 11; y1 = 13; z1 = 16; }
            }
        }
        return rotateSouth(facing, x0, y0, z0, x1, y1, z1);
    }

    private static VoxelShape rotateSouth(Direction facing, int x0, int y0, int z0, int x1, int y1, int z1) {
        return switch (facing) {
            case SOUTH -> Block.box(x0, y0, z0, x1, y1, z1);
            case NORTH -> Block.box(16 - x1, y0, 16 - z1, 16 - x0, y1, 16 - z0);
            case WEST -> Block.box(16 - z1, y0, x0, 16 - z0, y1, x1);
            case EAST -> Block.box(z0, y0, 16 - x1, z1, y1, 16 - x0);
            default -> Block.box(x0, y0, z0, x1, y1, z1);
        };
    }
}
