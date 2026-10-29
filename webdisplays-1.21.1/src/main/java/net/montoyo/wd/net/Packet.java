package net.montoyo.wd.net;

import net.minecraft.network.FriendlyByteBuf;

public class Packet {
    public Packet() {
    }

    public Packet(FriendlyByteBuf buf) {
    }

    public void write(FriendlyByteBuf buf) {
    }

    public void handle(WDContext ctx) {
    }

    public boolean checkClient(WDContext ctx) {
        return ctx.isClientSide();
    }

    public boolean checkServer(WDContext ctx) {
        return ctx.isServerSide();
    }

    public void respond(WDContext ctx, Packet packet) {
        ctx.enqueueWork(() -> WDNetworkRegistry.INSTANCE.reply(packet, ctx));
    }
}
