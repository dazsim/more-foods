package com.workshopcraft.morefood.client;

import com.workshopcraft.morefood.MoreFood;
import com.workshopcraft.morefood.machine.KitchenMachineMenu;
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public class KitchenMachineScreen extends AbstractContainerScreen<KitchenMachineMenu> {
    private static final Identifier TEXTURE = MoreFood.id("textures/gui/kitchen_machine.png");
    //? if >=1.21 {
    private static final Identifier LIT = Identifier.withDefaultNamespace("container/furnace/lit_progress");
    private static final Identifier BURN = Identifier.withDefaultNamespace("container/furnace/burn_progress");
    //?} else {
    /*private static final Identifier FURNACE = new Identifier("minecraft", "textures/gui/container/furnace.png");
    *///?}

    public KitchenMachineScreen(KitchenMachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;
    }

    //? if >=26.1 {
    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        final int x = leftPos;
        final int y = topPos;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);
        if (menu.drive() > 0) {
            final int h = Mth.ceil(menu.drive() * 13) + 1;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, LIT, 14, 14, 0, 14 - h, x + 45, y + 36 + 14 - h, 14, h);
        }
        final int w = Mth.ceil(menu.progress() * 24);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BURN, 24, 16, 0, 0, x + 85, y + 35, w, 16);
    }
    //?} else {
    /*@Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float a) {
        //? if <1.20.2 {
        /^renderBackground(graphics);
        ^///?}
        super.render(graphics, mouseX, mouseY, a);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float a, int mouseX, int mouseY) {
        final int x = leftPos;
        final int y = topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);
        if (menu.drive() > 0) {
            final int h = Mth.ceil(menu.drive() * 13) + 1;
            //? if >=1.21 {
            graphics.blitSprite(LIT, 14, 14, 0, 14 - h, x + 45, y + 36 + 14 - h, 14, h);
            //?} else {
            /^graphics.blit(FURNACE, x + 45, y + 36 + 14 - h, 176, 14 - h, 14, h);
            ^///?}
        }
        final int w = Mth.ceil(menu.progress() * 24);
        //? if >=1.21 {
        graphics.blitSprite(BURN, 24, 16, 0, 0, x + 85, y + 35, w, 16);
        //?} else {
        /^graphics.blit(FURNACE, x + 85, y + 35, 176, 14, w, 16);
        ^///?}
    }
    *///?}
}
