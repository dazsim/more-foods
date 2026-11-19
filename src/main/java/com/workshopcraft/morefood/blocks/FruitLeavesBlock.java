package com.workshopcraft.morefood.blocks;

import com.mojang.serialization.MapCodec;
import com.workshopcraft.morefood.worldgen.FruitTreeFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

public class FruitLeavesBlock extends LeavesBlock {
    private final FruitTreeFeature.Type type;

    //? if >=1.20.5 && <26.3 {
    public static final MapCodec<FruitLeavesBlock> CODEC = simpleCodec(p -> new FruitLeavesBlock(p, FruitTreeFeature.Type.JACKFRUIT));

    @Override
    public MapCodec<? extends FruitLeavesBlock> codec() {
        return CODEC;
    }
    //?}

    public FruitLeavesBlock(Properties properties, FruitTreeFeature.Type type) {
        //? if >=26.3 {
        /*super(net.minecraft.world.level.block.sounds.AmbientLeavesBlockSoundPlayer.noAmbientSound(), properties);
        *///?} else if >=26.1 {
        super(0.01F, properties);
        //?} else {
        /*super(properties);
        *///?}
        this.type = type;
    }

    public FruitTreeFeature.Type treeType() {
        return type;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return !state.getValue(PERSISTENT) || super.isRandomlyTicking(state);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        final BlockState current = level.getBlockState(pos);
        if (current.is(this) && !current.getValue(PERSISTENT) && random.nextInt(40) == 0) {
            FruitPodBlock.trySpawn(type, level, pos, random);
        }
    }

    //? if >=26.1 && <26.3 {
    @Override
    protected void spawnFallingLeavesParticle(Level level, BlockPos pos, RandomSource random) {
    }
    //?}
}
