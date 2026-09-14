package com.example.data.model

data class QuoteOfDay(
    val id: String,
    val quote: String,
    val author: String,
    val greetingContext: String,
    val theme: String,
    val affirmationBenefit: String = "+15 Consistency XP"
)

data class LeaderboardUser(
    val id: String,
    val name: String,
    val avatarInitials: String,
    val rank: Int,
    val score: Double,
    val microBreaks: Int,
    val streakDays: Int,
    val isCurrentUser: Boolean = false,
    val division: String = "Diamond",
    val cheersReceived: Int = 12,
    val hasUserCheered: Boolean = false
)

object QuoteCatalog {
    val dailyQuotes = listOf(
        QuoteOfDay(
            id = "q1",
            quote = "May your day be filled with steady breath, gentle strength, and peaceful clarity. You don't have to be extreme, just consistently kind to your body.",
            author = "Dr. Elena Rostova",
            greetingContext = "Daily Harmony Wish",
            theme = "Kind Consistency"
        ),
        QuoteOfDay(
            id = "q2",
            quote = "Small 2-minute pauses are not a delay in your day; they are the calm anchor that protects your focus from burnout.",
            author = "RhythmFit Mind Philosophy",
            greetingContext = "Mindful Focus Wish",
            theme = "Micro-Rhythms"
        ),
        QuoteOfDay(
            id = "q3",
            quote = "Wishing you steady energy that outlasts the morning rush and a calm heart that welcomes every opportunity to stretch and renew.",
            author = "Marcus Aurelius inspired",
            greetingContext = "Vitality & Strength",
            theme = "Enduring Flow"
        ),
        QuoteOfDay(
            id = "q4",
            quote = "May you find joy in simply showing up. Consistency is quiet momentum building mountains one small pebble at a time.",
            author = "James Clear inspired",
            greetingContext = "Momentum Wish",
            theme = "Quiet Power"
        ),
        QuoteOfDay(
            id = "q5",
            quote = "Health is about honoring seasons of rest as deeply as seasons of movement. Breathe deeply, move gently, and trust your progress.",
            author = "Somatic Wellness Guide",
            greetingContext = "Gentle Healing Wish",
            theme = "Grace & Renewal"
        )
    )

    fun getQuoteForToday(): QuoteOfDay {
        val dayOfYear = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
        return dailyQuotes[dayOfYear % dailyQuotes.size]
    }
}
