package com.avs.sea_battle.main


import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.MediumTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.avs.sea_battle.R
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@MediumTest
@RunWith(AndroidJUnit4::class)
class MainActivityStartTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun mainActivityStartTest() {
        onView(withId(R.id.viewPerson)).check(matches(isDisplayed()))
        onView(withId(R.id.viewComputer)).check(matches(isDisplayed()))
        onView(withId(R.id.ivMore)).check(matches(isDisplayed()))

        val tvGenerateButton = onView(
            allOf(
                withId(R.id.viewGenerate), withText(R.string.generate_ships_text),
                isDisplayed()
            )
        )
        tvGenerateButton.check(matches(withText(R.string.generate_ships_text)))

        val tvStatus = onView(
            allOf(
                withId(R.id.tvStatus), withText(R.string.status_welcome_text),
                isDisplayed()
            )
        )
        tvStatus.check(matches(withText(R.string.status_welcome_text)))
    }
}
