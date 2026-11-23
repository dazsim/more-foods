package com.workshopcraft.morefood.machine;

import com.workshopcraft.morefood.MoreFoodMachines;
import com.workshopcraft.workshopcore.machine.FluidSides;
import com.workshopcraft.workshopcore.machine.MachineTank;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/** Hoppers feed bottles, buckets and dough in from above or the sides; filled and washed items come out the bottom. */
public class KitchenSinkBlockEntity extends BlockEntity implements WorldlyContainer {
    public static final int IN = 0, OUT = 1, SIZE = 2;
    private static final int[] INPUT = {IN};
    private static final int[] OUTPUT = {OUT};

    // pipes drain water on every side, nothing goes in
    public final MachineTank water = MachineTank.infinite(Fluids.WATER).build();
    public final FluidSides fluidSides = new FluidSides();

    private NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    private int timer;

    public KitchenSinkBlockEntity(BlockPos pos, BlockState state) {
        super(MoreFoodMachines.sinkEntity, pos, state);
    }

    public void serverTick(ServerLevel level) {
        final ItemStack in = items.get(IN);
        if (in.isEmpty()) {
            timer = 0;
            return;
        }
        final ProcessRecipe recipe = MachineRecipes.sinkRecipe(level, in);
        if (recipe == null || ++timer < recipe.time()) return;
        timer = 0;
        final ItemStack out = items.get(OUT);
        final ItemStack made = recipe.result();
        if (!out.isEmpty() && (!ItemStack.isSameItemSameComponents(out, made) || out.getCount() + made.getCount() > out.getMaxStackSize())) return;
        in.shrink(recipe.inputs().get(0).count());
        if (out.isEmpty()) items.set(OUT, made.copy());
        else out.grow(made.getCount());
        setChanged();
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, items);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null) Containers.dropContents(level, pos, items);
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        final ItemStack out = ContainerHelper.removeItem(items, slot, count);
        if (!out.isEmpty()) setChanged();
        return out;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        stack.limitSize(getMaxStackSize(stack));
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == IN && MachineRecipes.sinkRecipe(level, stack.copyWithCount(64)) != null;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return side == Direction.DOWN ? OUTPUT : INPUT;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return side != Direction.DOWN && canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return side == Direction.DOWN && slot == OUT;
    }
}
