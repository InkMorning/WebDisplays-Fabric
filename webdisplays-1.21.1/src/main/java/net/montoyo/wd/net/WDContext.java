package net.montoyo.wd.net;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Executor;

/**
 * Replaces Forge's NetworkEvent.Context: the receiving side of a WebDisplays packet.
 */
public final class WDContext {
    @Nullable
    private final ServerPlayer sender;
    @Nullable
    private final MinecraftServer server;
    private final PacketSender response;
    private final Executor mainThread;
    private final boolean clientSide;

    public WDContext(@Nullable ServerPlayer sender, @Nullable MinecraftServer server,
                     PacketSender response, Executor mainThread, boolean clientSide) {
        this.sender = sender;
        this.server = server;
        this.response = response;
        this.mainThread = mainThread;
        this.clientSide = clientSide;
    }

    @Nullable
    public ServerPlayer getSender() {
        return sender;
    }

    @Nullable
    public MinecraftServer getServer() {
        return server;
    }

    public boolean isClientSide() {
        return clientSide;
    }

    public boolean isServerSide() {
        return !clientSide;
    }

    /**
     * Packets are decoded on the network thread, so handlers hop to the logical side with this.
     */
    public void enqueueWork(Runnable work) {
        mainThread.execute(work);
    }

    public void setPacketHandled(boolean handled) {
    }

    public void reply(Packet packet) {
        WDNetworkRegistry.INSTANCE.sendResponse(response, packet, clientSide);
    }
}
