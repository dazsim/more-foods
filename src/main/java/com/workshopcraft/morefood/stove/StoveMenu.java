package com.workshopcraft.morefood.stove;

import com.workshopcraft.morefood.MoreFoodKitchen;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

public class StoveMenu extends AbstractContainerMenu {
    public static final int DATA_COUNT = 6;
    private static final int INV_START = StoveBlockEntity.MENU_SIZE;
    private static final int HOTBAR_START = INV_START + 27;
    private static final int INV_END = HOTBAR_START + 9;

    private final Container container;
    private final ContainerData data;
    private final Level level;

    public StoveMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(StoveBlockEntity.MENU_SIZE), new SimpleContainerData(DATA_COUNT));
    }

    public StoveMenu(int id, Inventory inventory, Container container, ContainerData data) {
        super(MoreFoodKitchen.STOVE_MENU, id);
        checkContainerSize(container, StoveBlockEntity.MENU_SIZE);
        checkContainerDataCount(data, DATA_COUNT);
        this.container = container;
        this.data = data;
        this.level = inventory.player.level();
        addSlot(new Slot(container, StoveBlockEntity.FUEL, 56, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return level.fuelValues().isFuel(stack);
            }
        });
        addSlot(new Slot(container, StoveBlockEntity.OVEN, 56, 17));
        addSlot(new Slot(container, StoveBlockEntity.VESSEL, 26, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return StoveBlockEntity.isVessel(stack);
            }
        });
        for (int i = 0; i < StoveBlockEntity.OUT_COUNT; i++) {
            addSlot(new OutputSlot(inventory.player, container, StoveBlockEntity.OUT_FIRST + i, 112 + i * 18, 35));
        }
        addStandardInventorySlots(inventory, 8, 84);
        addDataSlots(data);
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    private boolean canBake(ItemStack stack) {
        return level instanceof ServerLevel server
                && server.recipeAccess().getRecipeFor(MoreFoodKitchen.BAKING, new SingleRecipeInput(stack), server).isPresent();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        final Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        final ItemStack stack = slot.getItem();
        final ItemStack before = stack.copy();
        if (index < INV_START) {
            if (!moveItemStackTo(stack, INV_START, INV_END, index >= StoveBlockEntity.OUT_FIRST)) return ItemStack.EMPTY;
            if (index >= StoveBlockEntity.OUT_FIRST) slot.onQuickCraft(stack, before);
        } else if (canBake(stack)) {
            if (!moveItemStackTo(stack, StoveBlockEntity.OVEN, StoveBlockEntity.OVEN + 1, false)) return ItemStack.EMPTY;
        } else if (StoveBlockEntity.isVessel(stack)) {
            if (!moveItemStackTo(stack, StoveBlockEntity.VESSEL, StoveBlockEntity.VESSEL + 1, false)) return ItemStack.EMPTY;
        } else if (level.fuelValues().isFuel(stack)) {
            if (!moveItemStackTo(stack, StoveBlockEntity.FUEL, StoveBlockEntity.FUEL + 1, false)) return ItemStack.EMPTY;
        } else if (index < HOTBAR_START) {
            if (!moveItemStackTo(stack, HOTBAR_START, INV_END, false)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, INV_START, HOTBAR_START, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == before.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return before;
    }

    public boolean isLit() {
        return data.get(0) > 0;
    }

    public float litProgress() {
        final int duration = data.get(1) == 0 ? 200 : data.get(1);
        return Mth.clamp((float) data.get(0) / duration, 0, 1);
    }

    public float ovenProgress() {
        final int done = data.get(2);
        final int total = data.get(3);
        return total == 0 || done == 0 ? 0 : Mth.clamp((float) done / total, 0, 1);
    }

    public float ringProgress(int ring) {
        return Mth.clamp(data.get(4 + ring) / 100F, 0, 1);
    }

    private static class OutputSlot extends Slot {
        private final Player player;
        private int taken;

        OutputSlot(Player player, Container container, int index, int x, int y) {
            super(container, index, x, y);
            this.player = player;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public ItemStack remove(int amount) {
            if (hasItem()) taken += Math.min(amount, getItem().getCount());
            return super.remove(amount);
        }

        @Override
        public void onTake(Player player, ItemStack carried) {
            checkTakeAchievements(carried);
            super.onTake(player, carried);
        }

        @Override
        protected void onQuickCraft(ItemStack picked, int count) {
            taken += count;
            checkTakeAchievements(picked);
        }

        @Override
        protected void checkTakeAchievements(ItemStack carried) {
            carried.onCraftedBy(player, taken);
            if (player instanceof ServerPlayer server && container instanceof StoveBlockEntity stove) {
                stove.awardExperience(server);
            }
            taken = 0;
        }
    }
}
