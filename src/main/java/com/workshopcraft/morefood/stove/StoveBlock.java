package com.workshopcraft.morefood.stove;

import com.mojang.serialization.MapCodec;
import com.workshopcraft.morefood.MoreFoodKitchen;
import com.workshopcraft.morefood.items.ItemBakingPeel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class StoveBlock extends BaseEntityBlock {
    //? if >=1.20.5 {
    public static final MapCodec<StoveBlock> CODEC = simpleCodec(StoveBlock::new);
    //?}
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final EnumProperty<HobTool> LEFT = EnumProperty.create("left", HobTool.class);
    public static final EnumProperty<HobTool> RIGHT = EnumProperty.create("right", HobTool.class);

    public StoveBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false)
                .setValue(LEFT, HobTool.NONE).setValue(RIGHT, HobTool.NONE));
    }

    //? if >=1.20.5 {
    @Override
    protected MapCodec<StoveBlock> codec() {
        return CODEC;
    }
    //?}

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StoveBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level instanceof ServerLevel server
                ? createTickerHelper(type, MoreFoodKitchen.stoveEntity, (l, pos, s, be) -> be.serverTick(server, pos, s))
                : createTickerHelper(type, MoreFoodKitchen.stoveEntity, (l, pos, s, be) -> be.clientTick(l, pos, s));
    }

    /** Which ring a hit on the top face lands on, as seen from the front. */
    public static int ringAt(BlockState state, BlockPos pos, Vec3 hit) {
        final Direction left = state.getValue(FACING).getClockWise();
        final double along = (hit.x - pos.getX() - 0.5) * left.getStepX() + (hit.z - pos.getZ() - 0.5) * left.getStepZ();
        return along > 0 ? StoveBlockEntity.LEFT : StoveBlockEntity.RIGHT;
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
        return result == InteractionResult.PASS ? useWithoutItem(state, level, pos, player, hit) : result;
    }
    *///?}

    private InteractionResult onItem(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.getItem() instanceof ItemBakingPeel && ItemBakingPeel.isLoaded(stack)) {
            if (level instanceof ServerLevel server && level.getBlockEntity(pos) instanceof StoveBlockEntity stove) ItemBakingPeel.useOnStove(server, stove, player, stack);
            return InteractionResult.SUCCESS;
        }
        if (hit.getDirection() != Direction.UP) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (!(level instanceof ServerLevel server) || !(level.getBlockEntity(pos) instanceof StoveBlockEntity stove)) {
            return InteractionResult.SUCCESS;
        }
        return stove.useHob(server, ringAt(state, pos, hit.getLocation()), player, hand, stack)
                ? InteractionResult.SUCCESS
                : InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    //? if >=1.20.5
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof StoveBlockEntity stove) {
            player.openMenu(stove);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
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

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        return level.getBlockEntity(pos) instanceof StoveBlockEntity stove ? stove.outputSignal() : 0;
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
        builder.add(FACING, LIT, LEFT, RIGHT);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) return;
        final double x = pos.getX() + 0.5;
        final double y = pos.getY();
        final double z = pos.getZ() + 0.5;
        if (random.nextDouble() < 0.1) {
            level.playLocalSound(x, y, z, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
        }
        final Direction facing = state.getValue(FACING);
        final double side = random.nextDouble() * 0.5 - 0.25;
        final double dx = facing.getAxis() == Direction.Axis.X ? facing.getStepX() * 0.52 : side;
        final double dz = facing.getAxis() == Direction.Axis.Z ? facing.getStepZ() * 0.52 : side;
        final double dy = (3 + random.nextDouble() * 5) / 16.0;
        level.addParticle(ParticleTypes.FLAME, x + dx, y + dy, z + dz, 0.0, 0.0, 0.0);
    }
}
