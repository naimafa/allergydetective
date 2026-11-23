package com.nfar.allergydetective.ui.profile

import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
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

/** Small heading of one part inside a grouped profile card (user 23.11.2026: fewer, grouped cards). */
@Composable
fun GroupSubtitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, color = AppColors.extra.home.ink, modifier = Modifier.padding(top = 4.dp))
}

/**
 * One line of a grouped profile card: title, the current value or a hint below, an arrow on the right
 * (like the phone's own settings). [onLongClick] is used only by the hidden payment test view.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileRow(title: String, subtitle: String?, onClick: () -> Unit, onLongClick: (() -> Unit)? = null) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).combinedClickable(onClick = onClick, onLongClick = onLongClick).padding(vertical = 6.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = AppColors.extra.home.ink)
            subtitle?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/**
 * One part of a grouped profile card in its own light box (user 23.11.2026: "eine optische Trennung zwischen den
 * drei darunter"), e.g. language, look and reminder inside "Sprache & Darstellung".
 */
@Composable
fun GroupPart(content: @Composable ColumnScope.() -> Unit) {
    val home = AppColors.extra.home
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = home.card,
        border = BorderStroke(1.dp, home.cardBorder),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
}
