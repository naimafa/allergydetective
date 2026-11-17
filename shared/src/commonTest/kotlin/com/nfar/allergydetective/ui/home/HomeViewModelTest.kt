package com.nfar.allergydetective.ui.home

import kotlin.test.Test
import kotlin.test.assertEquals

class HomeViewModelTest {

    @Test
    fun quoteChangesDailyAndRepeatsAfterThePool() {
        assertEquals(0, HomeViewModel.quoteIndex(20_400, 40))
        assertEquals(1, HomeViewModel.quoteIndex(20_401, 40))
        assertEquals(0, HomeViewModel.quoteIndex(20_440, 40))
    }

    @Test
    fun emptyPoolDoesNotCrash() {
        assertEquals(0, HomeViewModel.quoteIndex(20_401, 0))
    }
}
