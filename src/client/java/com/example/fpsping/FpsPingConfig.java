package com.example.fpsping;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * All display settings. A single "global" instance is used everywhere, except
 * on servers that have a profile: then the profile instance is the active one.
 * Everything persists to config/fpsping.json.
 */
public final class FpsPingConfig {
	public static final Logger LOGGER = LoggerFactory.getLogger("fpsping");

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("fpsping.json");

	// ---- Toggle & layout ----
	public boolean enabled = true;
	public boolean compact = false;
	public float scale = 1.0f;
	/** Normalized top-left position of the box (1.0 / 0.0 = top right). */
	public float posX = 1.0f;
	public float posY = 0.0f;
	/** 0 = shadow, 1 = outline, 2 = plain. */
	public int textStyle = 0;

	// ---- Lines & graphs ----
	public boolean showFps = true;
	public boolean showPing = true;
	public boolean showPingGraph = false;
	public boolean showFpsGraph = false;
	public boolean showRam = false;
	public boolean showEntities = false;
	public boolean showChunks = false;
	public boolean showTps = false;

	// ---- Look ----
	public HudTheme theme = HudTheme.DARK;
	public boolean useCustomColors = false;
	/** Base background color; alpha comes from bgOpacity. */
	public int bgColor = 0x000000;
	public float bgOpacity = 0.7f;
	public int labelColor = 0xFFAAAAAA;
	public int fpsColor = 0xFFFFFFFF;
	public int pingColor = 0xFF55FF55;

	// ---- Alerts ----
	/** 0 disables. */
	public int pingAlertMs = 0;
	/** 0 disables. */
	public int fpsAlert = 0;
	public boolean alertSound = true;

	// ---- Behavior ----
	public boolean hideWithDebug = true;

	// ---- Instance management ----
	private static FpsPingConfig global = new FpsPingConfig();
	private static final Map<String, FpsPingConfig> PROFILES = new HashMap<>();
	private static FpsPingConfig serverProfile;
	private static boolean useProfile;
	private static String currentServerIp;

	/** The instance the HUD renders with and the settings screen edits. */
	public static FpsPingConfig active() {
		return useProfile && serverProfile != null ? serverProfile : global;
	}

	public static boolean editingProfile() {
		return useProfile && serverProfile != null;
	}

	public static boolean hasServerProfile() {
		return serverProfile != null;
	}

	public static void setEditingProfile(boolean value) {
		useProfile = value && serverProfile != null;
	}

	public static void onJoinServer(String ip) {
		currentServerIp = ip;
		serverProfile = ip != null ? PROFILES.get(ip) : null;
		useProfile = serverProfile != null;
	}

	public static void onLeaveServer() {
		currentServerIp = null;
		serverProfile = null;
		useProfile = false;
	}

	public static String currentServerIp() {
		return currentServerIp;
	}

	/** Copies the currently displayed look into a profile for this server. */
	public static void saveAsServerProfile() {
		if (currentServerIp == null) {
			return;
		}
		FpsPingConfig copy = active().copy();
		PROFILES.put(currentServerIp, copy);
		serverProfile = copy;
		useProfile = true;
		saveAll();
	}

	public static void deleteServerProfile() {
		if (currentServerIp != null) {
			PROFILES.remove(currentServerIp);
		}
		serverProfile = null;
		useProfile = false;
		saveAll();
	}

	public FpsPingConfig copy() {
		FpsPingConfig c = new FpsPingConfig();
		c.copyFrom(this);
		return c;
	}

	private void copyFrom(FpsPingConfig c) {
		c.enabled = enabled;
		c.compact = compact;
		c.scale = scale;
		c.posX = posX;
		c.posY = posY;
		c.textStyle = textStyle;
		c.showFps = showFps;
		c.showPing = showPing;
		c.showPingGraph = showPingGraph;
		c.showFpsGraph = showFpsGraph;
		c.showRam = showRam;
		c.showEntities = showEntities;
		c.showChunks = showChunks;
		c.showTps = showTps;
		c.theme = theme;
		c.useCustomColors = useCustomColors;
		c.bgColor = bgColor;
		c.bgOpacity = bgOpacity;
		c.labelColor = labelColor;
		c.fpsColor = fpsColor;
		c.pingColor = pingColor;
		c.pingAlertMs = pingAlertMs;
		c.fpsAlert = fpsAlert;
		c.alertSound = alertSound;
		c.hideWithDebug = hideWithDebug;
	}

	/** Restores every option to its fresh-install default (position → top right). */
	public void resetToDefaults() {
		copyFrom(new FpsPingConfig());
	}

	// ---- Persistence ----

	public static void load() {
		global = new FpsPingConfig();
		PROFILES.clear();
		try {
			if (!Files.exists(PATH)) {
				return;
			}
			JsonObject root = GSON.fromJson(Files.readString(PATH), JsonObject.class);
			if (root == null) {
				return;
			}
			if (root.has("global")) {
				global.fromJson(root.getAsJsonObject("global"));
			}
			if (root.has("profiles") && root.get("profiles").isJsonObject()) {
				for (Map.Entry<String, JsonElement> e : root.getAsJsonObject("profiles").entrySet()) {
					if (e.getValue().isJsonObject()) {
						FpsPingConfig p = new FpsPingConfig();
						p.fromJson(e.getValue().getAsJsonObject());
						PROFILES.put(e.getKey(), p);
					}
				}
			}
		} catch (Exception e) {
			LOGGER.warn("Could not read {}, using defaults", PATH, e);
		}
	}

	/** Writes everything; called after any settings change. */
	public static void save() {
		saveAll();
	}

	private static void saveAll() {
		try {
			Files.createDirectories(PATH.getParent());
			JsonObject root = new JsonObject();
			root.add("global", global.toJson());
			JsonObject profiles = new JsonObject();
			for (Map.Entry<String, FpsPingConfig> e : PROFILES.entrySet()) {
				profiles.add(e.getKey(), e.getValue().toJson());
			}
			root.add("profiles", profiles);
			Files.writeString(PATH, GSON.toJson(root));
		} catch (IOException e) {
			LOGGER.warn("Could not write {}", PATH, e);
		}
	}

	private JsonObject toJson() {
		JsonObject o = new JsonObject();
		o.addProperty("enabled", enabled);
		o.addProperty("compact", compact);
		o.addProperty("scale", scale);
		o.addProperty("posX", posX);
		o.addProperty("posY", posY);
		o.addProperty("textStyle", textStyle);
		o.addProperty("showFps", showFps);
		o.addProperty("showPing", showPing);
		o.addProperty("showPingGraph", showPingGraph);
		o.addProperty("showFpsGraph", showFpsGraph);
		o.addProperty("showRam", showRam);
		o.addProperty("showEntities", showEntities);
		o.addProperty("showChunks", showChunks);
		o.addProperty("showTps", showTps);
		o.addProperty("theme", theme.name());
		o.addProperty("useCustomColors", useCustomColors);
		o.addProperty("bgColor", bgColor);
		o.addProperty("bgOpacity", bgOpacity);
		o.addProperty("labelColor", labelColor);
		o.addProperty("fpsColor", fpsColor);
		o.addProperty("pingColor", pingColor);
		o.addProperty("pingAlertMs", pingAlertMs);
		o.addProperty("fpsAlert", fpsAlert);
		o.addProperty("alertSound", alertSound);
		o.addProperty("hideWithDebug", hideWithDebug);
		return o;
	}

	private void fromJson(JsonObject o) {
		if (o.has("enabled")) enabled = o.get("enabled").getAsBoolean();
		if (o.has("compact")) compact = o.get("compact").getAsBoolean();
		if (o.has("scale")) scale = o.get("scale").getAsFloat();
		if (o.has("posX")) posX = o.get("posX").getAsFloat();
		if (o.has("posY")) posY = o.get("posY").getAsFloat();
		if (o.has("textStyle")) textStyle = o.get("textStyle").getAsInt();
		if (o.has("showFps")) showFps = o.get("showFps").getAsBoolean();
		if (o.has("showPing")) showPing = o.get("showPing").getAsBoolean();
		if (o.has("showPingGraph")) showPingGraph = o.get("showPingGraph").getAsBoolean();
		if (o.has("showFpsGraph")) showFpsGraph = o.get("showFpsGraph").getAsBoolean();
		if (o.has("showRam")) showRam = o.get("showRam").getAsBoolean();
		if (o.has("showEntities")) showEntities = o.get("showEntities").getAsBoolean();
		if (o.has("showChunks")) showChunks = o.get("showChunks").getAsBoolean();
		if (o.has("showTps")) showTps = o.get("showTps").getAsBoolean();
		if (o.has("theme")) {
			try {
				theme = HudTheme.valueOf(o.get("theme").getAsString().toUpperCase());
			} catch (IllegalArgumentException e) {
				LOGGER.warn("Unknown theme in config, falling back to DARK");
			}
		}
		if (o.has("useCustomColors")) useCustomColors = o.get("useCustomColors").getAsBoolean();
		if (o.has("bgColor")) bgColor = o.get("bgColor").getAsInt();
		if (o.has("bgOpacity")) bgOpacity = o.get("bgOpacity").getAsFloat();
		if (o.has("labelColor")) labelColor = o.get("labelColor").getAsInt();
		if (o.has("fpsColor")) fpsColor = o.get("fpsColor").getAsInt();
		if (o.has("pingColor")) pingColor = o.get("pingColor").getAsInt();
		if (o.has("pingAlertMs")) pingAlertMs = o.get("pingAlertMs").getAsInt();
		if (o.has("fpsAlert")) fpsAlert = o.get("fpsAlert").getAsInt();
		if (o.has("alertSound")) alertSound = o.get("alertSound").getAsBoolean();
		if (o.has("hideWithDebug")) hideWithDebug = o.get("hideWithDebug").getAsBoolean();
	}
}
