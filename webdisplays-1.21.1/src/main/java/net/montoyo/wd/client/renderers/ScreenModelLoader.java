package net.montoyo.wd.client.renderers;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;

public class ScreenModelLoader {
    public static final ResourceLocation SCREEN_SIDE = ResourceLocation.fromNamespaceAndPath("webdisplays", "block/screen");

    private static final ResourceLocation[] SIDES = new ResourceLocation[16];
    public static final Material[] MATERIALS_SIDES = new Material[16];

    static {
        for (int i = 0; i < SIDES.length; i++) {
            SIDES[i] = ResourceLocation.fromNamespaceAndPath(SCREEN_SIDE.getNamespace(), SCREEN_SIDE.getPath() + i);
            MATERIALS_SIDES[i] = new Material(TextureAtlas.LOCATION_BLOCKS, SIDES[i]);
        }
    }

    /**
     * Replaces the baked model of webdisplays:block/screen, which is what the Forge geometry loader did.
     */
    public static void register() {
        ModelLoadingPlugin.register(pluginContext -> pluginContext.modifyModelAfterBake().register((original, context) ->
                SCREEN_SIDE.equals(context.resourceId()) ? new ScreenBaker(context.textureGetter()) : original));
    }
}
