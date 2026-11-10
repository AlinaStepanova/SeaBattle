#!/usr/bin/env bash
# Captures the Play Store screenshots on a connected phone in English, Ukrainian and Russian.
#
# A generated instrumented test renders the real GameScreen in fixed, validated game positions;
# the test file is removed again when the script exits. The status bar is put into demo mode
# while capturing, and the phone's theme, rotation and app language are reset afterwards.
# The trim values below are for a Pixel 8a (1080x2400); adjust them for another phone.
set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(cd "$HERE/../.." && pwd)"
OUT="$ROOT/build/store-assets/screenshots"
RAW="$ROOT/build/store-assets/screenshots_raw"
CHROME="${CHROME:-/Applications/Google Chrome.app/Contents/MacOS/Google Chrome}"
ADB="${ADB:-$HOME/Library/Android/sdk/platform-tools/adb}"
PACKAGE=com.avs.sea.battle
TEST_FILE="$ROOT/app/src/androidTest/java/com/avs/sea/battle/ui/StoreScreenshots.kt"
DEVICE_DIR="/sdcard/Android/data/$PACKAGE/files/store"

# Pixel 8a: status bar 121px and gesture bar 63px in portrait. Portrait is widened to 2:1 by
# repeating the page's edge pixels; landscape keeps the top (only paper once the sides are
# trimmed) and trims the margins, which also drops the overflow menu icon.
PORTRAIT_TRIM="top=121&bottom=63&left=0&right=0"
PORTRAIT_SIZE=1108,2216
LANDSCAPE_TRIM="top=0&bottom=63&left=240&right=274"
LANDSCAPE_SIZE=1886,1017

demo() { "$ADB" shell am broadcast -a com.android.systemui.demo -e command "$@" >/dev/null; }

cleanup() {
  rm -f -- "$TEST_FILE"
  demo exit || true
  "$ADB" shell settings put global sysui_demo_allowed 0 || true
  "$ADB" shell cmd uimode night no >/dev/null || true
  "$ADB" shell settings put system user_rotation 0 || true
  "$ADB" shell cmd locale set-app-locales "$PACKAGE" --locales "" || true
  "$ADB" shell pm uninstall "$PACKAGE.test" >/dev/null || true
}
trap cleanup EXIT

python3 "$HERE/gen_screenshot_test.py" "$TEST_FILE"
(cd "$ROOT" && ./gradlew -q installDebug installDebugAndroidTest)

"$ADB" shell settings put global sysui_demo_allowed 1
demo enter
demo clock -e hhmm 1200
demo battery -e level 100 -e plugged false
demo network -e wifi show -e level 4 -e fully true
demo network -e mobile hide
demo notifications -e visible false
"$ADB" shell settings put system accelerometer_rotation 0
"$ADB" shell rm -rf "$DEVICE_DIR"

# setup <light|dark> <portrait|landscape>: theme first, then rotation, and wait until it applies.
setup() {
  if [ "$1" = dark ]; then "$ADB" shell cmd uimode night yes >/dev/null; else "$ADB" shell cmd uimode night no >/dev/null; fi
  sleep 2
  local rotation=0
  if [ "$2" = landscape ]; then rotation=1; fi
  "$ADB" shell settings put system user_rotation "$rotation"
  sleep 3
  [ "$("$ADB" shell settings get system user_rotation | tr -d '\r')" = "$rotation" ] || { echo "rotation did not apply"; exit 1; }
}

capture() {
  "$ADB" shell am instrument -w -e class "com.avs.sea.battle.ui.StoreScreenshots#$1" \
    "$PACKAGE.test/androidx.test.runner.AndroidJUnitRunner" | grep -q "OK (1 test)" || { echo "capture $1 failed"; exit 1; }
}

for lang in en uk ru; do
  "$ADB" shell cmd locale set-app-locales "$PACKAGE" --locales "$lang"
  setup light portrait;  capture placing; capture battle
  setup dark portrait;   capture computer_turn; capture win; capture lose
  setup light landscape; capture battle
  setup dark landscape;  capture lose
done

mkdir -p "$RAW"
"$ADB" pull -q "$DEVICE_DIR/." "$RAW/"

# Store order per language.
SHOTS="portrait_light_placing portrait_light_battle portrait_dark_computer_turn portrait_dark_win landscape_light_battle landscape_dark_lose portrait_dark_lose"
for lang in en uk ru; do
  mkdir -p "$OUT/$lang"
  i=1
  for shot in $SHOTS; do
    case $shot in
      landscape*) trim=$LANDSCAPE_TRIM; size=$LANDSCAPE_SIZE ;;
      *) trim=$PORTRAIT_TRIM; size=$PORTRAIT_SIZE ;;
    esac
    "$CHROME" --headless=new --disable-gpu --hide-scrollbars --allow-file-access-from-files \
      --force-device-scale-factor=1 --window-size="$size" --virtual-time-budget=4000 \
      --screenshot="$OUT/$lang/0${i}_$shot.png" \
      "file://$HERE/fit_screenshot.html?src=file://$RAW/${lang}_$shot.png&$trim" >/dev/null 2>&1
    i=$((i + 1))
  done
done

echo "Screenshots in $OUT"
