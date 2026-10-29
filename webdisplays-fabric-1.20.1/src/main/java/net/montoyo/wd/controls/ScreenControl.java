package net.montoyo.wd.controls;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.montoyo.wd.net.WDContext;
import net.montoyo.wd.core.MissingPermissionException;
import net.montoyo.wd.entity.TileEntityScreen;
import net.montoyo.wd.utilities.BlockSide;

import java.util.Objects;
import java.util.function.Function;

public abstract class ScreenControl {
	private final ResourceLocation id;
	
	public ScreenControl(ResourceLocation id) {
		this.id = id;
	}
	
	public abstract void write(FriendlyByteBuf buf);
	public abstract void handleServer(BlockPos pos, BlockSide side, TileEntityScreen tes, WDContext ctx, Function<Integer, Boolean> permissionChecker) throws MissingPermissionException;
	@Environment(EnvType.CLIENT)
	public abstract void handleClient(BlockPos pos, BlockSide side, TileEntityScreen tes, WDContext ctx);
	
	public void checkPerms(int perms, Function<Integer, Boolean> checker, ServerPlayer player) throws MissingPermissionException {
		if (!checker.apply(perms)) {
			throw new MissingPermissionException(perms, Objects.requireNonNull(player));
		}
	}
	
	public final ResourceLocation getId() {
		return id;
	}
}
