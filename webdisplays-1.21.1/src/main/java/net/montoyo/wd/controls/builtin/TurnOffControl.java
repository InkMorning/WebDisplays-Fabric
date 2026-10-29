package net.montoyo.wd.controls.builtin;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.montoyo.wd.net.WDContext;
import net.montoyo.wd.WebDisplays;
import net.montoyo.wd.controls.ScreenControl;
import net.montoyo.wd.core.MissingPermissionException;
import net.montoyo.wd.entity.TileEntityScreen;
import net.montoyo.wd.utilities.BlockSide;
import net.montoyo.wd.utilities.NameUUIDPair;

import java.util.function.Function;

public class TurnOffControl extends ScreenControl {
	public static final ResourceLocation id = ResourceLocation.parse("webdisplays:deactivate");

	public static final TurnOffControl INSTANCE = new TurnOffControl();

	public TurnOffControl() {
		super(id);
	}
	
	@Override
	public void write(FriendlyByteBuf buf) {
	}
	
	@Override
	public void handleServer(BlockPos pos, BlockSide side, TileEntityScreen tes, WDContext ctx, Function<Integer, Boolean> permissionChecker) throws MissingPermissionException {
		throw new RuntimeException("Cannot handle deactivation packet from server");
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	public void handleClient(BlockPos pos, BlockSide side, TileEntityScreen tes, WDContext ctx) {
		if (side != null) {
			WebDisplays.PROXY.closeGui(pos, side);
			tes.disableScreen(side);
		} else {
			for (BlockSide value : BlockSide.values()) {
				WebDisplays.PROXY.closeGui(pos, value);
				tes.disableScreen(value);
			}
		}
	}
}
