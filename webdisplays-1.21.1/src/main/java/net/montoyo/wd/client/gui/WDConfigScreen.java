package net.montoyo.wd.client.gui;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.montoyo.wd.config.annoconfg.AnnoCFG;

import java.util.Locale;

/**
 * Builds a config screen out of the AnnoCFG annotated config classes.
 */
@Environment(EnvType.CLIENT)
public final class WDConfigScreen {
    private WDConfigScreen() {
    }

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("webdisplays.config.screen.title"));
        ConfigEntryBuilder entryBuilder = builder.getEntryBuilder();

        for (AnnoCFG config : AnnoCFG.all()) {
            ConfigCategory category = builder.getOrCreateCategory(Component.translatable(
                    "webdisplays.config.screen.category." + config.getSide().name().toLowerCase(Locale.ROOT)));

            for (AnnoCFG.Entry entry : config.getEntries().values())
                category.addEntry(buildEntry(entryBuilder, config, entry));
        }

        builder.setSavingRunnable(() -> {
            for (AnnoCFG config : AnnoCFG.all())
                config.save();
        });

        return builder.build();
    }

    private static AbstractConfigListEntry<?> buildEntry(ConfigEntryBuilder builder, AnnoCFG config, AnnoCFG.Entry entry) {
        Component label = label(entry);
        Object value = entry.get();
        String path = entry.path;

        if (value instanceof Boolean bool)
            return builder.startBooleanToggle(label, bool)
                    .setDefaultValue(bool)
                    .setSaveConsumer(v -> config.set(path, v))
                    .build();

        if (value instanceof Integer i)
            return builder.startIntField(label, i)
                    .setDefaultValue(i)
                    .setMin(entry.hasRange ? (int) entry.min : Integer.MIN_VALUE)
                    .setMax(entry.hasRange ? (int) entry.max : Integer.MAX_VALUE)
                    .setSaveConsumer(v -> config.set(path, v))
                    .build();

        if (value instanceof Long l)
            return builder.startLongField(label, l)
                    .setDefaultValue(l)
                    .setMin(entry.hasRange ? (long) entry.min : Long.MIN_VALUE)
                    .setMax(entry.hasRange ? (long) entry.max : Long.MAX_VALUE)
                    .setSaveConsumer(v -> config.set(path, v))
                    .build();

        if (value instanceof Double d)
            return builder.startDoubleField(label, d)
                    .setDefaultValue(d)
                    .setMin(entry.hasRange ? entry.min : -Double.MAX_VALUE)
                    .setMax(entry.hasRange ? entry.max : Double.MAX_VALUE)
                    .setSaveConsumer(v -> config.set(path, v))
                    .build();

        if (value instanceof String[] array) {
            String joined = String.join(",", array);
            return builder.startStrField(label, joined)
                    .setDefaultValue(joined)
                    .setSaveConsumer(v -> config.set(path, v.split(",")))
                    .build();
        }

        String text = value == null ? "" : value.toString();
        return builder.startStrField(label, text)
                .setDefaultValue(text)
                .setSaveConsumer(v -> config.set(path, v))
                .build();
    }

    /**
     * Uses the config's translation key when the language file has one, and falls back to the
     * dotted entry path (with the file's comments as tooltip).
     */
    private static Component label(AnnoCFG.Entry entry) {
        if (!entry.translationKey.isEmpty())
            return Component.translatable(entry.translationKey);

        return Component.literal(entry.path);
    }
}
