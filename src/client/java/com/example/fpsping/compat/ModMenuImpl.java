package com.example.fpsping.compat;

import com.example.fpsping.FpsPingSettingsScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * Makes the settings screen appear on the mod's entry in the Mod Menu "Mods"
 * screen (the config button next to the mod). This entrypoint is only loaded
 * when Mod Menu is installed.
 */
public class ModMenuImpl implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return FpsPingSettingsScreen::new;
	}
}
