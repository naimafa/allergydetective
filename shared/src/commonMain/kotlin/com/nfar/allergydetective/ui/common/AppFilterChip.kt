package com.nfar.allergydetective.ui.common

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nfar.allergydetective.ui.theme.AppColors

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
    trailingIcon: (@Composable () -> Unit)? = null,
    /** Check mark on the selected chip. User 23.11.2026: "nur die Farbe soll gewählt zeigen" – off everywhere by default. */
    showCheck: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme
    FilterChip(
        selected = selected,
        onClick = onClick,
        // User 24.11.2026: one calmer label size on every chip.
        label = { ProvideTextStyle(MaterialTheme.typography.labelMedium) { label() } },
        modifier = modifier,
        enabled = enabled,
        trailingIcon = trailingIcon,
        leadingIcon = if (selected && leadingIcon == null && showCheck) {
            { Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(20.dp)) }
        } else {
            leadingIcon
        },
        // User note 22.11.2026: round pills like in the design pictures.
        shape = CircleShape,
        colors = FilterChipDefaults.filterChipColors(
            // User note 22.11.2026: on the leaf picture a see-through chip was hard to read, a white one did not
            // please. Unselected chips take the cream of the page: they look as before, but the leaf stays behind.
            containerColor = AppColors.extra.home.page,
            selectedContainerColor = scheme.primary,
            selectedLabelColor = scheme.onPrimary,
            selectedLeadingIconColor = scheme.onPrimary,
            selectedTrailingIconColor = scheme.onPrimary,
        ),
    )
}
