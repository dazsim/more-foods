package com.workshopcraft.morefood.items;

import com.workshopcraft.morefood.MoreFoodKitchen;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.List;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

public class ItemPizza extends Item {
    public ItemPizza(Properties properties) {
        super(properties);
    }

    // pizzas given by command or creative skip the oven, so fix their food here
    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        final List<Holder<Item>> toppings = stack.get(MoreFoodKitchen.TOPPINGS);
        final FoodProperties food = stack.get(DataComponents.FOOD);
        if (toppings != null && !toppings.isEmpty() && food != null && food.nutrition() != MoreFoodKitchen.pizzaFood(toppings.size()).nutrition())
            MoreFoodKitchen.feedToppings(stack);
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
