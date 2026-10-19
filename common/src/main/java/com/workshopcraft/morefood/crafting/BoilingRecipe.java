package com.workshopcraft.morefood.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.workshopcraft.morefood.MoreFoodKitchen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BoilingRecipe implements Recipe<PotInput> {
    public static final int MAX_INGREDIENTS = 6;

    public record Serving(Optional<Item> vessel, ItemStackTemplate result) {
        public static final Codec<Serving> CODEC = RecordCodecBuilder.create(i -> i.group(
                BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf("vessel").forGetter(Serving::vessel),
                ItemStackTemplate.CODEC.fieldOf("result").forGetter(Serving::result)
        ).apply(i, Serving::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, Serving> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.optional(ByteBufCodecs.registry(Registries.ITEM)), Serving::vessel,
                ItemStackTemplate.STREAM_CODEC, Serving::result,
                Serving::new);

        public boolean accepts(ItemStack stack) {
            return vessel.isPresent() && stack.is(vessel.get());
        }
    }

    public static final MapCodec<BoilingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Ingredient.CODEC.listOf(0, MAX_INGREDIENTS).optionalFieldOf("ingredients", List.of()).forGetter(o -> o.ingredients),
            Codec.intRange(0, 1000).optionalFieldOf("water", 0).forGetter(o -> o.water),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(o -> o.main.result()),
            BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf("vessel").forGetter(o -> o.main.vessel()),
            Serving.CODEC.listOf().optionalFieldOf("other_vessels", List.of()).forGetter(o -> o.others),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("portions", 1).forGetter(o -> o.portions),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("cookingtime", 400).forGetter(o -> o.cookingTime),
            ExtraCodecs.STRING_RGB_COLOR.optionalFieldOf("colour", 0xFFC08040).forGetter(o -> o.colour)
    ).apply(i, BoilingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BoilingRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), o -> o.ingredients,
            ByteBufCodecs.VAR_INT, o -> o.water,
            ItemStackTemplate.STREAM_CODEC, o -> o.main.result(),
            ByteBufCodecs.optional(ByteBufCodecs.registry(Registries.ITEM)), o -> o.main.vessel(),
            Serving.STREAM_CODEC.apply(ByteBufCodecs.list()), o -> o.others,
            ByteBufCodecs.VAR_INT, o -> o.portions,
            ByteBufCodecs.VAR_INT, o -> o.cookingTime,
            ByteBufCodecs.INT, o -> o.colour,
            BoilingRecipe::new);

    private final List<Ingredient> ingredients;
    private final int water;
    private final Serving main;
    private final List<Serving> others;
    private final int portions;
    private final int cookingTime;
    private final int colour;

    public BoilingRecipe(List<Ingredient> ingredients, int water, ItemStackTemplate result, Optional<Item> vessel, List<Serving> others,
                         int portions, int cookingTime, int colour) {
        this.ingredients = ingredients;
        this.water = water;
        this.main = new Serving(vessel, result);
        this.others = others;
        this.portions = portions;
        this.cookingTime = cookingTime;
        this.colour = colour;
    }

    public List<Ingredient> ingredients() {
        return ingredients;
    }

    public int water() {
        return water;
    }

    public int portions() {
        return portions;
    }

    public int cookingTime() {
        return cookingTime;
    }

    public int colour() {
        return colour;
    }

    public boolean needsVessel() {
        return main.vessel().isPresent();
    }

    public List<Serving> servings() {
        final List<Serving> all = new ArrayList<>();
        all.add(main);
        all.addAll(others);
        return all;
    }

    public ItemStack result() {
        return main.result().create();
    }

    @Override
    public boolean matches(PotInput input, Level level) {
        if (input.size() != ingredients.size() || input.water() < water) return false;
        return assign(input.items(), new boolean[ingredients.size()], 0);
    }

    private boolean assign(List<ItemStack> items, boolean[] used, int at) {
        if (at == items.size()) return true;
        for (int i = 0; i < ingredients.size(); i++) {
            if (!used[i] && ingredients.get(i).test(items.get(at))) {
                used[i] = true;
                if (assign(items, used, at + 1)) return true;
                used[i] = false;
            }
        }
        return false;
    }

    public boolean uses(ItemStack stack) {
        for (Ingredient ingredient : ingredients) {
            if (ingredient.test(stack)) return true;
        }
        return false;
    }

    @Override
    public ItemStack assemble(PotInput input) {
        return result();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public RecipeSerializer<BoilingRecipe> getSerializer() {
        return MoreFoodKitchen.BOILING_SERIALIZER;
    }

    @Override
    public RecipeType<BoilingRecipe> getType() {
        return MoreFoodKitchen.BOILING;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return MoreFoodKitchen.HOB_BOOK;
    }
}
