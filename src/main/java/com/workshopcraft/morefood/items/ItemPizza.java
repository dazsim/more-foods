package com.workshopcraft.morefood.items;

import com.workshopcraft.morefood.MoreFoodKitchen;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
//? if >=26.1 {
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.component.TooltipDisplay;
//?} else if >=1.20.5 {
/*import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.level.Level;
*///?} else {
/*import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
*///?}

import java.util.List;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

public class ItemPizza extends Item {
    public ItemPizza(Properties properties) {
        super(properties);
    }

    //? if >=26.1 {
    // pizzas given by command or creative skip the oven, so fix their food here
    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        fixFood(stack);
    }
    //?} else if >=1.20.5 {
    /*// pizzas given by command or creative skip the oven, so fix their food here
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity owner, int slot, boolean selected) {
        if (!level.isClientSide()) fixFood(stack);
    }
    *///?} else {
    /*// no per stack food here, so the toppings are eaten on top of the plain pizza
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        final int toppings = Math.min(MoreFoodKitchen.toppings(stack).size(), MoreFoodKitchen.MAX_TOPPINGS);
        if (toppings > 0 && entity instanceof Player player) {
            final var base = MoreFoodKitchen.pizzaFood(0);
            final var full = MoreFoodKitchen.pizzaFood(toppings);
            final float saturation = full.getNutrition() * full.getSaturationModifier() - base.getNutrition() * base.getSaturationModifier();
            player.getFoodData().eat(toppings, saturation / (toppings));
        }
        return super.finishUsingItem(stack, level, entity);
    }
    *///?}

    //? if >=1.20.5 {
    private static void fixFood(ItemStack stack) {
        final List<Holder<Item>> toppings = MoreFoodKitchen.toppings(stack);
        final FoodProperties food = stack.get(DataComponents.FOOD);
        if (!toppings.isEmpty() && food != null && food.nutrition() != MoreFoodKitchen.pizzaFood(toppings.size()).nutrition())
            MoreFoodKitchen.feedToppings(stack);
    }
    //?}

    private static void tooltip(ItemStack stack, Consumer<Component> builder) {
        final List<Holder<Item>> toppings = MoreFoodKitchen.toppings(stack);
        if (toppings.isEmpty()) return;
        final MutableComponent names = Component.empty();
        for (int i = 0; i < toppings.size(); i++) {
            if (i > 0) names.append(", ");
            names.append(Component.translatable(toppings.get(i).value().getDescriptionId()));
        }
        builder.accept(Component.translatable("tooltip.morefood.toppings", names).withStyle(ChatFormatting.GRAY));
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
