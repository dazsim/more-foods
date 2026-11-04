package com.workshopcraft.morefood.stove;

import com.mojang.serialization.MapCodec;
import com.workshopcraft.morefood.MoreFoodKitchen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class CookingPotBlock extends BaseEntityBlock {
    //? if >=1.20.5 && <26.3 {
    public static final MapCodec<CookingPotBlock> CODEC = simpleCodec(CookingPotBlock::new);
    //?}
    private static final VoxelShape SHAPE = Block.box(3, 0, 3, 13, 4, 13);

    public CookingPotBlock(Properties properties) {
        super(properties);
    }

    //? if >=1.20.5 && <26.3 {
    @Override
    protected MapCodec<CookingPotBlock> codec() {
        return CODEC;
    }
    //?}

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CookingPotBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level instanceof ServerLevel server
                ? createTickerHelper(type, MoreFoodKitchen.cookingPotEntity, (l, pos, s, be) -> be.serverTick(server, pos, s))
                : createTickerHelper(type, MoreFoodKitchen.cookingPotEntity, (l, pos, s, be) -> be.clientTick(l, pos));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
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
        if (!(level instanceof ServerLevel server) || !(level.getBlockEntity(pos) instanceof CookingPotBlockEntity pot)) {
            return InteractionResult.SUCCESS;
        }
        return pot.use(server, player, hand, stack) ? InteractionResult.SUCCESS : InteractionResult.TRY_WITH_EMPTY_HAND;
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
        return level.getBlockEntity(pos) instanceof CookingPotBlockEntity pot ? pot.outputSignal() : 0;
    }
}
