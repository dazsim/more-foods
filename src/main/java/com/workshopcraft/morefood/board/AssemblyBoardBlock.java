package com.workshopcraft.morefood.board;

import com.mojang.serialization.MapCodec;
import com.workshopcraft.morefood.MoreFoodBlocks;
import com.workshopcraft.morefood.MoreFoodKitchen;
import com.workshopcraft.morefood.items.ItemBakingPeel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
//? if >=26.1 {
import net.minecraft.world.level.ScheduledTickAccess;
//?} else {
/*import net.minecraft.world.level.LevelAccessor;
*///?}
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AssemblyBoardBlock extends BaseEntityBlock {
    //? if >=1.20.5 {
    public static final MapCodec<AssemblyBoardBlock> CODEC = simpleCodec(AssemblyBoardBlock::new);
    //?}
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    private static final VoxelShape BOARD_Z = Block.box(1, 0, 2, 15, 2, 14);
    private static final VoxelShape BOARD_X = Block.box(2, 0, 1, 14, 2, 15);
    private static final VoxelShape[] STACK_Z = new VoxelShape[13];
    private static final VoxelShape[] STACK_X = new VoxelShape[13];

    static {
        for (int h = 0; h < 13; h++) {
            STACK_Z[h] = Block.box(1, 0, 2, 15, Math.max(2, h), 14);
            STACK_X[h] = Block.box(2, 0, 1, 14, Math.max(2, h), 15);
        }
    }

    public AssemblyBoardBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    //? if >=1.20.5 {
    @Override
    protected MapCodec<AssemblyBoardBlock> codec() {
        return CODEC;
    }
    //?}

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AssemblyBoardBlockEntity(pos, state);
    }

    private static boolean alongX(BlockState state) {
        return state.getValue(FACING).getAxis() == Direction.Axis.Z;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        final int layers = level.getBlockEntity(pos) instanceof AssemblyBoardBlockEntity board ? board.layers().size() : 0;
        final int height = Math.min(12, 2 + (layers * 5 + 3) / 4);
        return alongX(state) ? STACK_Z[height] : STACK_X[height];
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return alongX(state) ? BOARD_Z : BOARD_X;
    }

    public static boolean canSitOn(LevelReader level, BlockPos below) {
        final BlockState support = level.getBlockState(below);
        if (support.is(MoreFoodBlocks.stove)) return false;
        if (support.is(MoreFoodKitchen.BOARD_SUPPORTS)) return !unassembled(support);
        return support.isFaceSturdy(level, below, Direction.UP, SupportType.FULL);
    }

    // a Much More Storage carcass has no fixed worktop until it is screwed together
    private static boolean unassembled(BlockState state) {
        for (Property<?> property : state.getProperties()) {
            if (property instanceof BooleanProperty flag && flag.getName().equals("assembled")) return !state.getValue(flag);
        }
        return false;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return canSitOn(level, pos.below());
    }

    //? if >=26.1 {
    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour,
                                     BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        return directionToNeighbour == Direction.DOWN && !canSurvive(state, level, pos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
    }
    //?} else {
    /*@Override
    public BlockState updateShape(BlockState state, Direction directionToNeighbour, BlockState neighbourState, LevelAccessor level, BlockPos pos,
                                  BlockPos neighbourPos) {
        return directionToNeighbour == Direction.DOWN && !canSurvive(state, level, pos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, directionToNeighbour, neighbourState, level, pos, neighbourPos);
    }
    *///?}

    //? if <26.1 {
    /*@Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        com.workshopcraft.morefood.Compat.removed(state, level, pos, newState);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public net.minecraft.world.level.block.RenderShape getRenderShape(BlockState state) {
        return net.minecraft.world.level.block.RenderShape.MODEL;
    }
    *///?}

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    //? if >=26.1 {
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return onItem(stack, state, level, pos, player, hand, hit);
    }
    //?} else if >=1.20.5 {
    /*@Override
    protected net.minecraft.world.ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                                                  InteractionHand hand, BlockHitResult hit) {
        return onItem(stack, state, level, pos, player, hand, hit) == InteractionResult.PASS
                ? net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION : net.minecraft.world.ItemInteractionResult.SUCCESS;
    }
    *///?} else {
    /*@Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        final InteractionResult result = onItem(player.getItemInHand(hand), state, level, pos, player, hand, hit);
        return result == InteractionResult.PASS ? InteractionResult.PASS : result;
    }
    *///?}

    private InteractionResult onItem(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level instanceof ServerLevel server) || !(level.getBlockEntity(pos) instanceof AssemblyBoardBlockEntity board)) {
            return InteractionResult.SUCCESS;
        }
        if (stack.getItem() instanceof ItemBakingPeel) return ItemBakingPeel.useOnBoard(server, board, player, stack);
        return board.use(server, player, hand, stack) ? InteractionResult.SUCCESS : InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        return level.getBlockEntity(pos) instanceof AssemblyBoardBlockEntity board ? board.outputSignal() : 0;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
