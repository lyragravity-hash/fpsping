# Changelog

## 1.1.1

The settings screen was getting crowded. Everything lived in one big two-column block, and once custom colors showed up it would run right past the bottom of the screen at higher GUI scales. It's split into tabs now (Display / Stats / Alerts / Colors / Profile) — same options, just sorted properly.

Also:

- "Reset to defaults" button under the Profile tab. It makes you click twice so you can't wipe your config by accident
- The alert sound can finally be toggled from the UI (before this you had to dig into config/fpsping.json by hand)
- The FPS graph scales to your actual peak FPS instead of assuming 60, so on a 30-50 fps machine you get a real waveform and not a flat line
- Alerts don't do any work at all when both thresholds are off

## 1.1.0 — Feature release

- **Draggable overlay**: drag the box anywhere in the settings-screen preview; snaps to edges/corners
- **Compact mode**: one-line display (`120 fps · 42 ms`)
- **Sparkline graphs**: 30-second ping and FPS bar graphs
- **Extra stats**: server TPS (client-side estimate, no server mod needed), RAM, entities, chunks
- **Lag alerts**: action-bar + optional sound when ping exceeds a threshold or FPS drops below one (10 s cooldown)
- **Custom colors**: background color + opacity, label/FPS/ping colors via hex boxes with swatches
- **Text styles**: shadow, outline, plain
- **Per-server profiles**: save a separate look per server, switch/delete anytime
- **Scale slider** (50–200%) replacing click-cycling
- **"Hide with F3"** option (on by default)
- Mod icon, German + French translations
- Fixed: lowered Fabric Loader requirement to 0.19.0 (was 0.19.5)

## 1.0.0 — Initial release

- FPS + ping overlay (top right), color-coded ping
- 4 themes (Dark, Light, Transparent, Rainbow), 50–200% scale
- Settings screen with live preview (O key or Mod Menu), persisted to `config/fpsping.json`
