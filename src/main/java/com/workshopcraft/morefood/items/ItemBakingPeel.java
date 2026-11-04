package com.workshopcraft.morefood.items;

import com.workshopcraft.morefood.MoreFoodKitchen;
import com.workshopcraft.morefood.board.AssemblyBoardBlockEntity;
import com.workshopcraft.morefood.stove.StoveBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

//carries a finished stack from the assembly board to the oven
public class ItemBakingPeel extends Item {
    public ItemBakingPeel(Properties properties) {
        super(properties);
    }

    public static boolean isLoaded(ItemStack peel) {
        return peel.has(MoreFoodKitchen.PEEL_CONTENTS);
    }

    public static ItemStack contents(ItemStack peel) {
        final ItemStackTemplate held = peel.get(MoreFoodKitchen.PEEL_CONTENTS);
        return held == null ? ItemStack.EMPTY : held.create();
    }

    /** A stove beside the board, or beside the worktop it sits on, with room in its oven. */
    public static @Nullable StoveBlockEntity stoveFor(ServerLevel level, BlockPos board, ItemStack stack) {
        for (BlockPos at : new BlockPos[]{board, board.below()}) {
            for (Direction side : Direction.Plane.HORIZONTAL) {
                if (level.getBlockEntity(at.relative(side)) instanceof StoveBlockEntity stove && stove.ovenAccepts(stack)) return stove;
            }
        }
        return null;
    }

    public static InteractionResult useOnBoard(ServerLevel level, AssemblyBoardBlockEntity board, Player player, ItemStack peel) {
        if (isLoaded(peel)) {
            player.sendOverlayMessage(Component.translatable("message.morefood.peel.full"));
            return InteractionResult.SUCCESS;
        }
        final ItemStack result = board.result(level);
        if (result.isEmpty()) {
            board.hint(level, player);
            return InteractionResult.SUCCESS;
        }
        final StoveBlockEntity stove = stoveFor(level, board.getBlockPos(), result);
        board.take(level, player);
        if (stove != null) {
            stove.ovenInsert(result);
            level.playSound(null, stove.getBlockPos(), SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.7F, 1.2F);
        } else {
            peel.set(MoreFoodKitchen.PEEL_CONTENTS, ItemStackTemplate.fromNonEmptyStack(result));
            level.playSound(null, board.getBlockPos(), SoundEvents.WOOD_HIT, SoundSource.BLOCKS, 0.6F, 1.2F);
        }
        return InteractionResult.SUCCESS;
    }

    public static void useOnStove(ServerLevel level, StoveBlockEntity stove, Player player, ItemStack peel) {
        final ItemStack held = contents(peel);
        if (!stove.canBake(held)) {
            player.sendOverlayMessage(Component.translatable("message.morefood.peel.not_bakeable", held.getHoverName()));
        } else if (!stove.ovenAccepts(held)) {
            player.sendOverlayMessage(Component.translatable("message.morefood.peel.oven_full"));
        } else {
            stove.ovenInsert(held);
            peel.remove(MoreFoodKitchen.PEEL_CONTENTS);
            level.playSound(null, stove.getBlockPos(), SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.7F, 1.2F);
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        final ItemStack peel = player.getItemInHand(hand);
        if (!player.isSecondaryUseActive() || !isLoaded(peel)) return InteractionResult.PASS;
        if (!level.isClientSide()) {
            player.getInventory().placeItemBackInInventory(contents(peel));
            peel.remove(MoreFoodKitchen.PEEL_CONTENTS);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        final ItemStack held = contents(stack);
        if (!held.isEmpty()) builder.accept(Component.translatable("tooltip.morefood.peel", held.getHoverName()).withStyle(ChatFormatting.GRAY));
    }
}
