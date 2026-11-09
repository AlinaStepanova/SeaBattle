#!/usr/bin/env bash
# Regenerates the app icon (vector layers and legacy launcher PNGs), the 512px Play Store icon
# and the Play Store feature graphics in English, Ukrainian and Russian.
set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(cd "$HERE/../.." && pwd)"
OUT="$ROOT/build/store-assets"
RES="$ROOT/app/src/main/res"
CHROME="${CHROME:-/Applications/Google Chrome.app/Contents/MacOS/Google Chrome}"

# render <file> <width,height> <png> [transparent]
render() {
  local background=()
  if [ "${4:-}" = transparent ]; then background=(--default-background-color=00000000); fi
  "$CHROME" --headless=new --disable-gpu --hide-scrollbars --allow-file-access-from-files \
    --force-device-scale-factor=1 ${background[@]+"${background[@]}"} --window-size="$2" --virtual-time-budget=5000 \
    --screenshot="$3" "file://$1" >/dev/null 2>&1
}

mkdir -p "$OUT/svg" "$OUT/icon" "$OUT/feature_graphic" "$OUT/banner_pages"

python3 "$HERE/gen_icon.py" "$RES" "$OUT/svg"
render "$OUT/svg/play_512.svg" 512,512 "$OUT/icon/play_store_icon_512.png"
for entry in mdpi:48 hdpi:72 xhdpi:96 xxhdpi:144 xxxhdpi:192; do
  density=${entry%:*}
  size=${entry#*:}
  render "$OUT/svg/legacy_square_$size.svg" "$size,$size" "$RES/mipmap-$density/ic_launcher.png" transparent
  render "$OUT/svg/legacy_round_$size.svg" "$size,$size" "$RES/mipmap-$density/ic_launcher_round.png" transparent
done

python3 "$HERE/gen_banner.py" "$RES/font/neucha.ttf" "$OUT/banner_pages"
for lang in en uk ru; do
  render "$OUT/banner_pages/banner_$lang.html" 1024,500 "$OUT/feature_graphic/feature_graphic_$lang.png"
done

echo "Icon drawables updated in $RES; Play Store icon and feature graphics in $OUT"
