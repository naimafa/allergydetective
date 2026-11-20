package com.nfar.allergydetective.ui.common

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Selection chip of the app. Phone test 19.11.: with the forest palette the Material default for
 * "selected" (secondaryContainer, grey-green) looked almost like the dialog background, so a chosen
 * intensity or day part was not visible. Selected is now forest green with white text and a check
 * mark (the accent marks what is active).
 */
@Composable
fun AppFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    /** Check mark on the selected chip; off for narrow chips like a 1–5 scale. */
    showCheck: Boolean = true,
) {
    val scheme = MaterialTheme.colorScheme
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = label,
        modifier = modifier,
        enabled = enabled,
        leadingIcon = if (selected && leadingIcon == null && showCheck) {
            { Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
        } else {
            leadingIcon
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = scheme.primary,
            selectedLabelColor = scheme.onPrimary,
            selectedLeadingIconColor = scheme.onPrimary,
            selectedTrailingIconColor = scheme.onPrimary,
        ),
    )
}
