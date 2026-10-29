package net.montoyo.wd.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents;
import net.montoyo.wd.entity.TileEntityScreen;
import net.montoyo.wd.WebDisplays;
import net.montoyo.wd.config.ClientConfig;
import net.montoyo.wd.net.WDNetworkRegistry;

public class WebDisplaysFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        WebDisplays.PROXY = new ClientProxy();
        WebDisplays.PROXY.preInit();
        WebDisplays.PROXY.init();
        WebDisplays.PROXY.postInit();

        ClientConfig.init();

        WDNetworkRegistry.initClient();
        ClientProxy.registerKeybinds();
        ClientProxy.onClientSetup();
        ClientProxy.registerClientEvents();

        // Replaces Forge's BlockEntity onLoad/onChunkUnloaded hooks.
        ClientBlockEntityEvents.BLOCK_ENTITY_LOAD.register((blockEntity, level) -> {
            if (blockEntity instanceof TileEntityScreen tes)
                tes.onClientLoad();
        });

        ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((blockEntity, level) -> {
            if (blockEntity instanceof TileEntityScreen tes)
                tes.onClientUnload();
        });
    }
}
