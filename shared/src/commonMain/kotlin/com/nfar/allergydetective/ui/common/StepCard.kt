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
import androidx.compose.ui.unit.dp

/**
 * One area of a form in its own card (self-test 19.11.: masks need a clear separation).
 * [number] numbers the steps of a form (null: no number), [title] is a question or topic,
 * [highlighted] marks a step that still needs an answer.
 */
@Composable
fun StepCard(
    title: String,
    number: Int? = null,
    highlighted: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerLowest),
        border = if (highlighted) BorderStroke(2.dp, scheme.error) else null,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (number != null) {
                    Surface(shape = CircleShape, color = scheme.primaryContainer, contentColor = scheme.primary, modifier = Modifier.size(28.dp)) {
                        Box(contentAlignment = Alignment.Center) { Text("$number", style = MaterialTheme.typography.labelLarge) }
                    }
                }
                Text(title, style = MaterialTheme.typography.titleMedium)
            }
            content()
        }
    }
}
