package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.domain.ImportParser
import com.example.domain.ScheduleLogic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Live Routine", appName)
    }

    @Test
    fun `test import parser in Robolectric`() {
        val sampleHtml = """
            <!DOCTYPE html>
            <html>
            <head><title>Test</title></head>
            <body>
            <script type="application/json" id="live-routine-data">
            {
              "app": "live-routine",
              "version": 1,
              "semester": { "name": "Fall 2026", "startDate": "2026-09-05", "endDate": "2026-12-31" },
              "courses": [
                { "code": "MAT 107", "title": "Mathematics", "section": "O", "credits": 4, "type": "theory", "instructor": { "name": "Arnab Mukherjee", "shortCode": "CAAS_AM" } }
              ],
              "classes": [
                { "day": "SAT", "start": "10:40", "end": "11:40", "courseCode": "MAT 107", "room": "805" }
              ]
            }
            </script>
            </body>
            </html>
        """.trimIndent()

        val parsed = ImportParser.parseHtmlContent(sampleHtml)
        assertTrue(parsed.isSuccess)
        val data = parsed.getOrNull()
        assertNotNull(data)
        assertEquals(1, data!!.courses.size)
        assertEquals(1, data.classes.size)
        assertEquals("MAT 107", data.courses[0].code)
    }
}
