package com.workshopcraft.morefood.forge;

//? if forge {
/*import com.workshopcraft.morefood.stove.CookingPotBlockEntity;
import com.workshopcraft.morefood.stove.PotHolder;
import com.workshopcraft.morefood.stove.StoveBlock;
import com.workshopcraft.morefood.stove.StoveBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jspecify.annotations.Nullable;

//water into the hob pots or a campfire pot, left ring first, fill only
public class PotFluidHandler implements IFluidHandler {
    private final PotHolder stove;

    public PotFluidHandler(PotHolder stove) {
        this.stove = stove;
    }

    public static @Nullable IFluidHandler of(StoveBlockEntity stove, @Nullable Direction side) {
        if (side == null || side == Direction.UP) return new PotFluidHandler(stove);
        final Direction front = stove.getBlockState().getValue(StoveBlock.FACING);
        return side.getAxis() != Direction.Axis.Y && side.getAxis() != front.getAxis() ? new PotFluidHandler(stove) : null;
    }

    public static @Nullable IFluidHandler ofCampfire(CookingPotBlockEntity pot, @Nullable Direction side) {
        return side == Direction.DOWN ? null : new PotFluidHandler(pot);
    }

    @Override
    public int getTanks() {
        return stove.potCount();
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        final int water = stove.potWater(tank);
        return water > 0 ? new FluidStack(Fluids.WATER, water) : FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        return stove.potWater(tank) >= 0 ? 1000 : 0;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return stack.getFluid().isSame(Fluids.WATER);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || !resource.getFluid().isSame(Fluids.WATER)) return 0;
        int moved = 0;
        for (int i = 0; i < stove.potCount(); i++) {
            final int mb = Math.min(resource.getAmount() - moved, stove.potRoom(i));
            if (mb <= 0) continue;
            if (action.execute()) stove.setPotWater(i, stove.potWater(i) + mb);
            moved += mb;
        }
        if (moved > 0 && action.execute()) stove.potChanged();
        return moved;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return FluidStack.EMPTY;
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return FluidStack.EMPTY;
    }
}
*///?}
