package com.workshopcraft.morefood.neoforge;

import com.workshopcraft.morefood.stove.CookingPotBlockEntity;
import com.workshopcraft.morefood.stove.PotHolder;
import com.workshopcraft.morefood.stove.StoveBlock;
import com.workshopcraft.morefood.stove.StoveBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

//water into the hob pots or a campfire pot, one index per pot, fill only
public class PotFluidHandler extends SnapshotJournal<int[]> implements ResourceHandler<FluidResource> {
    private static final FluidResource WATER = FluidResource.of(Fluids.WATER);
    private final PotHolder stove;

    public PotFluidHandler(PotHolder stove) {
        this.stove = stove;
    }

    public static @Nullable ResourceHandler<FluidResource> of(StoveBlockEntity stove, @Nullable Direction side) {
        if (side == null || side == Direction.UP) return new PotFluidHandler(stove);
        final Direction front = stove.getBlockState().getValue(StoveBlock.FACING);
        return side.getAxis() != Direction.Axis.Y && side.getAxis() != front.getAxis() ? new PotFluidHandler(stove) : null;
    }

    public static @Nullable ResourceHandler<FluidResource> ofCampfire(CookingPotBlockEntity pot, @Nullable Direction side) {
        return side == Direction.DOWN ? null : new PotFluidHandler(pot);
    }

    @Override
    public int size() {
        return stove.potCount();
    }

    @Override
    public FluidResource getResource(int index) {
        return stove.potWater(index) > 0 ? WATER : FluidResource.EMPTY;
    }

    @Override
    public long getAmountAsLong(int index) {
        return Math.max(0, stove.potWater(index));
    }

    @Override
    public long getCapacityAsLong(int index, FluidResource resource) {
        return stove.potWater(index) >= 0 && (resource.isEmpty() || resource.equals(WATER)) ? 1000 : 0;
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        return resource.equals(WATER);
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (!resource.equals(WATER)) return 0;
        final int moved = Math.min(amount, stove.potRoom(index));
        if (moved <= 0) return 0;
        updateSnapshots(transaction);
        stove.setPotWater(index, stove.potWater(index) + moved);
        return moved;
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        return 0;
    }

    @Override
    protected int[] createSnapshot() {
        final int[] water = new int[stove.potCount()];
        for (int i = 0; i < water.length; i++) water[i] = stove.potWater(i);
        return water;
    }

    @Override
    protected void revertToSnapshot(int[] snapshot) {
        for (int i = 0; i < snapshot.length; i++) stove.setPotWater(i, snapshot[i]);
    }

    @Override
    protected void onRootCommit(int[] originalState) {
        stove.potChanged();
    }
}
