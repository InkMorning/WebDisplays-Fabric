package net.montoyo.wd.mixins;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.montoyo.wd.client.renderers.ScreenModelContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelBlockRenderer.class)
public class ModelBlockRendererMixin {
	@Inject(method = "tesselateBlock", at = @At("HEAD"))
	private void webdisplays$captureTesselationContext(BlockAndTintGetter level, net.minecraft.client.resources.model.BakedModel model,
	                                                   BlockState state, BlockPos pos, PoseStack matrix, VertexConsumer buffer,
	                                                   boolean checkSides, RandomSource rand, long seed, int pass, CallbackInfo ci) {
		ScreenModelContext.set(level, pos);
	}
}
