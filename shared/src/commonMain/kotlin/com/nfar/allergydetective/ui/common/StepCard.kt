package com.nfar.allergydetective.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nfar.allergydetective.ui.theme.AppColors

/**
 * One area of a form in its own card (self-test 19.11.: masks need a clear separation).
 * [number] numbers the steps of a form (null: no number), [title] is a question or topic,
 * [highlighted] marks a step that still needs an answer.
 * User note 22.11.2026: the same card as the progress card on the start screen (cream white, fine border,
 * soft shadow), the step number in a sage circle.
 */
@Composable
fun StepCard(
    title: String,
    number: Int? = null,
    highlighted: Boolean = false,
    /** Inner side margin; the meal screen uses less (user 24.11.2026). */
    sidePadding: androidx.compose.ui.unit.Dp = 12.dp,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val home = AppColors.extra.home
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = home.card, contentColor = home.ink),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(if (highlighted) 2.dp else 1.dp, if (highlighted) scheme.error else home.cardBorder),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(horizontal = sidePadding, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (number != null) {
                    Surface(shape = CircleShape, color = home.icon, contentColor = Color.White, modifier = Modifier.size(24.dp)) {
                        Box(contentAlignment = Alignment.Center) { Text("$number", style = MaterialTheme.typography.labelMedium) }
                    }
                }
                // User 24.11.2026: smaller titles and gaps on every form, so the whole looks calmer.
                Text(title, style = MaterialTheme.typography.titleLarge)
            }
            content()
        }
    }
}
