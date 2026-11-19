package com.nfar.allergydetective.ui.sick

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.illness_hint
import allergydetective.shared.generated.resources.illness_question
import allergydetective.shared.generated.resources.illness_related
import allergydetective.shared.generated.resources.illness_unknown
import allergydetective.shared.generated.resources.illness_unrelated
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.nfar.allergydetective.domain.analysis.model.IllnessRelation
import org.jetbrains.compose.resources.stringResource

/**
 * "Part of the illness? Yes / No / Don't know" for a complaint on a sick day (self-test 19.11.).
 * Not answered counts like "don't know".
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IllnessRelationChoice(selected: IllnessRelation?, onSelect: (IllnessRelation) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(stringResource(Res.string.illness_question), style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IllnessRelation.entries.forEach { relation ->
                FilterChip(
                    selected = (selected ?: IllnessRelation.UNKNOWN) == relation,
                    onClick = { onSelect(relation) },
                    label = { Text(illnessRelationLabel(relation)) },
                )
            }
        }
        Text(stringResource(Res.string.illness_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun illnessRelationLabel(relation: IllnessRelation): String = stringResource(
    when (relation) {
        IllnessRelation.RELATED -> Res.string.illness_related
        IllnessRelation.UNRELATED -> Res.string.illness_unrelated
        IllnessRelation.UNKNOWN -> Res.string.illness_unknown
    },
)
