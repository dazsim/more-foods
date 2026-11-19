package com.workshopcraft.morefood.machine;

import com.workshopcraft.morefood.MoreFoodMachines;
import com.workshopcraft.workshopcore.machine.WorkBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.Supplier;

/** The grinder and the press: three inputs, a result, a byproduct slot and a fuel slot for the firebox. */
public class KitchenMachineBlockEntity extends WorkBlockEntity {
    public static final int IN_FIRST = 0, IN_COUNT = 3, OUT = 3, BYPRODUCT = 4, FUEL = 5, SIZE = 6;

    public enum Kind {
        GRINDER("block.morefood.block_grinder", MachineRecipes::grinder, () -> MoreFoodMachines.GRINDER_MENU),
        PRESS("block.morefood.block_press", MachineRecipes::press, () -> MoreFoodMachines.PRESS_MENU);

        private final String name;
        private final Supplier<List<MachineRecipe>> recipes;
        private final Supplier<MenuType<KitchenMachineMenu>> menu;

        Kind(String name, Supplier<List<MachineRecipe>> recipes, Supplier<MenuType<KitchenMachineMenu>> menu) {
            this.name = name;
            this.recipes = recipes;
            this.menu = menu;
        }

        public List<MachineRecipe> recipes() {
            return recipes.get();
        }

        public MenuType<KitchenMachineMenu> menu() {
            return menu.get();
        }

        public boolean isIngredient(ItemStack stack) {
            for (MachineRecipe recipe : recipes()) {
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
        if (!kind.isIngredient(stack)) return false;
        for (int s = IN_FIRST; s < IN_FIRST + IN_COUNT; s++) {
            if (s != slot && !getItem(s).isEmpty() && getItem(s).is(stack.getItem())) return false;
        }
        return true;
    }

    private MachineRecipe current() {
        for (MachineRecipe recipe : kind.recipes()) {
            if (recipe.match(this, IN_FIRST, IN_COUNT) != null) return recipe;
        }
        return null;
    }

    @Override
    protected int workTime(ServerLevel level) {
        final MachineRecipe recipe = current();
        if (recipe == null || !fits(OUT, recipe.result())) return 0;
        if (!recipe.byproduct().isEmpty() && !fits(BYPRODUCT, recipe.byproduct())) return 0;
        return recipe.time();
    }

    @Override
    protected void finishWork(ServerLevel level) {
        final MachineRecipe recipe = current();
        if (recipe == null) return;
        final int[] slots = recipe.match(this, IN_FIRST, IN_COUNT);
        for (int i = 0; i < slots.length; i++) getItem(slots[i]).shrink(recipe.inputs().get(i).count());
        store(OUT, recipe.result().copy());
        if (!recipe.byproduct().isEmpty()) store(BYPRODUCT, recipe.byproduct().copy());
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable(kind.name);
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new KitchenMachineMenu(kind, id, inventory, this, data);
    }
}
