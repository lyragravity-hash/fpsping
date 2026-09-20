# Changelog

## 1.1.1 — Polish release

- **Settings screen redesigned with tabs** (Display / Stats / Alerts / Colors / Profile) — no more widgets running off-screen at high GUI scales
- **Reset to defaults** button (with a click-twice confirmation)
- **Alert sound** is now toggleable in the UI (it was config-only before)
- **FPS graph autoscales** — no longer floored at 60 FPS, so 30–60 FPS machines get a real waveform instead of a flat line
- Internal: toggle buttons no longer decode their state from label text; alerts skip work entirely when both thresholds are off

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
