package net.montoyo.wd.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.montoyo.wd.block.BlockKeyboardLeft;
import net.montoyo.wd.block.BlockKeyboardRight;
import net.montoyo.wd.block.BlockPeripheral;
import net.montoyo.wd.block.BlockScreen;
import net.montoyo.wd.core.DefaultPeripheral;
import net.montoyo.wd.registry.DeferredRegister;
import net.montoyo.wd.registry.RegistryObject;

public class BlockInit {

    public static void init() {
        BLOCKS.register();
    }

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, "webdisplays");

    public static final RegistryObject<BlockScreen> blockScreen = BLOCKS.register("screen", () -> new BlockScreen(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)));

    public static final RegistryObject<BlockKeyboardLeft> blockKeyBoard = BLOCKS.register("kb_left", BlockKeyboardLeft::new);
    public static final RegistryObject<BlockKeyboardRight> blockKbRight = BLOCKS.register("kb_right", BlockKeyboardRight::new);

    public static final RegistryObject<BlockPeripheral> blockRedControl = BLOCKS.register("redctrl", () -> new BlockPeripheral(DefaultPeripheral.REDSTONE_CONTROLLER));
    public static final RegistryObject<BlockPeripheral> blockRControl = BLOCKS.register("rctrl", () -> new BlockPeripheral(DefaultPeripheral.REMOTE_CONTROLLER));
    public static final RegistryObject<BlockPeripheral> blockServer = BLOCKS.register("server", () -> new BlockPeripheral(DefaultPeripheral.SERVER));
}
