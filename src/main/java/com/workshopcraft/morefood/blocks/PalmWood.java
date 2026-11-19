package com.workshopcraft.morefood.blocks;

import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;

/** Thin subclasses so protected 26.x constructors stay callable, plus the palm set and wood type. */
public final class PalmWood {
    public static final BlockSetType SET = new BlockSetType("palm");
    public static final WoodType TYPE = new WoodType("palm", SET);

    private PalmWood() {}

    public static final class Stairs extends StairBlock {
        public Stairs(BlockState base, BlockBehaviour.Properties properties) {
            super(base, properties);
        }
    }

    public static final class Door extends DoorBlock {
        public Door(BlockBehaviour.Properties properties) {
            //? if <1.21 {
            /*super(properties, SET);
            *///?} else {
            super(SET, properties);
            //?}
        }
    }

    public static final class Trapdoor extends TrapDoorBlock {
        public Trapdoor(BlockBehaviour.Properties properties) {
            //? if <1.21 {
            /*super(properties, SET);
            *///?} else {
            super(SET, properties);
            //?}
        }
    }

    public static final class Button extends ButtonBlock {
        public Button(BlockBehaviour.Properties properties) {
            //? if <1.21 {
            /*super(properties, SET, 30, true);
            *///?} else {
            super(SET, 30, properties);
            //?}
        }
    }

    public static final class Plate extends PressurePlateBlock {
        public Plate(BlockBehaviour.Properties properties) {
            //? if <1.21 {
            /*super(PressurePlateBlock.Sensitivity.EVERYTHING, properties, SET);
            *///?} else {
            super(SET, properties);
            //?}
        }
    }

    public static final class Gate extends FenceGateBlock {
        public Gate(BlockBehaviour.Properties properties) {
            //? if <1.21 {
            /*super(properties, TYPE);
            *///?} else {
            super(TYPE, properties);
            //?}
        }
    }
}
