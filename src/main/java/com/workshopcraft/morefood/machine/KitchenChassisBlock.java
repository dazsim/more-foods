package com.workshopcraft.morefood.machine;

import com.workshopcraft.morefood.MoreFoodMachines;
import com.workshopcraft.workshopcore.compat.Players;
import com.workshopcraft.workshopcore.machine.ChassisBlock;
import com.workshopcraft.workshopcore.machine.Finishes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** The kitchen chassis. Only kitchen panels go on it; other mods' panels are refused instead of being lost. */
public class KitchenChassisBlock extends ChassisBlock {
    public KitchenChassisBlock(Builder builder, Properties properties) {
        super(builder, properties);
    }

    //? if >=1.20.5 && <26.1 {
    /*@Override
    protected net.minecraft.world.ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                                                  InteractionHand hand, BlockHitResult hit) {
        if (refuse(stack, level, player)) return net.minecraft.world.ItemInteractionResult.SUCCESS;
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }
    *///?} else {
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (refuse(stack, level, player)) return InteractionResult.SUCCESS;
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }
    //?}

    private static boolean refuse(ItemStack stack, Level level, Player player) {
        final String finish = Finishes.of(stack);
        if (finish == null || MoreFoodMachines.FINISH.equals(finish)) return false;
        if (!level.isClientSide()) Players.overlay(player, Component.translatable("message.morefood.kitchen_panels_only"));
        return true;
    }
}
