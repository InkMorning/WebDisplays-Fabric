package net.montoyo.wd.client.renderers;

import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import org.jetbrains.annotations.Nullable;

/**
 * Chunk meshing happens on worker threads, so the position being tessellated is tracked per thread.
 * {@link ModelBlockRenderer#tesselateBlock} fills this through a mixin, and the screen baked model reads it
 * to pick the edge-bar texture for each face (this replaces Forge's ModelData).
 */
public final class ScreenModelContext {
    private static final ThreadLocal<BlockAndTintGetter> LEVEL = new ThreadLocal<>();
    private static final ThreadLocal<BlockPos> POS = new ThreadLocal<>();

    private ScreenModelContext() {
    }

    public static void set(@Nullable BlockAndTintGetter level, @Nullable BlockPos pos) {
        LEVEL.set(level);
        POS.set(pos);
    }

    @Nullable
    public static BlockAndTintGetter level() {
        return LEVEL.get();
    }

    @Nullable
    public static BlockPos pos() {
        return POS.get();
    }
}
