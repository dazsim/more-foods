package com.workshopcraft.morefood.neoforge;

//? if >=26.1 {
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

//the kitchen sink, endless water out, nothing in
public class SinkFluidHandler implements ResourceHandler<FluidResource> {
    public static final SinkFluidHandler INSTANCE = new SinkFluidHandler();
    private static final FluidResource WATER = FluidResource.of(Fluids.WATER);

    @Override
    public int size() {
        return 1;
    }

    @Override
    public FluidResource getResource(int index) {
        return WATER;
    }

    @Override
    public long getAmountAsLong(int index) {
        return 1000;
    }

    @Override
    public long getCapacityAsLong(int index, FluidResource resource) {
        return 1000;
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        return resource.equals(WATER);
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        return resource.equals(WATER) ? amount : 0;
    }
}
//?} else {
/*import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

//the kitchen sink, endless water out, nothing in
public class SinkFluidHandler implements IFluidHandler {
    public static final SinkFluidHandler INSTANCE = new SinkFluidHandler();

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return new FluidStack(Fluids.WATER, 1000);
    }

    @Override
    public int getTankCapacity(int tank) {
        return 1000;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return stack.getFluid().isSame(Fluids.WATER);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return 0;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return resource.isEmpty() || !resource.getFluid().isSame(Fluids.WATER) ? FluidStack.EMPTY : new FluidStack(Fluids.WATER, resource.getAmount());
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return maxDrain <= 0 ? FluidStack.EMPTY : new FluidStack(Fluids.WATER, maxDrain);
    }
}
*///?}
