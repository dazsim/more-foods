package com.workshopcraft.morefood.forge;

//? if forge {
/*import com.workshopcraft.morefood.board.AssemblyBoardBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jspecify.annotations.Nullable;

//next layers go in the top and sides one at a time, the finished item comes out of the bottom
public final class BoardItemHandler implements IItemHandler {
    private final AssemblyBoardBlockEntity board;
    private final @Nullable Direction side;

    private BoardItemHandler(AssemblyBoardBlockEntity board, @Nullable Direction side) {
        this.board = board;
        this.side = side;
    }

    public static BoardItemHandler of(AssemblyBoardBlockEntity board, @Nullable Direction side) {
        return new BoardItemHandler(board, side);
    }

    private @Nullable ServerLevel level() {
        return board.getLevel() instanceof ServerLevel server ? server : null;
    }

    private ItemStack ready() {
        final ServerLevel level = level();
        if (level == null || side != null && side != Direction.DOWN || !board.canExtract(level)) return ItemStack.EMPTY;
        return board.result(level);
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return slot == 0 ? ready() : ItemStack.EMPTY;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || !isItemValid(slot, stack)) return stack;
        if (!simulate) {
            board.addRaw(stack.copyWithCount(1), level().getGameTime());
            board.changed();
        }
        return stack.copyWithCount(stack.getCount() - 1);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot != 0) return ItemStack.EMPTY;
        final ItemStack out = ready();
        if (out.isEmpty() || amount < out.getCount()) return ItemStack.EMPTY;
        if (!simulate) {
            board.clearRaw();
            board.changed();
        }
        return out;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        final ServerLevel level = level();
        return slot == 0 && level != null && side != Direction.DOWN && !stack.isEmpty() && board.canAdd(level, stack.copyWithCount(1));
    }
}
*///?}
