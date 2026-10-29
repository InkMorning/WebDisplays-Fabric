package net.montoyo.wd.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.montoyo.wd.client.ClientProxy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replaces Forge's RenderHandEvent for the first person hands.
 */
@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
	@Inject(method = "renderItem", at = @At("HEAD"), cancellable = true)
	private void webdisplays$renderCustomHeldItem(LivingEntity entity, ItemStack stack, ItemDisplayContext displayContext,
												  boolean leftHand, PoseStack poseStack, MultiBufferSource bufferSource,
												  int light, CallbackInfo ci) {
		if (displayContext != ItemDisplayContext.FIRST_PERSON_RIGHT_HAND && displayContext != ItemDisplayContext.FIRST_PERSON_LEFT_HAND)
			return;

		boolean left = displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
		if (ClientProxy.renderFirstPersonItem(stack, left, poseStack, bufferSource, light))
			ci.cancel();
	}
}
