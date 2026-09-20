package com.example.fpsping;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

/**
 * Threshold alerts: shows an action bar message (and optionally a sound)
 * when ping spikes or FPS drops, with a cooldown so it does not spam.
 */
public final class Alerts {
	private static final long COOLDOWN_MS = 10_000;
	private static long lastPingAlert;
	private static long lastFpsAlert;

	private Alerts() {
	}

	public static void check(Minecraft minecraft, FpsPingConfig cfg) {
		if (minecraft.player == null) {
			return;
		}
		long now = System.currentTimeMillis();

		if (cfg.pingAlertMs > 0) {
			Integer ping = Stats.getPing(minecraft, false);
			if (ping != null && ping >= cfg.pingAlertMs && now - lastPingAlert > COOLDOWN_MS) {
				lastPingAlert = now;
				warn(minecraft, Component.translatable("fpsping.alert.ping", ping));
			}
		}
		if (cfg.fpsAlert > 0) {
			int fps = minecraft.getFps();
			if (fps <= cfg.fpsAlert && now - lastFpsAlert > COOLDOWN_MS) {
				lastFpsAlert = now;
				warn(minecraft, Component.translatable("fpsping.alert.fps", fps));
			}
		}
	}

	private static void warn(Minecraft minecraft, Component message) {
		minecraft.player.displayClientMessage(message, true);
		if (FpsPingConfig.active().alertSound) {
			minecraft.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
		}
	}
}
