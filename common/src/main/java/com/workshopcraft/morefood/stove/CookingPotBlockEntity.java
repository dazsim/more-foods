package com.workshopcraft.morefood.stove;

import com.workshopcraft.morefood.MoreFoodItems;
import com.workshopcraft.morefood.MoreFoodKitchen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CookingPotBlockEntity extends BlockEntity implements WorldlyContainer, Kitchen, PotHolder {
    public static final float SLOWDOWN = 1.5F;
    public static final int TOP = 0;
    public static final int VESSEL = 1;
    public static final int OUT_FIRST = 2;
    public static final int OUT_COUNT = 2;
    public static final int SIZE = OUT_FIRST + OUT_COUNT;

    private static final int[] NONE = {};
    private static final int[] UP = {TOP};
    private static final int[] SIDES = {VESSEL, 2, 3};

    private NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    private final Pot pot = new Pot();
    private boolean pickedUp;

    public CookingPotBlockEntity(BlockPos pos, BlockState state) {
        super(MoreFoodKitchen.cookingPotEntity, pos, state);
    }

    public Pot pot() {
        return pot;
    }

    public static boolean onFire(Level level, BlockPos pos) {
        return CampfireBlock.isLitCampfire(level.getBlockState(pos.below()));
    }

    public void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        boolean visible = pot.tick(level, onFire(level, pos), SLOWDOWN, this);
        visible |= serveVessel();
        visible |= feedTop(level);
        if (visible) changed();
    }

    public void clientTick(Level level, BlockPos pos) {
        final RandomSource random = level.getRandom();
        if (!pot.isCooking() || random.nextInt(6) != 0) return;
        final double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.4;
        final double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.4;
        final double y = pos.getY() + liquidHeight(pot.surface()) / 16.0;
        level.addParticle(MoreFoodKitchen.STEAM, x, y, z, 0, 0.015 + random.nextDouble() * 0.015, 0);
    }

    /** The hob pot's liquid height, moved into this block's pixels. The pot sits 4px down into the campfire. */
    public static float liquidHeight(float hobSurface) {
        return 1.2F + (hobSurface - 17.5F) * 2.5F / 3.5F;
    }

    public void changed() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public boolean use(ServerLevel level, Player player, InteractionHand hand, ItemStack stack) {
        final BlockPos pos = getBlockPos();
        if (stack.isEmpty()) {
            if (player.isSecondaryUseActive()) {
                if (!pot.canPickUp()) return true;
                for (int i = 0; i < SIZE; i++) player.getInventory().placeItemBackInInventory(items.get(i));
                items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
                pickedUp = true;
                player.getInventory().placeItemBackInInventory(new ItemStack(MoreFoodItems.pot));
                level.removeBlock(pos, false);
                level.playSound(null, pos, SoundEvents.LANTERN_BREAK, SoundSource.BLOCKS, 1, 1);
                return true;
            }
            for (int i = OUT_FIRST; i < SIZE; i++) {
                if (!items.get(i).isEmpty()) {
                    player.getInventory().placeItemBackInInventory(items.get(i));
                    items.set(i, ItemStack.EMPTY);
                    changed();
                    return true;
                }
            }
            final ItemStack taken = pot.takeLast();
            if (taken.isEmpty()) return false;
            player.getInventory().placeItemBackInInventory(taken);
            changed();
            return true;
        }
        if (!pot.use(level, pos, player, hand, stack)) return false;
        changed();
        return true;
    }

    private boolean serveVessel() {
        final ItemStack vessel = items.get(VESSEL);
        if (vessel.isEmpty()) return false;
        final var serving = pot.servingFor(vessel);
        if (serving == null || !fits(serving.result().create())) return false;
        output(pot.serve(serving));
        vessel.shrink(1);
        return true;
    }

    private boolean feedTop(ServerLevel level) {
        final ItemStack buffer = items.get(TOP);
        if (buffer.isEmpty()) return false;
        final Optional<ItemStack> left = pot.insert(level, buffer, true);
        if (left.isEmpty() || !fits(left.get())) return false;
        pot.insert(level, buffer, false);
        if (!left.get().isEmpty()) output(left.get());
        buffer.shrink(1);
        return true;
    }

    @Override
    public boolean fits(ItemStack... stacks) {
        final List<ItemStack> copy = new ArrayList<>();
        for (int i = 0; i < OUT_COUNT; i++) copy.add(items.get(OUT_FIRST + i).copy());
        for (ItemStack stack : stacks) {
            if (!Kitchen.insert(copy, stack.copy(), getMaxStackSize())) return false;
        }
        return true;
    }

    @Override
    public void output(ItemStack stack) {
        Kitchen.insert(items.subList(OUT_FIRST, SIZE), stack.copy(), getMaxStackSize());
    }

    public int outputSignal() {
        float total = 0;
        for (int i = OUT_FIRST; i < SIZE; i++) {
            final ItemStack stack = items.get(i);
            if (!stack.isEmpty()) total += (float) stack.getCount() / getMaxStackSize(stack);
        }
        return Mth.lerpDiscrete(total / OUT_COUNT, 0, 15);
    }

    @Override
    public int potCount() {
        return 1;
    }

    @Override
    public int potWater(int index) {
        return pot.water();
    }

    @Override
    public int potRoom(int index) {
        return pot.room();
    }

    @Override
    public void setPotWater(int index, int mb) {
        pot.setWater(mb);
    }

    @Override
    public void potChanged() {
        changed();
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, items);
        pot.load(input.childOrEmpty("pot"));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
        pot.save(output.child("pot"));
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level == null || pickedUp) return;
        Containers.dropContents(level, pos, items);
        for (ItemStack stack : pot.items()) Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack.copy());
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        final ItemStack out = ContainerHelper.removeItem(items, slot, count);
        if (!out.isEmpty()) setChanged();
        return out;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        stack.limitSize(getMaxStackSize(stack));
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch (slot) {
            case TOP -> items.get(TOP).isEmpty();
            case VESSEL -> StoveBlockEntity.isVessel(stack);
            default -> false;
        };
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return side == Direction.UP ? UP : side == Direction.DOWN ? NONE : SIDES;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        if (side == Direction.UP) {
            return slot == TOP && items.get(TOP).isEmpty() && level instanceof ServerLevel server && pot.insert(server, stack, true).isPresent();
        }
        return side != Direction.DOWN && slot == VESSEL && StoveBlockEntity.isVessel(stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return side.getAxis() != Direction.Axis.Y && slot >= OUT_FIRST;
    }
}
