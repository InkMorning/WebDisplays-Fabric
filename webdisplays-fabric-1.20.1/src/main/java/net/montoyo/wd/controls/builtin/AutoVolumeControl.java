package net.montoyo.wd.controls.builtin;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.montoyo.wd.net.WDContext;
import net.montoyo.wd.controls.ScreenControl;
import net.montoyo.wd.core.MissingPermissionException;
import net.montoyo.wd.core.ScreenRights;
import net.montoyo.wd.entity.TileEntityScreen;
import net.montoyo.wd.utilities.BlockSide;

import java.util.function.Function;

public class AutoVolumeControl extends ScreenControl {
	public static final ResourceLocation id = new ResourceLocation("webdisplays:auto_volume");
	
	boolean autoVol;
	
	public AutoVolumeControl(boolean autoVol) {
		super(id);
		this.autoVol = autoVol;
	}
	
	public AutoVolumeControl(FriendlyByteBuf buf) {
		super(id);
		autoVol = buf.readBoolean();
	}
	
	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeBoolean(autoVol);
	}
	
	@Override
	public void handleServer(BlockPos pos, BlockSide side, TileEntityScreen tes, WDContext ctx, Function<Integer, Boolean> permissionChecker) throws MissingPermissionException {
		// I feel like there's probably a better permission category
		checkPerms(ScreenRights.MANAGE_UPGRADES, permissionChecker, ctx.getSender());
		tes.setAutoVolume(side, autoVol);
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	public void handleClient(BlockPos pos, BlockSide side, TileEntityScreen tes, WDContext ctx) {
		tes.setAutoVolume(side, autoVol);
	}
}
