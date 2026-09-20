package com.example.fpsping;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.Entity;

/**
 * Shared data helpers: reading the player's ping, and sampled values for
 * the extra stat lines (RAM, entities, chunks) so the renderer never has
 * to compute them per-frame.
 */
public final class Stats {
	// Cached samples, refreshed twice a second by the client tick handler.
	public static int ramUsedMb = -1;
	public static int ramMaxMb = -1;
	public static int entityCount = -1;
	public static int chunkCount = -1;

	private Stats() {
	}

	/**
	 * Ping in milliseconds from the player list entry, or null when not on a server.
	 */
	public static Integer getPing(Minecraft minecraft, boolean preview) {
		ClientPacketListener connection = minecraft.getConnection();
		if (connection == null || minecraft.getCurrentServer() == null) {
			return preview ? Integer.valueOf(32) : null; // sample value for the settings preview
		}
		if (minecraft.player != null) {
			PlayerInfo entry = connection.getPlayerInfo(minecraft.player.getUUID());
			if (entry != null) {
				return Math.max(0, entry.getLatency());
			}
		}
		return null;
	}

	/** Called twice a second from the client tick handler. */
	public static void sample(Minecraft minecraft, FpsPingConfig cfg) {
		History.samplePing(getPing(minecraft, false));
		History.sampleFps(minecraft.getFps());

		if (cfg.showRam) {
			Runtime rt = Runtime.getRuntime();
			ramUsedMb = (int) ((rt.totalMemory() - rt.freeMemory()) >> 20);
			ramMaxMb = (int) (rt.maxMemory() >> 20);
		}
		if (cfg.showEntities && minecraft.level != null) {
			int count = 0;
			for (Entity ignored : minecraft.level.entitiesForRendering()) {
				count++;
			}
			entityCount = count;
		}
		if (cfg.showChunks && minecraft.level != null) {
			chunkCount = minecraft.levelRenderer.countRenderedSections();
		}
	}
}
