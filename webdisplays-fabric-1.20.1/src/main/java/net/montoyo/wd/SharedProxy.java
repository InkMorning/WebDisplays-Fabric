/*
 * Copyright (C) 2019 BARBOTIN Nicolas
 */

package net.montoyo.wd;

import com.cinemamod.mcef.MCEF;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.montoyo.wd.core.HasAdvancement;
import net.montoyo.wd.core.JSServerRequest;
import net.montoyo.wd.data.GuiData;
import net.montoyo.wd.entity.TileEntityScreen;
import net.montoyo.wd.net.WDContext;
import net.montoyo.wd.utilities.*;

import org.jetbrains.annotations.NotNull;
import java.util.UUID;

public class SharedProxy {
    public void preInit() {
    }

    public void init() {
        MCEF.scheduleForInit((cef) -> onCefInit());
    }

    public void postInit() {
    }

    public void onCefInit() {
    }

    @Deprecated(forRemoval = true)
    public Level getWorld(ResourceKey<Level> dim) {
        return getServer().getLevel(dim);
    }

    public BlockGetter getWorld(WDContext context) {
        if (context.getSender() != null) return context.getSender().level();
        return null;
    }

    public void enqueue(Runnable r) {
        MinecraftServer server = getServer();
        if (server != null)
            server.execute(r);
    }

    public void displayGui(GuiData data) {
        Log.error("Called SharedProxy.displayGui() on server side...");
    }

    public void trackScreen(TileEntityScreen tes, boolean track) {
    }

    public void onAutocompleteResult(NameUUIDPair pairs[]) {
    }

    public GameProfile[] getOnlineGameProfiles() {
        MinecraftServer server = getServer();
        if (server == null)
            return new GameProfile[0];

        return server.getPlayerList().getPlayers().stream().map(Player::getGameProfile).toArray(GameProfile[]::new);
    }

    public void screenUpdateResolutionInGui(Vector3i pos, BlockSide side, Vector2i res) {
    }

    public void screenUpdateRotationInGui(Vector3i pos, BlockSide side, Rotation rot) {
    }

    public void screenUpdateAutoVolumeInGui(Vector3i pos, BlockSide side, boolean av) {
    }

    public void displaySetPadURLGui(ItemStack is, String padURL) {
        Log.error("Called SharedProxy.displaySetPadURLGui() on server side...");
    }

    public void openMinePadGui(UUID padId) {
        Log.error("Called SharedProxy.openMinePadGui() on server side...");
    }

    public void handleJSResponseSuccess(int reqId, JSServerRequest type, byte[] data) {
        Log.error("Called SharedProxy.handleJSResponseSuccess() on server side...");
    }

    public void handleJSResponseError(int reqId, JSServerRequest type, int errCode, String err) {
        Log.error("Called SharedProxy.handleJSResponseError() on server side...");
    }

    @NotNull
    public HasAdvancement hasClientPlayerAdvancement(@NotNull ResourceLocation rl) {
        return HasAdvancement.DONT_KNOW;
    }

    public MinecraftServer getServer() {
        return WebDisplaysFabric.currentServer;
    }

    public void setMiniservClientPort(int port) {
    }

    public void startMiniservClient() {
    }

    public boolean isMiniservDisabled() {
        return false;
    }

    public void closeGui(BlockPos bp, BlockSide bs) {
    }

    public void renderRecipes() {
    }

    public boolean isShiftDown() {
        return false;
    }
}
