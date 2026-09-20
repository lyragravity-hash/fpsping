package com.example.fpsping;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * Checks Modrinth once per game launch for a newer release and says so in
 * chat. Uses the public API, no token, and never blocks the game thread.
 */
public final class UpdateChecker {
	private static final String PROJECT_URL = "https://api.modrinth.com/v2/project/fps-ping-monitor/version";
	private static volatile String newerVersion;

	private UpdateChecker() {
	}

	/** Fire-and-forget; call once from the client entrypoint. */
	public static void check() {
		String current = FabricLoader.getInstance().getModContainer(FpsPingModClient.MOD_ID)
				.map(c -> c.getMetadata().getVersion().getFriendlyString())
				.orElse(null);
		if (current == null) {
			return;
		}
		Thread thread = new Thread(() -> fetch(current), "fpsping-update-check");
		thread.setDaemon(true);
		thread.start();
	}

	private static void fetch(String current) {
		try {
			HttpClient client = HttpClient.newBuilder()
					.connectTimeout(Duration.ofSeconds(10))
					.build();
			HttpRequest request = HttpRequest.newBuilder(URI.create(PROJECT_URL))
					.header("User-Agent", "lyragravity-hash/fpsping")
					.timeout(Duration.ofSeconds(10))
					.GET()
					.build();
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() != 200) {
				return;
			}
			JsonArray versions = JsonParser.parseString(response.body()).getAsJsonArray();
			if (!versions.isEmpty()) {
				JsonObject latest = versions.get(0).getAsJsonObject();
				if (isReleaseChannel(latest)) {
					String version = latest.get("version_number").getAsString();
					if (isNewer(version, current)) {
						newerVersion = version;
					}
				}
			}
		} catch (Exception ignored) {
			// Offline or Modrinth unreachable: never a problem.
		}
	}

	private static boolean isReleaseChannel(JsonObject version) {
		String type = version.has("version_type") && version.get("version_type").isJsonPrimitive()
				? version.get("version_type").getAsString()
				: "release";
		return !"beta".equals(type) && !"alpha".equals(type);
	}

	/** Simple three-part semantic comparison; equal or older returns false. */
	private static boolean isNewer(String candidate, String current) {
		try {
			String[] c = candidate.replaceAll("[^0-9.]", "").split("\\.");
			String[] cur = current.replaceAll("[^0-9.]", "").split("\\.");
			for (int i = 0; i < Math.max(c.length, cur.length); i++) {
				int a = i < c.length ? Integer.parseInt(c[i]) : 0;
				int b = i < cur.length ? Integer.parseInt(cur[i]) : 0;
				if (a != b) {
					return a > b;
				}
			}
			return false;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/** Called once per second from the tick handler; shows the notice once. */
	public static void tickNotice(Minecraft minecraft) {
		String version = newerVersion;
		if (version != null && minecraft.player != null) {
			newerVersion = null;
			minecraft.player.displayClientMessage(
					Component.translatable("fpsping.update", version), false);
		}
	}

	public static String newerVersion() {
		return newerVersion;
	}
}
