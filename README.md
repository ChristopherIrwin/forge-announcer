<p align="center">
  <img src="assets/logo.webp" width="160" alt="ForgeAnnouncer logo">
</p>

<h1 align="center">ForgeAnnouncer</h1>

<p align="center"><i>Scheduled MiniMessage broadcasts for Paper servers — chat, action bar, boss bar, and title.</i></p>

<p align="center">
  <img src="https://img.shields.io/badge/version-1.0.0-ff7b2e?style=for-the-badge" alt="version 1.0.0">
  <img src="https://img.shields.io/badge/Paper-26.3-2f9e6e?style=for-the-badge" alt="Paper 26.3">
  <img src="https://img.shields.io/badge/Java-25-f89820?style=for-the-badge" alt="Java 25">
  <img src="https://img.shields.io/badge/MiniMessage-native-b565d8?style=for-the-badge" alt="MiniMessage native">
  <img src="https://img.shields.io/badge/4_delivery_channels-2563eb?style=for-the-badge" alt="4 delivery channels">
  <img src="https://img.shields.io/badge/dependencies-zero-6b7280?style=for-the-badge" alt="zero dependencies">
</p>

<p align="center"><sub>Not affiliated with <a href="https://minecraftforge.net">MinecraftForge</a> — "Forge" is just a name.</sub></p>

---

Define any number of announcements in `config.yml`; each rotates on its own interval and is delivered to every online player via chat, the action bar, a boss bar, or a title screen. Messages support the full MiniMessage format, including gradients and clickable commands. An original implementation with zero runtime dependencies beyond the Paper API.

## Features

- Per-announcement rotation on independent intervals
- Four delivery channels: chat, action bar, boss bar, title
- Sequential or random message rotation
- MiniMessage formatting, including gradients and `<click:run_command:...>` click events
- Manual broadcast of any announcement by id
- Hot reload of `config.yml` without a server restart
- Invalid config entries are skipped with a console warning instead of breaking the plugin

## Requirements

- Paper 26.3 or newer (`api-version: 26.3`)
- Java 25

## Installation

1. Download `ForgeAnnouncer-1.0.0.jar` from the releases page.
2. Drop it into your server's `plugins/` folder.
3. Restart the server (or use your plugin manager's load command).
4. Edit `plugins/ForgeAnnouncer/config.yml` and run `/fannouncer reload`.

## Commands

| Command | Args | Description | Permission |
|---|---|---|---|
| `/fannouncer reload` | — | Reload `config.yml` and reschedule all announcements | `forgeannouncer.admin` |
| `/fannouncer broadcast` | `<id>` | Immediately broadcast the announcement with the given id | `forgeannouncer.admin` |
| `/fannouncer list` | — | List all configured announcement ids | `forgeannouncer.admin` |

All subcommands have tab completion, including announcement ids for `broadcast`.

## Permissions

| Permission | Default | Description |
|---|---|---|
| `forgeannouncer.admin` | op | Reload, manual broadcast, and list announcements |

## Configuration

`config.yml` holds a single top-level `announcements:` list. Each entry is one announcement:

| Key | Type | Default | Description |
|---|---|---|---|
| `id` | string | — (required) | Unique name, used by `/fannouncer broadcast <id>` |
| `messages` | list of strings | — (required, at least one) | MiniMessage strings; chat messages may use click events such as `<click:run_command:/rules>` |
| `interval-seconds` | number | `300` | How often this announcement fires; must be positive |
| `mode` | string | `sequential` | `sequential` cycles messages in order; `random` picks one at random |
| `delivery` | string | `chat` | `chat`, `actionbar`, `bossbar`, or `title` (case-insensitive; hyphens accepted) |
| `bossbar-color` | string | `BLUE` | Boss bar color: `PINK`, `BLUE`, `RED`, `GREEN`, `YELLOW`, `PURPLE`, `WHITE` |
| `bossbar-seconds` | number | `8` | How long the boss bar stays visible (minimum 1 second) |
| `title-fade-in` | number | `10` | Title fade-in time in ticks (20 ticks = 1 second) |
| `title-stay` | number | `70` | Title stay time in ticks |
| `title-fade-out` | number | `20` | Title fade-out time in ticks |

Unknown `mode`, `delivery`, or `bossbar-color` values fall back to their defaults. Entries missing an id, with no messages, or with a non-positive `interval-seconds` are skipped and logged as a warning on load/reload.

## Building from source

```bash
bash build.sh
```

The build script compiles with `javac` directly (JDK 25 at `~/workspace/.toolchains/jdk-25.0.4.1+1`) against the Paper API jars in `~/workspace/.toolchains/paper-deps` and packages `ForgeAnnouncer-1.0.0.jar`. A canonical `build.gradle.kts` is kept for environments where Gradle can run; the script pins the same Paper API version as `plugin.yml`.

## Code quality

- Compiled with `-Werror -Xlint:deprecation`: zero warnings, zero deprecated API usage.
- Nullness is explicit: every package is `@NotNullByDefault` (JetBrains annotations), with `@Nullable` marked on the few sites where null is genuinely possible (e.g. config lookups for optional keys).

---

<p align="center"><i>Part of the <a href="https://github.com/ForgePluginsMC">Forge</a> plugin suite — original implementations, zero dependencies.</i></p>
