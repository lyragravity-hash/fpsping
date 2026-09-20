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
 * Settings screen v3: tabbed (Display / Stats / Alerts / Colors / Profile) so
 * everything fits even at high GUI scales, with a live draggable preview of
 * the overlay. Includes an alert-sound toggle and a two-click "Reset to
 * defaults". Everything saves instantly.
 */
public final class FpsPingSettingsScreen extends Screen {
	private static final int COL_W = 155;
	private static final int BTN_H = 20;
	private static final int GAP = 4;
	private static final int TAB_W = 60;
	private static final String[] TABS = { "Display", "Stats", "Alerts", "Colors", "Profile" };

	private final Screen parent;
	/** Set while dragging the preview box with the mouse. */
	private boolean dragging;
	private int tab;
	/** Arms the two-click confirm on "Reset to defaults". */
	private boolean resetArmed;

	public FpsPingSettingsScreen(Screen parent) {
		super(Component.literal("FPS & Ping Monitor"));
		this.parent = parent;
	}

	private static FpsPingConfig config() {
		return FpsPingConfig.active();
	}

	private int tabX(int i) {
		return this.width / 2 - (TABS.length * (TAB_W + GAP) - GAP) / 2 + i * (TAB_W + GAP);
	}

	@Override
	protected void init() {
		// ---- Tab row ----
		for (int i = 0; i < TABS.length; i++) {
			final int index = i;
			addRenderableWidget(Button.builder(Component.literal(TABS[i]), b -> {
				if (this.tab != index) {
					this.tab = index;
					this.resetArmed = false;
					this.rebuildWidgets();
				}
			}).bounds(tabX(i), 26, TAB_W, BTN_H).build());
		}

		int left = this.width / 2 - COL_W - GAP / 2;
		int right = this.width / 2 + GAP / 2;
		int top = 26 + BTN_H + GAP * 3;
		int y = top;

		switch (this.tab) {
			case 0 -> { // Display
				addRenderableWidget(toggle(left, y, "Enabled", () -> config().enabled, v -> config().enabled = v));
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
				addRenderableWidget(toggle(left, y, "Compact mode", () -> config().compact, v -> config().compact = v));
				y += BTN_H + GAP;
				addRenderableWidget(toggle(left, y, "Hide with F3", () -> config().hideWithDebug, v -> config().hideWithDebug = v));
			}
			case 1 -> { // Stats (two columns of line/graph toggles)
				int colY = y;
				addRenderableWidget(toggle(left, colY, "Show FPS", () -> config().showFps, v -> config().showFps = v));
				addRenderableWidget(toggle(right, colY, "Ping graph", () -> config().showPingGraph, v -> config().showPingGraph = v));
				colY += BTN_H + GAP;
				addRenderableWidget(toggle(left, colY, "Show Ping", () -> config().showPing, v -> config().showPing = v));
				addRenderableWidget(toggle(right, colY, "FPS graph", () -> config().showFpsGraph, v -> config().showFpsGraph = v));
				colY += BTN_H + GAP;
				addRenderableWidget(toggle(left, colY, "Show TPS", () -> config().showTps, v -> config().showTps = v));
				addRenderableWidget(toggle(right, colY, "Show RAM", () -> config().showRam, v -> config().showRam = v));
				colY += BTN_H + GAP;
				addRenderableWidget(toggle(left, colY, "Show entities", () -> config().showEntities, v -> config().showEntities = v));
				addRenderableWidget(toggle(right, colY, "Show chunks", () -> config().showChunks, v -> config().showChunks = v));
			}
			case 2 -> { // Alerts
				addRenderableWidget(new ThresholdBox(left, y, COL_W, "Ping alert (ms)", config().pingAlertMs, v -> config().pingAlertMs = v));
				y += BTN_H + GAP + 10;
				addRenderableWidget(new ThresholdBox(left, y, COL_W, "FPS alert", config().fpsAlert, v -> config().fpsAlert = v));
				y += BTN_H + GAP + 10;
				addRenderableWidget(toggle(left, y, "Alert sound", () -> config().alertSound, v -> config().alertSound = v));
			}
			case 3 -> { // Colors
				addRenderableWidget(Button.builder(customColorsLabel(), b -> {
					config().useCustomColors = !config().useCustomColors;
					config().save();
					this.rebuildWidgets();
				}).bounds(left, y, COL_W * 2 + GAP, BTN_H).build());
				if (config().useCustomColors) {
					int cy = y + BTN_H + GAP + 10;
					addRenderableWidget(new HexBox(left, cy, COL_W, "Background", () -> config().bgColor, v -> config().bgColor = v));
					addRenderableWidget(new HexBox(right, cy, COL_W, "Label", () -> config().labelColor, v -> config().labelColor = v));
					cy += BTN_H + GAP + 10;
					addRenderableWidget(new HexBox(left, cy, COL_W, "FPS value", () -> config().fpsColor, v -> config().fpsColor = v));
					addRenderableWidget(new HexBox(right, cy, COL_W, "Ping value", () -> config().pingColor, v -> config().pingColor = v));
					cy += BTN_H + GAP + 10;
					addRenderableWidget(new OpacitySlider(left, cy, COL_W * 2 + GAP, BTN_H));
				}
			}
			default -> { // Profile
				if (FpsPingConfig.currentServerIp() != null) {
					addRenderableWidget(Button.builder(
							Component.literal(FpsPingConfig.hasServerProfile() ? "Delete server profile" : "Save as server profile"),
							b -> {
								if (FpsPingConfig.hasServerProfile()) {
									FpsPingConfig.deleteServerProfile();
								} else {
									FpsPingConfig.saveAsServerProfile();
								}
								this.rebuildWidgets();
							}).bounds(left, y, COL_W * 2 + GAP, BTN_H).build());
					y += BTN_H + GAP;
					if (FpsPingConfig.hasServerProfile()) {
						addRenderableWidget(Button.builder(
								Component.literal(FpsPingConfig.editingProfile() ? "Edit global settings instead" : "Edit this server's profile"),
								b -> {
									FpsPingConfig.setEditingProfile(!FpsPingConfig.editingProfile());
									this.rebuildWidgets();
								}).bounds(left, y, COL_W * 2 + GAP, BTN_H).build());
						y += BTN_H + GAP;
					}
				} else {
					graphics_centeredNote(y, "Connect to a server to create a profile");
					y += BTN_H + GAP + 6;
				}
				// Two-click confirm so a misclick can't wipe the config.
				addRenderableWidget(Button.builder(
						Component.literal(this.resetArmed ? "Are you sure? Click again" : "Reset to defaults"),
						b -> {
							if (this.resetArmed) {
								config().resetToDefaults();
								config().save();
								this.rebuildWidgets();
							} else {
								this.resetArmed = true;
								b.setMessage(Component.literal("Are you sure? Click again"));
							}
						}).bounds(left, y, COL_W * 2 + GAP, BTN_H).build());
			}
		}

		addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
				.bounds(this.width / 2 - COL_W / 2, this.height - BTN_H - 8, COL_W, BTN_H).build());
	}

	private void graphics_centeredNote(int y, String text) {
		// Rendered in render(); init has no graphics yet, so stash it.
		this.note = text;
		this.noteY = y;
	}

	private String note;
	private int noteY;

	// ---- Widgets ----

	/**
	 * ON/OFF button. State lives in a captured holder, not the label text.
	 */
	private Button toggle(int x, int y, String label, java.util.function.Supplier<Boolean> getter,
			java.util.function.Consumer<Boolean> setter) {
		boolean[] state = { getter.get() };
		Button b = Button.builder(boolLabel(label, state[0]), btn -> {
			state[0] = !state[0];
			setter.accept(state[0]);
			config().save();
			btn.setMessage(boolLabel(label, state[0]));
		}).bounds(x, y, COL_W, BTN_H).build();
		return b;
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
		// Highlight the active tab with an underline.
		int ux = tabX(this.tab);
		graphics.fill(ux, 26 + BTN_H, ux + TAB_W, 26 + BTN_H + 2, 0xFF55FF55);
		if (this.note != null) {
			graphics.drawCenteredString(this.font, this.note, this.width / 2, this.noteY + 5, 0xFFAAAAAA);
		}
		graphics.drawCenteredString(this.font, "Drag the box to move it", this.width / 2,
				this.height - BTN_H * 2 - 14, 0xFFAAAAAA);
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
				if (s.length() == 6) {
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
