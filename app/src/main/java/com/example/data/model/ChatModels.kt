package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class RobotExpression {
    HAPPY,
    THINKING,
    SPEAKING,
    SURPRISED,
    SLEEPY,
    CONFIDENT
}

enum class RobotAccent(val label: String, val color: Color) {
    CYAN("Cyan Core", Color(0xFF00E5FF)),
    PURPLE("Neon Violet", Color(0xFFB388FF)),
    EMERALD("Emerald Matrix", Color(0xFF00E676)),
    AMBER("Solar Amber", Color(0xFFFFD740)),
    ROSE("Cyber Rose", Color(0xFFFF4081))
}

enum class AssistantLanguage(val code: String, val displayName: String, val localeTag: String) {
    ENGLISH("en", "English", "en-US"),
    HINDI("hi", "हिन्दी (Hindi)", "hi-IN"),
    MARATHI("mr", "मराठी (Marathi)", "mr-IN")
}

data class WebSearchResult(
    val title: String,
    val snippet: String,
    val url: String,
    val source: String
)

data class PhoneAction(
    val title: String,
    val subtitle: String,
    val iconName: String,
    val actionType: PhoneActionType
)

enum class PhoneActionType {
    OPEN_BROWSER,
    SEARCH_GOOGLE,
    SEARCH_YOUTUBE,
    OPEN_MAPS,
    OPEN_CAMERA,
    CREATE_REMINDER,
    SEND_SMS,
    DIAL_PHONE,
    SYSTEM_SETTINGS,
    SHARE_APP
}
