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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nfar.allergydetective.ui.theme.AppColors

/** Soft colour of a profile card, the same two as the tiles on the start screen. */
enum class ProfileTint { PLAIN, WARM, SAGE }

/**
 * One area of the profile in its own card (user note 21.11.2026: "Profil sieht nicht ordentlich aus,
 * fast keine Trennung zwischen den Bereichen").
 * User note 22.11.2026: the style of the start screen in every screen. The cards take the soft apricot and
 * sage gradients of the start tiles, with the fine border and soft shadow of its cards.
 */
@Composable
fun ProfileCard(tint: ProfileTint = ProfileTint.PLAIN, content: @Composable ColumnScope.() -> Unit) {
    val home = AppColors.extra.home
    val gradient = when (tint) {
        ProfileTint.PLAIN -> null
        ProfileTint.WARM -> home.symptomTile
        ProfileTint.SAGE -> home.mealTile
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = if (gradient == null) home.card else Color.Transparent, contentColor = home.ink),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, home.cardBorder),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    if (gradient != null) {
                        // Lighter than the tiles: the card carries text and chips, not one big icon.
                        drawRect(home.card)
                        drawRect(
                            Brush.linearGradient(
                                listOf(gradient.start.copy(alpha = 0.55f), gradient.end.copy(alpha = 0.55f)),
                                start = Offset(size.width * 0.2f, 0f),
                                end = Offset(size.width * 0.8f, size.height),
                            ),
                        )
                    }
                }
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content,
        )
    }
}
