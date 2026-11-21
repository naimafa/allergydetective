package com.nfar.allergydetective.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

/**
 * One area of the profile in its own card (user note 21.11.2026: "Profil sieht nicht ordentlich aus,
 * fast keine Trennung zwischen den Bereichen"). White card on the page, rounded, inner padding.
 */
@Composable
fun ProfileCard(content: @Composable ColumnScope.() -> Unit) {
    // Dark mode (phone test 21.11.): the lowest surface is darker than the page, so the cards vanished. There they
    // take a lighter container and a fine outline.
    val scheme = MaterialTheme.colorScheme
    val dark = scheme.background.luminance() < 0.5f
    Card(
        colors = CardDefaults.cardColors(containerColor = if (dark) scheme.surfaceContainer else scheme.surfaceContainerLowest),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = if (dark) 0.dp else 1.dp),
        border = if (dark) BorderStroke(1.dp, scheme.outlineVariant) else null,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content,
        )
    }
}
