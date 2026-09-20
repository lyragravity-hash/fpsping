# FPS & Ping Monitor

A customizable FPS and ping overlay for **Minecraft 1.21.11** (Fabric).
Drag it anywhere, theme it, graph it — and get warned when lag hits.

**Requires:** Fabric Loader 0.19.0+, [Fabric API](https://modrinth.com/mod/fabric-api)
**Optional:** [Mod Menu](https://modrinth.com/mod/modmenu) for in-game settings via the Mods screen

---

## ✨ Features

### Overlay
- **FPS + Ping** always visible, ping color-coded (green < 80 ms · yellow < 150 · red < 300 · dark red above)
- **Drag anywhere** — open settings and drag the box; it snaps to screen edges and corners
- **Compact mode** — one tidy line: `120 fps · 42 ms`
- **Scale** — 50% to 200% slider
- **Text styles** — shadow, outline, or plain

### Stats & graphs
- **Ping sparkline** — the last 30 seconds at a glance, spikes impossible to miss
- **FPS sparkline** — spot drops without staring at numbers
- **Server TPS** — estimated client-side from server time packets, **no server mod needed**
- **RAM**, **entities rendered**, and **chunk** counts — the F3 numbers you actually care about, always on
- Toggle every line independently

### Alerts
- Set a **ping threshold** (e.g. 200 ms) or a **minimum FPS** — get an action-bar warning
  and an optional sound when crossed, with a 10-second cooldown so it never spams

### Themes & colors
- **4 built-in themes**: Dark, Light, Transparent, and Rainbow (values cycle hues over time)
- **Custom colors**: background color + opacity slider, label / FPS / ping colors —
  enter hex codes with a live swatch

### Profiles
- **Per-server profiles**: "Save as server profile" while connected, and that server
  keeps its own look — different theme for your SMP, minimal box for minigames
- Everything persists to `config/fpsping.json`

## 🎮 How to use

1. Drop the jar in your `mods` folder (with Fabric API)
2. Press **O** in game to open settings (rebindable under Options → Controls → Key Binds)
3. Drag the preview box wherever you want it, toggle what you need, hit Done

Or open settings from **Mod Menu → Mods → FPS & Ping Monitor → Configure**.

The overlay hides with **F1** (hide GUI) like vanilla HUD elements, and can
auto-hide while the **F3** debug screen is open (on by default — the debug screen
already shows FPS).

Ping is hidden in singleplayer (there is no server to ping).

## ❓ FAQ

**Does the TPS line require anything on the server?**
No. It estimates tick rate on the client from the timing of standard time-sync
packets every server already sends. It's an estimate — a rough but useful one.

**Is it client-side only?**
Yes. Install it on your client; servers need nothing.

**Does it work with OptiFine/Sodium?**
It's a standard Fabric HUD element — it renders fine alongside performance mods.

**Which Minecraft versions?**
Built for **1.21.11**. (Port requests welcome in the Discord/issues.)

## 📋 Links

- **Source:** _(add your GitHub repo URL here)_
- **Issues:** _(add your tracker URL here)_
- **License:** MIT

---

*Built with Fabric. If you enjoy it, leave a review — it helps a lot!*
