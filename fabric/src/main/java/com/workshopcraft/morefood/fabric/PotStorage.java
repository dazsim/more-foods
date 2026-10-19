package com.workshopcraft.morefood.fabric;

import com.workshopcraft.morefood.stove.StoveBlock;
import com.workshopcraft.morefood.stove.StoveBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.InsertionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.Nullable;

//water into the hob pots, left ring first, fill only. 1 mB is 81 droplets
public class PotStorage extends SnapshotParticipant<int[]> implements InsertionOnlyStorage<FluidVariant> {
    private static final long MB = FluidConstants.BUCKET / 1000;
    private final StoveBlockEntity stove;

    public PotStorage(StoveBlockEntity stove) {
        this.stove = stove;
    }

    public static @Nullable Storage<FluidVariant> of(StoveBlockEntity stove, @Nullable Direction side) {
        if (side == null || side == Direction.UP) return new PotStorage(stove);
        final Direction front = stove.getBlockState().getValue(StoveBlock.FACING);
        return side.getAxis() != Direction.Axis.Y && side.getAxis() != front.getAxis() ? new PotStorage(stove) : null;
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        if (!resource.isOf(Fluids.WATER)) return 0;
        long moved = 0;
        for (int i = 0; i < 2; i++) {
            final long mb = Math.min((maxAmount - moved) / MB, stove.potRoom(i));
            if (mb <= 0) continue;
            updateSnapshots(transaction);
            stove.setPotWater(i, stove.potWater(i) + (int) mb);
            moved += mb * MB;
        }
        return moved;
    }

    @Override
    protected int[] createSnapshot() {
        return new int[]{stove.potWater(0), stove.potWater(1)};
    }

    @Override
    protected void readSnapshot(int[] snapshot) {
        for (int i = 0; i < 2; i++) stove.setPotWater(i, snapshot[i]);
    }

    @Override
    protected void onFinalCommit() {
        stove.hobChanged();
    }
}
