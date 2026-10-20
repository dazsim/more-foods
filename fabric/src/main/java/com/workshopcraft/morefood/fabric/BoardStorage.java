package com.workshopcraft.morefood.fabric;

import com.workshopcraft.morefood.board.AssemblyBoardBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.List;

//next layers go in the top and sides one at a time, the finished item comes out of the bottom
public final class BoardStorage extends SnapshotParticipant<BoardStorage.Snapshot> implements SingleSlotStorage<ItemVariant> {
    record Snapshot(List<ItemStack> layers, long lastAdd) {}

    private final AssemblyBoardBlockEntity board;
    private final @Nullable Direction side;

    private BoardStorage(AssemblyBoardBlockEntity board, @Nullable Direction side) {
        this.board = board;
        this.side = side;
    }

    public static BoardStorage of(AssemblyBoardBlockEntity board, @Nullable Direction side) {
        return new BoardStorage(board, side);
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
    public long insert(ItemVariant resource, long maxAmount, TransactionContext tx) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        final ServerLevel level = level();
        if (level == null || side == Direction.DOWN || maxAmount < 1) return 0;
        final ItemStack stack = resource.toStack(1);
        if (!board.canAdd(level, stack)) return 0;
        updateSnapshots(tx);
        board.addRaw(stack, level.getGameTime());
        return 1;
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext tx) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        final ItemStack out = ready();
        if (out.isEmpty() || !resource.matches(out) || maxAmount < out.getCount()) return 0;
        updateSnapshots(tx);
        board.clearRaw();
        return out.getCount();
    }

    @Override
    public boolean isResourceBlank() {
        return ready().isEmpty();
    }

    @Override
    public ItemVariant getResource() {
        final ItemStack out = ready();
        return out.isEmpty() ? ItemVariant.blank() : ItemVariant.of(out);
    }

    @Override
    public long getAmount() {
        return ready().getCount();
    }

    @Override
    public long getCapacity() {
        return 1;
    }

    @Override
    protected Snapshot createSnapshot() {
        return new Snapshot(board.copyLayers(), board.lastAdd());
    }

    @Override
    protected void readSnapshot(Snapshot snapshot) {
        board.restore(snapshot.layers(), snapshot.lastAdd());
    }

    @Override
    protected void onFinalCommit() {
        board.changed();
    }
}
