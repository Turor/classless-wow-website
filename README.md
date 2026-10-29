# Classless Wow Web Server

This web server supports basic account creation and recovery for a Classless WoW server.

- Account recovery is facilitated by manually relaying a generated token to the user.
- The server generates presigned S3 URLs so a companion launcher can fetch game resources by bucket key.
- Authenticated users can download platform launchers (Windows, Linux, and Apple Silicon / macOS when published).

## Configuration

Copy `src/main/resources/application.dist.yml` to `src/main/resources/application.yml` (gitignored) and fill in local values. Never commit real credentials, keystores, recovery databases, or launcher zip caches.

Key config areas (placeholders only in the dist file):

- Gate password and Spring profile (`dev` / `prod` / `test`)
- Garage / S3 endpoint, allowlist, and credentials (via environment or local yml)
- MySQL auth DB and SQLite recovery DB URLs
- `launcherservice.*` cache directory and per-platform launcher object names

## Launcher downloads

| Platform | UI | API | Object name (configurable) |
|----------|----|-----|----------------------------|
| Windows | Download Windows | `GET /files/download/windows` → `/files/downloadlauncher/windows` | `launcherservice.windowsLauncherName` |
| Linux | Download Linux | `GET /files/download/linux` → `/files/downloadlauncher/linux` | `launcherservice.linuxLauncherName` |
| macOS / Apple Silicon | Apple Silicon tab | `GET /files/download/macos` → `/files/downloadlauncher/macos` | `launcherservice.macosLauncherName` |

macOS / Apple Silicon: publish `ClasslessLauncherMacos.zip` to bucket `wow` and allowlist `ClasslessLauncherMacos.zip` plus `mirrors/WoWSilicon.app.zip` for the shell launcher's WoWSilicon bootstrap. The Apple Silicon tab documents unsigned install + `xattr` quarantine removal and WrathSilicon profile setup.

## Build

```bash
./gradlew build bootJar
```

Run with a local `application.yml` and required services (MySQL and/or SQLite, Garage/S3 as configured).

## Deploy notes

Deploy as a normal Spring Boot service behind your preferred reverse proxy / TLS terminator. Keep production `application.yml`, keystores, and `recovery.db` on the host only — outside this repository.

## Roguelike info page

`/roguelike` is a public page (no gate password) describing the Roguelike Gauntlet realm: design, character
creation, realm rules and every boon and curse. Static files live in `src/main/resources/static/roguelike/`;
the boon/curse list, draft dials and dungeon list come from `catalog.json`, generated from the real Gauntlet Lua
in `Turor/mod-turoran-roguelike` (`feature/gauntlet-hero`):

```bash
tools/roguelike/refresh.sh                 # clones/pulls the mod into tools/roguelike/.cache
tools/roguelike/refresh.sh ~/path/to/mod-turoran-roguelike   # or use an existing checkout
```

It loads `GauntletBoons.lua` / `GauntletData.lua` under no-op stubs (`tools/roguelike/export_catalog.lua`, needs
lua5.1, luajit or lua5.4), writes `catalog.json` and downloads any missing icon from Wowhead's icon CDN into
`roguelike/icons/`. Commit the result and redeploy. The prose sections (creation, rules, core kit) are hand-written
in `roguelike/index.html`.
