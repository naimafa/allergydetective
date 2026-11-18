package com.nfar.allergydetective.ui.profile

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.allergies_hint
import allergydetective.shared.generated.resources.allergies_question
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nfar.allergydetective.ui.common.NamePrefix
import com.nfar.allergydetective.ui.common.displayName
import org.jetbrains.compose.resources.stringResource

/** Selection of known allergies from the trigger properties (TRD 3.1). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KnownAllergiesContent(state: KnownAllergiesState, onToggle: (Long) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(Res.string.allergies_question), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(Res.string.allergies_hint), style = MaterialTheme.typography.bodyMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            state.properties.forEach { property ->
                FilterChip(
                    selected = property.id in state.selected,
                    onClick = { onToggle(property.id) },
                    label = { Text(displayName(NamePrefix.PROPERTY, property.key)) },
                )
            }
        }
    }
}
