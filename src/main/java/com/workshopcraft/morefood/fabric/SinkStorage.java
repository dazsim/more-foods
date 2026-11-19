package com.workshopcraft.morefood.fabric;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ExtractionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.level.material.Fluids;

//the kitchen sink, endless water out, nothing in
public class SinkStorage implements ExtractionOnlyStorage<FluidVariant>, SingleSlotStorage<FluidVariant> {
    public static final SinkStorage INSTANCE = new SinkStorage();
    private static final FluidVariant WATER = FluidVariant.of(Fluids.WATER);

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        return resource.isOf(Fluids.WATER) ? maxAmount : 0;
    }

    @Override
    public boolean isResourceBlank() {
        return false;
    }

    @Override
    public FluidVariant getResource() {
        return WATER;
    }

    @Override
    public long getAmount() {
        return FluidConstants.BUCKET;
    }

    @Override
    public long getCapacity() {
        return FluidConstants.BUCKET;
    }
}
