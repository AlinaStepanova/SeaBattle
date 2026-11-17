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


FLEET_ORDER = [4, 3, 3, 2, 2, 2, 1, 1, 1, 1]


def fleet(ships, hits):
    """(size, sunk) per ship in FLEET order; within a size the leftmost ships are the sunk ones."""
    sunk_sizes = [len(s) for s in ships if all(c in hits for c in s)]
    return [(size, FLEET_ORDER[:i].count(size) < sunk_sizes.count(size)) for i, size in enumerate(FLEET_ORDER)]


def remaining(fleet, hits):
    return sorted(c for s in fleet for c in s if c not in hits)


validate_fleet(PERSON, "person")
validate_fleet(COMPUTER, "computer")

# Mid-game: the computer has sunk one ship of the player's and wounded another;
# the player has sunk two computer ships and wounded the four-deck one.
person_mid = board(PERSON, [(9, 2), (0, 2), (0, 3)], [(1, 5), (3, 3), (6, 6), (8, 1), (2, 9), (1, 1), (5, 5)])
computer_mid = board(COMPUTER, [(0, 3), (6, 5), (4, 3), (4, 4)], [(1, 6), (5, 8), (9, 0), (2, 6), (7, 3)])

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
    # The battle has no status text; the turn arrows show whose turn it is.
    "battle": dict(phase="BATTLE",
                   personBoardShips=sorted(all_person), personBoardCrosses=person_mid[0], personBoardDots=person_mid[1],
                   computerBoardCrosses=computer_mid[0], computerBoardDots=computer_mid[1],
                   personFleet=fleet(PERSON, person_mid[0]), computerFleet=fleet(COMPUTER, computer_mid[0])),
    "computer_turn": dict(phase="COMPUTER_TURN",
                          personBoardShips=sorted(all_person), personBoardCrosses=person_mid[0], personBoardDots=person_mid[1],
                          computerBoardCrosses=computer_mid[0], computerBoardDots=computer_mid[1],
                          personFleet=fleet(PERSON, person_mid[0]), computerFleet=fleet(COMPUTER, computer_mid[0])),
    "win": dict(phase="OVER", status="status_game_over_you_win_text", winner="PERSON",
                personBoardShips=sorted(all_person), personBoardCrosses=person_at_win[0], personBoardDots=person_at_win[1],
                computerBoardCrosses=computer_won[0], computerBoardDots=computer_won[1],
                personFleet=fleet(PERSON, person_at_win[0]), computerFleet=fleet(COMPUTER, computer_won[0])),
    "lose": dict(phase="OVER", status="status_game_over_you_lose_text", winner="COMPUTER",
                 personBoardShips=sorted(all_person), personBoardCrosses=person_lost[0], personBoardDots=person_lost[1],
                 computerBoardShips=computer_left, computerBoardCrosses=computer_at_loss[0], computerBoardDots=computer_at_loss[1],
                 personFleet=fleet(PERSON, person_lost[0]), computerFleet=fleet(COMPUTER, computer_at_loss[0])),
}


def state_kt(s):
    status = f"R.string.{s['status']}" if "status" in s else "null"
    parts = [f"phase = GamePhase.{s['phase']}", f"statusResId = {status}"]
    for key in ("personBoardShips", "selectedShipCells", "personBoardCrosses", "personBoardDots",
                "computerBoardShips", "computerBoardCrosses", "computerBoardDots"):
        if key in s:
            parts.append(f"{key} = {kt(s[key])}")
    for key in ("personFleet", "computerFleet"):
        if key in s:
            ships = ", ".join(f"FleetShipUi({size}, {'true' if sunk else 'false'})" for size, sunk in s[key])
            parts.append(f"{key} = listOf({ships})")
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onRoot
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.avs.sea.battle.R
import com.avs.sea.battle.battle_field.Coordinate
import com.avs.sea.battle.main.Player
import com.avs.sea.battle.ui.theme.SeaBattleTheme
import org.junit.Rule
import org.junit.Test
import org.json.JSONObject
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
                    onComputerCellClick = {{}},
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
        // Only the app's window: the paper continues behind the system bars, without their icons.
        val bitmap = rule.onRoot().captureToImage().asAndroidBitmap()
        val dir = File(rule.activity.getExternalFilesDir(null), "store")
        dir.mkdirs()
        val file = "${{language}}_${{orientation}}_${{theme}}_$name"
        File(dir, "$file.png").outputStream().use {{
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }}
        File(dir, "$file.json").writeText(layout(bitmap.width, bitmap.height).toString())
    }}

    // Where the game content and the system bars are in the capture, in pixels, so
    // crop_screenshot.py can crop around the content on any phone.
    private fun layout(width: Int, height: Int): JSONObject {{
        val insets = rule.runOnUiThread {{
            ViewCompat.getRootWindowInsets(rule.activity.window.decorView)!!
                .getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
        }}
        val boards = rule.onAllNodesWithTag(BOARD_GRID_TAG).fetchSemanticsNodes()
        val fleets = listOf(false, true).flatMap {{ rule.onAllNodesWithTag(fleetTrackerTag(it)).fetchSemanticsNodes() }}
        // Includes the boards' letter and number labels.
        val texts = rule.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Text), useUnmergedTree = true)
            .fetchSemanticsNodes()
        val content = (boards + fleets + texts).map {{ it.rootRect() }}.reduce {{ a, b ->
            Rect(minOf(a.left, b.left), minOf(a.top, b.top), maxOf(a.right, b.right), maxOf(a.bottom, b.bottom))
        }}
        return JSONObject()
            .put("width", width)
            .put("height", height)
            .put("insets", JSONObject().put("left", insets.left).put("top", insets.top).put("right", insets.right).put("bottom", insets.bottom))
            .put("content", content.json())
    }}

    // Not boundsInRoot: that clips, and an empty text slot then reports itself at (0, 0).
    private fun SemanticsNode.rootRect() = Rect(positionInRoot, Size(size.width.toFloat(), size.height.toFloat()))

    private fun Rect.json() = JSONObject().put("left", left).put("top", top).put("right", right).put("bottom", bottom)

{methods}}}
"""


if __name__ == "__main__":
    with open(sys.argv[1], "w") as f:
        f.write(test_class())
    print("states valid; test written:", ", ".join(STATES))
