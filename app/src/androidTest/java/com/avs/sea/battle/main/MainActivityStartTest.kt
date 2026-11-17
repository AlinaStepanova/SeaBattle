package com.avs.sea.battle.main

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.avs.sea.battle.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@MediumTest
@RunWith(AndroidJUnit4::class)
class MainActivityStartTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun mainActivityStartTest() {
        val activity = composeTestRule.activity
        composeTestRule.onNodeWithText(activity.getString(R.string.status_place_ships_text))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(activity.getString(R.string.shuffle_ships_text))
            .assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.more_settings))
            .assertIsDisplayed()
    }
}
