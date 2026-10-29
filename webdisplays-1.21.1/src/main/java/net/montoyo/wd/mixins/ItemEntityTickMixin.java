package net.montoyo.wd.mixins;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.montoyo.wd.item.IItemEntityTickHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replaces Forge's Item.onEntityItemUpdate hook.
 */
@Mixin(ItemEntity.class)
public class ItemEntityTickMixin {
	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void webdisplays$tickHeldItem(CallbackInfo ci) {
		ItemEntity self = (ItemEntity) (Object) this;
		ItemStack stack = self.getItem();

		if (stack.getItem() instanceof IItemEntityTickHandler handler && handler.onEntityItemUpdate(stack, self))
			ci.cancel();
	}
}
