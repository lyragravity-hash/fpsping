package com.example.fpsping;

/**
 * Ring buffers of recent FPS / ping samples (one sample every 500 ms,
 * so 60 samples = 30 seconds of history for the sparklines).
 */
public final class History {
	public static final int CAPACITY = 60;

	private static final int[] ping = new int[CAPACITY];
	private static final int[] fps = new int[CAPACITY];
	private static int pingCount;
	private static int fpsCount;
	/** Session extremes (not just the ring buffer window). */
	private static int sessionFpsMin = -1;
	private static int sessionFpsMax = -1;

	private History() {
	}

	public static void samplePing(Integer value) {
		if (value == null) {
			return;
		}
		insert(ping, pingCount, Math.max(0, value));
		pingCount = Math.min(pingCount + 1, CAPACITY);
	}

	public static void sampleFps(int value) {
		int v = Math.max(0, value);
		insert(fps, fpsCount, v);
		fpsCount = Math.min(fpsCount + 1, CAPACITY);
		if (sessionFpsMin < 0 || v < sessionFpsMin) {
			sessionFpsMin = v;
		}
		if (v > sessionFpsMax) {
			sessionFpsMax = v;
		}
	}

	public static int sessionFpsMin() {
		return Math.max(0, sessionFpsMin);
	}

	public static int sessionFpsMax() {
		return Math.max(0, sessionFpsMax);
	}

	public static boolean hasFpsSession() {
		return sessionFpsMin >= 0;
	}

	private static void insert(int[] array, int count, int value) {
		if (count < CAPACITY) {
			array[count] = value;
		} else {
			System.arraycopy(array, 1, array, 0, CAPACITY - 1);
			array[CAPACITY - 1] = value;
		}
	}

	public static int pingCount() {
		return pingCount;
	}

	public static int pingAt(int index) {
		return ping[index];
	}

	public static int fpsCount() {
		return fpsCount;
	}

	public static int fpsAt(int index) {
		return fps[index];
	}

	public static int pingMax() {
		return max(ping, pingCount, 100);
	}

	/** Autoscaled: 30 floor, rounded up to a multiple of 30 so bars stay readable. */
	public static int fpsMax() {
		int peak = max(fps, fpsCount, 0);
		return Math.max(30, ((peak + 29) / 30) * 30);
	}

	private static int max(int[] array, int count, int floor) {
		int m = floor;
		for (int i = 0; i < count; i++) {
			m = Math.max(m, array[i]);
		}
		return m;
	}

	public static void reset() {
		pingCount = 0;
		fpsCount = 0;
		sessionFpsMin = -1;
		sessionFpsMax = -1;
	}
}
