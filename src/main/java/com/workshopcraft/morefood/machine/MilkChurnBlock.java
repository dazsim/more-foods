package com.workshopcraft.morefood.machine;

import com.mojang.serialization.MapCodec;
import com.workshopcraft.morefood.MoreFoodMachines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/** Two blocks tall like a door. Holds up to 8 buckets of milk, shown in the window on the front. */
public class MilkChurnBlock extends BaseEntityBlock {
    //? if >=1.20.5 && <26.3 {
    public static final MapCodec<MilkChurnBlock> CODEC = simpleCodec(MilkChurnBlock::new);
    //?}
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final IntegerProperty MILK = IntegerProperty.create("milk", 0, 8);
    private static final VoxelShape LOWER = Block.box(3, 0, 3, 13, 16, 13);
    private static final VoxelShape UPPER = Block.box(3, 0, 3, 13, 14, 13);

    public MilkChurnBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(HALF, DoubleBlockHalf.LOWER).setValue(MILK, 0));
    }

    //? if >=1.20.5 && <26.3 {
    @Override
    protected MapCodec<MilkChurnBlock> codec() {
        return CODEC;
    }
    //?}

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF, MILK);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        final BlockPos pos = ctx.getClickedPos();
        final Level level = ctx.getLevel();
        if (level.isOutsideBuildHeight(pos.above()) || !level.getBlockState(pos.above()).canBeReplaced(ctx)) return null;
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity by, ItemStack stack) {
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);
    }

    private static boolean pairs(BlockState state, Direction towards, BlockState neighbour) {
        final boolean lower = state.getValue(HALF) == DoubleBlockHalf.LOWER;
        if (towards != (lower ? Direction.UP : Direction.DOWN)) return true;
        return neighbour.getBlock() instanceof MilkChurnBlock && neighbour.getValue(HALF) != state.getValue(HALF);
    }

    //? if >=26.1 {
    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction towards,
                                     BlockPos neighbourPos, BlockState neighbour, RandomSource random) {
        return pairs(state, towards, neighbour) ? super.updateShape(state, level, ticks, pos, towards, neighbourPos, neighbour, random)
                : Blocks.AIR.defaultBlockState();
    }
    //?} else {
    /*@Override
    public BlockState updateShape(BlockState state, Direction towards, BlockState neighbour, LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        return pairs(state, towards, neighbour) ? super.updateShape(state, towards, neighbour, level, pos, neighbourPos)
                : Blocks.AIR.defaultBlockState();
    }
    *///?}

    // in creative, breaking the top must not drop the churn from the bottom half
    private static void creativeBreak(Level level, BlockPos pos, BlockState state, Player player) {
        if (level.isClientSide() || !player.isCreative() || state.getValue(HALF) != DoubleBlockHalf.UPPER) return;
        final BlockPos below = pos.below();
        final BlockState lower = level.getBlockState(below);
        if (lower.getBlock() instanceof MilkChurnBlock && lower.getValue(HALF) == DoubleBlockHalf.LOWER) {
            level.setBlock(below, Blocks.AIR.defaultBlockState(), 35);
            level.levelEvent(player, 2001, below, Block.getId(lower));
        }
    }

    //? if >=1.20.5 {
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        creativeBreak(level, pos, state, player);
        return super.playerWillDestroy(level, pos, state, player);
    }
    //?} else {
    /*@Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        creativeBreak(level, pos, state, player);
        super.playerWillDestroy(level, pos, state, player);
    }
    *///?}

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? LOWER : UPPER;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MilkChurnBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level instanceof ServerLevel && state.getValue(HALF) == DoubleBlockHalf.LOWER
                ? createTickerHelper(type, MoreFoodMachines.churnEntity, (l, pos, s, be) -> be.serverTick())
                : null;
    }

    //? if >=26.1 {
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return onItem(stack, level, pos, player, hand);
    }
    //?} else if >=1.20.5 {
    /*@Override
    protected net.minecraft.world.ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                                                  InteractionHand hand, BlockHitResult hit) {
        return onItem(stack, level, pos, player, hand) == InteractionResult.PASS
                ? net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION : net.minecraft.world.ItemInteractionResult.SUCCESS;
    }
    *///?} else {
    /*@Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        final InteractionResult result = onItem(player.getItemInHand(hand), level, pos, player, hand);
        return result == InteractionResult.PASS && player.getItemInHand(hand).isEmpty() ? useWithoutItem(state, level, pos, player, hit) : result;
    }
    *///?}

    private InteractionResult onItem(ItemStack stack, Level level, BlockPos pos, Player player, InteractionHand hand) {
        if (!(level.getBlockEntity(pos) instanceof MilkChurnBlockEntity churn) || !MilkChurnBlockEntity.handles(stack)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        return churn.main().use(player, hand, stack) ? InteractionResult.SUCCESS : InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    //? if >=1.20.5
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof MilkChurnBlockEntity churn) {
            player.sendOverlayMessage(Component.translatable("message.morefood.churn.milk", churn.main().milk(), MilkChurnBlockEntity.CAPACITY));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        return level.getBlockEntity(pos) instanceof MilkChurnBlockEntity churn ? churn.main().milk() * 15 / MilkChurnBlockEntity.CAPACITY : 0;
    }

    //? if >=26.1 {
    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        Containers.updateNeighboursAfterDestroy(state, level, pos);
    }
    //?}
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
}
