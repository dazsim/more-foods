package com.workshopcraft.morefood.client;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.workshopcraft.morefood.MoreFood;
import com.workshopcraft.morefood.MoreFoodKitchen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.List;

/** Item model condition: true when a pizza carries the given topping. */
public record HasTopping(Holder<Item> topping) implements ConditionalItemModelProperty {
    public static final Identifier ID = MoreFood.id("has_topping");
    public static final MapCodec<HasTopping> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Item.CODEC.fieldOf("topping").forGetter(HasTopping::topping)
    ).apply(i, HasTopping::new));

    @Override
    public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed, ItemDisplayContext context) {
        final List<Holder<Item>> toppings = stack.get(MoreFoodKitchen.TOPPINGS);
        if (toppings == null) return false;
        for (Holder<Item> holder : toppings) {
            if (holder.value() == topping.value()) return true;
        }
        return false;
    }

    @Override
    public MapCodec<HasTopping> type() {
        return MAP_CODEC;
    }
}
