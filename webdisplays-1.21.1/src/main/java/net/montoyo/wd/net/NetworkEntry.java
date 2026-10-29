package net.montoyo.wd.net;

import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Function;

public class NetworkEntry<T extends Packet> {
    public enum Direction {
        CLIENT_TO_SERVER,
        SERVER_TO_CLIENT
    }

    private final Direction direction;
    private final Function<FriendlyByteBuf, T> fabricator;

    private NetworkEntry(Direction direction, Function<FriendlyByteBuf, T> fabricator) {
        this.direction = direction;
        this.fabricator = fabricator;
    }

    public static <T extends Packet> NetworkEntry<T> clientToServer(Function<FriendlyByteBuf, T> fabricator) {
        return new NetworkEntry<>(Direction.CLIENT_TO_SERVER, fabricator);
    }

    public static <T extends Packet> NetworkEntry<T> serverToClient(Function<FriendlyByteBuf, T> fabricator) {
        return new NetworkEntry<>(Direction.SERVER_TO_CLIENT, fabricator);
    }

    public Direction getDirection() {
        return direction;
    }

    public T read(FriendlyByteBuf buf) {
        return fabricator.apply(buf);
    }
}
