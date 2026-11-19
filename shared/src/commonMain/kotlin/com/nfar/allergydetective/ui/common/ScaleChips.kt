package com.nfar.allergydetective.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * A 1–5 scale (intensity, wellbeing, stress …) in exactly one row of equally wide chips. Phone test
 * 19.11.: with a large system font the "5" wrapped to a second line. No check mark here: the forest
 * green fill marks the choice, and the row stays narrow enough.
 */
@Composable
fun ScaleChips(
    value: Int?,
    onChange: (Int) -> Unit,
    labels: List<String> = (1..5).map { it.toString() },
    modifier: Modifier = Modifier,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = modifier.fillMaxWidth()) {
        labels.forEachIndexed { index, label ->
            val option = index + 1
            AppFilterChip(
                selected = value == option,
                onClick = { onChange(option) },
                label = { Text(label, textAlign = TextAlign.Center, maxLines = 1, modifier = Modifier.fillMaxWidth()) },
                showCheck = false,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
