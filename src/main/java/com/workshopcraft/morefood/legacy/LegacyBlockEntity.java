package com.workshopcraft.morefood.legacy;

//? if <26.1 {
/*import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/^* Gives older block entities the 26.1 value input/output save hooks. ^/
public abstract class LegacyBlockEntity extends net.minecraft.world.level.block.entity.BlockEntity {
    protected LegacyBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    //? if <1.21 {
    /^@Override
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

    /^* Called by the block just before it is removed, as 26.1 does itself. ^/
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
    }
}
*///?}
