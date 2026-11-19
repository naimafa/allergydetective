package com.nfar.allergydetective.ui.common

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nfar.allergydetective.ui.theme.AppColors

/** Self-test 19.11.: one look per status everywhere in the app. */
enum class StatusKind { DONE, OPEN, NEW }

/** Small pill: done = soft green, open = soft grey, new entries = soft blue. */
@Composable
fun StatusBadge(text: String, kind: StatusKind, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val extra = AppColors.extra
    val (container, content) = when (kind) {
        StatusKind.DONE -> scheme.primaryContainer to scheme.primary
        StatusKind.OPEN -> scheme.surfaceContainerHigh to scheme.onSurfaceVariant
        StatusKind.NEW -> extra.info to extra.onInfo
    }
    Surface(shape = CircleShape, color = container, contentColor = content, modifier = modifier) {
        Text(text, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp))
    }
}
