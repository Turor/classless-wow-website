#!/usr/bin/env bash
# Regenerate the Roguelike info page data from Turor/mod-turoran-roguelike (feature/gauntlet-hero).
#
#   tools/roguelike/refresh.sh [path to a mod-turoran-roguelike checkout]
#
# Without a path it clones/pulls a cache in tools/roguelike/.cache (gitignored). Writes
#   src/main/resources/static/roguelike/catalog.json   (boons, curses, dials, dungeons)
#   src/main/resources/static/roguelike/icons/*.jpg    (missing WoW icons, from Wowhead's icon CDN)
# then commit the result. Needs lua5.1 (or luajit / lua5.4), git and curl.
set -euo pipefail
here="$(cd "$(dirname "$0")" && pwd)"
repo_root="$(cd "$here/../.." && pwd)"
out="$repo_root/src/main/resources/static/roguelike"
src="${1:-}"
if [[ -z "$src" ]]; then
  src="$here/.cache/mod-turoran-roguelike"
  if [[ -d "$src/.git" ]]; then
    git -C "$src" fetch -q origin feature/gauntlet-hero
    git -C "$src" checkout -q -B feature/gauntlet-hero origin/feature/gauntlet-hero
  else
    mkdir -p "$here/.cache"
    git clone -q -b feature/gauntlet-hero https://github.com/Turor/mod-turoran-roguelike.git "$src"
  fi
fi
lua="$(command -v lua5.1 || command -v luajit || command -v lua5.4 || command -v lua)"
commit="$(git -C "$src" rev-parse --short HEAD 2>/dev/null || echo unknown)"
mkdir -p "$out/icons"
tmp="$(mktemp)"
"$lua" "$here/export_catalog.lua" "$src" "$commit" > "$tmp"
python3 -m json.tool "$tmp" > "$out/catalog.json"
rm -f "$tmp"
# Icons: Blizzard Entertainment artwork, served by Wowhead's public icon CDN (zamimg). Fetched once, kept in repo.
python3 - "$out/catalog.json" <<'PY' | sort -u | while read -r icon; do
import json, sys
d = json.load(open(sys.argv[1]))
for e in d["boons"] + d["curses"]:
    if e.get("icon"): print(e["icon"].lower())
print("inv_misc_questionmark")
PY
  f="$out/icons/$icon.jpg"
  [[ -s "$f" ]] && continue
  if ! curl -fsSL --retry 2 -o "$f" "https://wow.zamimg.com/images/wow/icons/large/$icon.jpg"; then
    rm -f "$f"; echo "warning: no icon for $icon (the page falls back to the question mark)" >&2
  fi
done
echo "catalog.json from $commit: $(python3 -c "import json,sys;d=json.load(open(sys.argv[1]));print(len(d['boons']),'boons,',len(d['curses']),'curses,',len(d['dungeons']),'dungeons')" "$out/catalog.json")"
