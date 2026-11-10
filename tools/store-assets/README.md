# Store assets

Scripts that generate the app icon and the Google Play listing graphics. Output goes to
`build/store-assets/`, which is not committed.

Requires Python 3 and Google Chrome (used to render SVG/HTML to PNG; set `CHROME` if it is not in
`/Applications`). Screenshots also need a connected phone and `adb` (set `ADB` if it is not in the
default Android SDK location).

## Icon and feature graphics

```bash
tools/store-assets/make_icons_and_banners.sh
```

- `gen_icon.py` draws the notebook icon once and writes it as the adaptive icon layers
  (`drawable/ic_launcher_background.xml`, `ic_launcher_foreground.xml`, `ic_launcher_monochrome.xml`)
  plus SVGs for the raster versions.
- The script renders the legacy launcher PNGs (`mipmap-*/ic_launcher*.png`, Android 6–7) and
  `build/store-assets/icon/play_store_icon_512.png`.
- `gen_banner.py` builds the 1024×500 feature graphics in English, Ukrainian and Russian
  (`build/store-assets/feature_graphic/`). It validates the board first: a legal fleet, no ships
  touching, hits only on ships, misses only on water, and sunk ships ringed with misses.

## Screenshots

```bash
tools/store-assets/capture_screenshots.sh
```

Captures seven screenshots per language (en, uk, ru) into `build/store-assets/screenshots/<lang>/`,
numbered in upload order: placing ships, mid-battle, the computer's turn, a win, battle in
landscape, a loss in landscape and a loss in portrait, across light and dark themes.

- `gen_screenshot_test.py` validates the game positions (same rules as the banner) and writes a
  temporary instrumented test that renders the real `GameScreen` in each position. The script
  deletes the test again when it finishes; never commit `StoreScreenshots.kt`.
- The status bar is switched to demo mode (12:00, full battery, no notifications) while capturing.
  The phone's theme, rotation and the app's language are reset afterwards.
- `fit_screenshot.html` trims the system bars and fits each image within Play's 2:1 limit. The trim
  values in `capture_screenshots.sh` are for a Pixel 8a (1080×2400).
