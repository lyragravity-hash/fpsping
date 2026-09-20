package com.example.fpsping;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import org.lwjgl.glfw.GLFW;

public final class FpsPingModClient implements ClientModInitializer {
	public static final String MOD_ID = "fpsping";

	private static KeyMapping openSettingsKey;
	private static int tickCounter;

	@Override
	public void onInitializeClient() {
		FpsPingConfig.load();

		openSettingsKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.fpsping.settings",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_O,
				KeyMapping.Category.MISC
		));

		// Attached after the last vanilla layer so it draws on top and hides
		// together with the rest of the HUD (F1).
		HudElementRegistry.attachElementAfter(VanillaHudElements.SUBTITLES, HudOverlay.ID, new HudOverlay());

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openSettingsKey.consumeClick()) {
				client.setScreen(new FpsPingSettingsScreen(client.screen));
			}

			FpsPingConfig cfg = FpsPingConfig.active();
			tickCounter++;
			if (tickCounter % 10 == 0) { // twice a second
				Stats.sample(client, cfg);
			}
			if (cfg.enabled && client.player != null && client.screen == null) {
				Alerts.check(client, cfg);
			}
		});

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
				FpsPingConfig.onJoinServer(client.getCurrentServer() != null ? client.getCurrentServer().ip : null));

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			FpsPingConfig.onLeaveServer();
			History.reset();
			TpsTracker.reset();
		});
	}
}
