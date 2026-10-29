/*
 * Copyright (C) 2019 BARBOTIN Nicolas
 */

package net.montoyo.wd.client;

import com.cinemamod.mcef.MCEF;
import com.cinemamod.mcef.MCEFBrowser;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackType;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.montoyo.wd.net.WDContext;
import net.montoyo.wd.SharedProxy;
import net.montoyo.wd.WebDisplays;
import net.montoyo.wd.block.BlockScreen;
import net.montoyo.wd.client.gui.*;
import net.montoyo.wd.client.gui.loading.GuiLoader;
import net.montoyo.wd.client.renderers.*;
import net.montoyo.wd.config.ClientConfig;
import net.montoyo.wd.core.HasAdvancement;
import net.montoyo.wd.core.JSServerRequest;
import net.montoyo.wd.data.GuiData;
import net.montoyo.wd.entity.TileEntityScreen;
import net.montoyo.wd.init.BlockInit;
import net.montoyo.wd.init.ItemInit;
import net.montoyo.wd.init.TileInit;
import net.montoyo.wd.item.ItemLaserPointer;
import net.montoyo.wd.item.ItemMinePad2;
import net.montoyo.wd.miniserv.client.Client;
import net.montoyo.wd.net.WDNetworkRegistry;
import net.montoyo.wd.net.server_bound.C2SMessageMinepadUrl;
import net.montoyo.wd.utilities.*;
import org.cef.CefSettings;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefDisplayHandler;
import org.cef.misc.CefCursorType;
import org.cef.network.CefRequest;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.jetbrains.annotations.NotNull;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.*;

@Environment(EnvType.CLIENT)
public class ClientProxy extends SharedProxy implements CefDisplayHandler/*, IJSQueryHandler*/ {
	
	private static ClientProxy INSTANCE;
	
	public ClientProxy() {
		INSTANCE = this;
	}
	
	public static void renderCrosshair(Options options, int screenWidth, int screenHeight, int offset, GuiGraphics poseStack, CallbackInfo ci) {
		if (Minecraft.getInstance().player == null)
			return;

		ItemStack stack = Minecraft.getInstance().player.getItemInHand(InteractionHand.MAIN_HAND);
		ItemStack stack1 = Minecraft.getInstance().player.getItemInHand(InteractionHand.OFF_HAND);
		
		if (stack.getItem() instanceof ItemMinePad2) {
			float sign = 1;
			if (Minecraft.getInstance().player.getMainArm() == HumanoidArm.LEFT) sign = -1;
			if (!MinePadRenderer.renderAtSide(sign)) {
				ci.cancel();
				return;
			}
		} else {
			if (stack1.getItem() instanceof ItemMinePad2) {
				float sign = -1;
				if (Minecraft.getInstance().player.getMainArm() == HumanoidArm.LEFT) sign = 1;
				if (!MinePadRenderer.renderAtSide(sign)) {
					ci.cancel();
					return;
				}
			}
		}
		
		if (!(stack.getItem() instanceof ItemLaserPointer ||
				stack1.getItem() instanceof ItemLaserPointer))
			return;
		
		if (!LaserPointerRenderer.isOn()) {
			RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

			poseStack.blit(new ResourceLocation(
					"webdisplays:textures/gui/cursors.png"
			), (screenWidth - 15) / 2, (screenHeight - 15) / 2, offset, 240, 240, 15, 15, 256, 256);
			ci.cancel();
			return;
		}
		
		Minecraft mc = Minecraft.getInstance();
		
		BlockHitResult result = raycast(64.0); //TODO: Make that distance configurable
		
		BlockPos bpos = result.getBlockPos();
		
		if (result.getType() != HitResult.Type.BLOCK || mc.level.getBlockState(bpos).getBlock() != BlockInit.blockScreen.get()) {
			RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

			poseStack.blit(new ResourceLocation(
					"webdisplays:textures/gui/cursors.png"
			), (screenWidth - 15) / 2, (screenHeight - 15) / 2, offset, 240, 240, 15, 15, 256, 256);
			ci.cancel();
			return;
		}

		Vector3i pos = new Vector3i(result.getBlockPos());
		BlockSide side = BlockSide.values()[result.getDirection().ordinal()];
		
		Multiblock.findOrigin(mc.level, pos, side, null);
		TileEntityScreen te = (TileEntityScreen) mc.level.getBlockEntity(pos.toBlock());
		
		TileEntityScreen.Screen sc = te.getScreen(side);
		
		if (sc == null) return;

		int coordX = sc.mouseType * 15;
		int coordY = coordX / 255;
		coordX -= coordY * 255;
		coordY *= 15;
		// for some reason, the cursor gets offset at this value
		if (sc.mouseType >= CefCursorType.NOT_ALLOWED.ordinal()) coordX -= 15;
		
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

		poseStack.blit(new ResourceLocation(
				"webdisplays:textures/gui/cursors.png"
		), (screenWidth - 15) / 2, (screenHeight - 15) / 2, offset, coordX, coordY, 15, 15, 256, 256);

		ci.cancel();
	}
	
	public class PadData {
		
		public CefBrowser view;
		private boolean isInHotbar;
		private final UUID id;
		private long lastURLSent;
		
		public int activeCursor;
		
		private PadData(String url, UUID id) {
			String webUrl;
			try {
				webUrl = TileEntityScreen.url(url);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			ensureSchemeHandlers();
			view = MCEF.createBrowser(WebDisplays.applyBlacklist(webUrl), false);
			if (view instanceof MCEFBrowser browser) {
				browser.resize((int) WebDisplays.INSTANCE.padResX, (int) WebDisplays.INSTANCE.padResY);
				browser.setCursorChangeListener((cursor) -> {
					activeCursor = cursor;
				});
			}
			isInHotbar = true;
			this.id = id;
		}
	}
	
	private Minecraft mc;
	private MinePadRenderer minePadRenderer;
//	private JSQueryDispatcher jsDispatcher;
	private LaserPointerRenderer laserPointerRenderer;
	private Screen nextScreen;
	
	//Miniserv handling
	private static boolean schemeHandlersRegistered;
	private int miniservPort;
	private boolean msClientStarted;
	
	//Client-side advancement hack
	private final Field advancementToProgressField = findAdvancementToProgressField();
	private ClientAdvancements lastAdvMgr;
	private Map advancementToProgress;
	
	//Tracking
	private final ArrayList<TileEntityScreen> screenTracking = new ArrayList<>();
	private int lastTracked = 0;
	
	//MinePads Management
	private final HashMap<UUID, PadData> padMap = new HashMap<>();
	private final ArrayList<PadData> padList = new ArrayList<>();
	private int minePadTickCounter = 0;
	
	/**************************************** INHERITED METHODS ****************************************/
	public static void onClientSetup() {
		BlockEntityRenderers.register(TileInit.SCREEN_BLOCK_ENTITY.get(), new ScreenRenderer.ScreenRendererProvider());
		ScreenModelLoader.register();
	}

	@Override
	public void preInit() {
		super.preInit();
		mc = Minecraft.getInstance();
	}
	
	@Override
	public void init() {
		super.init();
	}
	
	/**
	 * MCEF registers its own handlers from inside initialize(), and our onCefInit callback is only
	 * queued onto that same init pass, so registering here can be overwritten (or skipped while
	 * MCEF still reports itself as uninitialised). Called again from every browser creation site so
	 * the handlers are guaranteed to be ours by the time a page is requested.
	 */
	public static void ensureSchemeHandlers() {
		if (schemeHandlersRegistered || !MCEF.isInitialized())
			return;

		schemeHandlersRegistered = true;

		boolean wd = MCEF.getApp().getHandle().registerSchemeHandlerFactory(
				"webdisplays", "",
				(browser, frame, url, request) -> isMiniservHost(request.getURL())
						? new WDScheme(request.getURL()) : new WDModScheme()
		);
		// MCEF's own mod:// handler asks the ClassLoader for a resource name starting with a slash,
		// which never resolves under Fabric's Knot, so every bundled page came back missing. CEF
		// refuses a second factory for the same scheme+domain, so bundled pages live on our scheme.
		boolean mod = MCEF.getApp().getHandle().registerSchemeHandlerFactory(
				"mod", "",
				(browser, frame, url, request) -> new WDModScheme()
		);

		Log.info("Scheme handler registration: webdisplays=%b mod=%b", wd, mod);
	}

	private static boolean isMiniservHost(String url) {
		int schemeEnd = url.indexOf("://");
		if (schemeEnd < 0)
			return false;

		int slash = url.indexOf('/', schemeEnd + 3);
		String host = slash < 0 ? url.substring(schemeEnd + 3) : url.substring(schemeEnd + 3, slash);

		try {
			UUID.fromString(host);
			return true;
		} catch (IllegalArgumentException ex) {
			return false;
		}
	}

	@Override
	public void onCefInit(/*CefInitEvent event*/) {
		minePadRenderer = new MinePadRenderer();
		laserPointerRenderer = new LaserPointerRenderer();

		if (!MCEF.isInitialized()) return;

		ensureSchemeHandlers();

//		jsDispatcher = new JSQueryDispatcher(this);
		MCEF.getClient().addDisplayHandler(this);
//		mcef.registerJSQueryHandler(this);

		findAdvancementToProgressField();
	}
	
	@Override
	public void postInit() {
		// The client entrypoint runs inside Minecraft's constructor, so the resource manager
		// does not exist yet; Fabric queues reload listeners instead.
		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).addReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public ResourceLocation getFabricId() {
				return new ResourceLocation("webdisplays", "gui_loader");
			}

			@Override
			public void onResourceManagerReload(ResourceManager resourceManager) {
				GuiLoader.clearCache();
			}
		});
	}
	
	@Override
	public Level getWorld(ResourceKey<Level> dim) {
		Level ret = mc.level;
//        if(dim == CURRENT_DIMENSION)
//            return ret;
		if (ret != null) {
			if (!ret.dimension().equals(dim))
				throw new RuntimeException("Can't get non-current dimension " + dim + " from client.");
			return ret;
		} else {
			throw new RuntimeException("Level on client is null");
		}
	}
	
	@Override
	public void enqueue(Runnable r) {
		mc.submit(r);
	}
	
	@Override
	public void displayGui(GuiData data) {
		Screen gui = data.createGui(mc.screen, mc.level);
		if (gui != null)
			mc.setScreen(gui);
	}
	
	@Override
	public void trackScreen(TileEntityScreen tes, boolean track) {
		int idx = -1;
		for (int i = 0; i < screenTracking.size(); i++) {
			if (screenTracking.get(i) == tes) {
				idx = i;
				break;
			}
		}
		
		if (track) {
			if (idx < 0)
				screenTracking.add(tes);
		} else if (idx >= 0)
			screenTracking.remove(idx);
	}
	
	@Override
	public void onAutocompleteResult(NameUUIDPair[] pairs) {
		if (mc.screen != null && mc.screen instanceof WDScreen screen) {
			if (pairs.length == 0)
				(screen).onAutocompleteFailure();
			else
				(screen).onAutocompleteResult(pairs);
		}
	}
	
	@Override
	public GameProfile[] getOnlineGameProfiles() {
		return new GameProfile[]{mc.player.getGameProfile()};
	}
	
	@Override
	public void screenUpdateResolutionInGui(Vector3i pos, BlockSide side, Vector2i res) {
		if (mc.screen != null && mc.screen instanceof GuiScreenConfig gsc) {
			if (gsc.isForBlock(pos.toBlock(), side))
				gsc.updateResolution(res);
		}
	}
	
	@Override
	public void screenUpdateRotationInGui(Vector3i pos, BlockSide side, Rotation rot) {
		if (mc.screen != null && mc.screen instanceof GuiScreenConfig gsc) {
			if (gsc.isForBlock(pos.toBlock(), side))
				gsc.updateRotation(rot);
		}
	}
	
	@Override
	public void screenUpdateAutoVolumeInGui(Vector3i pos, BlockSide side, boolean av) {
		if (mc.screen != null && mc.screen instanceof GuiScreenConfig gsc) {
			if (gsc.isForBlock(pos.toBlock(), side))
				gsc.updateAutoVolume(av);
		}
	}
	
	@Override
	public void displaySetPadURLGui(ItemStack is, String padURL) {
		mc.setScreen(new GuiSetURL2(is, padURL));
	}
	
	@Override
	public void openMinePadGui(UUID padId) {
		PadData pd = padMap.get(padId);
		
		if (pd != null && pd.view != null)
			mc.setScreen(new GuiMinePad(pd));
	}
	
	@Override
	@NotNull
	public HasAdvancement hasClientPlayerAdvancement(@NotNull ResourceLocation rl) {
		if (advancementToProgressField != null && mc.player != null && mc.player.connection != null) {
			ClientAdvancements cam = mc.player.connection.getAdvancements();
			Advancement adv = cam.getAdvancements().get(rl);
			
			if (adv == null)
				return HasAdvancement.DONT_KNOW;
			
			if (lastAdvMgr != cam) {
				lastAdvMgr = cam;
				
				try {
					advancementToProgress = (Map) advancementToProgressField.get(cam);
				} catch (Throwable t) {
					Log.warningEx("Could not get ClientAdvancementManager.advancementToProgress field", t);
					advancementToProgress = null;
					return HasAdvancement.DONT_KNOW;
				}
			}
			
			if (advancementToProgress == null)
				return HasAdvancement.DONT_KNOW;
			
			Object progress = advancementToProgress.get(adv);
			if (progress == null)
				return HasAdvancement.NO;
			
			if (!(progress instanceof AdvancementProgress)) {
				Log.warning("The ClientAdvancementManager.advancementToProgress map does not contain AdvancementProgress instances");
				advancementToProgress = null; //Invalidate this: it's wrong
				return HasAdvancement.DONT_KNOW;
			}
			
			return ((AdvancementProgress) progress).isDone() ? HasAdvancement.YES : HasAdvancement.NO;
		}
		
		return HasAdvancement.DONT_KNOW;
	}
	
	@Override
	public MinecraftServer getServer() {
		return mc.getSingleplayerServer();
	}
	
//	@Override
//	public void handleJSResponseSuccess(int reqId, JSServerRequest type, byte[] data) {
//		JSQueryDispatcher.ServerQuery q = jsDispatcher.fulfillQuery(reqId);
//
//		if (q == null)
//			Log.warning("Received success response for invalid query ID %d of type %s", reqId, type.toString());
//		else {
//			if (type == JSServerRequest.CLEAR_REDSTONE || type == JSServerRequest.SET_REDSTONE_AT)
//				q.success("{\"status\":\"success\"}");
//			else
//				Log.warning("Received success response for query ID %d, but type is invalid", reqId);
//		}
//	}
//
//	@Override
//	public void handleJSResponseError(int reqId, JSServerRequest type, int errCode, String err) {
//		JSQueryDispatcher.ServerQuery q = jsDispatcher.fulfillQuery(reqId);
//
//		if (q == null)
//			Log.warning("Received error response for invalid query ID %d of type %s", reqId, type.toString());
//		else
//			q.error(errCode, err);
//	}
	
	@Override
	public void setMiniservClientPort(int port) {
		miniservPort = port;
	}
	
	@Override
	public void startMiniservClient() {
		if (miniservPort <= 0) {
			Log.warning("Can't start miniserv client: miniserv is disabled");
			return;
		}
		
		if (mc.player == null) {
			Log.warning("Can't start miniserv client: player is null");
			return;
		}
		
		SocketAddress saddr = mc.player.connection.getConnection().getRemoteAddress();
		if (saddr == null || !(saddr instanceof InetSocketAddress)) {
			Log.warning("Miniserv client: remote address is not inet, assuming local address");
			saddr = new InetSocketAddress("127.0.0.1", 1234);
		}
		
		InetSocketAddress msAddr = new InetSocketAddress(((InetSocketAddress) saddr).getAddress(), miniservPort);
		Client.getInstance().start(msAddr);
		msClientStarted = true;
	}
	
	@Override
	public boolean isMiniservDisabled() {
		return miniservPort <= 0;
	}
	
	@Override
	public void closeGui(BlockPos bp, BlockSide bs) {
		if (mc.screen instanceof WDScreen) {
			WDScreen scr = (WDScreen) mc.screen;

			if (scr.isForBlock(bp, bs))
				mc.setScreen(null);
		}
	}
	
	@Override
	public void renderRecipes() {
		nextScreen = new RenderRecipe();
	}
	
	@Override
	public boolean isShiftDown() {
		return Screen.hasShiftDown();
	}
	
	
	/**************************************** RESOURCE MANAGER METHODS ****************************************/
	
	/**************************************** DISPLAY HANDLER METHODS ****************************************/
	
	
	@Override
	public void onAddressChange(CefBrowser browser, CefFrame cefFrame, String url) {
		if (browser != null) {
			long t = System.currentTimeMillis();
			
			for (PadData pd : padList) {
				if (pd.view == browser && t - pd.lastURLSent >= 1000) {
					if (WebDisplays.isSiteBlacklisted(url))
						pd.view.loadURL(WebDisplays.BLACKLIST_URL);
					else {
						pd.lastURLSent = t; //Avoid spamming the server with porn URLs
						WDNetworkRegistry.INSTANCE.sendToServer(new C2SMessageMinepadUrl(pd.id, url));
					}
					
					break;
				}
			}
			
			for (TileEntityScreen tes : screenTracking)
				tes.updateClientSideURL(browser, url);
		}
	}
	
	@Override
	public void onTitleChange(CefBrowser cefBrowser, String s) {
	}
	
	@Override
	public boolean onTooltip(CefBrowser cefBrowser, String s) {
		return false;
	}
	
	@Override
	public void onStatusMessage(CefBrowser cefBrowser, String s) {
	}
	
	@Override
	public boolean onConsoleMessage(CefBrowser cefBrowser, CefSettings.LogSeverity logSeverity, String s, String s1, int i) {
		return false;
	}
	
	@Override
	public boolean onCursorChange(CefBrowser cefBrowser, int i) {
		return false;
	}
	
	/**************************************** JS HANDLER METHODS ****************************************/
	
//	@Override
//	public boolean handleQuery(IBrowser browser, long queryId, String query, boolean persistent, IJSQueryCallback cb) {
//		if (browser != null && persistent && query != null && cb != null) {
//			query = query.toLowerCase();
//
//			if (query.startsWith("webdisplays_")) {
//				query = query.substring(12);
//
//				String args;
//				int parenthesis = query.indexOf('(');
//				if (parenthesis < 0)
//					args = null;
//				else {
//					if (query.indexOf(')') != query.length() - 1) {
//						cb.failure(400, "Malformed request");
//						return true;
//					}
//
//					args = query.substring(parenthesis + 1, query.length() - 1);
//					query = query.substring(0, parenthesis);
//				}
//
//				if (jsDispatcher.canHandleQuery(query))
//					jsDispatcher.enqueueQuery(browser, query, args, cb);
//				else
//					cb.failure(404, "Unknown WebDisplays query");
//
//				return true;
//			}
//		}
//
//		return false;
//	}
//
//	@Override
//	public void cancelQuery(IBrowser browser, long queryId) {
//	}
	
	/**************************************** EVENT METHODS ****************************************/
	
//    @SubscribeEvent
//    public void onBakeModel(ModelBakeEvent ev) {
//        for(ResourceModelPair pair : modelBakers)
//            ev.getModelRegistry().put(pair.getResourceLocation(), pair.getModel());
//    }

 /*   @SubscribeEvent
    public void onRegisterModels(ModelRegistryEvent ev) {
        final WebDisplays wd = WebDisplays.INSTANCE;

        //I hope I'm doing this right because it doesn't seem like it...
       registerItemModel(wd.blockScreen.getItem(), 0, "inventory");
       ModelLoaderRegistry.setCustomModelResourceLocation(wd.blockPeripheral.getItem(), 0, new ModelResourceLocation("webdisplays:kb_inv", "normal"));
       registerItemModel(wd.blockPeripheral.getItem(), 1, "facing=2,type=ccinterface");
       registerItemModel(wd.blockPeripheral.getItem(), 2, "facing=2,type=cointerface");
       registerItemModel(wd.blockPeripheral.getItem(), 3, "facing=0,type=remotectrl");
       registerItemModel(wd.blockPeripheral.getItem(), 7, "facing=0,type=redstonectrl");
       registerItemModel(wd.blockPeripheral.getItem(), 11, "facing=0,type=server");
       registerItemModel(wd.itemScreenCfg, 0, "normal");
       registerItemModel(wd.itemOwnerThief, 0, "normal");
       registerItemModel(wd.itemLinker, 0, "normal");
       registerItemModel(wd.itemMinePad, 0, "normal");
       registerItemModel(wd.itemMinePad, 1, "normal");
       registerItemModel(wd.itemLaserPointer, 0, "normal");
       registerItemMultiModels(wd.itemUpgrade);
       registerItemMultiModels(wd.itemCraftComp);
       registerItemMultiModels(wd.itemAdvIcon);
    } */
	
	public void onLevelTick(ClientLevel level) {
		//Unload/load screens depending on client player distance
		if (mc.player == null || screenTracking.isEmpty())
			return;

		int id = lastTracked % screenTracking.size();

		TileEntityScreen tes = screenTracking.get(id);

		if (!tes.getLevel().equals(level))
			return;

		lastTracked++;
		if (tes.getLevel() != mc.player.level()) {
			// TODO: properly handle this
			// probably gonna want a helper class for cross-dimensional distances
			if (!tes.isLoaded())
				tes.load();
		} else {
			double dist = Double.POSITIVE_INFINITY;
			BlockPos bp = tes.getBlockPos();

			for (int i = 0; i < tes.screenCount(); i++) {
				TileEntityScreen.Screen scrn = tes.getScreen(i);

				// The plane extent is relative to the block origin, so anchor it before measuring.
				double extentX = scrn.side.right.x * scrn.size.x + (double) scrn.size.y * scrn.side.up.x;
				double extentY = scrn.side.right.y * scrn.size.x + (double) scrn.size.y * scrn.side.up.y;
				double extentZ = scrn.side.right.z * scrn.size.x + (double) scrn.size.y * scrn.side.up.z;

				double centerX = bp.getX() + 0.5D + extentX / 2.0D;
				double centerY = bp.getY() + 0.5D + extentY / 2.0D;
				double centerZ = bp.getZ() + 0.5D + extentZ / 2.0D;

				dist = Math.min(dist, mc.player.distanceToSqr(centerX, centerY, centerZ));
			}

			if (Double.isInfinite(dist))
				return;
			
			if (tes.isLoaded()) {
				if (dist > WebDisplays.INSTANCE.unloadDistance2)
					tes.unload();
//				else if (ClientConfig.AutoVolumeControl.enableAutoVolume)
//					tes.updateTrackDistance(dist, 80); //ToDo find master volume
			} else if (dist <= WebDisplays.INSTANCE.loadDistance2)
				tes.load();
		}
	}
	
	public void onTick() {
		updateBlockOutline();
				//Workaround cuz chat sux
		if (nextScreen != null && mc.screen == null) {
			mc.setScreen(nextScreen);
			nextScreen = null;
		}
		
		// handle r button
		if (KEY_MOUSE.isDown()) {
			if (!rDown) {
				rDown = true;
				mouseOn = !mouseOn;
			}
		} else rDown = false;
		if (
				Minecraft.getInstance().player == null ||
						!(Minecraft.getInstance().player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof ItemLaserPointer)
		) mouseOn = false;
		
		
		//Load/unload minePads depending on which item is in the player's hand
		if (++minePadTickCounter >= 10) {
			minePadTickCounter = 0;
			Player ep = mc.player;
			
			for (PadData pd : padList)
				pd.isInHotbar = false;
			
			if (ep != null) {
				updateInventory(ep.getInventory().items, ep.getItemInHand(InteractionHand.MAIN_HAND), 9);
				updateInventory(ep.getInventory().offhand, ep.getItemInHand(InteractionHand.OFF_HAND), 1); //Is this okay?
			}
			
			//TODO: Check for GuiContainer.draggedStack
			
			for (int i = padList.size() - 1; i >= 0; i--) {
				PadData pd = padList.get(i);
				
				if (!pd.isInHotbar) {
					pd.view.close(true);
					pd.view = null; //This is for GuiMinePad, in case the player dies with the GUI open
					padList.remove(i);
					padMap.remove(pd.id);
				}
			}
		}
		
		//Laser pointer raycast
		if (LaserPointerRenderer.isOn()) {
			ItemLaserPointer.tick(mc);
		} else {
			ItemLaserPointer.deselect(mc);
		}

//        //Handle JS queries
//        jsDispatcher.handleQueries();
		
		//Miniserv
		if (msClientStarted && mc.player == null) {
			msClientStarted = false;
			Client.getInstance().stop();
		}
	}
	
	/**
	 * Called from ItemInHandRendererMixin, replacing Forge's RenderHandEvent.
	 * Returns true when vanilla hand rendering should be skipped.
	 */
	public static boolean renderFirstPersonItem(ItemStack stack, boolean leftHand, PoseStack pose, MultiBufferSource buffers, int light) {
		ClientProxy proxy = INSTANCE;
		if (proxy == null || proxy.mc.player == null)
			return false;

		if (!ItemInit.MINEPAD.isPresent() || !ItemInit.LASER_POINTER.isPresent())
			return false;

		Item item = stack.getItem();
		IItemRenderer renderer;

		if (item == ItemInit.MINEPAD.get())
			renderer = proxy.minePadRenderer;
		else if (item == ItemInit.LASER_POINTER.get())
			renderer = proxy.laserPointerRenderer;
		else
			return false;

		float sign = leftHand ? -1.0f : 1.0f;
		float swingProgress = proxy.mc.player.getAttackAnim(proxy.mc.getFrameTime());
		return renderer.render(pose, stack, sign, swingProgress, 0.0f, buffers, light);
	}

	public void onWorldDisconnect() {
		Log.info("World unloaded; killing screens...");
		for (int i = screenTracking.size() - 1; i >= 0; i--)
			screenTracking.remove(i).unload();
	}
	
	public static BlockHitResult raycast(double dist) {
		Minecraft mc = Minecraft.getInstance();
		
		Vec3 start = mc.player.getEyePosition(1.0f);
		Vec3 lookVec = mc.player.getLookAngle();
		Vec3 end = start.add(lookVec.x * dist, lookVec.y * dist, lookVec.z * dist);
		
		// Forge patches the entity collision context to accept null; vanilla dereferences it.
		return mc.level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, mc.player));
	}
	
	private void updateInventory(NonNullList<ItemStack> inv, ItemStack heldStack, int cnt) {
		for (int i = 0; i < cnt; i++) {
			ItemStack item = inv.get(i);
			
			if (ItemInit.MINEPAD.isPresent()) {
				if (item.getItem() == ItemInit.MINEPAD.get()) {
					CompoundTag tag = item.getTag();
					
					if (tag != null && tag.contains("PadID"))
						updatePad(tag.getUUID("PadID"), tag, item == heldStack);
				}
			}
		}
	}
	
	private void updatePad(UUID id, CompoundTag tag, boolean isSelected) {
		PadData pd = padMap.get(id);
		
		if (pd != null)
			pd.isInHotbar = true;
		else if (isSelected && tag.contains("PadURL")) {
			pd = new PadData(tag.getString("PadURL"), id);
			padMap.put(id, pd);
			padList.add(pd);
		}
	}
	
	public MinePadRenderer getMinePadRenderer() {
		return minePadRenderer;
	}
	
	public PadData getPadByID(UUID id) {
		return padMap.get(id);
	}
	
	public static final class ScreenSidePair {
		
		public TileEntityScreen tes;
		public BlockSide side;
		
	}
	
	public boolean findScreenFromBrowser(CefBrowser browser, ScreenSidePair pair) {
		for (TileEntityScreen tes : screenTracking) {
			for (int i = 0; i < tes.screenCount(); i++) {
				TileEntityScreen.Screen scr = tes.getScreen(i);
				
				if (scr.browser == browser) {
					pair.tes = tes;
					pair.side = scr.side;
					return true;
				}
			}
		}
		
		return false;
	}
	
	private static Field findAdvancementToProgressField() {
		Field[] fields = ClientAdvancements.class.getDeclaredFields();
		Optional<Field> result = Arrays.stream(fields).filter(f -> f.getType() == Map.class).findAny();
		
		if (result.isPresent()) {
			try {
				Field ret = result.get();
				ret.setAccessible(true);
				return ret;
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		
		Log.warning("ClientAdvancementManager.advancementToProgress field could not be found");
		return null;
	}
	
	@Override
	public BlockGetter getWorld(WDContext context) {
		BlockGetter senderLevel = super.getWorld(context);
		if (senderLevel == null) return Minecraft.getInstance().level;
		return senderLevel;
	}
	
	private boolean outlineHiddenByMod = false;

	/**
	 * Replaces Forge's RenderHighlightEvent: hide the block outline when a live web surface is targeted.
	 */
	private void updateBlockOutline() {
		if (outlineHiddenByMod) {
			mc.gameRenderer.setRenderBlockOutline(true);
			outlineHiddenByMod = false;
		}

		if (!(mc.hitResult instanceof BlockHitResult bhr) || mc.level == null)
			return;

		BlockState state = mc.level.getBlockState(bhr.getBlockPos());
		if (!(state.getBlock() instanceof BlockScreen))
			return;

		Vector3i vec = new Vector3i(bhr.getBlockPos().getX(), bhr.getBlockPos().getY(), bhr.getBlockPos().getZ());
		BlockSide side = BlockSide.fromInt(bhr.getDirection().ordinal());
		Multiblock.findOrigin(mc.level, vec, side, null);

		if (mc.level.getBlockEntity(vec.toBlock()) instanceof TileEntityScreen tes && tes.getScreen(side) != null) {
			mc.gameRenderer.setRenderBlockOutline(false);
			outlineHiddenByMod = true;
		}
	}

	public static void onClientChat(String message) {
		if (message.equals("!WD render recipes"))
			WebDisplays.PROXY.renderRecipes();
	}

	/**
	 * KEYBINDS
	 **/
	public static final KeyMapping KEY_MOUSE = new KeyMapping("webdisplays.key.toggle_mouse", GLFW.GLFW_KEY_R, "key.categories.misc");
	static boolean rDown = false;
	public static boolean mouseOn = false;

	public static void registerKeybinds() {
		KeyBindingHelper.registerKeyBinding(KEY_MOUSE);
	}

	public static void registerClientEvents() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (INSTANCE != null)
				INSTANCE.onTick();
		});

		ClientTickEvents.END_WORLD_TICK.register(level -> {
			if (INSTANCE != null)
				INSTANCE.onLevelTick(level);
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			if (INSTANCE != null)
				INSTANCE.onWorldDisconnect();
		});

		ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
			if (message.equals("!WD render recipes")) {
				onClientChat(message);
				return false;
			}

			if (message.equals("!WD config")) {
				Minecraft mc = Minecraft.getInstance();
				mc.setScreen(WDConfigScreen.create(mc.screen));
				return false;
			}

			return true;
		});
	}
}
