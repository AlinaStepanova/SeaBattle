package com.avs.sea.battle

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class GameOverMessagesTest {

    // Unit tests run from the module directory, so the resources can be read directly.
    private fun strings(folder: String): Map<String, String> {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(File("src/main/res/$folder/strings.xml"))
        val nodes = document.getElementsByTagName("string")
        return (0 until nodes.length).associate { i ->
            val node = nodes.item(i)
            node.attributes.getNamedItem("name").nodeValue to node.textContent
        }
    }

    @Test
    fun englishGameOverMessagesSeparateTheClausesWithAComma() {
        val strings = strings("values")
        assertEquals("Game over, you win!", strings["status_game_over_you_win_text"])
        assertEquals("Game over, you lose", strings["status_game_over_you_lose_text"])
    }

    @Test
    fun ukrainianGameOverMessagesSeparateTheClausesWithAComma() {
        val strings = strings("values-uk")
        assertEquals("Гра завершена, ви перемогли!", strings["status_game_over_you_win_text"])
        assertEquals("Гра завершена, ви програли", strings["status_game_over_you_lose_text"])
    }

    @Test
    fun russianGameOverMessagesSeparateTheClausesWithAComma() {
        val strings = strings("values-ru")
        assertEquals("Игра окончена, вы победили!", strings["status_game_over_you_win_text"])
        assertEquals("Игра окончена, вы проиграли", strings["status_game_over_you_lose_text"])
    }
}
