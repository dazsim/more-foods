package com.workshopcraft.morefood.legacy;

//? if <26.1 {
/*import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.workshopcraft.morefood.Compat;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
//? if >=1.21 {
import net.minecraft.world.item.crafting.SingleRecipeInput;
//?}

/^* The 26.1 cooking recipe shape, for the baking and frying recipes on older versions. ^/
public abstract class LegacyCookingRecipe implements LegacyRecipe<SingleRecipeInput> {
    public record Recipe.CommonInfo(boolean showNotification) {
        public static final MapCodec<Recipe.CommonInfo> MAP_CODEC = Codec.BOOL.optionalFieldOf("show_notification", true).xmap(Recipe.CommonInfo::new, Recipe.CommonInfo::showNotification);
    }

    public record CookingBookInfo(CookingBookCategory category, String group) {
        public static final MapCodec<CookingBookInfo> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                CookingBookCategory.CODEC.optionalFieldOf("category", CookingBookCategory.MISC).forGetter(CookingBookInfo::category),
                Codec.STRING.optionalFieldOf("group", "").forGetter(CookingBookInfo::group)
        ).apply(i, CookingBookInfo::new));
    }

    public interface Factory<T extends LegacyCookingRecipe> {
        T create(Recipe.CommonInfo commonInfo, CookingBookInfo bookInfo, Ingredient ingredient, ItemStackTemplate result, float experience, int cookingTime);
    }

    public static <T extends LegacyCookingRecipe> MapCodec<T> cookingMapCodec(Factory<T> factory, int defaultTime) {
        return RecordCodecBuilder.mapCodec(i -> i.group(
                Recipe.CommonInfo.MAP_CODEC.forGetter(o -> o.commonInfo),
                CookingBookInfo.MAP_CODEC.forGetter(o -> o.bookInfo),
                ingredientCodec().fieldOf("ingredient").forGetter(LegacyCookingRecipe::input),
                ItemStackTemplate.CODEC.fieldOf("result").forGetter(LegacyCookingRecipe::result),
                Codec.FLOAT.fieldOf("experience").orElse(0.0F).forGetter(LegacyCookingRecipe::experience),
                Codec.INT.fieldOf("cookingtime").orElse(defaultTime).forGetter(LegacyCookingRecipe::cookingTime)
        ).apply(i, factory::create));
    }

    private static Codec<Ingredient> ingredientCodec() {
        //? if <1.21 {
        /^return Compat.INGREDIENT;
        ^///?} else {
        return Ingredient.CODEC;
        //?}
    }

    protected final Recipe.CommonInfo commonInfo;
    protected final CookingBookInfo bookInfo;
    private final Ingredient ingredient;
    private final ItemStackTemplate result;
    private final float experience;
    private final int cookingTime;

    protected LegacyCookingRecipe(Recipe.CommonInfo commonInfo, CookingBookInfo bookInfo, Ingredient ingredient, ItemStackTemplate result,
                                  float experience, int cookingTime) {
        this.commonInfo = commonInfo;
        this.bookInfo = bookInfo;
        this.ingredient = ingredient;
        this.result = result;
        this.experience = experience;
        this.cookingTime = cookingTime;
    }

    public Ingredient input() {
        return ingredient;
    }

    public ItemStackTemplate result() {
        return result;
    }

    public float experience() {
        return experience;
    }

    public int cookingTime() {
        return cookingTime;
    }

    public CookingBookCategory category() {
        return bookInfo.category();
    }

    @Override
    public String group() {
        return bookInfo.group();
    }

    @Override
    public boolean showNotification() {
        return commonInfo.showNotification();
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return ingredient.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input) {
        return result.create();
    }

    @Override
    public ItemStack resultItem() {
        return result.create();
    }

    @Override
    public NonNullList<Ingredient> ingredientList() {
        return NonNullList.of(Ingredient.EMPTY, ingredient);
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(furnaceIcon());
    }

    protected abstract Item furnaceIcon();
}
*///?}
