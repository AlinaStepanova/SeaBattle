"""Builds consistent, validated game positions for store screenshots and writes them as a
temporary instrumented test (usage: gen_screenshot_test.py <path to StoreScreenshots.kt>).
"""
import sys

PERSON = [
    [(0, 1), (0, 2), (0, 3), (0, 4)],
    [(2, 7), (3, 7), (4, 7)],
    [(7, 4), (7, 5), (7, 6)],
    [(5, 0), (6, 0)],
    [(9, 6), (9, 7)],
    [(2, 2), (3, 2)],
    [(9, 9)], [(9, 2)], [(4, 4)], [(5, 9)],
]
COMPUTER = [
    [(4, 2), (4, 3), (4, 4), (4, 5)],
    [(0, 0), (1, 0), (2, 0)],
    [(8, 7), (8, 8), (8, 9)],
    [(0, 6), (0, 7)],
    [(2, 8), (3, 8)],
    [(7, 1), (8, 1)],
    [(0, 3)], [(6, 5)], [(9, 4)], [(2, 4)],
]


def cells(fleet):
    return {c: i for i, s in enumerate(fleet) for c in s}


def validate_fleet(fleet, name):
    sizes = sorted(len(s) for s in fleet)
    assert sizes == [1, 1, 1, 1, 2, 2, 2, 3, 3, 4], f"{name}: wrong fleet {sizes}"
    cm = cells(fleet)
    assert len(cm) == 20, f"{name}: overlap"
    for i, ship in enumerate(fleet):
        rows = {r for r, _ in ship}; cols = {c for _, c in ship}
        assert len(rows) == 1 or len(cols) == 1, f"{name}: {ship} not straight"
        for r, c in ship:
            assert 0 <= r < 10 and 0 <= c < 10
            for dr in (-1, 0, 1):
                for dc in (-1, 0, 1):
                    o = cm.get((r + dr, c + dc))
                    assert o is None or o == i, f"{name}: {ship} touches {fleet[o]}"


def around(ship):
    s = set(ship)
    return {(r + dr, c + dc) for r, c in ship for dr in (-1, 0, 1) for dc in (-1, 0, 1)
            if 0 <= r + dr < 10 and 0 <= c + dc < 10 and (r + dr, c + dc) not in s}


def board(fleet, hits, misses):
    """Crosses on hit ship cells; dots on misses plus around every fully sunk ship."""
    cm = cells(fleet)
    for h in hits:
        assert h in cm, f"hit {h} not on a ship"
    for m in misses:
        assert m not in cm, f"miss {m} on a ship"
    dots = set(misses)
    for ship in fleet:
        if all(c in hits for c in ship):
            dots |= around(ship)
    return sorted(hits), sorted(dots)


def remaining(fleet, hits):
    return sorted(c for s in fleet for c in s if c not in hits)


validate_fleet(PERSON, "person")
validate_fleet(COMPUTER, "computer")

# Mid-game: the computer has sunk one ship of the player's and wounded another;
# the player has sunk two computer ships and wounded the four-deck one.
person_mid = board(PERSON, [(9, 2), (0, 2), (0, 3)], [(1, 5), (3, 3), (6, 6), (8, 1), (2, 9), (1, 1), (5, 5)])
computer_mid = board(COMPUTER, [(0, 3), (6, 5), (4, 3), (4, 4)], [(1, 6), (5, 8), (9, 0), (2, 6), (7, 3)])
selected = (4, 5)
assert selected not in set(computer_mid[0]) | set(computer_mid[1])

# The player wins: every computer ship sunk.
all_computer = [c for s in COMPUTER for c in s]
computer_won = board(COMPUTER, all_computer, [(1, 6), (5, 8), (9, 0), (2, 6), (7, 3), (6, 8)])
person_at_win = board(PERSON, [(9, 2), (0, 2), (0, 3), (5, 0)], [(1, 5), (3, 3), (6, 6), (8, 1), (2, 9), (1, 1), (5, 5), (8, 3), (3, 5)])

# The player loses: every player ship sunk; the computer's untouched ships are revealed.
all_person = [c for s in PERSON for c in s]
person_lost = board(PERSON, all_person, [(1, 5), (3, 3), (6, 6), (8, 1), (8, 3), (6, 7)])
computer_at_loss_hits = [(0, 3), (6, 5), (4, 3), (4, 4), (4, 2), (0, 0), (1, 0)]
computer_at_loss = board(COMPUTER, computer_at_loss_hits, [(1, 6), (5, 8), (9, 0), (2, 6), (7, 3), (6, 9), (3, 5)])
computer_left = remaining(COMPUTER, computer_at_loss_hits)


def kt(cs):
    return "listOf(" + ", ".join(f"Coordinate({r}, {c})" for r, c in cs) + ")"


STATES = {
    "placing": dict(phase="PLACING", status="status_place_ships_text",
                    personBoardShips=sorted(all_person), selectedShipCells=sorted(PERSON[0])),
    "battle": dict(phase="BATTLE", status="status_select_to_fire_text",
                   personBoardShips=sorted(all_person), personBoardCrosses=person_mid[0], personBoardDots=person_mid[1],
                   computerBoardCrosses=computer_mid[0], computerBoardDots=computer_mid[1], selected=selected),
    "computer_turn": dict(phase="COMPUTER_TURN", status="status_opponent_shot_text",
                          personBoardShips=sorted(all_person), personBoardCrosses=person_mid[0], personBoardDots=person_mid[1],
                          computerBoardCrosses=computer_mid[0], computerBoardDots=computer_mid[1]),
    "win": dict(phase="OVER", status="status_game_over_you_win_text", winner="PERSON",
                personBoardShips=sorted(all_person), personBoardCrosses=person_at_win[0], personBoardDots=person_at_win[1],
                computerBoardCrosses=computer_won[0], computerBoardDots=computer_won[1]),
    "lose": dict(phase="OVER", status="status_game_over_you_lose_text", winner="COMPUTER",
                 personBoardShips=sorted(all_person), personBoardCrosses=person_lost[0], personBoardDots=person_lost[1],
                 computerBoardShips=computer_left, computerBoardCrosses=computer_at_loss[0], computerBoardDots=computer_at_loss[1]),
}


def state_kt(s):
    parts = [f"phase = GamePhase.{s['phase']}", f"statusResId = R.string.{s['status']}"]
    for key in ("personBoardShips", "selectedShipCells", "personBoardCrosses", "personBoardDots",
                "computerBoardShips", "computerBoardCrosses", "computerBoardDots"):
        if key in s:
            parts.append(f"{key} = {kt(s[key])}")
    if "selected" in s:
        parts.append(f"selectedCoordinate = Coordinate{tuple(s['selected'])}")
    if "winner" in s:
        parts.append(f"winner = Player.{s['winner']}")
    return "GameUiState(\n            " + ",\n            ".join(parts) + ",\n        )"


def test_class():
    methods = "\n".join(
        f"""    @Test
    fun {name}() = capture("{name}", {state_kt(s)})
""" for name, s in STATES.items())
    return f"""package com.avs.sea.battle.ui

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.avs.sea.battle.R
import com.avs.sea.battle.battle_field.Coordinate
import com.avs.sea.battle.main.Player
import com.avs.sea.battle.ui.theme.SeaBattleTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

// Generated by tools/store-assets/gen_screenshot_test.py for Play Store screenshots. Do not commit.
@RunWith(AndroidJUnit4::class)
class StoreScreenshots {{

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private fun capture(name: String, state: GameUiState) {{
        rule.runOnUiThread {{ rule.activity.enableEdgeToEdge() }}
        rule.setContent {{
            SeaBattleTheme {{
                GameScreen(
                    uiState = state,
                    onGenerateShips = {{}},
                    onStartGame = {{}},
                    onNewGame = {{}},
                    onCellClick = {{}},
                    onFire = {{}},
                    onMenuAction = {{}},
                    onPersonCellClick = {{}},
                    onConfirmNewGame = {{}},
                    onDismissNewGame = {{}},
                )
            }}
        }}
        rule.waitForIdle()
        Thread.sleep(1500)
        val config = rule.activity.resources.configuration
        val theme = if (config.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) "dark" else "light"
        val orientation = if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) "landscape" else "portrait"
        val language = config.locales[0].language
        val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        val dir = File(rule.activity.getExternalFilesDir(null), "store")
        dir.mkdirs()
        File(dir, "${{language}}_${{orientation}}_${{theme}}_$name.png").outputStream().use {{
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }}
    }}

{methods}}}
"""


if __name__ == "__main__":
    with open(sys.argv[1], "w") as f:
        f.write(test_class())
    print("states valid; test written:", ", ".join(STATES))
