package com.workshopcraft.morefood.legacy;

//? if <26.1 {
/*import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/^* Gives older container block entities the 26.1 value input/output save hooks and item list helpers. ^/
public abstract class LegacyContainerBlockEntity extends net.minecraft.world.level.block.entity.BaseContainerBlockEntity {
    protected LegacyContainerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    //? if <1.21 {
    /^protected abstract NonNullList<ItemStack> getItems();

    protected abstract void setItems(NonNullList<ItemStack> items);

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        loadAdditional(new ValueInput(tag, null));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        saveAdditional(new ValueOutput(tag, null));
    }

    @Override
    public CompoundTag getUpdateTag() {
        return getUpdateTag(level != null ? level.registryAccess() : null);
    }

    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return super.getUpdateTag();
    }

    protected CompoundTag saveCustomOnly(HolderLookup.Provider registries) {
        return saveWithoutMetadata();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : getItems()) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return getItems().get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        final ItemStack out = ContainerHelper.removeItem(getItems(), slot, count);
        if (!out.isEmpty()) setChanged();
        return out;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(getItems(), slot);
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        getItems().clear();
    }
    ^///?} else {
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        loadAdditional(new ValueInput(tag, registries));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        saveAdditional(new ValueOutput(tag, registries));
    }
    //?}

    protected void loadAdditional(ValueInput input) {
    }

    protected void saveAdditional(ValueOutput output) {
    }

    //? if forge {
    /^private net.minecraftforge.common.util.LazyOptional<?>[] sided;

    // Forge wraps every container unsided, which would skip the stove's slot rules
    @Override
    public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> cap,
                                                                             net.minecraft.core.@org.jspecify.annotations.Nullable Direction side) {
        if (cap == net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER && !remove && side != null
                && this instanceof net.minecraft.world.WorldlyContainer worldly) {
            if (sided == null) sided = net.minecraftforge.items.wrapper.SidedInvWrapper.create(worldly, net.minecraft.core.Direction.values());
            return sided[side.ordinal()].cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        if (sided != null) for (var handler : sided) handler.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        sided = null;
    }
    ^///?}

    /^* Called by the block just before it is removed, as 26.1 does itself. ^/
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null) Containers.dropContents(level, pos, this);
    }
}
*///?}
