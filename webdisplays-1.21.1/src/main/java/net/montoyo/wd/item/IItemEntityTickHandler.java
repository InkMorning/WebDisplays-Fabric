package net.montoyo.wd.item;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Implemented by items that need to run logic while they lie on the ground, replacing Forge's
 * Item.onEntityItemUpdate hook.
 */
public interface IItemEntityTickHandler {
	boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity);
}
