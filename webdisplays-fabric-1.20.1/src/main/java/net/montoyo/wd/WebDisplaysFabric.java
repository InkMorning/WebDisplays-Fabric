package net.montoyo.wd;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.montoyo.wd.core.WDServerData;
import net.montoyo.wd.miniserv.server.Server;
import net.montoyo.wd.net.WDNetworkRegistry;
import net.montoyo.wd.net.client_bound.S2CMessageServerInfo;
import net.montoyo.wd.utilities.Log;
import net.montoyo.wd.utilities.Util;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WebDisplaysFabric implements ModInitializer {
    public static final String MOD_ID = "webdisplays";

    @Nullable
    public static MinecraftServer currentServer;

    private final List<ResourceKey<Level>> serverStartedDimensions = new ArrayList<>();

    @Override
    public void onInitialize() {
        WebDisplays.initialize();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> currentServer = server);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            currentServer = null;
            try {
                Server.getInstance().stopServer();
            } catch (IOException e) {
                Log.warningEx("Could not stop the miniserv.", e);
            }
        });

        ServerWorldEvents.LOAD.register(this::onWorldLoad);
        ServerWorldEvents.UNLOAD.register(this::onWorldUnload);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> onPlayerLogin(handler.player));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                Server.getInstance().getClientManager().revokeClientKey(handler.player.getGameProfile().getId()));

        ServerMessageEvents.CHAT_MESSAGE.register((message, player, link) -> onServerChat(message.signedContent(), player));
    }

    private void onWorldLoad(MinecraftServer server, ServerLevel world) {
        if (world.dimension() != Level.OVERWORLD || WebDisplays.INSTANCE.miniservPort == 0)
            return;

        File worldDir = server.getServerDirectory();
        Server sv = Server.getInstance();

        if (!serverStartedDimensions.contains(world.dimension())) {
            sv.setPort(WebDisplays.INSTANCE.miniservPort);
            sv.setDirectory(new File(worldDir, "wd_filehost"));
            sv.start();
            serverStartedDimensions.add(world.dimension());
        }
    }

    private void onWorldUnload(MinecraftServer server, ServerLevel world) {
        if (world.dimension() != Level.OVERWORLD)
            return;

        try {
            Server.getInstance().stopServer();
        } catch (IOException e) {
            Log.warningEx("Could not stop the miniserv.", e);
        }
        serverStartedDimensions.remove(world.dimension());
    }

    private void onPlayerLogin(ServerPlayer player) {
        WDServerData data = WDServerData.get(player.getServer());

        if (data.isFirstRun(player)) {
            Util.toast(player, ChatFormatting.LIGHT_PURPLE, "welcome1");
            Util.toast(player, ChatFormatting.LIGHT_PURPLE, "welcome2");
            Util.toast(player, ChatFormatting.LIGHT_PURPLE, "welcome3");
            data.markWelcomed(player);
        }

        S2CMessageServerInfo message = new S2CMessageServerInfo(WebDisplays.INSTANCE.miniservPort);
        WDNetworkRegistry.INSTANCE.send(net.montoyo.wd.net.WDDistributor.PLAYER.with(() -> player), message);
    }

    private void onServerChat(String rawMessage, ServerPlayer player) {
        String msg = rawMessage.replaceAll("\\s+", " ").toLowerCase();
        StringBuilder sb = new StringBuilder(msg.length());
        for (int i = 0; i < msg.length(); i++) {
            char chr = msg.charAt(i);

            if (chr != '.' && chr != ',' && chr != ';' && chr != '!' && chr != '?' && chr != ':' && chr != '\'' && chr != '\"' && chr != '`')
                sb.append(chr);
        }

        if (sb.toString().equals("ironic he could save others from death but not himself")) {
            Player ply = player;
            ply.level().playSound(null, ply.getX(), ply.getY(), ply.getZ(),
                    WebDisplays.INSTANCE.soundIronic, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
    }

    public static boolean hasPlayerAdvancement(ServerPlayer player, ResourceLocation id) {
        if (currentServer == null)
            return false;

        Advancement adv = currentServer.getAdvancements().getAdvancement(id);
        return adv != null && player.getAdvancements().getOrStartProgress(adv).isDone();
    }
}
