/*
 * Copyright (C) 2019 BARBOTIN Nicolas
 */

package net.montoyo.wd.core;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Per-world player records, replacing the Forge player capability that only tracked the "first run" flag.
 */
public class WDServerData extends SavedData {
    private static final String NAME = "webdisplays";

    private final Set<UUID> welcomedPlayers = new HashSet<>();

    public static WDServerData load(CompoundTag tag, HolderLookup.Provider registries) {
        WDServerData data = new WDServerData();

        if (tag.contains("welcomed", Tag.TAG_LIST)) {
            ListTag list = tag.getList("welcomed", Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                try {
                    data.welcomedPlayers.add(UUID.fromString(list.getString(i)));
                } catch (Throwable ignored) {
                }
            }
        }

        return data;
    }

    public static WDServerData get(MinecraftServer server) {
        // DataFixTypes must not be null here: DimensionDataStorage#readTagFromDisk dereferences it unconditionally.
        SavedData.Factory<WDServerData> factory = new SavedData.Factory<>(WDServerData::new, WDServerData::load, DataFixTypes.LEVEL);
        return server.overworld().getDataStorage().computeIfAbsent(factory, NAME);
    }

    public boolean isFirstRun(ServerPlayer player) {
        return !welcomedPlayers.contains(player.getGameProfile().getId());
    }

    public void markWelcomed(ServerPlayer player) {
        welcomedPlayers.add(player.getGameProfile().getId());
        setDirty();
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (UUID uuid : welcomedPlayers)
            list.add(StringTag.valueOf(uuid.toString()));

        tag.put("welcomed", list);
        return tag;
    }
}
