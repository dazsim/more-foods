package com.workshopcraft.morefood.machine;

import com.workshopcraft.morefood.MoreFoodKitchen;
import com.workshopcraft.morefood.MoreFoodMachines;
import com.workshopcraft.workshopcore.machine.ProcessMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/** The grinder and the press: three inputs, a result, a byproduct slot and a fuel slot for the firebox. */
public class KitchenMachineBlockEntity extends ProcessMachine<ProcessRecipe> {
    public static final int IN_FIRST = 0, IN_COUNT = 3, OUT = 3, BYPRODUCT = 4, FUEL = 5, SIZE = 6;

    public enum Kind {
        GRINDER("block.morefood.block_grinder", MoreFoodKitchen.GRINDING, () -> MoreFoodMachines.GRINDER_MENU),
        PRESS("block.morefood.block_press", MoreFoodKitchen.PRESSING, () -> MoreFoodMachines.PRESS_MENU);

        private final String name;
        private final RecipeType<ProcessRecipe> recipes;
        private final java.util.function.Supplier<MenuType<KitchenMachineMenu>> menu;

        Kind(String name, RecipeType<ProcessRecipe> recipes, java.util.function.Supplier<MenuType<KitchenMachineMenu>> menu) {
            this.name = name;
            this.recipes = recipes;
            this.menu = menu;
        }

        public List<ProcessRecipe> recipes(Level level) {
            return MachineRecipes.of(level, recipes);
        }

        public MenuType<KitchenMachineMenu> menu() {
            return menu.get();
        }

        public boolean isIngredient(Level level, ItemStack stack) {
            for (ProcessRecipe recipe : recipes(level)) {
                if (recipe.uses(stack)) return true;
            }
            return false;
        }
    }

    private final Kind kind;

    public KitchenMachineBlockEntity(BlockEntityType<?> type, Kind kind, BlockPos pos, BlockState state) {
        super(type, pos, state, SIZE, 4000, 100);
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }

    @Override
    protected boolean isInput(int slot) {
        return slot >= IN_FIRST && slot < IN_FIRST + IN_COUNT;
    }

    @Override
    protected boolean isOutput(int slot) {
        return slot == OUT || slot == BYPRODUCT;
    }

    @Override
    public int fuelSlot() {
        return FUEL;
    }

    // one kind of item per input slot, so a hopper cannot fill every slot with apples
    @Override
    protected boolean accepts(int slot, ItemStack stack) {
        if (!kind.isIngredient(level, stack)) return false;
        for (int s = IN_FIRST; s < IN_FIRST + IN_COUNT; s++) {
            if (s != slot && !getItem(s).isEmpty() && getItem(s).is(stack.getItem())) return false;
        }
        return true;
    }

    @Override
    protected ProcessRecipe findRecipe(ServerLevel level) {
        for (ProcessRecipe recipe : kind.recipes(level)) {
            if (recipe.match(this, IN_FIRST, IN_COUNT) != null) return recipe;
        }
        return null;
    }

    @Override
    protected boolean matches(ProcessRecipe recipe, ServerLevel level) {
        return recipe.match(this, IN_FIRST, IN_COUNT) != null;
    }

    @Override
    protected int recipeTime(ProcessRecipe recipe) {
        return recipe.time();
    }

    @Override
    protected List<ItemStack> outputs(ProcessRecipe recipe) {
        return recipe.byproduct().isEmpty() ? List.of(recipe.result()) : List.of(recipe.result(), recipe.byproduct());
    }

    @Override
    protected void consume(ProcessRecipe recipe) {
        final int[] slots = recipe.match(this, IN_FIRST, IN_COUNT);
        if (slots == null) return;
        for (int i = 0; i < slots.length; i++) removeItem(slots[i], recipe.inputs().get(i).count());
    }

    // the result goes in the result slot and the byproduct in its own slot, never mixed
    @Override
    public boolean outputsFit(List<ItemStack> stacks) {
        return fits(OUT, stacks.get(0)) && (stacks.size() < 2 || fits(BYPRODUCT, stacks.get(1)));
    }

    @Override
    protected List<ItemStack> results(ProcessRecipe recipe, ServerLevel level) {
        final List<ItemStack> left = new ArrayList<>();
        place(OUT, recipe.result(), left);
        if (!recipe.byproduct().isEmpty()) place(BYPRODUCT, recipe.byproduct(), left);
        return left;
    }

    private void place(int slot, ItemStack stack, List<ItemStack> left) {
        if (fits(slot, stack)) store(slot, stack.copy());
        else left.add(stack.copy());
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable(kind.name);
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new KitchenMachineMenu(kind, id, inventory, this, menuData());
    }
}
