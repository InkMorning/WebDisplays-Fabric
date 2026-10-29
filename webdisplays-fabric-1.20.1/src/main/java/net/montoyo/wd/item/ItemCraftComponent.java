/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.montoyo.wd.WebDisplays;
import net.montoyo.wd.WebDisplaysFabric;
import net.montoyo.wd.config.CommonConfig;
import net.montoyo.wd.core.CraftComponent;
import net.montoyo.wd.core.HasAdvancement;
import org.jetbrains.annotations.NotNull;

public class ItemCraftComponent extends ItemMulti implements WDItem {

    public ItemCraftComponent(Properties properties) {
        super(CraftComponent.class, properties);

        //Hide the bad extension card from the creative tab
        creativeTabItems.clear(CraftComponent.BADEXTCARD.ordinal());
    }

    /**
     * With hard recipes on, crafting the extension card without having broken a minePad first
     * yields a useless card instead. Replaces Forge's PlayerEvent.ItemCraftedEvent handler.
     */
    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);

        if (!CommonConfig.hardRecipes || !CraftComponent.EXTCARD.makeItemStack().is(stack.getItem()) || hasPadBreak(level, player))
            return;

        stack.setDamageValue(CraftComponent.BADEXTCARD.ordinal());

        if (!level.isClientSide)
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_BREAK, SoundSource.MASTER, 1.0f, 1.0f);
    }

    private static boolean hasPadBreak(Level level, Player player) {
        if (level.isClientSide)
            return WebDisplays.PROXY.hasClientPlayerAdvancement(WebDisplays.ADV_PAD_BREAK) == HasAdvancement.YES;

        return player instanceof ServerPlayer sp && WebDisplaysFabric.hasPlayerAdvancement(sp, WebDisplays.ADV_PAD_BREAK);
    }

    @Override
    public String getWikiName(@NotNull ItemStack is) {
        return is.getItem().getName(is).getString();
    }
}
