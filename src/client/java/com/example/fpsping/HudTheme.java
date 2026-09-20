package com.example.fpsping;

/**
 * Color theme presets for the overlay. All colors are ARGB. When
 * "custom colors" is on, the config's own colors override these.
 */
public enum HudTheme {
	DARK("Dark", 0xB4000000, 0xFFAAAAAA, 0xFFFFFFFF, false),
	LIGHT("Light", 0xC0E8E8E8, 0xFF555555, 0xFF101010, false),
	TRANSPARENT("Transparent", 0x00000000, 0xFFAAAAAA, 0xFFFFFFFF, false),
	RAINBOW("Rainbow", 0x90000000, 0xFFAAAAAA, 0xFFFFFFFF, true);

	private final String label;
	private final int background;
	private final int labelColor;
	private final int valueColor;
	private final boolean rainbow;

	HudTheme(String label, int background, int labelColor, int valueColor, boolean rainbow) {
		this.label = label;
		this.background = background;
		this.labelColor = labelColor;
		this.valueColor = valueColor;
		this.rainbow = rainbow;
	}

	/** Human readable name shown on the settings button. */
	public String label() {
		return this.label;
	}

	/** Background box color, or 0 for no box. */
	public int background() {
		return this.background;
	}

	/** Color of the "FPS:" / "Ping:" labels. */
	public int labelColor() {
		return this.labelColor;
	}

	/** Default color of the values. */
	public int valueColor() {
		return this.valueColor;
	}

	/** Whether value colors cycle through the rainbow over time. */
	public boolean rainbow() {
		return this.rainbow;
	}

	public HudTheme next() {
		HudTheme[] values = values();
		return values[(this.ordinal() + 1) % values.length];
	}
}
