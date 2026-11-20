package com.workshopcraft.morefood.machine;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.workshopcraft.morefood.MoreFoodKitchen;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;
//? if >=26.1 {
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.PlacementInfo;
//?}

/** A datapack grinder, press or sink job: up to three inputs, a result and an optional byproduct. */
public class ProcessRecipe implements Recipe<RecipeInput> {
    private final RecipeType<ProcessRecipe> type;
    private final List<CountedIngredient> inputs;
    private final ItemStackTemplate result;
    private final Optional<ItemStackTemplate> byproduct;
    private final int cookingTime;

    public ProcessRecipe(RecipeType<ProcessRecipe> type, List<CountedIngredient> inputs, ItemStackTemplate result,
                         Optional<ItemStackTemplate> byproduct, int cookingTime) {
        this.type = type;
        this.inputs = List.copyOf(inputs);
        this.result = result;
        this.byproduct = byproduct;
        this.cookingTime = cookingTime;
    }

    public static MapCodec<ProcessRecipe> codec(RecipeType<ProcessRecipe> type) {
        return RecordCodecBuilder.mapCodec(i -> i.group(
                CountedIngredient.list(1, 3).fieldOf("ingredients").forGetter(ProcessRecipe::inputs),
                ItemStackTemplate.CODEC.fieldOf("result").forGetter(o -> o.result),
                ItemStackTemplate.CODEC.optionalFieldOf("byproduct").forGetter(o -> o.byproduct),
                ExtraCodecs.POSITIVE_INT.optionalFieldOf("cookingtime", 60).forGetter(ProcessRecipe::time)
        ).apply(i, (inputs, result, byproduct, time) -> new ProcessRecipe(type, inputs, result, byproduct, time)));
    }

    //? if >=26.1 {
    public static StreamCodec<RegistryFriendlyByteBuf, ProcessRecipe> stream(RecipeType<ProcessRecipe> type) {
        return StreamCodec.composite(
                CountedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list(3)), ProcessRecipe::inputs,
                ItemStackTemplate.STREAM_CODEC, o -> o.result,
                ByteBufCodecs.optional(ItemStackTemplate.STREAM_CODEC), o -> o.byproduct,
                ByteBufCodecs.VAR_INT, ProcessRecipe::time,
                (inputs, result, byproduct, time) -> new ProcessRecipe(type, inputs, result, byproduct, time));
    }

    public static RecipeSerializer<ProcessRecipe> serializer(RecipeType<ProcessRecipe> type) {
        return new RecipeSerializer<>(codec(type), stream(type));
    }
    //?} else {
    /*public static RecipeSerializer<ProcessRecipe> serializer(RecipeType<ProcessRecipe> type) {
        return new com.workshopcraft.morefood.legacy.CodecSerializer<>(codec(type));
    }
    *///?}

    public List<CountedIngredient> inputs() {
        return inputs;
    }

    public ItemStack result() {
        return result.create();
    }

    public ItemStack byproduct() {
        return byproduct.map(ItemStackTemplate::create).orElse(ItemStack.EMPTY);
    }

    public int time() {
        return cookingTime;
    }

    public boolean uses(ItemStack stack) {
        for (CountedIngredient input : inputs) {
            if (input.test(stack)) return true;
        }
        return false;
    }

    /** The slot each input comes from, or null when the slots do not hold this recipe. */
    public int[] match(Container container, int first, int count) {
        final int[] slots = new int[inputs.size()];
        int used = 0;
        for (int i = 0; i < inputs.size(); i++) {
            slots[i] = -1;
            for (int s = first; s < first + count; s++) {
                if ((used & (1 << s)) == 0 && inputs.get(i).matches(container.getItem(s))) {
                    slots[i] = s;
                    used |= 1 << s;
                    break;
                }
            }
            if (slots[i] < 0) return null;
        }
        return slots;
    }

    @Override
    public boolean matches(RecipeInput input, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(RecipeInput input) {
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
    public RecipeSerializer<ProcessRecipe> getSerializer() {
        if (type == MoreFoodKitchen.PRESSING) return MoreFoodKitchen.PRESSING_SERIALIZER;
        if (type == MoreFoodKitchen.WASHING) return MoreFoodKitchen.WASHING_SERIALIZER;
        return MoreFoodKitchen.GRINDING_SERIALIZER;
    }

    @Override
    public RecipeType<ProcessRecipe> getType() {
        return type;
    }

    //? if >=26.1 {
    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }
    //?}

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return MoreFoodKitchen.MACHINE_BOOK;
    }
}
