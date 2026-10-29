package net.montoyo.wd.net;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Replaces Forge's PacketDistributor: picks which players a server-bound send reaches.
 */
public final class WDDistributor {
    public static final PlayerSelector PLAYER = new PlayerSelector();
    public static final NearSelector NEAR = new NearSelector();

    private WDDistributor() {
    }

    public static final class TargetPoint {
        @Nullable
        public final ServerPlayer excluded;
        public final double x;
        public final double y;
        public final double z;
        public final double radius;
        public final ResourceKey<Level> dimension;

        public TargetPoint(double x, double y, double z, double radius, ResourceKey<Level> dimension) {
            this(null, x, y, z, radius, dimension);
        }

        public TargetPoint(@Nullable ServerPlayer excluded, double x, double y, double z, double radius, ResourceKey<Level> dimension) {
            this.excluded = excluded;
            this.x = x;
            this.y = y;
            this.z = z;
            this.radius = radius;
            this.dimension = dimension;
        }
    }

    public interface Target {
        void forEachPlayer(MinecraftServer server, Consumer<ServerPlayer> action);
    }

    public static final class PlayerSelector {
        public Target with(Supplier<ServerPlayer> player) {
            return (server, action) -> {
                ServerPlayer receiver = player.get();
                if (receiver != null)
                    action.accept(receiver);
            };
        }
    }

    public static final class NearSelector {
        public Target with(Supplier<TargetPoint> point) {
            return (server, action) -> {
                TargetPoint tp = point.get();
                double radiusSq = tp.radius * tp.radius;

                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    if (player.level().dimension() != tp.dimension)
                        continue;
                    if (player == tp.excluded)
                        continue;

                    double dx = player.getX() - tp.x;
                    double dy = player.getY() - tp.y;
                    double dz = player.getZ() - tp.z;
                    if (dx * dx + dy * dy + dz * dz > radiusSq)
                        continue;

                    action.accept(player);
                }
            };
        }
    }
}
