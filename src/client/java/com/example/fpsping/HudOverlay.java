package com.example.fpsping;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix3x2fStack;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * The FPS & ping overlay. Supports: free position (drag in the settings
 * preview, snaps to edges/corners), compact one-line mode, per-line toggles,
 * ping/FPS sparklines, extra stat lines (TPS, RAM, entities, chunks), theme
 * presets or fully custom colors, and shadow/outline/plain text styles.
 */
public final class HudOverlay implements net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement {
	public static final Identifier ID = Identifier.fromNamespaceAndPath(FpsPingModClient.MOD_ID, "fps_ping");

	public static final int MARGIN = 4;
	private static final int PADDING = 4;
	private static final int LINE_SPACING = 2;
	private static final int GRAPH_HEIGHT = 12;
	private static final int GRAPH_MIN_WIDTH = 60;

	/** Hit box in real (GUI-scaled) pixels, for dragging in the settings preview. */
	public record Layout(int x, int y, int width, int height) {}

	private static volatile Layout lastLayout = null;

	/** One draw segment of a line (a line is a list of segments, right-aligned). */
	private record Seg(String text, int color) {}

	public static final int STYLE_SHADOW = 0;
	public static final int STYLE_OUTLINE = 1;
	public static final int STYLE_PLAIN = 2;

	private static final int PING_GOOD = 0xFF55FF55;
	private static final int PING_OKAY = 0xFFFFFF55;
	private static final int PING_BAD = 0xFFFF5555;
	private static final int PING_AWFUL = 0xFFAA0000;

	@Override
	public void render(GuiGraphics graphics, DeltaTracker tickCounter) {
		Minecraft minecraft = Minecraft.getInstance();
		FpsPingConfig cfg = FpsPingConfig.active();
		if (minecraft.player == null || !cfg.enabled || cfg.hiddenByHotkey) {
			lastLayout = null;
			return;
		}
		if (cfg.hideWithDebug && minecraft.getDebugOverlay().showDebugScreen()) {
			lastLayout = null;
			return;
		}
		if (cfg.hideInMenus && minecraft.screen != null) {
			lastLayout = null;
			return;
		}
		render(graphics, minecraft, false);
	}

	/** Draws the overlay; also used as the live preview on the settings screen. */
	public static void render(GuiGraphics graphics, Minecraft minecraft, boolean preview) {
		FpsPingConfig cfg = FpsPingConfig.active();
		Font font = minecraft.font;
		HudTheme theme = cfg.theme;

		// ---- Resolve colors ----
		int bg = cfg.useCustomColors
				? ((Math.round(Mth.clamp(cfg.bgOpacity, 0.0f, 1.0f) * 255.0f) << 24) | (cfg.bgColor & 0xFFFFFF))
				: theme.background();
		int labelColor = cfg.useCustomColors ? cfg.labelColor : theme.labelColor();
		int rainbow = rainbowColor();
		int fpsColor = cfg.useCustomColors ? cfg.fpsColor : theme.rainbow() ? rainbow : theme.valueColor();
		Integer ping = Stats.getPing(minecraft, preview);
		int pingColor = cfg.useCustomColors ? cfg.pingColor : theme.rainbow() ? rainbow : pingColor(ping == null ? -1 : ping);

		// ---- Gather lines ----
		List<List<Seg>> lines = new ArrayList<>();
		int fps = minecraft.getFps();
		if (cfg.compact) {
			List<Seg> line = new ArrayList<>();
			if (cfg.showFps) {
				line.add(new Seg(String.valueOf(fps), fpsColor));
				line.add(new Seg(" fps", labelColor));
			}
			if (cfg.showPing && ping != null) {
				if (!line.isEmpty()) {
					line.add(new Seg(" · ", labelColor));
				}
				line.add(new Seg(String.valueOf(ping), pingColor));
				line.add(new Seg(" ms", labelColor));
			}
			if (!line.isEmpty()) {
				lines.add(line);
			}
		} else {
			if (cfg.showFps) {
				lines.add(List.of(new Seg(label(cfg.labelFps, "FPS") + ": ", labelColor), new Seg(String.valueOf(fps), fpsColor)));
			}
			if (cfg.showPing && ping != null) {
				lines.add(List.of(new Seg(label(cfg.labelPing, "Ping") + ": ", labelColor), new Seg(ping + " ms", pingColor)));
			}
			if (cfg.showFpsRange && History.hasFpsSession()) {
				lines.add(List.of(new Seg(label(cfg.labelFps, "FPS") + " min/max: ", labelColor),
						new Seg(History.sessionFpsMin() + " / " + History.sessionFpsMax(), fpsColor)));
			}
			if (cfg.showTps && minecraft.getCurrentServer() != null && TpsTracker.isValid()) {
				double tps = TpsTracker.tps();
				lines.add(List.of(new Seg("TPS: ", labelColor), new Seg(String.format("%.1f", tps), tpsColor(tps))));
			}
			if (cfg.showRam && Stats.ramUsedMb >= 0) {
				lines.add(List.of(new Seg("RAM: ", labelColor), new Seg(Stats.ramUsedMb + " MB", fpsColor)));
			}
			if (cfg.showEntities && Stats.entityCount >= 0) {
				lines.add(List.of(new Seg("Entities: ", labelColor), new Seg(String.valueOf(Stats.entityCount), fpsColor)));
			}
			if (cfg.showChunks && Stats.chunkCount >= 0) {
				lines.add(List.of(new Seg("Chunks: ", labelColor), new Seg(String.valueOf(Stats.chunkCount), fpsColor)));
			}
		}

		int graphCount = (cfg.showPingGraph && History.pingCount() > 0 ? 1 : 0)
				+ (cfg.showFpsGraph && History.fpsCount() > 0 ? 1 : 0);
		if (lines.isEmpty() && graphCount == 0) {
			lastLayout = null;
			return;
		}

		// ---- Measure ----
		int lineHeight = font.lineHeight + LINE_SPACING;
		int lineW = 0;
		for (List<Seg> line : lines) {
			int w = 0;
			for (Seg seg : line) {
				w += font.width(seg.text());
			}
			lineW = Math.max(lineW, w);
		}
		int boxWidth = Math.max(lineW + PADDING * 2, GRAPH_MIN_WIDTH + PADDING * 2);
		int boxHeight = lines.size() * lineHeight + graphCount * (GRAPH_HEIGHT + LINE_SPACING) + PADDING * 2 - LINE_SPACING;

		// ---- Position (box top-left, in overlay-scaled space) ----
		float scale = Mth.clamp(cfg.scale, 0.5f, 2.0f);
		float overlaySpaceWidth = graphics.guiWidth() / scale;
		float overlaySpaceHeight = graphics.guiHeight() / scale;
		int x = Math.round(cfg.posX * overlaySpaceWidth) - boxWidth;
		int y = Math.round(cfg.posY * overlaySpaceHeight);
		x = Mth.clamp(x, MARGIN, Math.max(MARGIN, (int) overlaySpaceWidth - boxWidth - MARGIN));
		y = Mth.clamp(y, MARGIN, Math.max(MARGIN, (int) overlaySpaceHeight - boxHeight - MARGIN));

		// Hit box in real gui pixels for the settings-screen drag.
		lastLayout = new Layout(Math.round(x * scale), Math.round(y * scale),
				Math.round(boxWidth * scale), Math.round(boxHeight * scale));

		// ---- Draw ----
		Matrix3x2fStack matrices = graphics.pose();
		matrices.pushMatrix();
		matrices.translate(x, y);
		matrices.scale(scale, scale);

		if (bg != 0) {
			graphics.fill(0, 0, boxWidth, boxHeight, bg);
		}

		int style = cfg.textStyle;
		int textY = PADDING;
		for (List<Seg> line : lines) {
			int segX = boxWidth - PADDING;
			for (int i = line.size() - 1; i >= 0; i--) {
				Seg seg = line.get(i);
				int w = font.width(seg.text());
				segX -= w;
				drawText(graphics, font, seg.text(), segX, textY, seg.color(), style);
			}
			textY += lineHeight;
		}

		int graphX = boxWidth - PADDING - (boxWidth - PADDING * 2);
		int graphW = boxWidth - PADDING * 2;
		if (cfg.showPingGraph && History.pingCount() > 0) {
			textY += LINE_SPACING;
			drawBars(graphics, graphX, textY, graphW, GRAPH_HEIGHT, History.pingCount(), History::pingAt,
					History.pingMax(), v -> cfg.useCustomColors ? cfg.pingColor : pingColor(v));
			textY += GRAPH_HEIGHT;
		}
		if (cfg.showFpsGraph && History.fpsCount() > 0) {
			textY += LINE_SPACING;
			drawBars(graphics, graphX, textY, graphW, GRAPH_HEIGHT, History.fpsCount(), History::fpsAt,
					History.fpsMax(), v -> fpsColor);
			textY += GRAPH_HEIGHT;
		}

		matrices.popMatrix();
	}

	private interface SampleSource {
		int at(int index);
	}

	private interface ColorSource {
		int color(int sample);
	}

	/** Draws a sparkline of samples; most recent sample at the right. */
	private static void drawBars(GuiGraphics graphics, int x, int y, int width, int height,
			int sampleCount, SampleSource samples, int maxSample, ColorSource colors) {
		int usable = Math.min(sampleCount, width);
		int barW = Math.max(1, width / History.CAPACITY);
		int max = Math.max(1, maxSample);
		for (int i = 0; i < usable; i++) {
			int sample = samples.at(sampleCount - usable + i);
			int barH = Math.max(1, Math.round(sample / (float) max * (height - 1)));
			int bx = x + width - (usable - i) * barW;
			graphics.fill(bx, y + height - barH, bx + barW, y + height, colors.color(sample));
		}
	}

	private static void drawText(GuiGraphics graphics, Font font, String text, int x, int y, int color, int style) {
		switch (style) {
			case STYLE_OUTLINE -> {
				graphics.drawString(font, text, x - 1, y, 0xFF000000, false);
				graphics.drawString(font, text, x + 1, y, 0xFF000000, false);
				graphics.drawString(font, text, x, y - 1, 0xFF000000, false);
				graphics.drawString(font, text, x, y + 1, 0xFF000000, false);
				graphics.drawString(font, text, x, y, color, false);
			}
			case STYLE_PLAIN -> graphics.drawString(font, text, x, y, color, false);
			default -> graphics.drawString(font, text, x, y, color, true);
		}
	}

	/** Custom label if set, otherwise the English default. */
	private static String label(String custom, String fallback) {
		return custom == null || custom.isBlank() ? fallback : custom;
	}

	private static int pingColor(int ping) {
		if (ping < 0 || ping < 80) {
			return PING_GOOD;
		}
		if (ping < 150) {
			return PING_OKAY;
		}
		if (ping < 300) {
			return PING_BAD;
		}
		return PING_AWFUL;
	}

	private static int tpsColor(double tps) {
		if (tps >= 19.0) {
			return PING_GOOD;
		}
		if (tps >= 15.0) {
			return PING_OKAY;
		}
		if (tps >= 10.0) {
			return PING_BAD;
		}
		return PING_AWFUL;
	}

	private static int rainbowColor() {
		float hue = (System.currentTimeMillis() % 2000L) / 2000.0f;
		return 0xFF000000 | Mth.hsvToRgb(hue, 0.85f, 1.0f);
	}

	public static Layout lastLayout() {
		return lastLayout;
	}
}
