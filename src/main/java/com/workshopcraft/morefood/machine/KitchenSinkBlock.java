package com.workshopcraft.morefood.machine;

import com.mojang.serialization.MapCodec;
import com.workshopcraft.morefood.MoreFoodMachines;
import com.workshopcraft.workshopcore.machine.FluidTanks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/** Never runs dry: fills buckets and bottles, washes dough into seitan, and lets pipes draw water. */
public class KitchenSinkBlock extends BaseEntityBlock {
    //? if >=1.20.5 && <26.3 {
    public static final MapCodec<KitchenSinkBlock> CODEC = simpleCodec(KitchenSinkBlock::new);
    //?}
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

    public KitchenSinkBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    //? if >=1.20.5 && <26.3 {
    @Override
    protected MapCodec<KitchenSinkBlock> codec() {
        return CODEC;
    }
    //?}

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new KitchenSinkBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level instanceof ServerLevel server
                ? createTickerHelper(type, MoreFoodMachines.sinkEntity, (l, pos, s, be) -> be.serverTick(server))
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
        return onItem(player.getItemInHand(hand), level, pos, player, hand);
    }
    *///?}

    private InteractionResult onItem(ItemStack stack, Level level, BlockPos pos, Player player, InteractionHand hand) {
        if (level.getBlockEntity(pos) instanceof KitchenSinkBlockEntity sink && FluidTanks.useItem(level, pos, player, hand, sink.water)) {
            return InteractionResult.SUCCESS;
        }
        final ProcessRecipe recipe = stack.isEmpty() ? null : MachineRecipes.sinkRecipe(level, stack);
        if (recipe == null) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, recipe.result().copy()));
        level.playSound(null, pos, stack.is(Items.BUCKET) ? SoundEvents.BUCKET_FILL : stack.is(Items.GLASS_BOTTLE) ? SoundEvents.BOTTLE_FILL
                : SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 1, 1);
        return InteractionResult.SUCCESS;
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
