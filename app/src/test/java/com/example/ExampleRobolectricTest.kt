package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.offline.OfflineKnowledgeEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("NOVA AI", appName)
    }

    @Test
    fun `verify offline knowledge engine responses`() {
        val greetingRes = OfflineKnowledgeEngine.query("hello", "en")
        assertTrue(greetingRes.text.contains("NOVA"))

        val stemRes = OfflineKnowledgeEngine.query("speed of light", "en")
        assertTrue(stemRes.text.contains("299,792,458"))

        val mathRes = OfflineKnowledgeEngine.query("25 * 4", "en")
        assertTrue(mathRes.text.contains("100"))
    }
}
