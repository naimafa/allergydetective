package com.nfar.allergydetective.domain.diary

/**
 * Products that are sold in a gluten-free version as well (user 25.11.2026): logging offers "normal / glutenfrei",
 * and the gluten-free one counts without gluten. Not for grains and flours themselves (wheat flour is never
 * gluten-free, millet always is), nor for the gluten-free products that have their own entry (bread 113, pasta 135).
 */
val GLUTEN_FREE_OPTION_IDS: Set<Long> = setOf(
    // Grains & flour: oats are sold certified gluten-free; muesli, cornflakes, breading.
    108, 127, 129, 130, 142,
    // Pasta & noodles.
    105, 106, 128, 1222, 1601,
    // Bread & baked goods.
    101, 109, 110, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126,
    // Beer and bars.
    1106, 1115, 1216,
)
