package com.nfar.allergydetective.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nfar.allergydetective.ui.theme.AppColors

/**
 * One choice as a pill (design picture 22.11.2026): like [AppFilterChip], but with a narrow inner margin and no
 * check mark, so several options fit in one row at font 125 % without shrinking any text. Selected: forest green fill.
 */
@Composable
fun ChoicePill(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        selected = selected,
        shape = CircleShape,
        color = if (selected) scheme.primary else AppColors.extra.home.page,
        contentColor = if (selected) scheme.onPrimary else scheme.onSurfaceVariant,
        border = if (selected) null else BorderStroke(1.dp, scheme.outlineVariant),
        modifier = modifier.heightIn(min = 38.dp),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 4.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge, maxLines = 1, softWrap = false, textAlign = TextAlign.Center)
        }
    }
}

/**
 * Options side by side in one row that fill it (user note 22.11.2026: "14 · 30 · 90 Tage" and
 * "Automatisch · Hell · Dunkel" each in one line). Each pill gets room by the length of its name.
 */
@Composable
fun ChoiceRow(labels: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = modifier.fillMaxWidth()) {
        labels.forEachIndexed { index, label ->
            ChoicePill(
                label = label,
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
                modifier = Modifier.weight(label.length + 2f),
            )
        }
    }
}

