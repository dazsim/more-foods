package com.workshopcraft.morefood.machine;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;
//? if >=26.1 {
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
//?}

/** One recipe input: an item or tag, a count, or a water bottle. */
public record CountedIngredient(Ingredient ingredient, int count, boolean waterBottle) {
    private static final Codec<CountedIngredient> WATER = RecordCodecBuilder.create(i -> i.group(
            Codec.BOOL.fieldOf("water_bottle").forGetter(CountedIngredient::waterBottle),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("count", 1).forGetter(CountedIngredient::count)
    ).apply(i, (water, count) -> new CountedIngredient(Ingredient.of(), count, water)));

    private static final Codec<CountedIngredient> COUNTED = RecordCodecBuilder.create(i -> i.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(CountedIngredient::ingredient),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("count", 1).forGetter(CountedIngredient::count)
    ).apply(i, (ingredient, count) -> new CountedIngredient(ingredient, count, false)));

    public static final Codec<CountedIngredient> COUNTED_CODEC = new Codec<>() {
        @Override
        public <T> DataResult<Pair<CountedIngredient, T>> decode(DynamicOps<T> ops, T input) {
            final DataResult<Pair<CountedIngredient, T>> water = WATER.decode(ops, input);
            if (water.result().isPresent() && water.result().get().getFirst().waterBottle()) return water;
            final DataResult<Pair<CountedIngredient, T>> counted = COUNTED.decode(ops, input);
            if (counted.result().isPresent()) return counted;
            return Ingredient.CODEC.decode(ops, input).map(p -> p.mapFirst(ing -> new CountedIngredient(ing, 1, false)));
        }

        @Override
        public <T> DataResult<T> encode(CountedIngredient value, DynamicOps<T> ops, T prefix) {
            if (value.waterBottle()) return WATER.encode(value, ops, prefix);
            if (value.count() != 1) return COUNTED.encode(value, ops, prefix);
            return Ingredient.CODEC.encode(value.ingredient(), ops, prefix);
        }
    };

    public static Codec<List<CountedIngredient>> list(int min, int max) {
        return COUNTED_CODEC.listOf().flatXmap(l -> l.size() < min || l.size() > max
                ? DataResult.error(() -> "Expected " + min + "-" + max + " ingredients, got " + l.size())
                : DataResult.success(l), DataResult::success);
    }

    //? if >=26.1 {
    public static final StreamCodec<RegistryFriendlyByteBuf, CountedIngredient> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, CountedIngredient::waterBottle,
            Ingredient.CONTENTS_STREAM_CODEC, CountedIngredient::ingredient,
            ByteBufCodecs.VAR_INT, CountedIngredient::count,
            (water, ingredient, count) -> new CountedIngredient(ingredient, count, water));
    //?}

    public boolean matches(ItemStack stack) {
        return !stack.isEmpty() && stack.getCount() >= count && test(stack);
    }

    public boolean test(ItemStack stack) {
        return waterBottle ? MachineRecipes.isWaterBottle(stack) : ingredient.test(stack);
    }

    public List<ItemStack> shown() {
        if (waterBottle) return List.of(MachineRecipes.waterBottle());
        final List<ItemStack> out = new ArrayList<>();
        ingredient.items().map(Holder::value).forEach(item -> out.add(new ItemStack(item, count)));
        return out;
    }
}
