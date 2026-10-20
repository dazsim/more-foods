package com.workshopcraft.morefood.neoforge;

import com.workshopcraft.morefood.board.AssemblyBoardBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

import java.util.List;

//next layers go in the top and sides one at a time, the finished item comes out of the bottom
public final class BoardItemHandler extends SnapshotJournal<BoardItemHandler.Snapshot> implements ResourceHandler<ItemResource> {
    record Snapshot(List<ItemStack> layers, long lastAdd) {}

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
    public int size() {
        return 1;
    }

    @Override
    public ItemResource getResource(int index) {
        final ItemStack out = index == 0 ? ready() : ItemStack.EMPTY;
        return out.isEmpty() ? ItemResource.EMPTY : ItemResource.of(out);
    }

    @Override
    public long getAmountAsLong(int index) {
        return index == 0 ? ready().getCount() : 0;
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        return index == 0 ? 1 : 0;
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        final ServerLevel level = level();
        return index == 0 && level != null && side != Direction.DOWN && !resource.isEmpty() && board.canAdd(level, resource.toStack(1));
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext tx) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount < 1 || !isValid(index, resource)) return 0;
        final ServerLevel level = level();
        updateSnapshots(tx);
        board.addRaw(resource.toStack(1), level.getGameTime());
        return 1;
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext tx) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (index != 0) return 0;
        final ItemStack out = ready();
        if (out.isEmpty() || !resource.matches(out) || amount < out.getCount()) return 0;
        updateSnapshots(tx);
        board.clearRaw();
        return out.getCount();
    }

    @Override
    protected Snapshot createSnapshot() {
        return new Snapshot(board.copyLayers(), board.lastAdd());
    }

    @Override
    protected void revertToSnapshot(Snapshot snapshot) {
        board.restore(snapshot.layers(), snapshot.lastAdd());
    }

    @Override
    protected void onRootCommit(Snapshot original) {
        board.changed();
    }
}
