package com.workshopcraft.morefood.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

import java.util.function.Supplier;

public class FruitLeavesBlock extends LeavesBlock implements BonemealableBlock {
    public static final BooleanProperty FRUIT = BooleanProperty.create("fruit");
    private final Supplier<Item> fruit;

    //? if >=1.20.5 && <26.3 {
    public static final MapCodec<FruitLeavesBlock> CODEC = simpleCodec(p -> new FruitLeavesBlock(p, () -> net.minecraft.world.item.Items.APPLE));

    @Override
    public MapCodec<? extends FruitLeavesBlock> codec() {
        return CODEC;
    }
    //?}

    public FruitLeavesBlock(Properties properties, Supplier<Item> fruit) {
        //? if >=26.3 {
        /*super(net.minecraft.world.level.block.sounds.AmbientLeavesBlockSoundPlayer.noAmbientSound(), properties);
        *///?} else if >=26.1 {
        super(0.01F, properties);
        //?} else {
        /*super(properties);
        *///?}
        this.fruit = fruit;
        registerDefaultState(defaultBlockState().setValue(FRUIT, false));
    }

    public Item fruitItem() {
        return fruit.get();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FRUIT);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return !state.getValue(FRUIT) || super.isRandomlyTicking(state);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        final BlockState current = level.getBlockState(pos);
        if (current.is(this) && !current.getValue(FRUIT) && random.nextInt(12) == 0) {
            level.setBlock(pos, current.setValue(FRUIT, true), 2);
        }
    }

    //? if >=26.1 && <26.3 {
    @Override
    protected void spawnFallingLeavesParticle(Level level, BlockPos pos, RandomSource random) {
    }
    //?}

    private InteractionResult pick(BlockState state, Level level, BlockPos pos, Player player) {
        if (!state.getValue(FRUIT)) return InteractionResult.PASS;
        if (!level.isClientSide()) {
            final ItemStack stack = new ItemStack(fruit.get(), 1 + level.getRandom().nextInt(2));
            if (!player.getInventory().add(stack)) Block.popResource(level, pos, stack);
            level.setBlock(pos, state.setValue(FRUIT, false), 2);
            level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        }
        return InteractionResult.SUCCESS;
    }

    //? if >=26.1 {
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return pick(state, level, pos, player);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return pick(state, level, pos, player);
    }
    //?} else if >=1.20.5 {
    /*@Override
    protected net.minecraft.world.ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                                                  InteractionHand hand, BlockHitResult hit) {
        return pick(state, level, pos, player) == InteractionResult.PASS
                ? net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION : net.minecraft.world.ItemInteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return pick(state, level, pos, player);
    }
    *///?} else {
    /*@Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return pick(state, level, pos, player);
    }
    *///?}

    //? if >=26.3 {
    /*@Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, net.minecraft.world.level.block.BonemealSource source) {
        return !state.getValue(FRUIT);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state, net.minecraft.world.level.block.BonemealSource source) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, net.minecraft.world.level.block.BonemealSource source) {
        level.setBlock(pos, state.setValue(FRUIT, true), 2);
    }
    *///?} else if >=1.20.2 {
    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return !state.getValue(FRUIT);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        level.setBlock(pos, state.setValue(FRUIT, true), 2);
    }
    //?} else {
    /*@Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
        return !state.getValue(FRUIT);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        level.setBlock(pos, state.setValue(FRUIT, true), 2);
    }
    *///?}
}
