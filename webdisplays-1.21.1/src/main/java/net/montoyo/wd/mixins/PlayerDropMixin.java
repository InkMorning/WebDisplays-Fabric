package net.montoyo.wd.mixins;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.montoyo.wd.init.ItemInit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

/**
 * Replaces Forge's ItemTossEvent: stamps who threw a minePad and from how high, so that it breaks
 * when it lands far below (see ItemMinePad2.onEntityItemUpdate).
 */
@Mixin(Player.class)
public class PlayerDropMixin {
	@Inject(method = {"drop(Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/entity/item/ItemEntity;",
			"drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;"},
			at = @At("RETURN"))
	private void webdisplays$stampTossedMinePad(CallbackInfoReturnable<ItemEntity> cir) {
		ItemEntity entity = cir.getReturnValue();
		if (entity == null || entity.level().isClientSide)
			return;

		ItemStack stack = entity.getItem();
		if (stack.getItem() != ItemInit.MINEPAD.get())
			return;

		CustomData custom = stack.get(DataComponents.CUSTOM_DATA);
		CompoundTag tag = custom == null ? new CompoundTag() : custom.copyTag();

		Player throwerPlayer = (Player) (Object) this;
		UUID thrower = throwerPlayer.getGameProfile().getId();
		tag.putLong("ThrowerMSB", thrower.getMostSignificantBits());
		tag.putLong("ThrowerLSB", thrower.getLeastSignificantBits());
		tag.putDouble("ThrowHeight", throwerPlayer.getY() + throwerPlayer.getEyeHeight());
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}
}
