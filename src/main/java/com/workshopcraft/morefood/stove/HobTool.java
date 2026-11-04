package com.workshopcraft.morefood.stove;

import net.minecraft.util.StringRepresentable;

public enum HobTool implements StringRepresentable {
    NONE("none"), PAN("pan"), POT("pot");

    private final String name;

    HobTool(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
