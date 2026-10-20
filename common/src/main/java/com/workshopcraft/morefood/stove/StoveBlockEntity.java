package com.workshopcraft.morefood.stove;

import com.mojang.serialization.Codec;
import com.workshopcraft.morefood.MoreFoodItems;
import com.workshopcraft.morefood.MoreFoodKitchen;
import com.workshopcraft.morefood.crafting.BakingRecipe;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Containers;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StoveBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer, Kitchen, PotHolder {
    public static class Ring {
        public HobTool tool = HobTool.NONE;
        public final Pan pan = new Pan();
        public final Pot pot = new Pot();
        private int progress;

        public boolean isEmpty() {
            return tool == HobTool.PAN ? pan.isEmpty() : tool != HobTool.POT || pot.canPickUp();
        }
    }

    public static final int FUEL = 0;
    public static final int OVEN = 1;
    public static final int VESSEL = 2;
    public static final int OUT_FIRST = 3;
    public static final int OUT_COUNT = 3;
    public static final int MENU_SIZE = OUT_FIRST + OUT_COUNT;
    public static final int HOB = MENU_SIZE;
    public static final int SIZE = HOB + 1;
    public static final int LEFT = 0;
    public static final int RIGHT = 1;

    private static final int[] NONE = {};
    private static final int[] TOP = {HOB};
    private static final int[] BACK = {OVEN};
    private static final int[] SIDES = {FUEL, VESSEL};
    private static final int[] BOTTOM = {3, 4, 5, FUEL};
    private static final Codec<HobTool> TOOL_CODEC = StringRepresentable.fromEnum(HobTool::values);
    private static final Codec<Map<ResourceKey<Recipe<?>>, Integer>> USED_CODEC = Codec.unboundedMap(Recipe.KEY_CODEC, Codec.INT);

    private NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    private int litTime;
    private int litDuration;
    private int ovenTime;
    private int ovenTotal;
    private final Ring[] rings = {new Ring(), new Ring()};
    private final Reference2IntOpenHashMap<ResourceKey<Recipe<?>>> used = new Reference2IntOpenHashMap<>();
    private final RecipeManager.CachedCheck<SingleRecipeInput, BakingRecipe> bakingCheck = RecipeManager.createCheck(MoreFoodKitchen.BAKING);

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int id) {
            return switch (id) {
                case 0 -> litTime;
                case 1 -> litDuration;
                case 2 -> ovenTime;
                case 3 -> ovenTotal;
                case 4 -> rings[LEFT].progress;
                case 5 -> rings[RIGHT].progress;
                default -> 0;
            };
        }

        @Override
        public void set(int id, int value) {
            switch (id) {
                case 0 -> litTime = value;
                case 1 -> litDuration = value;
                case 2 -> ovenTime = value;
                case 3 -> ovenTotal = value;
                case 4 -> rings[LEFT].progress = value;
                case 5 -> rings[RIGHT].progress = value;
                default -> {}
            }
        }

        @Override
        public int getCount() {
            return StoveMenu.DATA_COUNT;
        }
    };

    public StoveBlockEntity(BlockPos pos, BlockState state) {
        super(MoreFoodKitchen.stoveEntity, pos, state);
    }

    public void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        final boolean wasLit = litTime > 0;
        boolean changed = false;
        if (litTime > 0) litTime--;

        final ItemStack input = items.get(OVEN);
        RecipeHolder<BakingRecipe> recipe = null;
        ItemStack result = ItemStack.EMPTY;
        if (!input.isEmpty()) {
            final SingleRecipeInput in = new SingleRecipeInput(input);
            recipe = bakingCheck.getRecipeFor(in, level).orElse(null);
            if (recipe != null) result = recipe.value().assemble(in);
        }
        final boolean ovenReady = recipe != null && !result.isEmpty() && fitsOutputs(result, remainderOf(input));

        boolean hobWants = false;
        for (Ring ring : rings) {
            if (ring.tool == HobTool.PAN) hobWants |= ring.pan.wantsHeat(level);
            else if (ring.tool == HobTool.POT) hobWants |= ring.pot.wantsHeat(level);
        }
        if ((ovenReady || hobWants) && litTime <= 0) {
            final ItemStack fuel = items.get(FUEL);
            final int burn = level.fuelValues().burnDuration(fuel);
            if (burn > 0) {
                litTime = litDuration = burn;
                final Item fuelItem = fuel.getItem();
                fuel.shrink(1);
                if (fuel.isEmpty()) {
                    final ItemStackTemplate left = fuelItem.getCraftingRemainder();
                    items.set(FUEL, left != null ? left.create() : ItemStack.EMPTY);
                }
                changed = true;
            }
        }

        if (ovenReady) {
            ovenTotal = recipe.value().cookingTime();
            if (litTime > 0) {
                if (++ovenTime >= ovenTotal) {
                    ovenTime = 0;
                    final ItemStack remainder = remainderOf(input);
                    input.shrink(1);
                    insertOutputs(result);
                    insertOutputs(remainder);
                    used.addTo(recipe.id(), 1);
                    changed = true;
                }
            } else if (ovenTime > 0) {
                ovenTime = Mth.clamp(ovenTime - 2, 0, ovenTotal);
            }
        } else {
            ovenTime = 0;
        }

        boolean visible = false;
        for (Ring ring : rings) {
            if (ring.tool == HobTool.PAN) {
                visible |= ring.pan.tick(level, litTime > 0, this);
                ring.progress = ring.pan.progress(level);
            } else if (ring.tool == HobTool.POT) {
                visible |= ring.pot.tick(level, litTime > 0, 1, this);
                ring.progress = ring.pot.progress(level, 1);
                visible |= serveVessels(ring.pot);
            } else {
                ring.progress = 0;
            }
        }
        visible |= feedHob(level);
        if (visible) {
            changed = true;
            level.sendBlockUpdated(pos, state, state, 3);
        }

        final boolean lit = litTime > 0;
        if (wasLit != lit) {
            state = state.setValue(StoveBlock.LIT, lit);
            level.setBlock(pos, state, 3);
            changed = true;
        }
        if (changed) setChanged(level, pos, state);
    }

    private static ItemStack remainderOf(ItemStack input) {
        return MoreFoodItems.remainder(input);
    }

    private boolean fitsOutputs(ItemStack result, ItemStack remainder) {
        final List<ItemStack> copy = new ArrayList<>();
        for (int i = 0; i < OUT_COUNT; i++) copy.add(items.get(OUT_FIRST + i).copy());
        return insert(copy, result.copy()) && insert(copy, remainder.copy());
    }

    private void insertOutputs(ItemStack stack) {
        insert(items.subList(OUT_FIRST, OUT_FIRST + OUT_COUNT), stack);
    }

    private boolean insert(List<ItemStack> slots, ItemStack stack) {
        return Kitchen.insert(slots, stack, getMaxStackSize());
    }

    @Override
    public boolean fits(ItemStack... stacks) {
        final List<ItemStack> copy = new ArrayList<>();
        for (int i = 0; i < OUT_COUNT; i++) copy.add(items.get(OUT_FIRST + i).copy());
        for (ItemStack stack : stacks) {
            if (!insert(copy, stack.copy())) return false;
        }
        return true;
    }

    @Override
    public void output(ItemStack stack) {
        insertOutputs(stack.copy());
    }

    public boolean useHob(ServerLevel level, int index, Player player, InteractionHand hand, ItemStack stack) {
        final Ring ring = rings[index];
        final BlockPos pos = getBlockPos();
        if (ring.tool == HobTool.NONE) {
            final HobTool tool = stack.is(MoreFoodItems.fryingPan) ? HobTool.PAN : stack.is(MoreFoodItems.pot) ? HobTool.POT : HobTool.NONE;
            if (tool == HobTool.NONE) return false;
            setTool(index, tool);
            stack.consume(1, player);
            level.playSound(null, pos, SoundEvents.LANTERN_PLACE, SoundSource.BLOCKS, 1, 1);
            return true;
        }
        if (stack.isEmpty()) {
            if (player.isSecondaryUseActive()) {
                if (ring.isEmpty()) {
                    player.getInventory().placeItemBackInInventory(new ItemStack(ring.tool == HobTool.PAN ? MoreFoodItems.fryingPan : MoreFoodItems.pot));
                    setTool(index, HobTool.NONE);
                    level.playSound(null, pos, SoundEvents.LANTERN_BREAK, SoundSource.BLOCKS, 1, 1);
                }
                return true;
            }
            final ItemStack taken = ring.tool == HobTool.PAN ? ring.pan.takeLast() : ring.pot.takeLast();
            if (taken.isEmpty()) return false;
            player.getInventory().placeItemBackInInventory(taken);
            hobChanged();
            return true;
        }
        if (ring.tool == HobTool.POT) {
            if (!ring.pot.use(level, pos, player, hand, stack)) return false;
            hobChanged();
            return true;
        }
        final Optional<ItemStack> left = insertInto(level, ring, stack, false);
        if (left.isEmpty()) return false;
        if (left.get().isEmpty()) stack.consume(1, player);
        else player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, left.get()));
        hobChanged();
        return true;
    }

    public void clientTick(Level level, BlockPos pos, BlockState state) {
        if (!state.getValue(StoveBlock.LIT)) return;
        final RandomSource random = level.getRandom();
        final Direction left = state.getValue(StoveBlock.FACING).getClockWise();
        for (int i = 0; i < rings.length; i++) {
            final Ring ring = rings[i];
            if (ring.tool != HobTool.POT || !ring.pot.isCooking() || random.nextInt(6) != 0) continue;
            final double side = i == LEFT ? 0.25 : -0.25;
            final double x = pos.getX() + 0.5 + left.getStepX() * side + (random.nextDouble() - 0.5) * 0.25;
            final double z = pos.getZ() + 0.5 + left.getStepZ() * side + (random.nextDouble() - 0.5) * 0.25;
            final double y = pos.getY() + ring.pot.surface() / 16.0;
            level.addParticle(MoreFoodKitchen.STEAM, x, y, z, 0, 0.015 + random.nextDouble() * 0.015, 0);
        }
    }

    public Ring ring(int index) {
        return rings[index];
    }

    @Override
    public int potCount() {
        return rings.length;
    }

    @Override
    public void potChanged() {
        hobChanged();
    }

    @Override
    public int potWater(int index) {
        return rings[index].tool == HobTool.POT ? rings[index].pot.water() : -1;
    }

    @Override
    public int potRoom(int index) {
        return rings[index].tool == HobTool.POT ? rings[index].pot.room() : 0;
    }

    @Override
    public void setPotWater(int index, int mb) {
        if (rings[index].tool == HobTool.POT) rings[index].pot.setWater(mb);
    }

    public void setTool(int index, HobTool tool) {
        rings[index].tool = tool;
        rings[index].pan.clear();
        rings[index].pot.clear();
        if (level != null) {
            final BlockState state = getBlockState().setValue(index == LEFT ? StoveBlock.LEFT : StoveBlock.RIGHT, tool);
            level.setBlock(worldPosition, state, 3);
        }
        hobChanged();
    }

    public void hobChanged() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    private boolean serveVessels(Pot pot) {
        final ItemStack vessel = items.get(VESSEL);
        if (vessel.isEmpty()) return false;
        final var serving = pot.servingFor(vessel);
        if (serving == null || !fits(serving.result().create())) return false;
        output(pot.serve(serving));
        vessel.shrink(1);
        return true;
    }

    /** What happens when an item reaches the hob, by hand or from a hopper. Empty when nothing takes it. */
    public Optional<ItemStack> hobInsert(ServerLevel level, ItemStack stack, boolean simulate) {
        for (Ring ring : rings) {
            final Optional<ItemStack> left = insertInto(level, ring, stack, simulate);
            if (left.isPresent()) return left;
        }
        return Optional.empty();
    }

    public Optional<ItemStack> insertInto(ServerLevel level, Ring ring, ItemStack stack, boolean simulate) {
        if (ring.tool == HobTool.POT) return ring.pot.insert(level, stack, simulate);
        if (ring.tool == HobTool.PAN && ring.pan.accepts(level, stack)) {
            if (!simulate) ring.pan.add(stack);
            return Optional.of(remainderOf(stack));
        }
        return Optional.empty();
    }

    private boolean feedHob(ServerLevel level) {
        final ItemStack buffer = items.get(HOB);
        if (buffer.isEmpty()) return false;
        final Optional<ItemStack> left = hobInsert(level, buffer, true);
        if (left.isEmpty() || !fits(left.get())) return false;
        hobInsert(level, buffer, false);
        if (!left.get().isEmpty()) output(left.get());
        buffer.shrink(1);
        return true;
    }

    public int outputSignal() {
        float total = 0;
        for (int i = OUT_FIRST; i < MENU_SIZE; i++) {
            final ItemStack stack = items.get(i);
            if (!stack.isEmpty()) total += (float) stack.getCount() / getMaxStackSize(stack);
        }
        return Mth.lerpDiscrete(total / OUT_COUNT, 0, 15);
    }

    public boolean isFuel(ItemStack stack) {
        return level != null && level.fuelValues().isFuel(stack);
    }

    public boolean canBake(ItemStack stack) {
        return level instanceof ServerLevel server && bakingCheck.getRecipeFor(new SingleRecipeInput(stack), server).isPresent();
    }

    /** Room in the oven for one more of this, and the oven can bake it. */
    public boolean ovenAccepts(ItemStack stack) {
        if (stack.isEmpty() || !canBake(stack)) return false;
        final ItemStack in = items.get(OVEN);
        return in.isEmpty() || ItemStack.isSameItemSameComponents(in, stack) && in.getCount() < Math.min(getMaxStackSize(), in.getMaxStackSize());
    }

    public void ovenInsert(ItemStack stack) {
        final ItemStack in = items.get(OVEN);
        if (in.isEmpty()) setItem(OVEN, stack.copyWithCount(1));
        else in.grow(1);
        setChanged();
    }

    public static boolean isVessel(ItemStack stack) {
        return stack.is(MoreFoodKitchen.VESSELS);
    }

    public void awardExperience(ServerPlayer player) {
        final List<RecipeHolder<?>> recipes = popExperience(player.level(), player.position());
        player.awardRecipes(recipes);
        used.clear();
    }

    private List<RecipeHolder<?>> popExperience(ServerLevel level, Vec3 at) {
        final List<RecipeHolder<?>> recipes = new ArrayList<>();
        for (var entry : used.reference2IntEntrySet()) {
            level.recipeAccess().byKey(entry.getKey()).ifPresent(holder -> {
                recipes.add(holder);
                if (holder.value() instanceof BakingRecipe baking) {
                    final float xp = entry.getIntValue() * baking.experience();
                    int whole = Mth.floor(xp);
                    if (level.getRandom().nextFloat() < Mth.frac(xp)) whole++;
                    ExperienceOrb.award(level, at, whole);
                }
            });
        }
        return recipes;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, items);
        litTime = input.getIntOr("lit_time", 0);
        litDuration = input.getIntOr("lit_duration", 0);
        ovenTime = input.getIntOr("oven_time", 0);
        ovenTotal = input.getIntOr("oven_total", 0);
        used.clear();
        used.putAll(input.read("recipes_used", USED_CODEC).orElse(Map.of()));
        for (int i = 0; i < rings.length; i++) {
            final Ring ring = rings[i];
            final ValueInput in = input.childOrEmpty(i == LEFT ? "left" : "right");
            ring.tool = in.read("tool", TOOL_CODEC).orElse(HobTool.NONE);
            ring.pan.load(in.childOrEmpty("pan"));
            ring.pot.load(in.childOrEmpty("pot"));
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
        output.putInt("lit_time", litTime);
        output.putInt("lit_duration", litDuration);
        output.putInt("oven_time", ovenTime);
        output.putInt("oven_total", ovenTotal);
        output.store("recipes_used", USED_CODEC, used);
        for (int i = 0; i < rings.length; i++) {
            final Ring ring = rings[i];
            final ValueOutput out = output.child(i == LEFT ? "left" : "right");
            out.store("tool", TOOL_CODEC, ring.tool);
            if (ring.tool == HobTool.PAN) ring.pan.save(out.child("pan"));
            if (ring.tool == HobTool.POT) ring.pot.save(out.child("pot"));
        }
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
        super.preRemoveSideEffects(pos, state);
        if (level != null) {
            for (Ring ring : rings) {
                if (ring.tool == HobTool.PAN) {
                    for (int i = 0; i < Pan.SPOTS; i++) Containers.dropItemStack(level, pos.getX(), pos.getY() + 1, pos.getZ(), ring.pan.get(i).copy());
                    Containers.dropItemStack(level, pos.getX(), pos.getY() + 1, pos.getZ(), new ItemStack(MoreFoodItems.fryingPan));
                } else if (ring.tool == HobTool.POT) {
                    for (ItemStack stack : ring.pot.items()) Containers.dropItemStack(level, pos.getX(), pos.getY() + 1, pos.getZ(), stack.copy());
                    Containers.dropItemStack(level, pos.getX(), pos.getY() + 1, pos.getZ(), new ItemStack(MoreFoodItems.pot));
                }
            }
            if (level instanceof ServerLevel server) {
                popExperience(server, Vec3.atCenterOf(pos));
            }
        }
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        final boolean same = !stack.isEmpty() && ItemStack.isSameItemSameComponents(items.get(slot), stack);
        items.set(slot, stack);
        stack.limitSize(getMaxStackSize(stack));
        if (slot == OVEN && !same) {
            ovenTime = 0;
            setChanged();
        }
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch (slot) {
            case FUEL -> isFuel(stack);
            case OVEN -> true;
            case VESSEL -> isVessel(stack);
            case HOB -> items.get(HOB).isEmpty();
            default -> false;
        };
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        if (side == Direction.DOWN) return BOTTOM;
        if (side == Direction.UP) return TOP;
        final Direction front = getBlockState().getValue(StoveBlock.FACING);
        if (side == front) return NONE;
        if (side == front.getOpposite()) return BACK;
        return SIDES;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        if (side == null) return canPlaceItem(slot, stack);
        if (side == Direction.UP) {
            return slot == HOB && items.get(HOB).isEmpty() && level instanceof ServerLevel server && hobInsert(server, stack, true).isPresent();
        }
        if (side == Direction.DOWN) return false;
        final Direction front = getBlockState().getValue(StoveBlock.FACING);
        if (side == front) return false;
        if (side == front.getOpposite()) return slot == OVEN && canBake(stack);
        if (slot == VESSEL) return isVessel(stack);
        return slot == FUEL && !isVessel(stack) && isFuel(stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        if (side != Direction.DOWN) return false;
        return slot >= OUT_FIRST || slot == FUEL && stack.is(Items.BUCKET);
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.morefood.stove");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new StoveMenu(id, inventory, this, data);
    }
}
