# TTP (Tab Teleport) — agent guide

Forge client mod for **Minecraft 1.18.1**. Opens a player list (Tab key binding) and teleports to a selected online player via server-side packet handling.

## Stack

| Item | Value |
|------|--------|
| MC | 1.18.1 |
| Forge | `1.18.1-39.0.5` |
| Mappings | official 1.18.1 |
| Java | **21** (Gradle toolchain) |
| Gradle | 8.5 (wrapper) |
| Mod ID | `ttp` |
| Package | `com.ttp` |

## Repository layout

```
src/main/java/com/ttp/
  TTP.java                 — @Mod entry, registers KeyHandler + networking on client setup
  key/KeyHandler.java      — keybind opens PlayerListScreen
  screen/PlayerListScreen.java — GUI + sends teleport request
  net/Networking.java      — SimpleChannel registration
  net/TeleportRequestPack.java — server validates target and teleports sender
src/main/resources/
  META-INF/mods.toml
  assets/ttp/lang/         — en_us.json, zh_cn.json
```

Build output: `build/libs/ttp-1.0.0.jar` (version from `build.gradle`).

## Commands (Windows)

Run from repo root (`C:\Users\63569\Desktop\dump`):

```powershell
java -version          # must be JDK 21
.\gradlew.bat build --no-daemon
.\gradlew.bat runClient --no-daemon   # optional dev client
```

First build downloads Forge/Minecraft via Gradle; a local PCL `.minecraft` folder does **not** replace that.

## Conventions for changes

- Keep `MOD_ID` in `TTP.MOD_ID`; use `ResourceLocation(TTP.MOD_ID, ...)` for assets and network channel.
- User-facing strings: add keys to **both** `en_us.json` and `zh_cn.json`.
- Teleport logic runs on the **server** in `TeleportRequestPack` (client only sends target UUID).
- Do not commit `.gradle/`, `build/`, `run/`, or leftover `ttp/` cache folder.

## Scope

This repo is **only** the TTP mod. No other game dumps or unrelated mods live here.
