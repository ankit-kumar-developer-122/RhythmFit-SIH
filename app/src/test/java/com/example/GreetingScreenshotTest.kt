package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.example.engine.RhythmEngine
import com.example.ui.components.TodayRhythmCard
import com.example.ui.theme.RhythmFitTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GreetingScreenshotTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun rhythm_card_renders_properly() {
        composeTestRule.setContent {
            RhythmFitTheme {
                TodayRhythmCard(
                    score = 76.5,
                    category = RhythmEngine.getRhythmCategory(76.5),
                    completedRoutinesCount = 2,
                    totalKcal = 145,
                    steps = 4200,
                    hasFlaggedDampener = true,
                    dampenerReason = "Exam Stress"
                )
            }
        }

        composeTestRule.onNodeWithTag("today_rhythm_card").assertIsDisplayed()
    }
}
