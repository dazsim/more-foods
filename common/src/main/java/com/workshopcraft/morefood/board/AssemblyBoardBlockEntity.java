package com.workshopcraft.morefood.board;

import com.workshopcraft.morefood.MoreFoodItems;
import com.workshopcraft.morefood.MoreFoodKitchen;
import com.workshopcraft.morefood.crafting.AssemblyRecipe;
import com.workshopcraft.morefood.crafting.BoardInput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.UseRemainder;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class AssemblyBoardBlockEntity extends BlockEntity {
    public static final int SETTLE = 20;
    private static final int HINT_NAMES = 2;

    private final List<ItemStack> layers = new ArrayList<>();
    private final List<ItemStack> leftovers = new ArrayList<>();
    private long lastAdd;
    private boolean complete;

    public AssemblyBoardBlockEntity(BlockPos pos, BlockState state) {
        super(MoreFoodKitchen.boardEntity, pos, state);
    }

    public List<ItemStack> layers() {
        return layers;
    }

    /** Set on the server whenever the layers change, so the client can draw a finished stack. */
    public boolean isComplete() {
        return complete;
    }

    private static List<RecipeHolder<AssemblyRecipe>> recipes(ServerLevel level) {
        return MoreFoodKitchen.assembly(level);
    }

    public boolean canAdd(ServerLevel level, ItemStack stack) {
        if (stack.isEmpty() || layers.size() >= AssemblyRecipe.MAX_LAYERS) return false;
        for (RecipeHolder<AssemblyRecipe> holder : recipes(level)) {
            if (holder.value().accepts(layers, stack)) return true;
        }
        return false;
    }

    public @Nullable RecipeHolder<AssemblyRecipe> finished(ServerLevel level) {
        for (RecipeHolder<AssemblyRecipe> holder : recipes(level)) {
            if (holder.value().isComplete(layers)) return holder;
        }
        return null;
    }

    public ItemStack result(ServerLevel level) {
        final RecipeHolder<AssemblyRecipe> holder = finished(level);
        return holder == null ? ItemStack.EMPTY : holder.value().assemble(new BoardInput(layers));
    }

    private boolean open(ServerLevel level) {
        for (RecipeHolder<AssemblyRecipe> holder : recipes(level)) {
            if (holder.value().isOpen(layers)) return true;
        }
        return false;
    }

    /** A finished stack that could still take extras waits a second after the last layer before a hopper may take it. */
    public boolean canExtract(ServerLevel level) {
        if (finished(level) == null) return false;
        return !open(level) || level.getGameTime() - lastAdd >= SETTLE;
    }

    public void addRaw(ItemStack stack, long time) {
        layers.add(stack.copyWithCount(1));
        lastAdd = time;
    }

    public void add(ServerLevel level, ItemStack stack) {
        addRaw(stack, level.getGameTime());
        changed();
    }

    /** Takes the finished item. Bowls and the like left by the layers go to the player, or pop out on the board. */
    public ItemStack take(ServerLevel level, @Nullable Player player) {
        final ItemStack out = result(level);
        if (out.isEmpty()) return out;
        clearRaw();
        if (player != null) {
            for (ItemStack left : leftovers) player.getInventory().placeItemBackInInventory(left);
            leftovers.clear();
        }
        changed();
        return out;
    }

    private static ItemStack leftover(ItemStack layer) {
        final ItemStack left = MoreFoodItems.remainder(layer);
        if (!left.isEmpty()) return left;
        final UseRemainder use = layer.get(DataComponents.USE_REMAINDER);
        return use != null ? use.convertInto().create() : ItemStack.EMPTY;
    }

    public List<ItemStack> copyLayers() {
        final List<ItemStack> copy = new ArrayList<>();
        for (ItemStack layer : layers) copy.add(layer.copy());
        return copy;
    }

    public long lastAdd() {
        return lastAdd;
    }

    public void clearRaw() {
        leftovers.clear();
        for (ItemStack layer : layers) {
            final ItemStack left = leftover(layer);
            if (!left.isEmpty()) leftovers.add(left);
        }
        layers.clear();
    }

    public void restore(List<ItemStack> saved, long time) {
        leftovers.clear();
        layers.clear();
        for (ItemStack layer : saved) layers.add(layer.copy());
        lastAdd = time;
    }

    public void changed() {
        if (level instanceof ServerLevel server) {
            complete = !layers.isEmpty() && finished(server) != null;
            for (ItemStack left : leftovers) Block.popResource(server, worldPosition, left);
        }
        leftovers.clear();
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public boolean use(ServerLevel level, Player player, InteractionHand hand, ItemStack stack) {
        final BlockPos pos = getBlockPos();
        if (stack.isEmpty()) {
            if (player.isSecondaryUseActive()) {
                if (layers.isEmpty()) return false;
                player.getInventory().placeItemBackInInventory(layers.removeLast());
                changed();
                level.playSound(null, pos, SoundEvents.WOOD_HIT, SoundSource.BLOCKS, 0.6F, 1.2F);
                return true;
            }
            final ItemStack out = take(level, player);
            if (!out.isEmpty()) {
                player.setItemInHand(hand, out);
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.4F, 1.0F);
                return true;
            }
            if (layers.isEmpty()) return false;
            hint(level, player);
            return true;
        }
        if (canAdd(level, stack)) {
            add(level, stack);
            stack.consume(1, player);
            level.playSound(null, pos, SoundEvents.WOOD_HIT, SoundSource.BLOCKS, 0.6F, 1.0F);
            return true;
        }
        hint(level, player);
        return true;
    }

    public void hint(ServerLevel level, Player player) {
        final Set<Item> items = new LinkedHashSet<>();
        final Set<Ingredient> next = new LinkedHashSet<>();
        for (RecipeHolder<AssemblyRecipe> holder : recipes(level)) holder.value().next(layers, next);
        for (Ingredient ingredient : next) {
            ingredient.items().map(Holder::value).forEach(item -> {
                if (!items.contains(item) && canAdd(level, new ItemStack(item))) items.add(item);
            });
        }
        if (items.isEmpty()) {
            player.sendOverlayMessage(Component.translatable(finished(level) != null ? "message.morefood.board.done" : "message.morefood.board.nothing"));
            return;
        }
        MutableComponent names = Component.empty();
        int i = 0;
        for (Item item : items) {
            if (i == HINT_NAMES) {
                names.append(Component.translatable("message.morefood.board.more", items.size() - HINT_NAMES));
                break;
            }
            if (i > 0) names.append(", ");
            names.append(Component.translatable(item.getDescriptionId()));
            i++;
        }
        player.sendOverlayMessage(Component.translatable(finished(level) != null ? "message.morefood.board.next_or_take" : "message.morefood.board.next", names));
    }

    public int outputSignal() {
        if (layers.isEmpty()) return 0;
        return complete ? 15 : Math.min(8, layers.size());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        layers.clear();
        layers.addAll(input.read("layers", ItemStack.CODEC.listOf()).orElse(List.of()));
        lastAdd = input.getLongOr("last_add", 0);
        complete = input.getBooleanOr("complete", false);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("layers", ItemStack.CODEC.listOf(), layers);
        output.putLong("last_add", lastAdd);
        output.putBoolean("complete", complete);
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
        if (level == null) return;
        for (ItemStack layer : layers) Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), layer.copy());
    }
}
