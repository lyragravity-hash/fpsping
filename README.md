# FPS & Ping Monitor

A client-side Fabric mod for **Minecraft 1.21.11** that shows your FPS and server
ping in the corner of the screen, with themes, scaling, graphs, extra stats, and
lag alerts.

## Features

**Display**
- Live **FPS** and **Ping** (ms), color coded: green < 80, yellow < 150, red < 300, dark red above
- **Draggable overlay** — drag the box anywhere on the settings screen preview; it
  snaps to screen edges/corners (default: top right)
- **Compact mode**: one line, `120 fps · 42 ms`
- Per-line toggles: FPS, ping, **TPS** (client-side estimate from server time packets
  — no server mod needed), **RAM**, **entities**, **chunks**
- **Ping & FPS sparklines** — bar graphs of the last 30 seconds, spikes visible at a glance
- Text styles: shadow / outline / plain

**Look**
- **4 themes**: Dark, Light, Transparent, Rainbow (value colors cycle over time)
- **Custom colors**: background color + opacity, label/FPS/ping colors (hex input with live swatch)
- **HUD scale**: 50% – 200% slider
- Hidden with F1 (hide GUI), and with F3 if "Hide with F3" is on

**Behavior**
- **Alerts**: action-bar message + optional sound when ping spikes above a
  threshold or FPS drops below one (0 = off, 10 s cooldown)
- **Per-server profiles**: "Save as server profile" while connected stores the
  current look for that server only; edit/delete the profile anytime
- Settings persist to `config/fpsping.json`

## Settings GUI

Open with `O` (rebindable under Options > Controls > Key Binds, category
"FPS & Ping Monitor") or via the config button on the mod's page in **Mod Menu**.
The overlay is rendered live in the preview — drag it to move, tweak anything,
changes save instantly. Mod Menu is optional.

## Building

Requires JDK 21+ (Gradle wrapper handles the rest):

```bash
./gradlew build
```

The mod jar is written to `build/libs/fpsping-<version>.jar`.

> On machines where the default `java` is old, point Gradle at a newer JDK first,
> e.g. in Git Bash:
> `export JAVA_HOME="/c/Users/<you>/AppData/Local/Programs/Eclipse Adoptium/jdk-25.0.4.101-hotspot"`

## Installing

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft **1.21.11** (0.19.0+)
2. Install [Fabric API](https://modrinth.com/mod/fabric-api) (`0.141.x+1.21.11` or newer)
3. Optional: install [Mod Menu](https://modrinth.com/mod/modmenu)
4. Drop `build/libs/fpsping-1.1.0.jar` into your `mods` folder

## Project layout

```
src/client/java/com/example/fpsping/
  FpsPingModClient.java      client entrypoint (keybind, sampling, profiles, alerts)
  FpsPingConfig.java         JSON config, global + per-server profiles
  HudOverlay.java            overlay renderer (drag layout, graphs, all lines)
  HudTheme.java              theme presets
  FpsPingSettingsScreen.java settings screen (sliders, hex colors, drag preview)
  History.java               ring buffers for the sparklines
  Stats.java                 ping/RAM/entity/chunk sampling
  Alerts.java                ping/FPS threshold alerts
  TpsTracker.java            server TPS estimate
  mixin/ClientPacketListenerMixin.java  feeds TpsTracker from time packets
  compat/ModMenuImpl.java    Mod Menu entrypoint -> settings screen
src/main/resources/
  fpsping.mixins.json
  assets/fpsping/            icon + lang (en, de, fr)
tools/GenIcon.java           one-off icon generator
```
