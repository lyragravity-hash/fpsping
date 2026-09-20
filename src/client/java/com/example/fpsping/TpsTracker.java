package com.example.fpsping;

import net.minecraft.util.Mth;

/**
 * Estimates server TPS from the rate at which the server's gameTime advances
 * (fed by the time-packet mixin). Exponential moving average, clamped to 20.
 */
public final class TpsTracker {
	private static long lastGameTime = -1;
	private static long lastWallClock;
	private static double tps = 20.0;
	private static boolean valid;

	private TpsTracker() {
	}

	public static void onTimeUpdate(long gameTime) {
		long now = System.currentTimeMillis();
		if (lastGameTime >= 0 && gameTime > lastGameTime) {
			double dt = (now - lastWallClock) / 1000.0;
			if (dt > 0.25 && dt < 60.0) {
				double instant = Mth.clamp((gameTime - lastGameTime) / dt, 0.0, 20.0);
				tps = tps * 0.7 + instant * 0.3;
				valid = true;
			}
		}
		lastGameTime = gameTime;
		lastWallClock = now;
	}

	public static void reset() {
		lastGameTime = -1;
		valid = false;
		tps = 20.0;
	}

	public static double tps() {
		return tps;
	}

	public static boolean isValid() {
		return valid;
	}
}
