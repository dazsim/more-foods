package com.workshopcraft.morefood.items;

import com.workshopcraft.morefood.MoreFoodKitchen;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.List;
import java.util.function.Consumer;

public class ItemPizza extends Item {
    public ItemPizza(Properties properties) {
        super(properties);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        final List<Holder<Item>> toppings = stack.get(MoreFoodKitchen.TOPPINGS);
        if (toppings == null || toppings.isEmpty()) return;
        final MutableComponent names = Component.empty();
        for (int i = 0; i < toppings.size(); i++) {
            if (i > 0) names.append(", ");
            names.append(Component.translatable(toppings.get(i).value().getDescriptionId()));
        }
        builder.accept(Component.translatable("tooltip.morefood.toppings", names).withStyle(ChatFormatting.GRAY));
    }
}
