package com.workshopcraft.morefood.machine;

import com.workshopcraft.morefood.MoreFoodItems;
import com.workshopcraft.morefood.MoreFoodKitchen;
import com.workshopcraft.morefood.MoreFoodMachines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
//? if >=1.20.5
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Milk in buckets (1000 mB) and bottles (250 mB). Hoppers and pipes put full or empty buckets and bottles in from the top
 * or sides of either half and take what comes back out of the bottom. The top half hands everything to the bottom one.
 */
public class MilkChurnBlockEntity extends BlockEntity implements WorldlyContainer {
    public static final int CAPACITY = 8000, BUCKET = 1000, BOTTLE = 250;
    public static final int IN = 0, OUT = 1, SIZE = 2;
    private static final int[] INPUT = {IN};
    private static final int[] OUTPUT = {OUT};

    private NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    private int milk;

    public MilkChurnBlockEntity(BlockPos pos, BlockState state) {
        super(MoreFoodMachines.churnEntity, pos, state);
    }

    public boolean isUpper() {
        return getBlockState().getValue(MilkChurnBlock.HALF) == DoubleBlockHalf.UPPER;
    }

    /** The bottom half, which holds the milk and the items. */
    public MilkChurnBlockEntity main() {
        if (isUpper() && level != null && level.getBlockEntity(worldPosition.below()) instanceof MilkChurnBlockEntity lower && !lower.isUpper()) return lower;
        return this;
    }

    public int milk() {
        return milk;
    }

    public int space() {
        return CAPACITY - milk;
    }

    /** Takes mb out, or nothing when there is not that much. For whatever uses the milk later, like butter. */
    /** Puts back the milk a broken churn kept. */
    public void setMilk(int mb) {
        milk = Math.max(0, Math.min(CAPACITY, mb));
        changed();
    }

    public boolean drain(int mb) {
        if (milk < mb) return false;
        milk -= mb;
        changed();
        return true;
    }

    public boolean fill(int mb) {
        if (space() < mb) return false;
        milk += mb;
        changed();
        return true;
    }

    private static @Nullable Item milkBottle() {
        return MoreFoodItems.milkBottle;
    }

    public static boolean handles(ItemStack stack) {
        return stack.is(Items.MILK_BUCKET) || stack.is(Items.BUCKET) || stack.is(Items.GLASS_BOTTLE) || milkBottle() != null && stack.is(milkBottle());
    }

    /** What the stack turns into here, or empty when it cannot be used right now. */
    private ItemStack swap(ItemStack stack, boolean apply) {
        final Item bottle = milkBottle();
        final int mb;
        final ItemStack back;
        if (stack.is(Items.MILK_BUCKET)) { mb = BUCKET; back = new ItemStack(Items.BUCKET); }
        else if (bottle != null && stack.is(bottle)) { mb = BOTTLE; back = new ItemStack(Items.GLASS_BOTTLE); }
        else if (stack.is(Items.BUCKET)) { mb = -BUCKET; back = new ItemStack(Items.MILK_BUCKET); }
        else if (stack.is(Items.GLASS_BOTTLE) && bottle != null) { mb = -BOTTLE; back = new ItemStack(bottle); }
        else return ItemStack.EMPTY;
        if (mb > 0 ? space() < mb : milk < -mb) return ItemStack.EMPTY;
        if (apply) {
            if (mb > 0) fill(mb);
            else drain(-mb);
        }
        return back;
    }

    public boolean use(Player player, InteractionHand hand, ItemStack stack) {
        if (swap(stack, false).isEmpty()) return false;
        if (level == null || level.isClientSide()) return true;
        final boolean emptying = stack.is(Items.MILK_BUCKET) || !stack.is(Items.BUCKET) && !stack.is(Items.GLASS_BOTTLE);
        final boolean bucket = stack.is(Items.MILK_BUCKET) || stack.is(Items.BUCKET);
        final ItemStack back = swap(stack, true);
        player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, back));
        final SoundEvent sound = bucket ? emptying ? SoundEvents.BUCKET_EMPTY : SoundEvents.BUCKET_FILL
                : emptying ? SoundEvents.BOTTLE_EMPTY : SoundEvents.BOTTLE_FILL;
        level.playSound(null, worldPosition, sound, SoundSource.BLOCKS, 1, 1);
        return true;
    }

    public void serverTick() {
        final ItemStack in = items.get(IN);
        if (in.isEmpty()) return;
        final ItemStack back = swap(in, false);
        if (back.isEmpty() || !items.get(OUT).isEmpty()) return;
        swap(in, true);
        items.set(IN, ItemStack.EMPTY);
        items.set(OUT, back);
        setChanged();
    }

    private void changed() {
        setChanged();
        if (level == null || level.isClientSide()) return;
        final int shown = (milk + BUCKET - 1) / BUCKET;
        for (BlockPos at : new BlockPos[] {worldPosition, worldPosition.above()}) {
            final BlockState state = level.getBlockState(at);
            if (state.getBlock() instanceof MilkChurnBlock && state.getValue(MilkChurnBlock.MILK) != shown) {
                level.setBlock(at, state.setValue(MilkChurnBlock.MILK, shown), 3);
            }
        }
        level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, items);
        milk = Math.max(0, Math.min(CAPACITY, input.getIntOr("milk", 0)));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
        // left out when empty, so an emptied churn drops the same as a new one and they stack (1.20.1 copies this tag)
        if (milk > 0) output.putInt("milk", milk);
    }

    //? if >=1.20.5 {
    /** Lets the loot table copy the milk onto the dropped churn. */
    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (milk > 0) components.set(MoreFoodKitchen.MILK, milk);
    }
    //?}

    // reading the milk here marks it used, so the placed churn does not also keep a stale copy to drop later
    //? if >=26.1 {
    @Override
    protected void applyImplicitComponents(net.minecraft.core.component.DataComponentGetter components) {
        super.applyImplicitComponents(components);
        milk = Math.max(0, Math.min(CAPACITY, components.getOrDefault(MoreFoodKitchen.MILK, 0)));
    }
    //?} else if >=1.20.5 {
    /*@Override
    protected void applyImplicitComponents(net.minecraft.world.level.block.entity.BlockEntity.DataComponentInput components) {
        super.applyImplicitComponents(components);
        milk = Math.max(0, Math.min(CAPACITY, components.getOrDefault(MoreFoodKitchen.MILK, 0)));
    }
    *///?}

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && state.getValue(MilkChurnBlock.HALF) == DoubleBlockHalf.LOWER) Containers.dropContents(level, pos, items);
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        final MilkChurnBlockEntity m = main();
        return m.items.get(IN).isEmpty() && m.items.get(OUT).isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return main().items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        final MilkChurnBlockEntity m = main();
        final ItemStack out = ContainerHelper.removeItem(m.items, slot, count);
        if (!out.isEmpty()) m.setChanged();
        return out;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(main().items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        final MilkChurnBlockEntity m = main();
        m.items.set(slot, stack);
        stack.limitSize(getMaxStackSize(stack));
        m.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    @Override
    public void clearContent() {
        main().items.clear();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        final MilkChurnBlockEntity m = main();
        return slot == IN && m.items.get(IN).isEmpty() && m.items.get(OUT).isEmpty() && !m.swap(stack, false).isEmpty();
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return side == Direction.DOWN ? OUTPUT : INPUT;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return side != Direction.DOWN && canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return side == Direction.DOWN && slot == OUT;
    }
}
