package com.workshopcraft.morefood.machine;

import com.workshopcraft.workshopcore.machine.MachineComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
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

/** A finished grinder or press. Lists its power part and upgrades, which it keeps when mined. */
public class KitchenMachineItem extends BlockItem {
    public KitchenMachineItem(Block block, Properties properties) {
        super(block, properties);
    }

    private static void tooltip(ItemStack stack, Consumer<Component> out) {
        final MachineComponents c = MachineComponents.CORE;
        //? if >=1.20.5 {
        part(stack.get(c.drive()), out);
        final String upgrades = stack.get(c.upgrades());
        //?} else {
        /*final net.minecraft.nbt.CompoundTag tag = stack.getTagElement(c.storageKey());
        if (tag == null) return;
        part(tag.getString("drive"), out);
        final String upgrades = tag.getString("upgrades");
        *///?}
        if (upgrades != null && !upgrades.isEmpty()) {
            for (String id : upgrades.split(",")) part(id.trim(), out);
        }
    }

    private static void part(@Nullable String id, Consumer<Component> out) {
        final Identifier key = id == null || id.isEmpty() ? null : Identifier.tryParse(id);
        if (key == null || !BuiltInRegistries.ITEM.containsKey(key)) return;
        //? if >=26.1 {
        final ItemStack part = new ItemStack(BuiltInRegistries.ITEM.getValue(key));
        //?} else {
        /*final ItemStack part = new ItemStack(BuiltInRegistries.ITEM.get(key));
        *///?}
        out.accept(part.getHoverName().copy().withStyle(ChatFormatting.GRAY));
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
