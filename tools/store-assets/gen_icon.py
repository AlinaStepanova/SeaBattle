"""Generates the SeaBattle notebook icon (design "A v2") as Android vector drawables and SVG.

One source of truth for the shapes, so the adaptive icon, the legacy raster icons and the
Play Store icon stay identical. Coordinates use the 108x108 adaptive-icon canvas.
"""
import os
import sys

PAPER = "FCFBF6"
INK = "2A4DA8"
RED = "B23A30"
CELL = 18
GRID = [9, 27, 45, 63, 81, 99]
SHIPS = [(63, 9), (45, 45), (9, 45), (9, 63), (81, 45), (81, 63)]  # top single, sunk centre, left pair, right pair
CROSS = (45, 45)
DOTS = [(27, 45), (63, 45), (45, 27), (45, 63), (27, 27), (63, 27), (27, 63), (63, 63), (27, 9)]
LINE_W, HATCH_W, CROSS_W, DOT_R = 0.8, 0.95, 2.4, 1.5


def hatch_strokes(x, y):
    strokes = []
    for i in range(1, 8):
        s = i * CELL / 4
        if s <= CELL:
            strokes.append((x, y + s, x + s, y))
        else:
            strokes.append((x + s - CELL, y + CELL, x + CELL, y + s - CELL))
    return strokes


def cross_strokes():
    x, y = CROSS
    i = CELL * 0.1
    return [(x + i, y + i, x + CELL - i, y + CELL - i), (x + CELL - i, y + i, x + i, y + CELL - i)]


def fmt(v):
    return ("%.2f" % v).rstrip("0").rstrip(".")


# ---------- Android vector drawables ----------

def vector(body):
    return (
        '<?xml version="1.0" encoding="utf-8"?>\n'
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
        '    android:width="108dp"\n'
        '    android:height="108dp"\n'
        '    android:viewportWidth="108"\n'
        '    android:viewportHeight="108">\n' + body + '</vector>\n'
    )


def v_line(x1, y1, x2, y2, color, width, cap="butt", indent="    "):
    return (
        f'{indent}<path\n'
        f'{indent}    android:pathData="M{fmt(x1)},{fmt(y1)} L{fmt(x2)},{fmt(y2)}"\n'
        f'{indent}    android:strokeColor="#{color}"\n'
        f'{indent}    android:strokeWidth="{fmt(width)}"\n'
        f'{indent}    android:strokeLineCap="{cap}" />\n'
    )


def foreground(ink, red, hatch_alpha):
    body = '    <!-- Pen-shaded ships; ships never touch, even diagonally -->\n'
    for x, y in SHIPS:
        body += '    <group>\n'
        body += f'        <clip-path android:pathData="M{x},{y} h{CELL} v{CELL} h-{CELL} z" />\n'
        for x1, y1, x2, y2 in hatch_strokes(x, y):
            body += v_line(x1, y1, x2, y2, hatch_alpha + ink, HATCH_W, "round", "        ")
        body += '    </group>\n'
    body += '    <!-- Board grid in pen -->\n'
    for v in GRID:
        body += v_line(v, 0, v, 108, ink, LINE_W)
        body += v_line(0, v, 108, v, ink, LINE_W)
    body += '    <!-- Sunk ship crossed out in red -->\n'
    for x1, y1, x2, y2 in cross_strokes():
        body += v_line(x1, y1, x2, y2, red, CROSS_W, "round")
    body += '    <!-- Misses -->\n'
    for x, y in DOTS:
        cx, cy = x + CELL / 2, y + CELL / 2
        body += (
            '    <path\n'
            f'        android:pathData="M{fmt(cx - DOT_R)},{fmt(cy)} a{fmt(DOT_R)},{fmt(DOT_R)} 0 1,0 {fmt(2 * DOT_R)},0 a{fmt(DOT_R)},{fmt(DOT_R)} 0 1,0 -{fmt(2 * DOT_R)},0"\n'
            f'        android:fillColor="#{ink}" />\n'
        )
    return vector(body)


def background():
    return vector(
        '    <path\n'
        '        android:pathData="M0,0h108v108h-108z"\n'
        f'        android:fillColor="#{PAPER}" />\n'
    )


# ---------- SVG twin for raster exports ----------

def svg(view_box, size, shape=None, transparent_corners=False):
    parts = []
    clip = ""
    if shape == "circle":
        clip = '<clipPath id="m"><circle cx="54" cy="54" r="36"/></clipPath>'
    elif shape == "squircle":
        clip = '<clipPath id="m"><rect x="18" y="18" width="72" height="72" rx="16" ry="16"/></clipPath>'
    parts.append(f'<rect width="108" height="108" fill="#{PAPER}"/>')
    for i, (x, y) in enumerate(SHIPS):
        parts.append(f'<clipPath id="s{i}"><rect x="{x}" y="{y}" width="{CELL}" height="{CELL}"/></clipPath>')
        parts.append(f'<g clip-path="url(#s{i})">' + "".join(
            f'<line x1="{fmt(a)}" y1="{fmt(b)}" x2="{fmt(c)}" y2="{fmt(d)}" stroke="#{INK}" stroke-opacity="0.7" stroke-width="{HATCH_W}" stroke-linecap="round"/>'
            for a, b, c, d in hatch_strokes(x, y)) + '</g>')
    for v in GRID:
        parts.append(f'<line x1="{v}" y1="0" x2="{v}" y2="108" stroke="#{INK}" stroke-width="{LINE_W}"/>')
        parts.append(f'<line x1="0" y1="{v}" x2="108" y2="{v}" stroke="#{INK}" stroke-width="{LINE_W}"/>')
    for a, b, c, d in cross_strokes():
        parts.append(f'<line x1="{fmt(a)}" y1="{fmt(b)}" x2="{fmt(c)}" y2="{fmt(d)}" stroke="#{RED}" stroke-width="{CROSS_W}" stroke-linecap="round"/>')
    for x, y in DOTS:
        parts.append(f'<circle cx="{fmt(x + CELL / 2)}" cy="{fmt(y + CELL / 2)}" r="{DOT_R}" fill="#{INK}"/>')
    inner = "".join(parts)
    if clip:
        inner = f'<defs>{clip}</defs><g clip-path="url(#m)">{inner}</g>'
    return (f'<svg xmlns="http://www.w3.org/2000/svg" width="{size}" height="{size}" viewBox="{view_box}">'
            f'{inner}</svg>')


if __name__ == "__main__":
    res, out = sys.argv[1], sys.argv[2]
    with open(os.path.join(res, "drawable", "ic_launcher_background.xml"), "w") as f:
        f.write(background())
    with open(os.path.join(res, "drawable", "ic_launcher_foreground.xml"), "w") as f:
        f.write(foreground(INK, RED, "B3"))
    # Themed icons: Android tints this single-colour layer; only its alpha matters.
    with open(os.path.join(res, "drawable", "ic_launcher_monochrome.xml"), "w") as f:
        f.write(foreground("FFFFFF", "FFFFFF", "8C"))
    os.makedirs(out, exist_ok=True)
    with open(os.path.join(out, "play_512.svg"), "w") as f:
        f.write(svg("18 18 72 72", 512))
    for size in (48, 72, 96, 144, 192):
        with open(os.path.join(out, f"legacy_square_{size}.svg"), "w") as f:
            f.write(svg("18 18 72 72", size, "squircle"))
        with open(os.path.join(out, f"legacy_round_{size}.svg"), "w") as f:
            f.write(svg("18 18 72 72", size, "circle"))
    print("generated")
