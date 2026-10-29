/*
 * Copyright (C) 2019 BARBOTIN Nicolas
 */

package net.montoyo.wd;

import com.google.gson.Gson;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.montoyo.wd.config.CommonConfig;
import net.montoyo.wd.controls.ScreenControlRegistry;
import net.montoyo.wd.core.Criterion;
import net.montoyo.wd.init.BlockInit;
import net.montoyo.wd.init.ItemInit;
import net.montoyo.wd.init.TabInit;
import net.montoyo.wd.init.TileInit;
import net.montoyo.wd.net.WDNetworkRegistry;
import net.montoyo.wd.registry.DeferredRegister;
import net.montoyo.wd.utilities.Util;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.Registries;

import java.net.MalformedURLException;
import java.net.URL;

public class WebDisplays {
    public static WebDisplays INSTANCE;

    public static SharedProxy PROXY = null;

    public static final ResourceLocation ADV_PAD_BREAK = ResourceLocation.fromNamespaceAndPath("webdisplays", "webdisplays/pad_break");
    public static final String BLACKLIST_URL = "webdisplays://webdisplays/blacklisted.html";
    public static final Gson GSON = new Gson();

    //Sounds
    public SoundEvent soundTyping;
    public SoundEvent soundUpgradeAdd;
    public SoundEvent soundUpgradeDel;
    public SoundEvent soundScreenCfg;
    public SoundEvent soundServer;
    public SoundEvent soundIronic;

    //Criterions
    public Criterion criterionPadBreak;
    public Criterion criterionUpgradeScreen;
    public Criterion criterionLinkPeripheral;
    public Criterion criterionKeyboardCat;

    //Config
    public static final double PAD_RATIO = 59.0 / 30.0;
    public double padResX;
    public double padResY;
    public double unloadDistance2;
    public double loadDistance2;
    public int miniservPort;
    public String miniservBind;
    public long miniservQuota;
    public float ytVolume;
    public float avDist100;
    public float avDist0;

    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, "webdisplays");

    public static void initialize() {
        INSTANCE = new WebDisplays();
        PROXY = new SharedProxy();

        CommonConfig.init();

        INSTANCE.criterionPadBreak = new Criterion("pad_break");
        INSTANCE.criterionUpgradeScreen = new Criterion("upgrade_screen");
        INSTANCE.criterionLinkPeripheral = new Criterion("link_peripheral");
        INSTANCE.criterionKeyboardCat = new Criterion("keyboard_cat");
        registerTrigger(INSTANCE.criterionPadBreak, INSTANCE.criterionUpgradeScreen,
                INSTANCE.criterionLinkPeripheral, INSTANCE.criterionKeyboardCat);

        WDNetworkRegistry.init();
        INSTANCE.onRegisterSounds();
        BlockInit.init();
        ItemInit.init();
        TileInit.init();
        TabInit.init();

        if (!FabricLoader.getInstance().isDevelopmentEnvironment())
            ScreenControlRegistry.init();
    }

    private void onRegisterSounds() {
        soundTyping = registerSound("keyboard_type");
        soundUpgradeAdd = registerSound("upgrade_add");
        soundUpgradeDel = registerSound("upgrade_del");
        soundScreenCfg = registerSound("screencfg_open");
        soundServer = registerSound("server");
        soundIronic = registerSound("ironic");
    }

    private static SoundEvent registerSound(String resName) {
        ResourceLocation resLoc = ResourceLocation.fromNamespaceAndPath("webdisplays", resName);
        SoundEvent ret = SoundEvent.createVariableRangeEvent(resLoc);

        SOUNDS.register(resName, () -> ret);
        return ret;
    }

    private static void registerTrigger(Criterion... criteria) {
        for (Criterion c : criteria)
            Registry.register(BuiltInRegistries.TRIGGER_TYPES, c.getId(), c);
    }

    public static boolean isSiteBlacklisted(String url) {
        try {
            URL url2 = new URL(Util.addProtocol(url));
            for (String str : CommonConfig.Browser.blacklist)
                if (str.equalsIgnoreCase(url2.getHost())) return true;
            return false;
        } catch (MalformedURLException ex) {
            return false;
        }
    }

    public static String applyBlacklist(String url) {
        return isSiteBlacklisted(url) ? BLACKLIST_URL : rewriteLegacyModScheme(url);
    }

    /**
     * MCEF's mod:// handler resolves through the ClassLoader and never finds anything under Fabric,
     * and its registration cannot be replaced from outside. Screens created before this port
     * migrated to it stored that scheme in their block NBT, so it is rewritten on every load.
     */
    private static String rewriteLegacyModScheme(String url) {
        return url != null && url.startsWith("mod://") ? "webdisplays://" + url.substring("mod://".length()) : url;
    }
}
