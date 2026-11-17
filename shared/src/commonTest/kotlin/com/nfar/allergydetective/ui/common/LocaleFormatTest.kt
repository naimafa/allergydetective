package com.nfar.allergydetective.ui.common

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class LocaleFormatTest {

    @Test
    fun wholeHoursHaveNoDecimals() {
        assertEquals("7", formatDecimal(7f, "de"))
        assertEquals("0", formatDecimal(0f, "en"))
    }

    @Test
    fun decimalSeparatorFollowsTheLanguage() {
        assertEquals("6,5", formatDecimal(6.5f, "de"))
        assertEquals("6,5", formatDecimal(6.5f, "fr"))
        assertEquals("6.5", formatDecimal(6.5f, "en"))
    }

    @Test
    fun dateIsGermanOrIso() {
        val date = LocalDate(2026, 11, 7)

        assertEquals("07.11.2026", formatDate(date, "de"))
        assertEquals("2026-11-07", formatDate(date, "en"))
    }

    @Test
    fun emergencyNumberDependsOnTheRegion() {
        assertEquals("112", emergencyNumberFor("DE"))
        assertEquals("112", emergencyNumberFor(null))
        assertEquals("911", emergencyNumberFor("us"))
        assertEquals("000", emergencyNumberFor("AU"))
    }
}
