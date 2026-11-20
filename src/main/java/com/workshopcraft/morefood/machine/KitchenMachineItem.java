package com.workshopcraft.morefood.machine;

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

/** A finished grinder or press, carrying the parts it was assembled from. */
public class KitchenMachineItem extends BlockItem {
    public KitchenMachineItem(Block block, Properties properties) {
        super(block, properties);
    }

    private static void tooltip(ItemStack stack, Consumer<Component> out) {
        //? if >=1.20.5 {
        final MachinePartsData data = stack.get(MoreFoodKitchen.MACHINE_PARTS);
        if (data == null || data.isEmpty()) return;
        for (ItemStack part : data.listed()) {
            out.accept(part.getHoverName().copy().withStyle(ChatFormatting.GRAY));
        }
        //?} else {
        /*final net.minecraft.nbt.CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("BlockEntityTag", net.minecraft.nbt.Tag.TAG_COMPOUND)) return;
        final net.minecraft.nbt.CompoundTag be = tag.getCompound("BlockEntityTag");
        for (String key : List.of("parts_chassis", "parts_core", "parts_board", "parts_panels", "parts_worktop")) {
            if (be.contains(key, net.minecraft.nbt.Tag.TAG_COMPOUND)) {
                final ItemStack part = ItemStack.of(be.getCompound(key));
                if (!part.isEmpty()) out.accept(part.getHoverName().copy().withStyle(ChatFormatting.GRAY));
            }
        }
        if (be.contains("parts_upgrades", net.minecraft.nbt.Tag.TAG_LIST)) {
            final net.minecraft.nbt.ListTag list = be.getList("parts_upgrades", net.minecraft.nbt.Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                final ItemStack part = ItemStack.of(list.getCompound(i));
                if (!part.isEmpty()) out.accept(part.getHoverName().copy().withStyle(ChatFormatting.GRAY));
            }
        }
        *///?}
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
