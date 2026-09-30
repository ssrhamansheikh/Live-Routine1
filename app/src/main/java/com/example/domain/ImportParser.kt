package com.example.domain

import com.example.data.model.ClassSlotEntity
import com.example.data.model.CourseEntity
import com.example.data.model.Semester
import org.json.JSONObject
import java.time.LocalTime
import java.util.UUID

data class ParsedRoutine(
    val semester: Semester?,
    val courses: List<CourseEntity>,
    val classes: List<ClassSlotEntity>,
    val warnings: List<String>
)

object ImportParser {

    const val IMPORT_PROMPT_TEXT = """You are a data formatter for the "Live Routine" app. I will give you my university class routine (as text, a photo description, or a table). Convert it into ONE complete HTML file and output ONLY the raw HTML code, nothing else, no explanations, no markdown fences.

The HTML file must have exactly this structure:

<!DOCTYPE html>
<html>
<head><meta charset="utf-8"><title>Live Routine Import</title></head>
<body>
<h1>My Routine</h1>
<script type="application/json" id="live-routine-data">
{
  "app": "live-routine",
  "version": 1,
  "semester": { "name": "Fall 2026", "startDate": "YYYY-MM-DD", "endDate": "YYYY-MM-DD" },
  "courses": [
    { "code": "MAT 107", "title": "Mathematics", "section": "O", "credits": 4, "type": "theory", "instructor": { "name": "Full Name", "shortCode": "SHORT_CODE" } }
  ],
  "classes": [
    { "day": "SAT", "start": "10:40", "end": "11:40", "courseCode": "MAT 107", "room": "805" }
  ]
}
</script>
</body>
</html>

Rules:
1. Output valid JSON only inside the script tag (double quotes, no comments, no trailing commas).
2. "day" must be one of SAT, SUN, MON, TUE, WED, THU, FRI.
3. Times use 24-hour "HH:mm". The end time must be later than the start time.
4. "type" is "theory" or "lab". Lab courses are separate courses with their own code.
5. Every "courseCode" in classes must exactly match a "code" in courses.
6. Keep the instructor short code as a separate field. If I did not give one, build it from the department and the instructor's initials.
7. If a lab or class runs across two consecutive periods, create two separate class entries.
8. If any information is missing (semester dates, room, credits, section), ask me first, then produce the file. Do not guess.
9. Inside the body you may also add a simple HTML table of the routine for human reading, but the script tag is what the app reads.

Here is my routine:
[PASTE YOUR ROUTINE HERE]"""

    private val VALID_DAYS = setOf("SAT", "SUN", "MON", "TUE", "WED", "THU", "FRI")

    fun parseHtmlContent(content: String): Result<ParsedRoutine> {
        return try {
            val jsonString = extractJsonFromHtml(content)
                ?: return Result.failure(Exception("Could not find <script id=\"live-routine-data\"> in HTML. Please verify the file format."))

            val root = JSONObject(jsonString)

            // Parse Semester if present
            var semester: Semester? = null
            if (root.has("semester")) {
                val semObj = root.getJSONObject("semester")
                semester = Semester(
                    name = semObj.optString("name", "New Semester"),
                    startDate = semObj.optString("startDate", "2026-09-05"),
                    endDate = semObj.optString("endDate", "2026-12-31")
                )
            }

            // Parse Courses
            if (!root.has("courses")) {
                return Result.failure(Exception("Missing \"courses\" array in data."))
            }
            val coursesArray = root.getJSONArray("courses")
            val courses = mutableListOf<CourseEntity>()
            val courseCodes = mutableSetOf<String>()

            for (i in 0 until coursesArray.length()) {
                val cObj = coursesArray.getJSONObject(i)
                val code = cObj.getString("code").trim()
                if (courseCodes.contains(code)) {
                    return Result.failure(Exception("Duplicate course code found: $code"))
                }
                courseCodes.add(code)

                val title = cObj.getString("title").trim()
                val section = cObj.optString("section", "A").trim()
                val credits = cObj.optDouble("credits", 3.0)
                val type = cObj.optString("type", "theory").lowercase().trim()
                if (type != "theory" && type != "lab") {
                    return Result.failure(Exception("Invalid type '$type' for course $code. Must be 'theory' or 'lab'."))
                }

                val instructor = cObj.optJSONObject("instructor")
                val instName = instructor?.optString("name") ?: cObj.optString("instructorName", "Instructor")
                val shortCode = instructor?.optString("shortCode") ?: cObj.optString("instructorShortCode", "")

                courses.add(
                    CourseEntity(
                        code = code,
                        title = title,
                        section = section,
                        credits = credits,
                        type = type,
                        instructorName = instName,
                        instructorShortCode = shortCode
                    )
                )
            }

            // Parse Classes
            if (!root.has("classes")) {
                return Result.failure(Exception("Missing \"classes\" array in data."))
            }
            val classesArray = root.getJSONArray("classes")
            val classes = mutableListOf<ClassSlotEntity>()
            val warnings = mutableListOf<String>()

            for (i in 0 until classesArray.length()) {
                val sObj = classesArray.getJSONObject(i)
                val day = sObj.getString("day").uppercase().trim()
                if (!VALID_DAYS.contains(day)) {
                    return Result.failure(Exception("Invalid day '$day'. Must be one of SAT, SUN, MON, TUE, WED, THU, FRI."))
                }

                val start = sObj.getString("start").trim()
                val end = sObj.getString("end").trim()
                val courseCode = sObj.getString("courseCode").trim()
                val room = sObj.getString("room").trim()

                // Validate course exists
                if (!courseCodes.contains(courseCode)) {
                    return Result.failure(Exception("Class refers to unknown courseCode '$courseCode' not listed in courses."))
                }

                // Validate 24h format and end > start
                val startTime = try {
                    LocalTime.parse(start)
                } catch (_: Exception) {
                    return Result.failure(Exception("Invalid start time '$start'. Expected HH:mm in 24h format."))
                }

                val endTime = try {
                    LocalTime.parse(end)
                } catch (_: Exception) {
                    return Result.failure(Exception("Invalid end time '$end'. Expected HH:mm in 24h format."))
                }

                if (!endTime.isAfter(startTime)) {
                    return Result.failure(Exception("End time '$end' must be later than start time '$start' for course $courseCode on $day."))
                }

                val slotId = sObj.optString("id", "${day.lowercase()}_slot_${i + 1}")
                classes.add(
                    ClassSlotEntity(
                        id = slotId,
                        day = day,
                        start = start,
                        end = end,
                        courseCode = courseCode,
                        room = room
                    )
                )
            }

            // Check for overlapping classes
            val dayGroups = classes.groupBy { it.day }
            for ((day, daySlots) in dayGroups) {
                val sorted = daySlots.sortedBy { it.start }
                for (j in 0 until sorted.size - 1) {
                    val cur = sorted[j]
                    val next = sorted[j + 1]
                    val curEnd = LocalTime.parse(cur.end)
                    val nextStart = LocalTime.parse(next.start)
                    if (curEnd.isAfter(nextStart)) {
                        warnings.add("Overlap on $day: ${cur.courseCode} (${cur.start}-${cur.end}) overlaps with ${next.courseCode} (${next.start}-${next.end})")
                    }
                }
            }

            Result.success(
                ParsedRoutine(
                    semester = semester,
                    courses = courses,
                    classes = classes,
                    warnings = warnings
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception("Invalid file, please regenerate it with the prompt above. (${e.message})"))
        }
    }

    private fun extractJsonFromHtml(html: String): String? {
        val pattern = Regex(
            """<script[^>]*id=["']live-routine-data["'][^>]*>([\s\S]*?)<\/script>""",
            RegexOption.IGNORE_CASE
        )
        val match = pattern.find(html)
        if (match != null) {
            return match.groupValues[1].trim()
        }

        // Alternative regex if attributes are in reverse order
        val altPattern = Regex(
            """<script[^>]*id=["']live-routine-data["'][^>]*>([\s\S]*?)<\/script>""",
            RegexOption.IGNORE_CASE
        )
        val altMatch = altPattern.find(html)
        if (altMatch != null) {
            return altMatch.groupValues[1].trim()
        }

        // Strip markdown fences
        var cleaned = html.trim()
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.substringAfter("\n").substringBeforeLast("```").trim()
        }

        // Direct JSON with courses and classes
        if (cleaned.startsWith("{") && cleaned.endsWith("}")) {
            if (cleaned.contains("\"courses\"") && cleaned.contains("\"classes\"")) {
                return cleaned
            }
        }

        // Search for JSON block inside text
        val jsonBlockMatch = Regex("""\{[\s\S]*"courses"[\s\S]*"classes"[\s\S]*\}""").find(cleaned)
        if (jsonBlockMatch != null) {
            return jsonBlockMatch.value.trim()
        }

        return null
    }

    const val SAMPLE_AGRI_IMPORT_HTML = """<!DOCTYPE html>
<html>
<head><meta charset="utf-8"><title>BSAg 262 Live Routine Import</title></head>
<body>
<h1>BSc Agriculture 2nd Semester Timetable (BSAg 262)</h1>
<script type="application/json" id="live-routine-data">
{
  "app": "live-routine",
  "version": 1,
  "semester": {
    "name": "Fall 2026",
    "startDate": "2026-10-03",
    "endDate": "2027-02-28"
  },
  "courses": [
    { "code": "MAT 107", "title": "Mathematics", "section": "O", "credits": 4, "type": "theory", "instructor": { "name": "Arnab Mukherjee", "shortCode": "CAAS_AM" } },
    { "code": "ENG 102", "title": "English Comprehension and Speaking", "section": "Z", "credits": 3, "type": "theory", "instructor": { "name": "Ms. Fatema Tasnim", "shortCode": "DEML_FT" } },
    { "code": "BOT 107", "title": "Crop Botany", "section": "C", "credits": 3, "type": "theory", "instructor": { "name": "Dr. Syada Nizer Sultana", "shortCode": "BSAg_DSNS" } },
    { "code": "BOT 108", "title": "Crop Botany Lab", "section": "C", "credits": 1, "type": "lab", "instructor": { "name": "Dr. Syada Nizer Sultana", "shortCode": "BSAg_DSNS" } },
    { "code": "AGR 101", "title": "Agronomy", "section": "E", "credits": 3, "type": "theory", "instructor": { "name": "Dr. Mohammad Rezaul Karim", "shortCode": "BSAg_DMRK" } },
    { "code": "AGR 102", "title": "Agronomy Lab", "section": "E", "credits": 1, "type": "lab", "instructor": { "name": "Dr. Mohammad Rezaul Karim", "shortCode": "BSAg_DMRK" } }
  ],
  "classes": [
    { "day": "SAT", "start": "10:40", "end": "11:40", "courseCode": "BOT 107", "room": "601" },
    { "day": "SAT", "start": "13:10", "end": "14:10", "courseCode": "AGR 101", "room": "521" },
    { "day": "SAT", "start": "15:20", "end": "16:20", "courseCode": "MAT 107", "room": "805" },

    { "day": "SUN", "start": "10:40", "end": "11:40", "courseCode": "BOT 107", "room": "520" },
    { "day": "SUN", "start": "13:10", "end": "14:10", "courseCode": "AGR 101", "room": "521" },
    { "day": "SUN", "start": "15:20", "end": "16:20", "courseCode": "MAT 107", "room": "805" },
    { "day": "SUN", "start": "16:25", "end": "17:25", "courseCode": "ENG 102", "room": "307" },

    { "day": "MON", "start": "10:40", "end": "11:40", "courseCode": "BOT 107", "room": "1003" },
    { "day": "MON", "start": "13:10", "end": "14:10", "courseCode": "AGR 101", "room": "310" },

    { "day": "TUE", "start": "10:40", "end": "11:40", "courseCode": "BOT 108", "room": "AGRIlab4" },
    { "day": "TUE", "start": "11:45", "end": "12:45", "courseCode": "BOT 108", "room": "AGRIlab4" },
    { "day": "TUE", "start": "15:20", "end": "16:20", "courseCode": "MAT 107", "room": "805" },
    { "day": "TUE", "start": "16:25", "end": "17:25", "courseCode": "ENG 102", "room": "307" },

    { "day": "WED", "start": "13:10", "end": "14:10", "courseCode": "AGR 102", "room": "AGRIlab1" },
    { "day": "WED", "start": "14:15", "end": "15:15", "courseCode": "AGR 102", "room": "AGRIlab1" },
    { "day": "WED", "start": "15:20", "end": "16:20", "courseCode": "MAT 107", "room": "805" },
    { "day": "WED", "start": "16:25", "end": "17:25", "courseCode": "ENG 102", "room": "307" }
  ]
}
</script>
</body>
</html>"""
}
