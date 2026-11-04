package com.workshopcraft.morefood;

import com.workshopcraft.morefood.board.AssemblyBoardBlockEntity;
import com.workshopcraft.morefood.crafting.AssemblyRecipe;
import com.workshopcraft.morefood.crafting.BakingRecipe;
import com.workshopcraft.morefood.crafting.BoilingRecipe;
import com.workshopcraft.morefood.crafting.FryingRecipe;
import com.workshopcraft.morefood.stove.CookingPotBlockEntity;
import com.workshopcraft.morefood.stove.StoveBlockEntity;
import com.workshopcraft.morefood.stove.StoveMenu;
import net.minecraft.core.Holder;
//? if >=1.20.5 {
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.codec.ByteBufCodecs;
//?} else {
/*import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
*///?}
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

public class MoreFoodKitchen {
    public static final RecipeType<BakingRecipe> BAKING = type("baking");
    //? if >=26.1 {
    public static final RecipeSerializer<BakingRecipe> BAKING_SERIALIZER = new RecipeSerializer<>(BakingRecipe.MAP_CODEC, BakingRecipe.STREAM_CODEC);
    //?} else {
    /*public static final RecipeSerializer<BakingRecipe> BAKING_SERIALIZER = new com.workshopcraft.morefood.legacy.CodecSerializer<>(BakingRecipe.MAP_CODEC);
    *///?}
    public static final RecipeBookCategory BAKING_BOOK = new RecipeBookCategory();
    public static final RecipeType<FryingRecipe> FRYING = type("frying");
    //? if >=26.1 {
    public static final RecipeSerializer<FryingRecipe> FRYING_SERIALIZER = new RecipeSerializer<>(FryingRecipe.MAP_CODEC, FryingRecipe.STREAM_CODEC);
    //?} else {
    /*public static final RecipeSerializer<FryingRecipe> FRYING_SERIALIZER = new com.workshopcraft.morefood.legacy.CodecSerializer<>(FryingRecipe.MAP_CODEC);
    *///?}
    public static final RecipeType<BoilingRecipe> BOILING = type("boiling");
    //? if >=26.1 {
    public static final RecipeSerializer<BoilingRecipe> BOILING_SERIALIZER = new RecipeSerializer<>(BoilingRecipe.MAP_CODEC, BoilingRecipe.STREAM_CODEC);
    //?} else {
    /*public static final RecipeSerializer<BoilingRecipe> BOILING_SERIALIZER = new com.workshopcraft.morefood.legacy.CodecSerializer<>(BoilingRecipe.MAP_CODEC);
    *///?}
    public static final RecipeBookCategory HOB_BOOK = new RecipeBookCategory();
    public static final RecipeType<AssemblyRecipe> ASSEMBLY = type("assembly");
    //? if >=26.1 {
    public static final RecipeSerializer<AssemblyRecipe> ASSEMBLY_SERIALIZER = new RecipeSerializer<>(AssemblyRecipe.MAP_CODEC, AssemblyRecipe.STREAM_CODEC);
    //?} else {
    /*public static final RecipeSerializer<AssemblyRecipe> ASSEMBLY_SERIALIZER = new com.workshopcraft.morefood.legacy.CodecSerializer<>(AssemblyRecipe.MAP_CODEC);
    *///?}
    public static final RecipeBookCategory BOARD_BOOK = new RecipeBookCategory();
    //? if >=1.20.5 {
    public static final DataComponentType<List<Holder<Item>>> TOPPINGS = DataComponentType.<List<Holder<Item>>>builder()
            .persistent(Item.CODEC.listOf().xmap(MoreFoodKitchen::capToppings, l -> l))
            .networkSynchronized(Item.STREAM_CODEC.apply(ByteBufCodecs.list()).map(MoreFoodKitchen::capToppings, l -> l))
            .build();
    public static final DataComponentType<ItemStackTemplate> PEEL_CONTENTS = DataComponentType.<ItemStackTemplate>builder()
            .persistent(ItemStackTemplate.CODEC)
            .networkSynchronized(ItemStackTemplate.STREAM_CODEC)
            .build();
    //?}
    //? if >=1.20.5 && <26.1 {
    /*public static final DataComponentType<Item> VESSEL = DataComponentType.<Item>builder()
            .persistent(net.minecraft.core.registries.BuiltInRegistries.ITEM.byNameCodec())
            .networkSynchronized(ByteBufCodecs.registry(Registries.ITEM))
            .build();
    *///?}
    //? if <1.20.5 {
    /*private static final String TOPPINGS_TAG = "morefood:toppings";
    private static final String PEEL_TAG = "morefood:peel_contents";
    *///?}
    public static final TagKey<Block> BOARD_SUPPORTS = TagKey.create(Registries.BLOCK, MoreFood.id("assembly_board_supports"));
    //? if >=26.1 {
    public static final SimpleParticleType STEAM = new SimpleParticleType(false);
    //?} else {
    /*public static final SimpleParticleType STEAM = new SimpleParticleType(false) {};
    *///?}
    public static final SoundEvent SIZZLE = SoundEvent.createVariableRangeEvent(MoreFood.id("block.stove.sizzle"));
    public static final SoundEvent BUBBLE = SoundEvent.createVariableRangeEvent(MoreFood.id("block.cooking_pot.bubble"));
    public static final TagKey<Item> VESSELS = TagKey.create(Registries.ITEM, MoreFood.id("vessels"));
    public static final MenuType<StoveMenu> STOVE_MENU = new MenuType<>(StoveMenu::new, FeatureFlags.VANILLA_SET);
    public static BlockEntityType<StoveBlockEntity> stoveEntity;
    public static BlockEntityType<CookingPotBlockEntity> cookingPotEntity;
    public static BlockEntityType<AssemblyBoardBlockEntity> boardEntity;

    private static <T extends Recipe<?>> RecipeType<T> type(String name) {
        return new RecipeType<>() {
            @Override
            public String toString() {
                return MoreFood.MODID + ":" + name;
            }
        };
    }

    public static List<RecipeHolder<BoilingRecipe>> boiling(ServerLevel level) {
        return all(level, BoilingRecipe.class);
    }

    public static List<RecipeHolder<AssemblyRecipe>> assembly(ServerLevel level) {
        return all(level, AssemblyRecipe.class);
    }

    private static <T extends Recipe<?>> List<RecipeHolder<T>> all(ServerLevel level, Class<T> type) {
        final List<RecipeHolder<T>> out = new ArrayList<>();
        for (RecipeHolder<?> holder : level.recipeAccess().getRecipes()) {
            if (type.isInstance(holder.value())) {
                @SuppressWarnings("unchecked") final RecipeHolder<T> cast = (RecipeHolder<T>) holder;
                out.add(cast);
            }
        }
        return out;
    }

    public static final int MAX_TOPPINGS = 3;

    // a pizza never carries more than 3 toppings, whatever a command or old save hands it
    private static List<Holder<Item>> capToppings(List<Holder<Item>> toppings) {
        return toppings.size() > MAX_TOPPINGS ? List.copyOf(toppings.subList(0, MAX_TOPPINGS)) : toppings;
    }

    public static List<Holder<Item>> toppings(ItemStack stack) {
        //? if >=1.20.5 {
        final List<Holder<Item>> toppings = stack.get(TOPPINGS);
        return toppings == null ? List.of() : toppings;
        //?} else {
        /*final CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TOPPINGS_TAG, Tag.TAG_LIST)) return List.of();
        final List<Holder<Item>> out = new ArrayList<>();
        for (Tag t : tag.getList(TOPPINGS_TAG, Tag.TAG_STRING)) {
            final Identifier id = Identifier.tryParse(t.getAsString());
            if (id != null && BuiltInRegistries.ITEM.containsKey(id)) out.add(BuiltInRegistries.ITEM.get(id).builtInRegistryHolder());
        }
        return capToppings(out);
        *///?}
    }

    public static void setToppings(ItemStack stack, List<Holder<Item>> toppings) {
        //? if >=1.20.5 {
        stack.set(TOPPINGS, List.copyOf(capToppings(toppings)));
        //?} else {
        /*final ListTag list = new ListTag();
        for (Holder<Item> t : capToppings(toppings)) list.add(StringTag.valueOf(BuiltInRegistries.ITEM.getKey(t.value()).toString()));
        stack.getOrCreateTag().put(TOPPINGS_TAG, list);
        *///?}
    }

    public static @org.jspecify.annotations.Nullable ItemStackTemplate peelContents(ItemStack peel) {
        //? if >=1.20.5 {
        return peel.get(PEEL_CONTENTS);
        //?} else {
        /*final CompoundTag tag = peel.getTag();
        if (tag == null || !tag.contains(PEEL_TAG, Tag.TAG_COMPOUND)) return null;
        final ItemStack held = ItemStack.of(tag.getCompound(PEEL_TAG));
        return held.isEmpty() ? null : ItemStackTemplate.fromNonEmptyStack(held);
        *///?}
    }

    public static void setPeelContents(ItemStack peel, @org.jspecify.annotations.Nullable ItemStackTemplate held) {
        //? if >=1.20.5 {
        if (held == null) peel.remove(PEEL_CONTENTS);
        else peel.set(PEEL_CONTENTS, held);
        //?} else {
        /*if (held == null) {
            peel.removeTagKey(PEEL_TAG);
        } else {
            peel.getOrCreateTag().put(PEEL_TAG, held.create().save(new CompoundTag()));
        }
        *///?}
    }

    /** A pizza fills 8 and 0.2 saturation, plus 1 and 0.1 for each topping. */
    public static FoodProperties pizzaFood(int toppings) {
        toppings = Math.min(toppings, MAX_TOPPINGS);
        return new FoodProperties.Builder().nutrition(8 + toppings).saturationModifier(0.2F + 0.1F * toppings).build();
    }

    /** Sets the food values of a baked pizza from its toppings. */
    public static void feedToppings(ItemStack stack) {
        //? if >=1.20.5 {
        final List<Holder<Item>> toppings = stack.get(TOPPINGS);
        if (toppings != null && !toppings.isEmpty() && stack.has(DataComponents.FOOD)) stack.set(DataComponents.FOOD, pizzaFood(toppings.size()));
        //?}
    }

    public static void registerBlockEntities(BiConsumer<Identifier, BlockEntityType<?>> registry) {
        //? if >=26.1 {
        stoveEntity = new BlockEntityType<>(StoveBlockEntity::new, Set.of(MoreFoodBlocks.stove));
        registry.accept(MoreFood.id("stove"), stoveEntity);
        cookingPotEntity = new BlockEntityType<>(CookingPotBlockEntity::new, Set.of(MoreFoodBlocks.cookingPot));
        registry.accept(MoreFood.id("cooking_pot"), cookingPotEntity);
        boardEntity = new BlockEntityType<>(AssemblyBoardBlockEntity::new, Set.of(MoreFoodBlocks.assemblyBoard));
        //?} else {
        /*stoveEntity = BlockEntityType.Builder.of(StoveBlockEntity::new, MoreFoodBlocks.stove).build(null);
        registry.accept(MoreFood.id("stove"), stoveEntity);
        cookingPotEntity = BlockEntityType.Builder.of(CookingPotBlockEntity::new, MoreFoodBlocks.cookingPot).build(null);
        registry.accept(MoreFood.id("cooking_pot"), cookingPotEntity);
        boardEntity = BlockEntityType.Builder.of(AssemblyBoardBlockEntity::new, MoreFoodBlocks.assemblyBoard).build(null);
        *///?}
        registry.accept(MoreFood.id("assembly_board"), boardEntity);
    }

    //? if >=26.1 {
    public static void registerComponents(BiConsumer<Identifier, DataComponentType<?>> registry) {
        registry.accept(MoreFood.id("toppings"), TOPPINGS);
        registry.accept(MoreFood.id("peel_contents"), PEEL_CONTENTS);
    }
    //?} else if >=1.20.5 {
    /*public static void registerComponents(BiConsumer<Identifier, DataComponentType<?>> registry) {
        registry.accept(MoreFood.id("toppings"), TOPPINGS);
        registry.accept(MoreFood.id("peel_contents"), PEEL_CONTENTS);
        registry.accept(MoreFood.id("vessel"), VESSEL);
    }
    *///?}

    public static void registerMenus(BiConsumer<Identifier, MenuType<?>> registry) {
        registry.accept(MoreFood.id("stove"), STOVE_MENU);
    }

    public static void registerRecipeTypes(BiConsumer<Identifier, RecipeType<?>> registry) {
        registry.accept(MoreFood.id("baking"), BAKING);
        registry.accept(MoreFood.id("frying"), FRYING);
        registry.accept(MoreFood.id("boiling"), BOILING);
        registry.accept(MoreFood.id("assembly"), ASSEMBLY);
    }

    public static void registerRecipeSerializers(BiConsumer<Identifier, RecipeSerializer<?>> registry) {
        registry.accept(MoreFood.id("baking"), BAKING_SERIALIZER);
        registry.accept(MoreFood.id("frying"), FRYING_SERIALIZER);
        registry.accept(MoreFood.id("boiling"), BOILING_SERIALIZER);
        registry.accept(MoreFood.id("assembly"), ASSEMBLY_SERIALIZER);
    }

    public static void registerBookCategories(BiConsumer<Identifier, RecipeBookCategory> registry) {
        registry.accept(MoreFood.id("baking"), BAKING_BOOK);
        registry.accept(MoreFood.id("hob"), HOB_BOOK);
        registry.accept(MoreFood.id("board"), BOARD_BOOK);
    }

    public static void registerSounds(BiConsumer<Identifier, SoundEvent> registry) {
        registry.accept(MoreFood.id("block.stove.sizzle"), SIZZLE);
        registry.accept(MoreFood.id("block.cooking_pot.bubble"), BUBBLE);
    }

    public static void registerParticles(BiConsumer<Identifier, ParticleType<?>> registry) {
        registry.accept(MoreFood.id("steam"), STEAM);
    }
}
