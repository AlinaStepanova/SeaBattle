"""Builds the Play Store feature graphic (1024x500) in English, Ukrainian and Russian.

Validates the board first: a legal fleet (1x4, 2x3, 3x2, 4x1), no ships touching even
diagonally, hits only on ships, misses only on water, and a sunk ship fully surrounded by misses.
"""
import json
import os
import sys

SHIPS = [
    [(0, 1), (0, 2), (0, 3), (0, 4)],
    [(2, 7), (3, 7), (4, 7)],
    [(7, 4), (7, 5), (7, 6)],
    [(5, 0), (6, 0)],
    [(9, 6), (9, 7)],
    [(2, 2), (3, 2)],
    [(9, 9)],
    [(9, 2)],
    [(4, 4)],
    [(5, 9)],
]
HITS = [(0, 2), (0, 3), (5, 0), (4, 4)]
MISSES = [(1, 5), (3, 3), (6, 6), (8, 1), (2, 9), (1, 1), (0, 0), (1, 4),
          (3, 4), (3, 5), (4, 3), (4, 5), (5, 3), (5, 4), (5, 5)]  # last eight surround the sunk (4,4)
SUNK = [[(4, 4)]]

LANGS = {
    "en": {"title": "Sea Battle", "subtitle": "the classic game from your childhood", "letters": "ABCDEFGHIJ"},
    "uk": {"title": "Морський Бій", "subtitle": "класична гра з твого дитинства", "letters": "АБВГҐДЄЖЗИ"},
    "ru": {"title": "Морской Бой", "subtitle": "классическая игра из твоего детства", "letters": "АБВГДЕЖЗИК"},
}


def validate():
    sizes = sorted(len(s) for s in SHIPS)
    assert sizes == [1, 1, 1, 1, 2, 2, 2, 3, 3, 4], f"wrong fleet: {sizes}"
    cells = {c: i for i, s in enumerate(SHIPS) for c in s}
    assert len(cells) == 20, "overlapping ships"
    for i, ship in enumerate(SHIPS):
        rows = {r for r, _ in ship}; cols = {c for _, c in ship}
        assert len(rows) == 1 or len(cols) == 1, f"ship {ship} is not straight"
        for r, c in ship:
            for dr in (-1, 0, 1):
                for dc in (-1, 0, 1):
                    other = cells.get((r + dr, c + dc))
                    assert other is None or other == i, f"ships {ship} and {SHIPS[other]} touch"
    for h in HITS:
        assert h in cells, f"hit {h} is not on a ship"
    for m in MISSES:
        assert m not in cells, f"miss {m} is on a ship"
    for ship in SUNK:
        assert all(c in HITS for c in ship), "sunk ship not fully hit"
        for r, c in ship:
            for dr in (-1, 0, 1):
                for dc in (-1, 0, 1):
                    n = (r + dr, c + dc)
                    if n not in ship and 0 <= n[0] < 10 and 0 <= n[1] < 10:
                        assert n in MISSES, f"cell {n} around sunk ship is not marked"


def page(lang, font_path):
    data = dict(LANGS[lang])
    data.update(ships=[c for s in SHIPS for c in s], hits=HITS, misses=MISSES)
    return """<!doctype html><html><head><meta charset="utf-8"><style>
@font-face { font-family: Neucha; src: url('file://%s'); }
html, body { margin: 0; background: #FCFBF6; }
</style></head><body>
<svg id="b" xmlns="http://www.w3.org/2000/svg" width="1024" height="500" viewBox="0 0 1024 500"></svg>
<script>
const D = %s;
const NS = 'http://www.w3.org/2000/svg', svg = document.getElementById('b');
const PAPER = '#FCFBF6', INK = '#2A4DA8', RED = '#B23A30', PAGE = 'rgba(96,140,190,.30)';
const C = 34, BX = 594, BY = 94;
function el(t, a, p) { const e = document.createElementNS(NS, t); for (const k in a) e.setAttribute(k, a[k]); (p || svg).appendChild(e); return e; }
function txt(x, y, s, size, extra) { const t = el('text', Object.assign({ x, y, 'font-family': 'Neucha', 'font-size': size, fill: INK, 'text-anchor': 'middle' }, extra || {})); t.textContent = s; return t; }
el('rect', { width: 1024, height: 500, fill: PAPER });
for (let x = BX %% C; x <= 1024; x += C) el('line', { x1: x, y1: 0, x2: x, y2: 500, stroke: PAGE, 'stroke-width': 1 });
for (let y = BY %% C; y <= 500; y += C) el('line', { x1: 0, y1: y, x2: 1024, y2: y, stroke: PAGE, 'stroke-width': 1 });
D.ships.forEach(([r, k], i) => {
  const cp = el('clipPath', { id: 's' + i }); el('rect', { x: BX + k * C, y: BY + r * C, width: C, height: C }, cp);
  const g = el('g', { 'clip-path': 'url(#s' + i + ')' });
  for (let j = 1; j < 8; j++) { const d = j * C / 4; el('line', { x1: BX + k * C, y1: BY + r * C + d, x2: BX + k * C + d, y2: BY + r * C, stroke: INK, 'stroke-opacity': .7, 'stroke-width': 2, 'stroke-linecap': 'round' }, g); }
});
for (let i = 0; i <= 10; i++) {
  el('line', { x1: BX + i * C, y1: BY, x2: BX + i * C, y2: BY + 10 * C, stroke: INK, 'stroke-width': 1.5 });
  el('line', { x1: BX, y1: BY + i * C, x2: BX + 10 * C, y2: BY + i * C, stroke: INK, 'stroke-width': 1.5 });
}
D.letters.split('').forEach((l, i) => txt(BX + i * C + C / 2, BY - 10, l, 21));
for (let i = 0; i < 10; i++) txt(BX - 17, BY + i * C + 24, String(i + 1), 21);
D.misses.forEach(([r, k]) => el('circle', { cx: BX + k * C + C / 2, cy: BY + r * C + C / 2, r: 2.4, fill: INK }));
D.hits.forEach(([r, k]) => { const i = C * .1, X = BX + k * C, Y = BY + r * C;
  el('path', { d: `M${X+i} ${Y+i}L${X+C-i} ${Y+C-i}M${X+C-i} ${Y+i}L${X+i} ${Y+C-i}`, stroke: RED, 'stroke-width': 2.6, 'stroke-linecap': 'round', fill: 'none' }); });
document.fonts.load('104px Neucha').then(() => {
  const MAXW = 500, CX = 290;
  const title = txt(CX, 235, D.title, 104);
  const tw = title.getComputedTextLength(); if (tw > MAXW) title.setAttribute('font-size', 104 * MAXW / tw);
  // Subtitle: one line when it fits, otherwise two balanced lines.
  const sub = txt(CX, 300, D.subtitle, 34);
  if (sub.getComputedTextLength() > MAXW) {
    sub.remove();
    const w = D.subtitle.split(' '); let best = 1, bestDiff = 1e9;
    for (let i = 1; i < w.length; i++) { const d = Math.abs(w.slice(0, i).join(' ').length - w.slice(i).join(' ').length); if (d < bestDiff) { bestDiff = d; best = i; } }
    txt(CX, 295, w.slice(0, best).join(' '), 34); txt(CX, 337, w.slice(best).join(' '), 34);
  }
  document.title = 'ready';
});
</script></body></html>""" % (font_path, json.dumps(data, ensure_ascii=False))


if __name__ == "__main__":
    validate()
    font, out = sys.argv[1], sys.argv[2]
    os.makedirs(out, exist_ok=True)
    for lang in LANGS:
        with open(os.path.join(out, f"banner_{lang}.html"), "w") as f:
            f.write(page(lang, font))
    print("board valid; pages written")
