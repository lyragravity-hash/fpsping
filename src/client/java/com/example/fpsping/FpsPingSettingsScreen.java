package com.example.fpsping;

import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * Settings screen v2: live draggable preview (drag the box anywhere, it snaps
 * to edges/corners), sliders for scale and background opacity, toggle buttons
 * for every line/graph, a text style cycler, custom color pickers (hex), alert
 * thresholds, and per-server profile management. Everything saves instantly.
 */
public final class FpsPingSettingsScreen extends Screen {
	private static final int COL_W = 155;
	private static final int BTN_H = 20;
	private static final int GAP = 4;

	private final Screen parent;
	/** Set while dragging the preview box with the mouse. */
	private boolean dragging;

	public FpsPingSettingsScreen(Screen parent) {
		super(Component.literal("FPS & Ping Monitor"));
		this.parent = parent;
	}

	private static FpsPingConfig config() {
		return FpsPingConfig.active();
	}

	@Override
	protected void init() {
		int left = this.width / 2 - COL_W - GAP / 2;
		int right = this.width / 2 + GAP / 2;
		int top = Math.max(30, this.height / 2 - 100);
		int y = top;

		// ---- Left column ----
		addRenderableWidget(toggle(left, y, "Enabled", config().enabled, v -> config().enabled = v));
		y += BTN_H + GAP;
		addRenderableWidget(Button.builder(themeLabel(), b -> {
			config().theme = config().theme.next();
			config().save();
			b.setMessage(themeLabel());
		}).bounds(left, y, COL_W, BTN_H).build());
		y += BTN_H + GAP;
		addRenderableWidget(new ScaleSlider(left, y, COL_W, BTN_H));
		y += BTN_H + GAP;
		addRenderableWidget(Button.builder(styleLabel(), b -> {
			config().textStyle = (config().textStyle + 1) % 3;
			config().save();
			b.setMessage(styleLabel());
		}).bounds(left, y, COL_W, BTN_H).build());
		y += BTN_H + GAP;
		addRenderableWidget(toggle(left, y, "Compact mode", config().compact, v -> config().compact = v));
		y += BTN_H + GAP;
		addRenderableWidget(toggle(left, y, "Hide with F3", config().hideWithDebug, v -> config().hideWithDebug = v));
		y += BTN_H + GAP;
		addRenderableWidget(Button.builder(customColorsLabel(), b -> {
			config().useCustomColors = !config().useCustomColors;
			config().save();
			b.setMessage(customColorsLabel());
			this.rebuildWidgets();
		}).bounds(left, y, COL_W, BTN_H).build());

		// ---- Right column: line toggles ----
		y = top;
		addRenderableWidget(toggle(right, y, "Show FPS", config().showFps, v -> config().showFps = v));
		y += BTN_H + GAP;
		addRenderableWidget(toggle(right, y, "Show Ping", config().showPing, v -> config().showPing = v));
		y += BTN_H + GAP;
		addRenderableWidget(toggle(right, y, "Ping graph", config().showPingGraph, v -> config().showPingGraph = v));
		y += BTN_H + GAP;
		addRenderableWidget(toggle(right, y, "FPS graph", config().showFpsGraph, v -> config().showFpsGraph = v));
		y += BTN_H + GAP;
		addRenderableWidget(toggle(right, y, "Show TPS", config().showTps, v -> config().showTps = v));
		y += BTN_H + GAP;
		addRenderableWidget(toggle(right, y, "Show RAM", config().showRam, v -> config().showRam = v));
		y += BTN_H + GAP;
		addRenderableWidget(toggle(right, y, "Show entities", config().showEntities, v -> config().showEntities = v));
		y += BTN_H + GAP;
		addRenderableWidget(toggle(right, y, "Show chunks", config().showChunks, v -> config().showChunks = v));

		// ---- Second block: alerts + colors + profiles ----
		int y2 = y + BTN_H + GAP * 3;
		addRenderableWidget(new ThresholdBox(right, y2, COL_W, "Ping alert (ms)", config().pingAlertMs, v -> config().pingAlertMs = v));
		addRenderableWidget(new ThresholdBox(left, y2, COL_W, "FPS alert", config().fpsAlert, v -> config().fpsAlert = v));

		if (config().useCustomColors) {
			int y3 = y2 + BTN_H + GAP;
			addRenderableWidget(new HexBox(left, y3, COL_W, "Background", () -> config().bgColor, v -> config().bgColor = v));
			addRenderableWidget(new HexBox(right, y3, COL_W, "Label", () -> config().labelColor, v -> config().labelColor = v));
			y3 += BTN_H + GAP;
			addRenderableWidget(new HexBox(left, y3, COL_W, "FPS value", () -> config().fpsColor, v -> config().fpsColor = v));
			addRenderableWidget(new HexBox(right, y3, COL_W, "Ping value", () -> config().pingColor, v -> config().pingColor = v));
			y3 += BTN_H + GAP;
			addRenderableWidget(new OpacitySlider(left, y3, COL_W * 2 + GAP, BTN_H));
		}

		// ---- Bottom row ----
		int bottom = this.height - BTN_H - 8;
		if (FpsPingConfig.currentServerIp() != null) {
			if (FpsPingConfig.hasServerProfile()) {
				addRenderableWidget(Button.builder(Component.literal("Delete server profile"), b -> {
					FpsPingConfig.deleteServerProfile();
					this.rebuildWidgets();
				}).bounds(left, bottom, COL_W, BTN_H).build());
			} else {
				addRenderableWidget(Button.builder(Component.literal("Save as server profile"), b -> {
					FpsPingConfig.saveAsServerProfile();
					this.rebuildWidgets();
				}).bounds(left, bottom, COL_W, BTN_H).build());
			}
		}
		if (FpsPingConfig.editingProfile()) {
			addRenderableWidget(Button.builder(Component.literal("Edit global settings"), b -> {
				FpsPingConfig.setEditingProfile(false);
				this.rebuildWidgets();
			}).bounds(right, bottom, COL_W, BTN_H).build());
		} else if (FpsPingConfig.hasServerProfile()) {
			addRenderableWidget(Button.builder(Component.literal("Edit server profile"), b -> {
				FpsPingConfig.setEditingProfile(true);
				this.rebuildWidgets();
			}).bounds(right, bottom, COL_W, BTN_H).build());
		}
		addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
				.bounds(this.width / 2 - COL_W / 2, bottom - BTN_H - GAP, COL_W, BTN_H).build());
	}

	private Button toggle(int x, int y, String label, boolean initial, java.util.function.Consumer<Boolean> setter) {
		return Button.builder(boolLabel(label, initial), b -> {
			setter.accept(!boolState(b));
			config().save();
			b.setMessage(boolLabel(label, boolState(b)));
		}).bounds(x, y, COL_W, BTN_H).build();
	}

	/** Button messages carry the state; parse it back out of the label. */
	private static boolean boolState(Button b) {
		return b.getMessage().getString().endsWith("ON");
	}

	private static Component boolLabel(String label, boolean value) {
		return Component.literal(label + ": " + (value ? "ON" : "OFF"));
	}

	private static Component themeLabel() {
		return Component.literal("Theme: " + config().theme.label());
	}

	private static Component styleLabel() {
		return Component.literal("Text: " + switch (config().textStyle) {
			case HudOverlay.STYLE_OUTLINE -> "Outline";
			case HudOverlay.STYLE_PLAIN -> "Plain";
			default -> "Shadow";
		});
	}

	private static Component customColorsLabel() {
		return Component.literal("Custom colors: " + (config().useCustomColors ? "ON" : "OFF"));
	}

	// ---- Drag handling for the preview box ----

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
		HudOverlay.Layout box = HudOverlay.lastLayout();
		if (box != null && event.x() >= box.x() && event.x() <= box.x() + box.width()
				&& event.y() >= box.y() && event.y() <= box.y() + box.height()) {
			this.dragging = true;
			return true;
		}
		return super.mouseClicked(event, doubled);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		if (this.dragging && event.x() > 0) {
			FpsPingConfig cfg = config();
			cfg.posX = Mth.clamp((float) (event.x() / this.width), 0.0f, 1.0f);
			cfg.posY = Mth.clamp((float) (event.y() / this.height), 0.0f, 1.0f);
			return true;
		}
		return super.mouseDragged(event, dragX, dragY);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (this.dragging) {
			this.dragging = false;
			snapToEdges(config());
			config().save();
			return true;
		}
		return super.mouseReleased(event);
	}

	/** Snaps posX/posY to the nearest edge/corner when the box is close to one. */
	private static void snapToEdges(FpsPingConfig cfg) {
		HudOverlay.Layout box = HudOverlay.lastLayout();
		if (box == null) {
			return;
		}
		float snapDist = 16.0f / Math.max(cfg.scale, 0.5f);
		float px = cfg.posX, py = cfg.posY;
		if (Math.abs(px) < snapDist) {
			px = 0.0f;
		} else if (Math.abs(px - 1.0f) < snapDist) {
			px = 1.0f;
		}
		if (Math.abs(py) < snapDist) {
			py = 0.0f;
		} else if (Math.abs(py - 1.0f) < snapDist) {
			py = 1.0f;
		}
		cfg.posX = px;
		cfg.posY = py;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.render(graphics, mouseX, mouseY, partialTick);
		graphics.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFFFFF);
		graphics.drawCenteredString(this.font, "Drag the box to move it", this.width / 2,
				this.height - BTN_H * 2 - 22, 0xFFAAAAAA);
		// Live preview of the overlay with the current settings.
		HudOverlay.render(graphics, Minecraft.getInstance(), true);
	}

	@Override
	public void onClose() {
		config().save();
		if (this.minecraft != null) {
			this.minecraft.setScreen(this.parent);
		} else {
			super.onClose();
		}
	}

	// ---- Custom widgets ----

	/** 50% - 200% scale slider. */
	private final class ScaleSlider extends AbstractSliderButton {
		private ScaleSlider(int x, int y, int w, int h) {
			super(x, y, w, h, Component.empty(), (Mth.clamp(config().scale, 0.5f, 2.0f) - 0.5) / 1.5);
			updateMessage();
		}

		@Override
		protected void updateMessage() {
			setMessage(Component.literal("Scale: " + Math.round(config().scale * 100.0f) + "%"));
		}

		@Override
		protected void applyValue() {
			config().scale = Math.round((0.5 + this.value * 1.5) * 20.0) / 20.0f;
			config().save();
		}
	}

	/** Background opacity slider (only shown with custom colors). */
	private final class OpacitySlider extends AbstractSliderButton {
		private OpacitySlider(int x, int y, int w, int h) {
			super(x, y, w, h, Component.empty(), Mth.clamp(config().bgOpacity, 0.0f, 1.0f));
			updateMessage();
		}

		@Override
		protected void updateMessage() {
			setMessage(Component.literal("Background opacity: " + Math.round(config().bgOpacity * 100.0f) + "%"));
		}

		@Override
		protected void applyValue() {
			config().bgOpacity = Math.round(this.value * 20.0) / 20.0f;
			config().save();
		}
	}

	/** Number box for alert thresholds; 0 = disabled. */
	private final class ThresholdBox extends EditBox {
		private final java.util.function.Consumer<Integer> setter;

		private ThresholdBox(int x, int y, int w, String label, int initial, java.util.function.Consumer<Integer> setter) {
			super(FpsPingSettingsScreen.this.font, x, y, w - 34, BTN_H, Component.literal(label));
			this.setter = setter;
			setMaxLength(5);
			setFilter(s -> s.isEmpty() || s.chars().allMatch(c -> c >= '0' && c <= '9'));
			setValue(initial > 0 ? String.valueOf(initial) : "0");
			setHint(Component.literal("0 = off"));
			setResponder(s -> {
				try {
					setter.accept(Math.min(99999, Integer.parseInt(s.isEmpty() ? "0" : s)));
				} catch (NumberFormatException ignored) {
				}
				config().save();
			});
		}

		@Override
		public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
			super.renderWidget(graphics, mouseX, mouseY, partialTick);
			graphics.drawString(FpsPingSettingsScreen.this.font, this.getMessage().getString(),
					getX() - 1, getY() - 10, 0xFFAAAAAA);
		}
	}

	/** "RRGGBB" hex color box. */
	private final class HexBox extends EditBox {
		private final java.util.function.Supplier<Integer> getter;
		private final java.util.function.Consumer<Integer> setter;
		private boolean valid = true;

		private HexBox(int x, int y, int w, String label, java.util.function.Supplier<Integer> getter,
				java.util.function.Consumer<Integer> setter) {
			super(FpsPingSettingsScreen.this.font, x, y, w - 70, BTN_H, Component.literal(label));
			this.getter = getter;
			this.setter = setter;
			setMaxLength(6);
			setFilter(s -> s.chars().allMatch(c -> (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')));
			setValue(String.format("%06X", getter.get() & 0xFFFFFF));
			setHint(Component.literal("RRGGBB"));
			setResponder(s -> {
				this.valid = s.length() == 6;
				if (this.valid) {
					try {
						setter.accept((int) Long.parseLong(s.toLowerCase(Locale.ROOT), 16) | 0xFF000000);
						config().save();
					} catch (NumberFormatException ignored) {
					}
				}
			});
		}

		@Override
		public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
			super.renderWidget(graphics, mouseX, mouseY, partialTick);
			// Label + a swatch of the current color.
			graphics.drawString(FpsPingSettingsScreen.this.font, this.getMessage().getString(),
					getX() - 1, getY() - 10, 0xFFAAAAAA);
			int swatchX = getX() + this.width + 4;
			graphics.fill(swatchX, getY(), swatchX + BTN_H, getY() + BTN_H, 0xFF000000 | (this.getter.get() & 0xFFFFFF));
			graphics.fill(swatchX, getY(), swatchX + BTN_H, getY() + 1, 0xFF555555);
			graphics.fill(swatchX, getY() + BTN_H - 1, swatchX + BTN_H, getY() + BTN_H, 0xFF555555);
			graphics.fill(swatchX, getY(), swatchX + 1, getY() + BTN_H, 0xFF555555);
			graphics.fill(swatchX + BTN_H - 1, getY(), swatchX + BTN_H, getY() + BTN_H, 0xFF555555);
		}
	}
}
