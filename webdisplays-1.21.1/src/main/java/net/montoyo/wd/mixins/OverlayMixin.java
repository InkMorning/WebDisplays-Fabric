package net.montoyo.wd.mixins;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.montoyo.wd.client.ClientProxy;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class OverlayMixin {
	@Shadow
	@Final
	protected Minecraft minecraft;

	// Gui.screenWidth/screenHeight were dropped in favour of the window's scaled size.
	@Inject(at = @At("HEAD"), method = "renderCrosshair", cancellable = true)
	public void preDrawCrosshair(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		Window window = minecraft.getWindow();
		ClientProxy.renderCrosshair(minecraft.options, window.getGuiScaledWidth(), window.getGuiScaledHeight(), 0, guiGraphics, ci);
	}
}
