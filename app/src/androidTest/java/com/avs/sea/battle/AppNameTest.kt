package com.avs.sea.battle

import android.content.res.Configuration
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class AppNameTest {

    private fun appName(language: String): String {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val configuration = Configuration(context.resources.configuration).apply { setLocale(Locale(language)) }
        return context.createConfigurationContext(configuration).getString(R.string.app_name)
    }

    @Test
    fun appNameIsLocalized() {
        assertEquals("Sea Battle", appName("en"))
        assertEquals("Морський Бій", appName("uk"))
        assertEquals("Морской Бой", appName("ru"))
    }
}
