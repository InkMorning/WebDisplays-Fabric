package net.montoyo.wd.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.montoyo.wd.core.DefaultPeripheral;
import net.montoyo.wd.entity.*;
import net.montoyo.wd.registry.DeferredRegister;
import net.montoyo.wd.registry.RegistryObject;

public class TileInit {

    public static final DeferredRegister<BlockEntityType<?>> TILE_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "webdisplays");

    //Register tile entities
    public static final RegistryObject<BlockEntityType<TileEntityScreen>> SCREEN_BLOCK_ENTITY = TILE_TYPES
            .register("screen", () -> BlockEntityType.Builder
                    .of(TileEntityScreen::new, BlockInit.blockScreen.get()).build(null));

    public static final RegistryObject<BlockEntityType<?>> KEYBOARD = TILE_TYPES.register("kb_left", () -> BlockEntityType.Builder
            .of(TileEntityKeyboard::new, BlockInit.blockKeyBoard.get()).build(null));

    public static final RegistryObject<BlockEntityType<?>> REMOTE_CONTROLLER = TILE_TYPES.register("rctrl",
            () -> BlockEntityType.Builder.of(TileEntityRCtrl::new, BlockInit.blockRControl.get()).build(null));         //WITHOUT FACING (>= 3)

    public static final RegistryObject<BlockEntityType<?>> REDSTONE_CONTROLLER = TILE_TYPES.register("redctrl",
            () -> BlockEntityType.Builder.of(TileEntityRedCtrl::new, BlockInit.blockRedControl.get()).build(null));

    public static final RegistryObject<BlockEntityType<?>> SERVER = TILE_TYPES.register("server",
            () -> BlockEntityType.Builder.of(TileEntityServer::new, BlockInit.blockServer.get()).build(null));

    public static void init() {
        TILE_TYPES.register();
    }
}
