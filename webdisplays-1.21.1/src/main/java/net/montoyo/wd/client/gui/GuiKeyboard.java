/*
 * Copyright (C) 2019 BARBOTIN Nicolas
 */

package net.montoyo.wd.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.montoyo.wd.WebDisplays;
import net.montoyo.wd.client.gui.controls.Button;
import net.montoyo.wd.client.gui.controls.Control;
import net.montoyo.wd.client.gui.controls.Label;
import net.montoyo.wd.client.gui.loading.FillControl;
import net.montoyo.wd.entity.TileEntityScreen;
import net.montoyo.wd.net.WDNetworkRegistry;
import net.montoyo.wd.net.server_bound.C2SMessageScreenCtrl;
import net.montoyo.wd.utilities.BlockSide;
import net.montoyo.wd.utilities.Log;
import net.montoyo.wd.utilities.TypeData;
import net.montoyo.wd.utilities.Util;
import org.lwjgl.glfw.GLFW;

import java.io.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class GuiKeyboard extends WDScreen {

    private static final String WARNING_FNAME = "wd_keyboard_warning.txt";

    private TileEntityScreen tes;
    private BlockSide side;
    private final ArrayList<TypeData> evStack = new ArrayList<>();
    private BlockPos kbPos;
    private boolean showWarning = true;

    @FillControl
    private Label lblInfo;

    @FillControl
    private Button btnOk;

    public GuiKeyboard() {
        super(Component.nullToEmpty(null));
    }

    public GuiKeyboard(TileEntityScreen tes, BlockSide side, BlockPos kbPos) {
        this();
        this.tes = tes;
        this.side = side;
        this.kbPos = kbPos;
    }

    @Override
    protected void addLoadCustomVariables(Map<String, Double> vars) {
        vars.put("showWarning", showWarning ? 1.0 : 0.0);
    }

    private static final Method viveGetPlayer;
    private static final Method viveSetOverlayShowing;

    static {
        Method setOverlay = null;
        Method getPlayer = null;

        // Vivecraft moved its classes to org.vivecraft.client_vr in 1.20, and the non-mixin
        // variant is not a proper mod, so both layouts are probed reflectively.
        String[][] layouts = {
                {"org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler", "org.vivecraft.client_vr.gameplay.VRPlayer"},
                {"org.vivecraft.gameplay.screenhandlers.KeyboardHandler", "org.vivecraft.gameplay.VRPlayer"},
        };

        for (String[] layout : layouts) {
            try {
                Class<?> handler = Class.forName(layout[0]);
                getPlayer = Class.forName(layout[1]).getMethod("get");
                setOverlay = handler.getMethod("setOverlayShowing", boolean.class);
                break;
            } catch (Throwable ignored) {
                getPlayer = null;
                setOverlay = null;
            }
        }

        if (setOverlay == null && FabricLoader.getInstance().isModLoaded("vivecraft"))
            Log.warning("Vivecraft is loaded but its keyboard overlay methods could not be found.");

        viveGetPlayer = getPlayer;
        viveSetOverlayShowing = setOverlay;
    }

    /**
     * Shows Vivecraft's own VR keyboard instead of typing on the in-world one, but only while actually in VR.
     */
    private static void showVivecraftKeyboard(boolean showing) {
        if (viveSetOverlayShowing == null)
            return;

        try {
            if (viveGetPlayer.invoke(null) == null)
                return;

            viveSetOverlayShowing.invoke(null, showing);
        } catch (Throwable ignored) {
        }
    }
    
    @Override
    public void init() {
        super.init();

        if (minecraft.getSingleplayerServer() != null && !minecraft.getSingleplayerServer().isPublished())
            showWarning = false; //NO NEED
        else
            showWarning = !hasUserReadWarning();

        loadFrom(ResourceLocation.fromNamespaceAndPath("webdisplays", "gui/kb_right.json"));

        if (showWarning) {
            int maxLabelW = 0;
            int totalH = 0;

            for (Control ctrl : controls) {
                if (ctrl != lblInfo && ctrl instanceof Label) {
                    if (ctrl.getWidth() > maxLabelW)
                        maxLabelW = ctrl.getWidth();

                    totalH += ctrl.getHeight();
                    ctrl.setPos((width - ctrl.getWidth()) / 2, 0);
                }
            }

            btnOk.setWidth(maxLabelW);
            btnOk.setPos((width - maxLabelW) / 2, 0);
            totalH += btnOk.getHeight();

            int y = (height - totalH) / 2;
            for (Control ctrl : controls) {
                if (ctrl != lblInfo) {
                    ctrl.setPos(ctrl.getX(), y);
                    y += ctrl.getHeight();
                }
            }
        } else {
            grabMouseKeepingScreen();
        }

        defaultBackground = showWarning;
        syncTicks = 5;
    
        showVivecraftKeyboard(true);
    }
    
    @Override
    public void onClose() {
        showVivecraftKeyboard(false);
        super.onClose();
    }
    
    private boolean closeRequested = false;

    @Override
    public void tick() {
        super.tick();

        if (closeRequested && minecraft.screen == this) {
            closeRequested = false;
            minecraft.setScreen(null);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(quitOnEscape && keyCode == GLFW.GLFW_KEY_ESCAPE)
            // KeyboardHandler only skips the ESC->pause branch if minecraft.screen is still set
            // after dispatching the key, so close on the next tick instead of right here.
            closeRequested = true;
        addKey(new TypeData(TypeData.Action.PRESS, keyCode, modifiers, scanCode));
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        addKey(new TypeData(TypeData.Action.TYPE, codePoint, modifiers, 0));
        return super.charTyped(codePoint, modifiers);
    }
    
    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        addKey(new TypeData(TypeData.Action.RELEASE, keyCode, modifiers, scanCode));
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    void addKey(TypeData data) {
        tes.type(side, "[" + WebDisplays.GSON.toJson(data) + "]", kbPos);
        
        evStack.add(data);
        if (!evStack.isEmpty() && !syncRequested())
            requestSync();
    }

    @Override
    protected void sync() {
        if(!evStack.isEmpty()) {
            WDNetworkRegistry.INSTANCE.sendToServer(C2SMessageScreenCtrl.type(tes, side, WebDisplays.GSON.toJson(evStack), kbPos));
            evStack.clear();
        }
    }

    @GuiSubscribe
    public void onClick(Button.ClickEvent ev) {
        if(showWarning && ev.getSource() == btnOk) {
            writeUserAcknowledge();

            for(Control ctrl: controls) {
                if(ctrl instanceof Label) {
                    Label lbl = (Label) ctrl;
                    lbl.setVisible(!lbl.isVisible());
                }
            }

            btnOk.setDisabled(true);
            btnOk.setVisible(false);
            showWarning = false;
            defaultBackground = false;
            minecraft.setWindowActive(true);
            grabMouseKeepingScreen();
        }
    }

    /**
     * Vanilla's grabMouse() ends with Minecraft.setScreen(null), which would close this very screen.
     * View deltas are only accumulated while the mouse is grabbed, so replicate the grab itself.
     */
    private void grabMouseKeepingScreen() {
        MouseHandler mouse = minecraft.mouseHandler;

        if (!minecraft.isWindowActive() || mouse.isMouseGrabbed())
            return;

        mouse.mouseGrabbed = true;
        GLFW.glfwSetInputMode(minecraft.getWindow().getWindow(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
        mouse.setIgnoreFirstMove();
        minecraft.missTime = 10000;
    }

    private boolean hasUserReadWarning() {
        try {
            File f = new File(FabricLoader.getInstance().getGameDir().toString(), WARNING_FNAME);

            if(f.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(f));
                String str = br.readLine();
                Util.silentClose(br);

                return str != null && str.trim().equalsIgnoreCase("read");
            }
        } catch(Throwable t) {
            Log.warningEx("Can't know if user has already read the warning", t);
        }

        return false;
    }

    private void writeUserAcknowledge() {
        try {
            File f = new File(FabricLoader.getInstance().getGameDir().toString(), WARNING_FNAME);

            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            bw.write("read\n");
            Util.silentClose(bw);
        } catch(Throwable t) {
            Log.warningEx("Can't write that the user read the warning", t);
        }
    }

    @Override
    public boolean isForBlock(BlockPos bp, BlockSide side) {
        return bp.equals(kbPos) || (bp.equals(tes.getBlockPos()) && side == this.side);
    }

}
