package com.avs.sea.battle

import android.graphics.drawable.AdaptiveIconDrawable
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppIconTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun launcherIconsAreAdaptive() {
        listOf(R.mipmap.ic_launcher, R.mipmap.ic_launcher_round).forEach { id ->
            assertTrue(context.getDrawable(id) is AdaptiveIconDrawable)
        }
    }

    @Test
    fun launcherIconsSupportThemedIcons() {
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        listOf(R.mipmap.ic_launcher, R.mipmap.ic_launcher_round).forEach { id ->
            assertNotNull((context.getDrawable(id) as AdaptiveIconDrawable).monochrome)
        }
    }
}
