package com.workshopcraft.morefood.machine;

//? if >=1.20.5
import com.workshopcraft.morefood.MoreFoodKitchen;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
//? if >=26.1 {
import net.minecraft.world.item.component.TooltipDisplay;
//?} else {
/*import net.minecraft.world.level.Level;
*///?}

import java.util.List;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

/**
 * A churn as an item. Keeps the milk it had when broken (a component, or the block entity tag on 1.20.1), so full and
 * empty churns never stack together, and shows it.
 */
public class MilkChurnItem extends BlockItem {
    public MilkChurnItem(Block block, Properties properties) {
        super(block, properties);
    }

    /** The milk this churn holds, in mB. */
    public static int milk(ItemStack stack) {
        //? if >=1.20.5 {
        final int mb = stack.getOrDefault(MoreFoodKitchen.MILK, 0);
        //?} else {
        /*final net.minecraft.nbt.CompoundTag tag = stack.getTagElement("BlockEntityTag");
        final int mb = tag == null ? 0 : tag.getInt("milk");
        *///?}
        return Math.max(0, Math.min(MilkChurnBlockEntity.CAPACITY, mb));
    }

    private static void tooltip(ItemStack stack, Consumer<Component> out) {
        out.accept(Component.translatable("message.morefood.churn.milk", milk(stack), MilkChurnBlockEntity.CAPACITY).withStyle(ChatFormatting.GRAY));
    }

    //? if >=26.1 {
    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        tooltip(stack, builder);
    }
    //?} else if >=1.20.5 {
    /*@Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        tooltip(stack, lines::add);
    }
    *///?} else {
    /*@Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        tooltip(stack, lines::add);
    }
    *///?}
}
