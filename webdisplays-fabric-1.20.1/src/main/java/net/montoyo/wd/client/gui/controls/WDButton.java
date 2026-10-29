package net.montoyo.wd.client.gui.controls;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/**
 * Vanilla Button without Forge's extra accessors: adjustable height and text colour.
 */
@Environment(EnvType.CLIENT)
public class WDButton extends Button {
    private int foregroundColor = 0xFFFFFF;

    public WDButton(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setFGColor(int color) {
        foregroundColor = color;
    }

    public int getFGColor() {
        return foregroundColor;
    }

    @Override
    public void renderString(GuiGraphics guiGraphics, Font font, int color) {
        super.renderString(guiGraphics, font, foregroundColor);
    }
}
