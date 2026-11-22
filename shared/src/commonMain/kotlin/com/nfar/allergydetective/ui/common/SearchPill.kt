package com.nfar.allergydetective.ui.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.nfar.allergydetective.ui.theme.AppColors

/**
 * Colours of the round search field (design pictures 22.11.2026): a soft cream-grey pill without an
 * underline, used with shape = CircleShape.
 */
@Composable
fun searchPillColors(): TextFieldColors {
    val home = AppColors.extra.home
    val scheme = MaterialTheme.colorScheme
    // Opaque (phone test 22.11.: the leaf picture showed through): the soft sage mixed into the page cream.
    val fill = lerp(home.page, home.badge, 0.45f)
    return TextFieldDefaults.colors(
        focusedContainerColor = fill,
        unfocusedContainerColor = fill,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        focusedLeadingIconColor = home.ink,
        unfocusedLeadingIconColor = home.ink,
        focusedTrailingIconColor = home.ink,
        unfocusedTrailingIconColor = home.ink,
        focusedPlaceholderColor = scheme.onSurfaceVariant,
        unfocusedPlaceholderColor = scheme.onSurfaceVariant,
    )
}
