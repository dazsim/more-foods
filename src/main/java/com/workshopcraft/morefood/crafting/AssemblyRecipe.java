package com.workshopcraft.morefood.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.workshopcraft.morefood.MoreFoodKitchen;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Built layer by layer on the assembly board: a base, the fillings, optional extras, then an optional lid. */
public class AssemblyRecipe implements Recipe<BoardInput> {
    public static final int MAX_LAYERS = 8;

    public record Extras(Ingredient ingredient, int max) {
        public static final Codec<Extras> CODEC = RecordCodecBuilder.create(i -> i.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter(Extras::ingredient),
                Codec.intRange(1, MAX_LAYERS).fieldOf("max").forGetter(Extras::max)
        ).apply(i, Extras::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, Extras> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, Extras::ingredient,
                ByteBufCodecs.VAR_INT, Extras::max,
                Extras::new);
    }

    public static final MapCodec<AssemblyRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Ingredient.CODEC.fieldOf("base").forGetter(o -> o.base),
            Ingredient.CODEC.listOf(0, MAX_LAYERS - 1).optionalFieldOf("fillings", List.of()).forGetter(o -> o.fillings),
            Codec.BOOL.optionalFieldOf("ordered", false).forGetter(o -> o.ordered),
            Extras.CODEC.optionalFieldOf("extras").forGetter(o -> o.extras),
            Ingredient.CODEC.optionalFieldOf("lid").forGetter(o -> o.lid),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(o -> o.result),
            Codec.BOOL.optionalFieldOf("keep_extras", false).forGetter(o -> o.keepExtras)
    ).apply(i, AssemblyRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AssemblyRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, o -> o.base,
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), o -> o.fillings,
            ByteBufCodecs.BOOL, o -> o.ordered,
            ByteBufCodecs.optional(Extras.STREAM_CODEC), o -> o.extras,
            Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC, o -> o.lid,
            ItemStackTemplate.STREAM_CODEC, o -> o.result,
            ByteBufCodecs.BOOL, o -> o.keepExtras,
            AssemblyRecipe::new);

    /** Where a stack of layers has got to: which fillings are in, how many extras, whether the lid is on. */
    private record State(int fillings, int extras, boolean lid) {}

    private final Ingredient base;
    private final List<Ingredient> fillings;
    private final boolean ordered;
    private final Optional<Extras> extras;
    private final Optional<Ingredient> lid;
    private final ItemStackTemplate result;
    private final boolean keepExtras;

    public AssemblyRecipe(Ingredient base, List<Ingredient> fillings, boolean ordered, Optional<Extras> extras, Optional<Ingredient> lid,
                          ItemStackTemplate result, boolean keepExtras) {
        this.base = base;
        this.fillings = fillings;
        this.ordered = ordered;
        this.extras = extras;
        this.lid = lid;
        this.result = result;
        this.keepExtras = keepExtras;
    }

    public Ingredient base() {
        return base;
    }

    public List<Ingredient> fillings() {
        return fillings;
    }

    public boolean ordered() {
        return ordered;
    }

    public Optional<Extras> extras() {
        return extras;
    }

    public Optional<Ingredient> lid() {
        return lid;
    }

    public ItemStack result() {
        return result.create();
    }

    public boolean keepsExtras() {
        return keepExtras;
    }

    private int all() {
        return (1 << fillings.size()) - 1;
    }

    private Set<State> states(List<ItemStack> layers) {
        Set<State> states = new LinkedHashSet<>();
        if (layers.isEmpty() || !base.test(layers.getFirst())) return states;
        states.add(new State(0, 0, false));
        for (int i = 1; i < layers.size() && !states.isEmpty(); i++) {
            final Set<State> next = new LinkedHashSet<>();
            for (State state : states) next.addAll(after(state, layers.subList(0, i), layers.get(i)));
            states = next;
        }
        return states;
    }

    private List<State> after(State state, List<ItemStack> below, ItemStack stack) {
        final List<State> out = new ArrayList<>();
        if (state.lid || below.size() >= MAX_LAYERS) return out;
        if (state.fillings != all()) {
            for (int j = 0; j < fillings.size(); j++) {
                if ((state.fillings & 1 << j) != 0) continue;
                if (fillings.get(j).test(stack)) out.add(new State(state.fillings | 1 << j, 0, false));
                if (ordered) break;
            }
            return out;
        }
        if (extras.isPresent() && state.extras < extras.get().max() && extras.get().ingredient().test(stack) && !contains(below, stack)) {
            out.add(new State(state.fillings, state.extras + 1, false));
        }
        if (lid.isPresent() && lid.get().test(stack)) out.add(new State(state.fillings, state.extras, true));
        return out;
    }

    private static boolean contains(List<ItemStack> layers, ItemStack stack) {
        for (ItemStack layer : layers) {
            if (layer.is(stack.getItem())) return true;
        }
        return false;
    }

    private boolean complete(State state) {
        return state.fillings == all() && (lid.isEmpty() || state.lid);
    }

    public boolean accepts(List<ItemStack> layers, ItemStack stack) {
        if (layers.isEmpty()) return base.test(stack);
        for (State state : states(layers)) {
            if (!after(state, layers, stack).isEmpty()) return true;
        }
        return false;
    }

    public boolean isComplete(List<ItemStack> layers) {
        for (State state : states(layers)) {
            if (complete(state)) return true;
        }
        return false;
    }

    /** True while another layer could still go on. */
    public boolean isOpen(List<ItemStack> layers) {
        if (layers.size() >= MAX_LAYERS) return false;
        if (layers.isEmpty()) return true;
        for (State state : states(layers)) {
            if (state.lid) continue;
            if (state.fillings != all() || lid.isPresent() || extras.isPresent() && state.extras < extras.get().max()) return true;
        }
        return false;
    }

    /** The ingredients that could go on next. */
    public void next(List<ItemStack> layers, Set<Ingredient> out) {
        if (layers.isEmpty()) {
            out.add(base);
            return;
        }
        if (layers.size() >= MAX_LAYERS) return;
        for (State state : states(layers)) {
            if (state.lid) continue;
            if (state.fillings != all()) {
                for (int j = 0; j < fillings.size(); j++) {
                    if ((state.fillings & 1 << j) != 0) continue;
                    out.add(fillings.get(j));
                    if (ordered) break;
                }
                continue;
            }
            if (extras.isPresent() && state.extras < extras.get().max()) out.add(extras.get().ingredient());
            lid.ifPresent(out::add);
        }
    }

    @Override
    public boolean matches(BoardInput input, Level level) {
        return isComplete(input.layers());
    }

    @Override
    public ItemStack assemble(BoardInput input) {
        final ItemStack out = result.create();
        if (keepExtras) {
            final List<Holder<Item>> added = new ArrayList<>();
            final List<ItemStack> layers = input.layers();
            for (int i = 1 + fillings.size(); i < layers.size(); i++) {
                final ItemStack layer = layers.get(i);
                if (i == layers.size() - 1 && lid.isPresent() && lid.get().test(layer)) break;
                added.add(layer.typeHolder());
            }
            if (!added.isEmpty()) MoreFoodKitchen.setToppings(out, added);
        }
        return out;
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
    public RecipeSerializer<AssemblyRecipe> getSerializer() {
        return MoreFoodKitchen.ASSEMBLY_SERIALIZER;
    }

    @Override
    public RecipeType<AssemblyRecipe> getType() {
        return MoreFoodKitchen.ASSEMBLY;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return MoreFoodKitchen.BOARD_BOOK;
    }
}
