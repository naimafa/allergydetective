package com.nfar.allergydetective.ui.common

import kotlinx.datetime.LocalDate

/*
 * Small locale helpers until the design phase brings a full formatting setup.
 * Language and region come from the device (Locale.current), see CODING_RICHTLINIEN 6.
 */

/** Languages that write decimals with a comma ("6,5 h"). */
private val DECIMAL_COMMA_LANGUAGES = setOf("de", "fr", "es", "it", "nl", "pt", "pl", "tr", "ru")

/** 7.0 → "7", 6.5 → "6,5" (de) or "6.5" (en). One decimal at most. */
fun formatDecimal(value: Float, language: String): String {
    val tenths = kotlin.math.round(value * 10).toInt()
    if (tenths % 10 == 0) return (tenths / 10).toString()
    val separator = if (language.lowercase() in DECIMAL_COMMA_LANGUAGES) "," else "."
    return "${tenths / 10}$separator${kotlin.math.abs(tenths % 10)}"
}

/** "17.11.2026" in German, ISO "2026-11-17" elsewhere (unambiguous in every language). */
fun formatDate(date: LocalDate, language: String): String {
    val day = date.day.toString().padStart(2, '0')
    val month = (date.month.ordinal + 1).toString().padStart(2, '0')
    return if (language.lowercase() == "de") "$day.$month.${date.year}" else "${date.year}-$month-$day"
}

/**
 * Emergency number of the device region. 112 works in the EU and on most mobile networks
 * worldwide; regions with a different main number are listed explicitly.
 * Morocco and other target regions still need a professional check (OFFENE_PUNKTE).
 */
fun emergencyNumberFor(region: String?): String = when (region?.uppercase()) {
    "US", "CA" -> "911"
    "AU" -> "000"
    else -> "112"
}
