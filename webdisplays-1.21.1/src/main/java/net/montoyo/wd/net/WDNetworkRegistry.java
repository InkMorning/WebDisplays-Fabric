package net.montoyo.wd.net;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.montoyo.wd.WebDisplays;
import net.montoyo.wd.net.client_bound.*;
import net.montoyo.wd.net.server_bound.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WDNetworkRegistry {
    public static final String networkingVersion = "2";
    public static final ResourceLocation CHANNEL = WDPayload.C2S.TYPE.id();

    public static final WDNetworkRegistry INSTANCE = new WDNetworkRegistry();

    private static final ArrayList<NetworkEntry<?>> entries = new ArrayList<>();
    private static final Map<Class<? extends Packet>, Integer> indexByClass = new HashMap<>();
    private static boolean payloadsRegistered;

    private static <T extends Packet> void add(NetworkEntry<T> entry, Class<T> clazz) {
        indexByClass.put(clazz, entries.size());
        entries.add(entry);
    }

    static {
        // login handshake
        add(NetworkEntry.serverToClient(S2CMessageServerInfo::new), S2CMessageServerInfo.class);
        add(NetworkEntry.clientToServer(C2SMessageMiniservConnect::new), C2SMessageMiniservConnect.class);
        add(NetworkEntry.serverToClient(S2CMessageMiniservKey::new), S2CMessageMiniservKey.class);

        // guis
        add(NetworkEntry.serverToClient(S2CMessageCloseGui::new), S2CMessageCloseGui.class);
        add(NetworkEntry.serverToClient(S2CMessageOpenGui::new), S2CMessageOpenGui.class);

        // screen creation
        add(NetworkEntry.serverToClient(S2CMessageAddScreen::new), S2CMessageAddScreen.class);

        // screen modifications
        add(NetworkEntry.clientToServer(C2SMessageScreenCtrl::new), C2SMessageScreenCtrl.class);
        add(NetworkEntry.serverToClient(S2CMessageScreenUpdate::new), S2CMessageScreenUpdate.class);

        // redstone control
        add(NetworkEntry.clientToServer(C2SMessageRedstoneCtrl::new), C2SMessageRedstoneCtrl.class);

        // autocomplete
        add(NetworkEntry.clientToServer(C2SMessageACQuery::new), C2SMessageACQuery.class);
        add(NetworkEntry.serverToClient(S2CMessageACResult::new), S2CMessageACResult.class);

        // jsquery
        add(NetworkEntry.serverToClient(S2CMessageJSResponse::new), S2CMessageJSResponse.class);

        // minepad
        add(NetworkEntry.clientToServer(C2SMessageMinepadUrl::new), C2SMessageMinepadUrl.class);
    }

    private static void registerPayloadTypes() {
        if (payloadsRegistered)
            return;

        payloadsRegistered = true;
        PayloadTypeRegistry.playC2S().register(WDPayload.C2S.TYPE, WDPayload.C2S.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(WDPayload.S2C.TYPE, WDPayload.S2C.STREAM_CODEC);
    }

    public static void init() {
        registerPayloadTypes();
        ServerPlayNetworking.registerGlobalReceiver(WDPayload.C2S.TYPE, (payload, context) ->
                dispatch(payload.body(), context.responseSender(), context.player(), context.server(), false));
    }

    public static void initClient() {
        registerPayloadTypes();
        ClientPlayNetworking.registerGlobalReceiver(WDPayload.S2C.TYPE, (payload, context) ->
                dispatch(payload.body(), context.responseSender(), null, null, true));
    }

    private static void dispatch(byte[] body, PacketSender response, ServerPlayer sender,
                                 MinecraftServer server, boolean clientSide) {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(body),
                clientSide ? Minecraft.getInstance().level.registryAccess() : server.registryAccess());

        int index = buf.readVarInt();
        if (index < 0 || index >= entries.size())
            throw new IllegalArgumentException("Unknown WebDisplays packet index " + index);

        NetworkEntry<?> entry = entries.get(index);
        boolean expectedFromClient = entry.getDirection() == NetworkEntry.Direction.CLIENT_TO_SERVER;
        if (expectedFromClient == clientSide)
            throw new IllegalArgumentException("WebDisplays packet " + index + " arrived on the wrong side");

        // The buffer is only valid on the network thread, so the packet is decoded here and
        // only the decoded payload is handed to the main thread.
        Packet packet = entry.read(buf);
        WDContext ctx = new WDContext(sender, server, response,
                clientSide ? Minecraft.getInstance()::execute : server::execute, clientSide);
        packet.handle(ctx);
    }

    private static byte[] toBytes(Packet packet, RegistryAccess registries) {
        Integer index = indexByClass.get(packet.getClass());
        if (index == null)
            throw new IllegalArgumentException("Packet " + packet.getClass().getName() + " was not registered");

        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries);
        buf.writeVarInt(index);
        packet.write(buf);

        byte[] out = new byte[buf.readableBytes()];
        buf.readBytes(out);
        buf.release();
        return out;
    }

    public void sendToServer(Packet packet) {
        ClientPlayNetworking.send(new WDPayload.C2S(toBytes(packet, Minecraft.getInstance().level.registryAccess())));
    }

    public void send(WDDistributor.Target target, Packet packet) {
        MinecraftServer server = WebDisplays.PROXY.getServer();
        if (server == null)
            return;

        byte[] body = toBytes(packet, server.registryAccess());
        target.forEachPlayer(server, player -> ServerPlayNetworking.send(player, new WDPayload.S2C(body)));
    }

    public void reply(Packet packet, WDContext ctx) {
        ctx.reply(packet);
    }

    void sendResponse(PacketSender sender, Packet packet, boolean clientSide) {
        // A reply goes back over the channel the *receiver* owns, so the payload type is picked by
        // which side is sending: the client answers serverbound (C2S), the server clientbound (S2C).
        if (clientSide)
            sender.sendPacket(new WDPayload.C2S(toBytes(packet, Minecraft.getInstance().level.registryAccess())));
        else
            sender.sendPacket(new WDPayload.S2C(toBytes(packet, WebDisplays.PROXY.getServer().registryAccess())));
    }
}
