package net.montoyo.wd.client.gui.controls;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

/**
 * Vanilla EditBox without Forge's setHeight.
 */
@Environment(EnvType.CLIENT)
public class WDEditBox extends EditBox {
    public WDEditBox(Font font, int x, int y, int width, int height, Component message) {
        super(font, x, y, width, height, message);
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
