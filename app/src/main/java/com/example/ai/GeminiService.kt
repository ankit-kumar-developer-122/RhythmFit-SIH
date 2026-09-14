package com.example.ai

import android.util.Log
import com.example.BuildConfig
import com.example.data.local.entity.UserProfile
import com.example.engine.CalorieEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * MODULE 3: Gemini AI Service.
 * Uses Direct REST API with OkHttp per gemini-api skill (Option B).
 * Direct, lightweight, and avoids Ktor class-loading conflicts.
 * Uses gemini-3.5-flash model with deterministic offline heuristic fallbacks.
 */
class GeminiService(
    private val modelName: String = "gemini-3.5-flash"
) {
    private val tag = "GeminiService"

    private val apiKey: String
        get() = try {
            BuildConfig.GEMINI_API_KEY.takeIf { it.isNotBlank() && it != "MY_GEMINI_API_KEY" }
                ?: ""
        } catch (e: Throwable) {
            ""
        }

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Executes generateContent request against Google Generative Language REST API.
     */
    private suspend fun callGeminiApi(prompt: String): String? = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            Log.d(tag, "Gemini API key is blank. Using offline heuristic.")
            return@withContext null
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

            val jsonBody = JSONObject().apply {
                val parts = JSONArray().apply {
                    put(JSONObject().apply { put("text", prompt) })
                }
                val contents = JSONArray().apply {
                    put(JSONObject().apply { put("parts", parts) })
                }
                put("contents", contents)
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody(jsonMediaType))
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.w(tag, "Gemini API response unsuccessful: code=${response.code}")
                    return@withContext null
                }

                val bodyString = response.body?.string() ?: return@withContext null
                val rootJson = JSONObject(bodyString)
                val candidates = rootJson.optJSONArray("candidates") ?: return@withContext null
                if (candidates.length() == 0) return@withContext null

                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content") ?: return@withContext null
                val responseParts = content.optJSONArray("parts") ?: return@withContext null
                if (responseParts.length() == 0) return@withContext null

                return@withContext responseParts.getJSONObject(0).optString("text", "").trim()
            }
        } catch (e: Exception) {
            Log.w(tag, "Gemini API call failed: ${e.message}")
            return@withContext null
        }
    }

    /**
     * Parses free-text user entry into structured JSON:
     * { "activity": String, "durationMins": Int, "estimatedKcal": Int, "tags": List<String> }
     */
    suspend fun parseFreeTextLog(
        text: String,
        userProfile: UserProfile
    ): ParsedLogResult = withContext(Dispatchers.IO) {
        val prompt = """
            You are a health data extractor. Extract fitness activity from user input into strictly valid JSON.
            Input: "$text"
            User Profile: Weight=${userProfile.weightKg}kg, Age=${userProfile.age}, Gender=${userProfile.gender}.
            
            Return ONLY valid JSON with this exact schema:
            {
              "activity": "Short activity name",
              "durationMins": 30,
              "estimatedKcal": 120,
              "tags": ["tag1", "tag2"],
              "steps": 0
            }
            No markdown formatting, no code blocks, only the JSON object.
        """.trimIndent()

        val responseText = callGeminiApi(prompt)
        if (!responseText.isNullOrBlank()) {
            try {
                val cleanJson = responseText
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()

                val json = JSONObject(cleanJson)
                val activity = json.optString("activity", text.take(30))
                val duration = json.optInt("durationMins", 15)
                val kcalFromJson = json.optInt("estimatedKcal", 0)
                val steps = json.optInt("steps", 0)

                val tagsList = mutableListOf<String>()
                val tagsArr = json.optJSONArray("tags")
                if (tagsArr != null) {
                    for (i in 0 until tagsArr.length()) {
                        tagsList.add(tagsArr.getString(i))
                    }
                }

                // Verify calorie expenditure with local MET engine if AI returned 0
                val met = CalorieEngine.resolveMet(activity, tagsList)
                val finalKcal = if (kcalFromJson > 0) kcalFromJson else CalorieEngine.calculateExpenditure(met, duration, userProfile)

                return@withContext ParsedLogResult(
                    activity = activity,
                    durationMins = duration,
                    estimatedKcal = finalKcal,
                    tags = tagsList,
                    steps = steps
                )
            } catch (e: Exception) {
                Log.w(tag, "Failed parsing Gemini JSON, falling back to local heuristic: ${e.message}")
            }
        }

        // OFFLINE FALLBACK: Deterministic NLP extractor & MET Calorie computation
        return@withContext parseOfflineHeuristic(text, userProfile)
    }

    /**
     * Generates a context-aware micro-routine (2-5 mins) adapted to user mood & duration.
     */
    suspend fun generateContextualRoutine(
        mood: String,
        durationMins: Int,
        userProfile: UserProfile
    ): MicroRoutine = withContext(Dispatchers.IO) {
        val clampedDuration = durationMins.coerceIn(2, 5)

        val prompt = """
            Generate a quick $clampedDuration-minute adaptive micro-routine for a user who feels "$mood".
            Return ONLY valid JSON matching this schema:
            {
              "title": "Routine Title",
              "category": "Mobility/Calm/Posture/Energy",
              "met": 2.8,
              "steps": [
                {
                  "title": "Step 1",
                  "instruction": "Detailed physical guide",
                  "durationSeconds": 45,
                  "audioCue": "Spoken TTS audio prompt"
                }
              ]
            }
        """.trimIndent()

        val responseText = callGeminiApi(prompt)
        if (!responseText.isNullOrBlank()) {
            try {
                val cleanJson = responseText
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()

                val json = JSONObject(cleanJson)
                val title = json.optString("title", "$clampedDuration-Min $mood Reset")
                val category = json.optString("category", "Mobility")
                val met = json.optDouble("met", 2.8)
                val stepsJson = json.optJSONArray("steps") ?: JSONArray()

                val steps = mutableListOf<RoutineStep>()
                for (i in 0 until stepsJson.length()) {
                    val stepObj = stepsJson.getJSONObject(i)
                    steps.add(
                        RoutineStep(
                            title = stepObj.optString("title", "Step ${i + 1}"),
                            instruction = stepObj.optString("instruction", "Breathe smoothly and hold posture"),
                            durationSeconds = stepObj.optInt("durationSeconds", 45),
                            audioCue = stepObj.optString("audioCue", "Begin steady breathing and hold posture")
                        )
                    )
                }

                if (steps.isNotEmpty()) {
                    val kcalRange = CalorieEngine.formatCalorieRange(clampedDuration, met, userProfile.weightKg)
                    return@withContext MicroRoutine(
                        id = "gemini_${System.currentTimeMillis()}",
                        title = title,
                        category = category,
                        durationMins = clampedDuration,
                        metValue = met,
                        calorieEstimateStr = kcalRange,
                        targetMood = mood,
                        steps = steps
                    )
                }
            } catch (e: Exception) {
                Log.w(tag, "Gemini routine parsing fallback: ${e.message}")
            }
        }

        // Curated offline routine catalog based on mood
        return@withContext getCuratedRoutineForMood(mood, clampedDuration, userProfile)
    }

    /**
     * Offline deterministic heuristic parser for free text.
     */
    private fun parseOfflineHeuristic(text: String, userProfile: UserProfile): ParsedLogResult {
        val lower = text.lowercase()

        // Extract duration in minutes (e.g. "30 min", "45 mins", "1 hour")
        var durationMins = 15
        val minRegex = Regex("(\\d+)\\s*(min|mins|minute|minutes)")
        val hourRegex = Regex("(\\d+)\\s*(hr|hrs|hour|hours)")

        minRegex.find(lower)?.let {
            durationMins = it.groupValues[1].toIntOrNull() ?: 15
        } ?: hourRegex.find(lower)?.let {
            durationMins = (it.groupValues[1].toIntOrNull() ?: 1) * 60
        }

        // Extract steps (e.g. "5000 steps", "8k steps")
        var steps = 0
        val stepRegex = Regex("(\\d+)\\s*steps")
        val kStepRegex = Regex("(\\d+(\\.\\d+)?)\\s*k\\s*steps")
        stepRegex.find(lower)?.let {
            steps = it.groupValues[1].toIntOrNull() ?: 0
        } ?: kStepRegex.find(lower)?.let {
            steps = ((it.groupValues[1].toDoubleOrNull() ?: 0.0) * 1000).toInt()
        }

        // Detect tags
        val detectedTags = mutableListOf<String>()
        val keywords = listOf(
            "exam stress", "exam", "exams", "study", "travel", "wedding",
            "tired", "sick", "illness", "walk", "yoga", "stretch", "hiit", "desk"
        )
        for (kw in keywords) {
            if (lower.contains(kw)) {
                detectedTags.add(kw)
            }
        }
        if (detectedTags.isEmpty()) {
            detectedTags.add("activity")
        }

        // Determine activity title
        val activityTitle = when {
            lower.contains("yoga") && lower.contains("walk") -> "College Walk & Light Yoga"
            lower.contains("walk") -> "Brisk Walk"
            lower.contains("yoga") -> "Light Yoga Flow"
            lower.contains("stretch") -> "Desk Stretch & Mobility"
            lower.contains("run") || lower.contains("jog") -> "Outdoor Jog"
            lower.contains("cycle") -> "Cycling"
            lower.contains("hiit") -> "Quick HIIT"
            else -> text.take(35).replaceFirstChar { it.uppercase() }
        }

        val met = CalorieEngine.resolveMet(activityTitle, detectedTags)
        val kcal = CalorieEngine.calculateExpenditure(met, durationMins, userProfile)

        return ParsedLogResult(
            activity = activityTitle,
            durationMins = durationMins,
            estimatedKcal = kcal,
            tags = detectedTags,
            steps = steps
        )
    }

    /**
     * Curated evidence-based micro-routines for offline use.
     */
    fun getCuratedRoutineForMood(
        mood: String,
        durationMins: Int,
        userProfile: UserProfile
    ): MicroRoutine {
        val lower = mood.lowercase()
        return when {
            lower.contains("calm") || lower.contains("center") -> {
                val met = 1.8
                MicroRoutine(
                    id = "calm_unwind",
                    title = "Mindful Breath & Neck Unwind",
                    category = "Calm",
                    durationMins = durationMins,
                    metValue = met,
                    calorieEstimateStr = CalorieEngine.formatCalorieRange(durationMins, met, userProfile.weightKg),
                    targetMood = mood,
                    breathingCadence = "4s Inhale • 4s Exhale",
                    steps = listOf(
                        RoutineStep(
                            title = "Mindful Diaphragmatic Breath",
                            instruction = "Place one hand on your heart and one on your belly. Breathe deeply through your nose, letting your belly expand.",
                            durationSeconds = 60,
                            audioCue = "Place your hands on your chest and belly. Feel the quiet rise and fall of your breath."
                        ),
                        RoutineStep(
                            title = "Gentle Neck Half-Circles",
                            instruction = "Slowly roll your chin toward your chest, then gently toward your right shoulder. Repeat softly to the left.",
                            durationSeconds = 60,
                            audioCue = "Gently roll your chin across your collarbone. Soft, smooth motion without straining."
                        ),
                        RoutineStep(
                            title = "Seated Heart Opening",
                            instruction = "Rest hands behind hips, lift sternum toward ceiling, soften eyes and take 3 nourishing breaths.",
                            durationSeconds = 60,
                            audioCue = "Lift your heart gently upward. Take three deep, nourishing breaths."
                        )
                    )
                )
            }
            lower.contains("anxious") || lower.contains("stress") || lower.contains("overwhelm") -> {
                val met = 1.6
                MicroRoutine(
                    id = "stress_reset",
                    title = "Vagus Nerve & 4-7-8 Breathing Reset",
                    category = "Calm",
                    durationMins = durationMins,
                    metValue = met,
                    calorieEstimateStr = CalorieEngine.formatCalorieRange(durationMins, met, userProfile.weightKg),
                    targetMood = mood,
                    breathingCadence = "4s Inhale • 7s Hold • 8s Exhale",
                    steps = listOf(
                        RoutineStep(
                            title = "Seated Posture Align",
                            instruction = "Sit tall, feet flat on the floor, gently drop your shoulders away from ears.",
                            durationSeconds = 40,
                            audioCue = "Sit comfortably with your spine tall. Let your shoulders drop and relax your jaw."
                        ),
                        RoutineStep(
                            title = "4-7-8 Parasympathetic Breath",
                            instruction = "Inhale through your nose for 4 seconds, hold for 7, exhale completely through mouth for 8.",
                            durationSeconds = 80,
                            audioCue = "Inhale deeply for four seconds. Hold gently for seven. Now release slowly for eight."
                        ),
                        RoutineStep(
                            title = "Neck & Trap Tension Release",
                            instruction = "Gently tilt right ear to right shoulder. Hold 20s, then switch sides smoothly.",
                            durationSeconds = 60,
                            audioCue = "Tilt your right ear to your right shoulder. Feel the tension melt away, then gently switch sides."
                        )
                    )
                )
            }
            lower.contains("fatigue") || lower.contains("tired") || lower.contains("drain") -> {
                val met = 2.4
                MicroRoutine(
                    id = "gentle_revive",
                    title = "Low-Energy Circulation Wakeup",
                    category = "Mobility",
                    durationMins = durationMins,
                    metValue = met,
                    calorieEstimateStr = CalorieEngine.formatCalorieRange(durationMins, met, userProfile.weightKg),
                    targetMood = mood,
                    steps = listOf(
                        RoutineStep(
                            title = "Seated Ankle & Calf Pumps",
                            instruction = "Pump your ankles up and down rhythmically to push fresh venous blood up to the heart.",
                            durationSeconds = 45,
                            audioCue = "Pump your ankles up and down smoothly. Boost your circulation without exhausting your energy."
                        ),
                        RoutineStep(
                            title = "Spinal Cat-Cow in Chair",
                            instruction = "Arch your back gently on inhale, round your spine tucking chin on exhale.",
                            durationSeconds = 60,
                            audioCue = "Gently arch your back on your inhale, and round your spine on your exhale. Soft and effortless."
                        ),
                        RoutineStep(
                            title = "Chest & Ribcage Opener",
                            instruction = "Interlace hands behind back or chair, open chest upward, take 3 deep belly breaths.",
                            durationSeconds = 60,
                            audioCue = "Open your chest towards the ceiling and take three deep belly breaths. Feel revitalized."
                        )
                    )
                )
            }
            lower.contains("steady") || lower.contains("focus") -> {
                val met = 2.6
                MicroRoutine(
                    id = "focus_posture",
                    title = "Desk Posture & Spine Lengthening",
                    category = "Posture",
                    durationMins = durationMins,
                    metValue = met,
                    calorieEstimateStr = CalorieEngine.formatCalorieRange(durationMins, met, userProfile.weightKg),
                    targetMood = mood,
                    steps = listOf(
                        RoutineStep(
                            title = "Cervical Retraction & Chin Tucks",
                            instruction = "Draw chin straight backward into a gentle double chin. Realign cervical spine and hold 3 seconds.",
                            durationSeconds = 45,
                            audioCue = "Gently draw your chin straight back. Align your cervical spine and release screen strain."
                        ),
                        RoutineStep(
                            title = "W-to-Y Scapular Squeeze",
                            instruction = "Bend elbows into a 'W' squeezing shoulder blades down, then smoothly extend arms up into a 'Y'.",
                            durationSeconds = 60,
                            audioCue = "Squeeze shoulder blades together in a W shape, then reach into a Y. Strengthen posture."
                        ),
                        RoutineStep(
                            title = "Thoracic Chair Rotation",
                            instruction = "Turn torso gently to the right using chair back for light support, hold 15s, then rotate left.",
                            durationSeconds = 60,
                            audioCue = "Rotate your upper torso to the side smoothly. Unlock thoracic stiffness."
                        )
                    )
                )
            }
            lower.contains("stiff") || lower.contains("sore") || lower.contains("ach") -> {
                val met = 2.5
                MicroRoutine(
                    id = "somatic_release",
                    title = "Somatic Hip & Trap Tension Release",
                    category = "Mobility",
                    durationMins = durationMins,
                    metValue = met,
                    calorieEstimateStr = CalorieEngine.formatCalorieRange(durationMins, met, userProfile.weightKg),
                    targetMood = mood,
                    steps = listOf(
                        RoutineStep(
                            title = "Seated Figure-Four Hip Stretch",
                            instruction = "Cross right ankle over left knee. Keep spine straight and hinge forward gently until you feel hip release.",
                            durationSeconds = 60,
                            audioCue = "Cross your ankle over your knee and hinge gently forward. Let your glutes and hips soften."
                        ),
                        RoutineStep(
                            title = "Shoulder Shrug & Drop",
                            instruction = "Inhale shoulders tight up to ears for 3 seconds, then exhale sharply and drop them completely.",
                            durationSeconds = 45,
                            audioCue = "Shrug shoulders high to your ears, hold tight, and release them completely with an exhale."
                        ),
                        RoutineStep(
                            title = "Standing Calves & Hamstring Reach",
                            instruction = "Step forward with heel down, toe up. Hinge at hips with flat back to lengthen posterior chain.",
                            durationSeconds = 60,
                            audioCue = "Step heel forward, toes up, and reach forward with a long spine. Melt hamstring stiffness."
                        )
                    )
                )
            }
            lower.contains("restless") || lower.contains("fidget") -> {
                val met = 3.6
                MicroRoutine(
                    id = "tension_shake",
                    title = "Standing Tension Shake-Out & Stretch",
                    category = "Discharge",
                    durationMins = durationMins,
                    metValue = met,
                    calorieEstimateStr = CalorieEngine.formatCalorieRange(durationMins, met, userProfile.weightKg),
                    targetMood = mood,
                    steps = listOf(
                        RoutineStep(
                            title = "Somatic Full-Body Shake-Out",
                            instruction = "Stand up and bounce gently on balls of feet. Shake your hands, wrists, and shoulders to discharge nervous energy.",
                            durationSeconds = 50,
                            audioCue = "Stand and shake out your wrists, arms, and shoulders. Discharge all restless energy."
                        ),
                        RoutineStep(
                            title = "Torso Twists with Swinging Arms",
                            instruction = "Twist gently side to side, letting your arms wrap around your torso in a fluid rhythm.",
                            durationSeconds = 50,
                            audioCue = "Swing arms freely side to side, rotating through your hips and torso."
                        ),
                        RoutineStep(
                            title = "Grounding Standing Mountain Pose",
                            instruction = "Plant both feet firmly. Reach crown high, inhale deeply for 4 seconds, and feel solid ground underfoot.",
                            durationSeconds = 60,
                            audioCue = "Root your feet firmly into the ground. Breathe deeply and feel completely anchored."
                        )
                    )
                )
            }
            lower.contains("joy") || lower.contains("uplift") || lower.contains("great") -> {
                val met = 3.5
                MicroRoutine(
                    id = "radiant_awakening",
                    title = "Radiant Heart & Core Awakening",
                    category = "Energy",
                    durationMins = durationMins,
                    metValue = met,
                    calorieEstimateStr = CalorieEngine.formatCalorieRange(durationMins, met, userProfile.weightKg),
                    targetMood = mood,
                    steps = listOf(
                        RoutineStep(
                            title = "Standing Sun Reach & Side Bends",
                            instruction = "Inhale arms high overhead. Interlock thumbs and curve gently right and left, expanding ribcage.",
                            durationSeconds = 50,
                            audioCue = "Reach high toward the sky, interlock fingers, and side bend gently with joy."
                        ),
                        RoutineStep(
                            title = "Dynamic Arm Swings & Chest Fly",
                            instruction = "Open arms wide on inhale, hug yourself on exhale. Alternate top arm with every breath.",
                            durationSeconds = 50,
                            audioCue = "Open wide with vitality, then wrap into a warm self hug. Keep a lively tempo."
                        ),
                        RoutineStep(
                            title = "Rhythmic Calf Bounce to Balance",
                            instruction = "Bounce lightly on toes, then pause in single-leg tree balance for 10s per leg.",
                            durationSeconds = 60,
                            audioCue = "Bounce lightly and find steady single-leg balance. Celebrate your body's strength."
                        )
                    )
                )
            }
            lower.contains("energ") || lower.contains("hyper") -> {
                val met = 4.2
                MicroRoutine(
                    id = "high_energy_flow",
                    title = "Dynamic Dopamine Micro-Burst",
                    category = "Energy",
                    durationMins = durationMins,
                    metValue = met,
                    calorieEstimateStr = CalorieEngine.formatCalorieRange(durationMins, met, userProfile.weightKg),
                    targetMood = mood,
                    steps = listOf(
                        RoutineStep(
                            title = "High Knee Arm Reaches",
                            instruction = "March in place lifting knees high while reaching opposite arm overhead dynamically.",
                            durationSeconds = 50,
                            audioCue = "March actively with high knees and reach high overhead! Keep the rhythm dynamic."
                        ),
                        RoutineStep(
                            title = "Chair Squats with Pulse",
                            instruction = "Lower hips towards chair, touch lightly, and power up with glutes engaged.",
                            durationSeconds = 60,
                            audioCue = "Perform controlled chair squats. Drive through your heels with steady power."
                        ),
                        RoutineStep(
                            title = "Calf Raises to Reach",
                            instruction = "Rise high onto the balls of your feet, hold for 1 second, lower smoothly.",
                            durationSeconds = 50,
                            audioCue = "Rise up on your toes, hold at the peak, and lower down with control."
                        )
                    )
                )
            }
            else -> { // Default / Neutral
                val met = 2.8
                MicroRoutine(
                    id = "desk_posture_reset",
                    title = "5-Min Desk Posture Reset",
                    category = "Posture",
                    durationMins = durationMins,
                    metValue = met,
                    calorieEstimateStr = CalorieEngine.formatCalorieRange(durationMins, met, userProfile.weightKg),
                    targetMood = mood,
                    steps = listOf(
                        RoutineStep(
                            title = "Chin Tucks & Neck Lengthening",
                            instruction = "Draw chin straight backward without tilting head, creating double chin to realign cervical spine.",
                            durationSeconds = 45,
                            audioCue = "Pull your chin straight back to decompress your neck. Hold for three seconds, then release."
                        ),
                        RoutineStep(
                            title = "Shoulder Blade Pinches (W-to-Y)",
                            instruction = "Squeeze shoulder blades back and down, drawing elbows to ribs, then extend into a Y.",
                            durationSeconds = 60,
                            audioCue = "Squeeze your shoulder blades together firmly, hold for two seconds, and repeat."
                        ),
                        RoutineStep(
                            title = "Standing Hip Flexor Stretch",
                            instruction = "Step one foot back, tuck pelvis under, feel stretch in front of the back hip.",
                            durationSeconds = 60,
                            audioCue = "Stand up and take a gentle lunge step back. Tuck your tailbone to lengthen your hip flexor."
                        )
                    )
                )
            }
        }
    }

}
