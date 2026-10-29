package net.montoyo.wd.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * One channel per direction carries every WebDisplays message; the body starts with the packet
 * index dispatched by WDNetworkRegistry.
 */
public final class WDPayload {

    public record C2S(byte[] body) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<C2S> TYPE = new CustomPacketPayload.Type<>(
                ResourceLocation.fromNamespaceAndPath("webdisplays", "packetsystem_c2s"));
        public static final StreamCodec<RegistryFriendlyByteBuf, C2S> STREAM_CODEC = StreamCodec.of(
                (buf, packet) -> buf.writeBytes(packet.body()),
                buf -> new C2S(readBody(buf)));

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record S2C(byte[] body) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<S2C> TYPE = new CustomPacketPayload.Type<>(
                ResourceLocation.fromNamespaceAndPath("webdisplays", "packetsystem_s2c"));
        public static final StreamCodec<RegistryFriendlyByteBuf, S2C> STREAM_CODEC = StreamCodec.of(
                (buf, packet) -> buf.writeBytes(packet.body()),
                buf -> new S2C(readBody(buf)));

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    private static byte[] readBody(RegistryFriendlyByteBuf buf) {
        byte[] out = new byte[buf.readableBytes()];
        buf.readBytes(out);
        return out;
    }

    private WDPayload() {
    }
}
