package com.nfar.allergydetective.ui.symptom

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.nfar.allergydetective.ui.theme.AppColors

/** Tile symbols from CONTENT_Symptome 1, by the fixed symptom id. */
private val SYMPTOM_EMOJI = mapOf(
    1001L to "🤢", 1002L to "🎈", 1003L to "🔥", 1004L to "📉", 1005L to "💩", 1006L to "🧱",
    2001L to "🔴", 2002L to "🐜", 2003L to "🖐️", 2004L to "💧", 2005L to "🌵",
    3001L to "🤯", 3002L to "🌀", 3003L to "⚡", 3004L to "💤", 3005L to "🧠", 3006L to "💓",
    4001L to "🤧", 4002L to "👃", 4003L to "😤", 4004L to "🌬️", 4005L to "👄", 4006L to "👁️",
    9001L to "❓",
)

fun symptomEmoji(symptomId: Long): String = SYMPTOM_EMOJI[symptomId] ?: "❓"

/** Pastel surface per symptom group; emergency symptoms always use the red-flag colour. */
@Composable
fun symptomGroupColor(groupKey: String, isRedFlag: Boolean = false): Color {
    val extra = AppColors.extra
    if (isRedFlag) return extra.redFlag
    return when (groupKey) {
        "digestive" -> extra.digestive
        "skin" -> extra.skin
        "head" -> extra.head
        "airways" -> extra.airways
        else -> extra.other
    }
}
