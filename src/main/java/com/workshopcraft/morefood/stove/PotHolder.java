package com.workshopcraft.morefood.stove;

/** A block holding one or more pots that water can be piped into. Water is in mB, -1 when there is no pot at that index. */
public interface PotHolder {
    int potCount();

    int potWater(int index);

    int potRoom(int index);

    void setPotWater(int index, int mb);

    void potChanged();
}
