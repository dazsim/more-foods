package com.workshopcraft.morefood.items;

import com.workshopcraft.morefood.MoreFoodBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.gameevent.GameEvent;

//the pot goes on a lit campfire as block_cooking_pot
public class ItemPot extends Item {
    public ItemPot(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        final Level level = context.getLevel();
        final BlockPos fire = context.getClickedPos();
        final BlockPos above = fire.above();
        if (!CampfireBlock.isLitCampfire(level.getBlockState(fire)) || !level.getBlockState(above).canBeReplaced()) return InteractionResult.PASS;
        if (!level.isClientSide()) {
            level.setBlock(above, MoreFoodBlocks.cookingPot.defaultBlockState(), 3);
            level.playSound(null, above, SoundEvents.LANTERN_PLACE, SoundSource.BLOCKS, 1, 1);
            level.gameEvent(context.getPlayer(), GameEvent.BLOCK_PLACE, above);
            context.getItemInHand().consume(1, context.getPlayer());
        }
        return InteractionResult.SUCCESS;
    }
}
