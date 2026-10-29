package net.montoyo.wd.config.annoconfg;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.montoyo.wd.config.annoconfg.annotation.format.CFGSegment;
import net.montoyo.wd.config.annoconfg.annotation.format.Comment;
import net.montoyo.wd.config.annoconfg.annotation.format.Config;
import net.montoyo.wd.config.annoconfg.annotation.format.ConfigSide;
import net.montoyo.wd.config.annoconfg.annotation.format.Name;
import net.montoyo.wd.config.annoconfg.annotation.format.Skip;
import net.montoyo.wd.config.annoconfg.annotation.format.Translation;
import net.montoyo.wd.config.annoconfg.annotation.value.Default;
import net.montoyo.wd.config.annoconfg.annotation.value.DoubleRange;
import net.montoyo.wd.config.annoconfg.annotation.value.IntRange;
import net.montoyo.wd.config.annoconfg.annotation.value.LongRange;
import net.montoyo.wd.config.annoconfg.handle.UnsafeHandle;
import net.montoyo.wd.config.annoconfg.util.EnumType;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Annotation driven config. Values live in static fields of the annotated class and are kept in a JSON file
 * under the config directory.
 */
public class AnnoCFG {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final ArrayList<AnnoCFG> configs = new ArrayList<>();

	public static final class Entry {
		public final String path;
		public final Class<?> type;
		public final UnsafeHandle handle;
		public final String comment;
		public final String translationKey;
		public final double min;
		public final double max;
		public final boolean hasRange;

		Entry(String path, Class<?> type, UnsafeHandle handle, String comment, String translationKey,
			  double min, double max, boolean hasRange) {
			this.path = path;
			this.type = type;
			this.handle = handle;
			this.comment = comment;
			this.translationKey = translationKey;
			this.min = min;
			this.max = max;
			this.hasRange = hasRange;
		}

		public Object get() {
			return handle.get();
		}

		public void set(Object value) {
			handle.set(value);
		}
	}

	private final Class<?> root;
	private final ConfigSide side;
	private final Path file;
	private final Method postInit;
	private final Map<String, Entry> entries = new LinkedHashMap<>();
	private final Map<String, Object> defaults = new LinkedHashMap<>();
	private final Map<String, Object> values = new LinkedHashMap<>();

	public AnnoCFG(ConfigSide side, Class<?> clazz) {
		root = clazz;
		this.side = side;

		Config descriptor = clazz.getAnnotation(Config.class);
		String prefix = descriptor == null || descriptor.path().isEmpty() ? "" : descriptor.path() + "/";
		String name = prefix + "webdisplays_" + side.name().toLowerCase(Locale.ROOT) + ".json";
		file = FabricLoader.getInstance().getConfigDir().resolve(name);

		Method m = null;
		try {
			m = clazz.getDeclaredMethod("postLoad");
		} catch (Throwable ignored) {
		}
		postInit = m;

		setup("", clazz);
		configs.add(this);
		load();
		apply();
	}

	public static List<AnnoCFG> all() {
		return configs;
	}

	public Class<?> getRootClass() {
		return root;
	}

	public ConfigSide getSide() {
		return side;
	}

	public Path getFile() {
		return file;
	}

	public Map<String, Entry> getEntries() {
		return entries;
	}

	private static String commentOf(AnnotatedElement element) {
		Comment comment = element.getAnnotation(Comment.class);
		if (comment == null)
			return "";

		return String.join("\n", comment.value());
	}

	private static String translationOf(AnnotatedElement element) {
		Translation translation = element.getAnnotation(Translation.class);
		return translation == null ? "" : translation.value();
	}

	private void setup(String dir, Class<?> clazz) {
		if (dir.startsWith("."))
			dir = dir.substring(1);

		for (Field field : clazz.getFields()) {
			if (!field.canAccess(null))
				continue;

			Skip skip = field.getAnnotation(Skip.class);
			if (skip != null)
				continue;

			Name name = field.getAnnotation(Name.class);
			String path = dir.isEmpty() ? (name != null ? name.value() : field.getName())
					: dir + "." + (name != null ? name.value() : field.getName());

			Default defaultValue = field.getAnnotation(Default.class);
			if (defaultValue == null)
				throw new RuntimeException("Config entry " + path + " has no @Default annotation.");

			Object def;
			double min = Double.MIN_VALUE;
			double max = Double.MAX_VALUE;
			boolean hasRange = false;

			switch (EnumType.forClass(field.getType())) {
				case INT -> {
					def = defaultValue.valueI();
					IntRange range = field.getAnnotation(IntRange.class);
					if (range != null) {
						def = clamp(((Number) def).intValue(), range.minV(), range.maxV());
						min = range.minV();
						max = range.maxV();
						hasRange = true;
					}
				}
				case LONG -> {
					def = defaultValue.valueL();
					LongRange range = field.getAnnotation(LongRange.class);
					if (range != null) {
						def = clamp(((Number) def).longValue(), range.minV(), range.maxV());
						min = range.minV();
						max = range.maxV();
						hasRange = true;
					}
				}
				case DOUBLE -> {
					def = defaultValue.valueD();
					DoubleRange range = field.getAnnotation(DoubleRange.class);
					if (range != null) {
						def = clamp(((Number) def).doubleValue(), range.minV(), range.maxV());
						min = range.minV();
						max = range.maxV();
						hasRange = true;
					}
				}
				case BOOLEAN -> def = defaultValue.valueBoolean();
				default -> {
					if (field.getType().equals(String[].class))
						def = defaultValue.valueStr();
					else if (field.getType().equals(String.class))
						def = defaultValue.valueStr();
					else
						throw new RuntimeException("NYI " + field.getType());
				}
			}

			defaults.put(path, def);
			values.put(path, def);
			entries.put(path, new Entry(path, field.getType(), new UnsafeHandle(field),
					commentOf(field), translationOf(field), min, max, hasRange));
		}

		for (Class<?> nestMember : clazz.getClasses()) {
			if (nestMember == clazz || !nestMember.getName().startsWith(clazz.getName()))
				continue;

			CFGSegment segment = nestMember.getAnnotation(CFGSegment.class);
			if (segment == null)
				throw new RuntimeException("NYI: default name for nested config class " + nestMember.getName());

			setup(dir.isEmpty() ? segment.value() : dir + "." + segment.value(), nestMember);
		}
	}

	private static int clamp(int v, int min, int max) {
		return Math.max(min, Math.min(max, v));
	}

	private static long clamp(long v, long min, long max) {
		return Math.max(min, Math.min(max, v));
	}

	private static double clamp(double v, double min, double max) {
		return Math.max(min, Math.min(max, v));
	}

	private Object parse(String path, JsonElement element) {
		Object def = defaults.get(path);
		if (element == null || element.isJsonNull())
			return def;

		Entry entry = entries.get(path);

		try {
			if (def instanceof Integer) {
				int v = element.getAsInt();
				return entry.hasRange ? clamp(v, (int) entry.min, (int) entry.max) : v;
			}
			if (def instanceof Long) {
				long v = element.getAsLong();
				return entry.hasRange ? clamp(v, (long) entry.min, (long) entry.max) : v;
			}
			if (def instanceof Double) {
				double v = element.getAsDouble();
				return entry.hasRange ? clamp(v, entry.min, entry.max) : v;
			}
			if (def instanceof Boolean)
				return element.getAsBoolean();

			return element.getAsString();
		} catch (Throwable t) {
			net.montoyo.wd.utilities.Log.warningEx("Invalid value for config entry %s, using default.", t, path);
			return def;
		}
	}

	public void load() {
		try {
			if (!Files.exists(file)) {
				save();
				return;
			}

			JsonObject json = JsonParser.parseReader(Files.newBufferedReader(file)).getAsJsonObject();
			for (String path : values.keySet()) {
				JsonElement element = json;
				for (String part : path.split("\\.")) {
					if (element == null || !element.isJsonObject() || !element.getAsJsonObject().has(part)) {
						element = null;
						break;
					}
					element = element.getAsJsonObject().get(part);
				}
				values.put(path, parse(path, element));
			}
		} catch (Throwable t) {
			net.montoyo.wd.utilities.Log.warningEx("Could not read config file %s.", t, file.toString());
		}

		apply();
	}

	public void save() {
		try {
			JsonObject rootJson = new JsonObject();

			for (Map.Entry<String, Object> e : values.entrySet()) {
				JsonObject target = rootJson;
				String[] parts = e.getKey().split("\\.");
				for (int i = 0; i < parts.length - 1; i++) {
					JsonElement child = target.get(parts[i]);
					if (child == null || !child.isJsonObject()) {
						child = new JsonObject();
						target.add(parts[i], child);
					}
					target = child.getAsJsonObject();
				}

				Object value = e.getValue();
				if (value instanceof Integer)
					target.addProperty(parts[parts.length - 1], (Integer) value);
				else if (value instanceof Long)
					target.addProperty(parts[parts.length - 1], (Long) value);
				else if (value instanceof Double)
					target.addProperty(parts[parts.length - 1], (Double) value);
				else if (value instanceof Boolean)
					target.addProperty(parts[parts.length - 1], (Boolean) value);
				else
					target.addProperty(parts[parts.length - 1], String.valueOf(value));
			}

			Files.createDirectories(file.getParent());
			Files.writeString(file, GSON.toJson(rootJson));
		} catch (Throwable t) {
			net.montoyo.wd.utilities.Log.warningEx("Could not write config file %s.", t, file.toString());
		}
	}

	/**
	 * Pushes the stored values into the static fields, then lets the config class recompute its derived values.
	 */
	public void apply() {
		for (Entry entry : entries.values()) {
			Object value = values.get(entry.path);

			if (entry.type.equals(String[].class))
				entry.set(((String) value).split(","));
			else if (value instanceof Double d && entry.type.equals(float.class))
				entry.set(d.floatValue());
			else
				entry.set(value);
		}

		try {
			if (postInit != null)
				postInit.invoke(null);
		} catch (Throwable err) {
			err.printStackTrace();
		}
	}

	public void set(String path, Object value) {
		Entry entry = entries.get(path);
		if (entry == null)
			throw new IllegalArgumentException("Unknown config entry " + path);

		Object stored = value;
		if (entry.type.equals(String[].class))
			stored = String.join(",", (CharSequence[]) value);
		else if (value instanceof Number n) {
			if (entry.type.equals(int.class))
				stored = n.intValue();
			else if (entry.type.equals(long.class))
				stored = n.longValue();
			else if (entry.type.equals(double.class))
				stored = n.doubleValue();
		}

		if (entry.hasRange && stored instanceof Number n) {
			double d = n.doubleValue();
			if (d < entry.min || d > entry.max)
				throw new IllegalArgumentException("Value " + d + " out of range for " + path);
		}

		values.put(path, stored);
		apply();
		save();
	}

	public Object get(String path) {
		return values.get(path);
	}
}
