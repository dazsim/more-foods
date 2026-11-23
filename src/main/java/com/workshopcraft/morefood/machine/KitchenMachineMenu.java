package com.workshopcraft.morefood.machine;

import com.workshopcraft.workshopcore.machine.FuelDrive;
import com.workshopcraft.workshopcore.machine.Fuels;
import com.workshopcraft.workshopcore.machine.MachineBlockEntity;
import com.workshopcraft.workshopcore.machine.MachineMenuData;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import static com.workshopcraft.morefood.machine.KitchenMachineBlockEntity.*;

public class KitchenMachineMenu extends AbstractContainerMenu {
    private static final int INV_START = SIZE;
    private static final int INV_END = INV_START + 36;

    private final Kind kind;
    private final Container container;
    private final ContainerData data;
    private final Level level;
    private final Slot fuel;
    // the client has no drive, so the server says whether the drive burns fuel
    private final DataSlot burnsFuel;

    public static KitchenMachineMenu grinder(int id, Inventory inventory) {
        return new KitchenMachineMenu(Kind.GRINDER, id, inventory, new SimpleContainer(SIZE), new SimpleContainerData(MachineMenuData.COUNT));
    }

    public static KitchenMachineMenu press(int id, Inventory inventory) {
        return new KitchenMachineMenu(Kind.PRESS, id, inventory, new SimpleContainer(SIZE), new SimpleContainerData(MachineMenuData.COUNT));
    }

    public KitchenMachineMenu(Kind kind, int id, Inventory inventory, Container container, ContainerData data) {
        super(kind.menu(), id);
        checkContainerSize(container, SIZE);
        checkContainerDataCount(data, MachineMenuData.COUNT);
        this.kind = kind;
        this.container = container;
        this.data = data;
        this.level = inventory.player.level();
        for (int i = 0; i < IN_COUNT; i++) {
            addSlot(new Slot(container, IN_FIRST + i, 26 + i * 18, 17) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return kind.isIngredient(level, stack);
                }
            });
        }
        addSlot(new Slot(container, OUT, 116, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        addSlot(new Slot(container, BYPRODUCT, 142, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        burnsFuel = container instanceof MachineBlockEntity m ? new DataSlot() {
            @Override
            public int get() {
                return m.drive() instanceof FuelDrive ? 1 : 0;
            }

            @Override
            public void set(int value) {
            }
        } : DataSlot.standalone();
        fuel = addSlot(new Slot(container, FUEL, 44, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                if (container instanceof MachineBlockEntity m) return m.drive().acceptsFuel(m, stack);
                return burnsFuel.get() == 1 && Fuels.isFuel(level, stack);
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
        }
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        addDataSlots(data);
        addDataSlot(burnsFuel);
    }

    public Kind kind() {
        return kind;
    }

    public float progress() {
        return MachineMenuData.progress(data);
    }

    public float drive() {
        return MachineMenuData.gauge(data);
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        final Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        final ItemStack stack = slot.getItem();
        final ItemStack copy = stack.copy();
        if (index < INV_START) {
            if (!moveItemStackTo(stack, INV_START, INV_END, true)) return ItemStack.EMPTY;
        } else if (kind.isIngredient(level, stack)) {
            if (!moveItemStackTo(stack, IN_FIRST, IN_FIRST + IN_COUNT, false)) return ItemStack.EMPTY;
        } else if (fuel.mayPlace(stack)) {
            if (!moveItemStackTo(stack, FUEL, FUEL + 1, false)) return ItemStack.EMPTY;
        } else {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return copy;
    }
}
