package com.workshopcraft.morefood.forge;

//? if forge {
/*import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

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
