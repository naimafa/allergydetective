package com.nfar.allergydetective.ui.symptom

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.symptom_duration_h1_to_3
import allergydetective.shared.generated.resources.symptom_duration_h3_to_12
import allergydetective.shared.generated.resources.symptom_duration_h3_to_6
import allergydetective.shared.generated.resources.symptom_duration_h6_to_12
import allergydetective.shared.generated.resources.symptom_duration_over_12h
import allergydetective.shared.generated.resources.symptom_duration_title
import allergydetective.shared.generated.resources.symptom_duration_under_1h
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.nfar.allergydetective.domain.diary.SymptomDuration
import com.nfar.allergydetective.ui.common.AppFilterChip
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** "How long?" (feedback 20.11.): optional, tapping the chosen chip again removes it. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SymptomDurationChoice(selected: SymptomDuration?, onSelect: (SymptomDuration) -> Unit, showTitle: Boolean = true) {
    if (showTitle) Text(stringResource(Res.string.symptom_duration_title), style = MaterialTheme.typography.titleSmall)
    // An old entry with "3–12 h" keeps showing its step until another one is chosen.
    val choices = if (selected == SymptomDuration.H3_TO_12) SymptomDuration.offered + selected else SymptomDuration.offered
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        choices.forEach { duration ->
            AppFilterChip(
                selected = duration == selected,
                onClick = { onSelect(duration) },
                label = { Text(symptomDurationLabel(duration)) },
            )
        }
    }
}

@Composable
fun symptomDurationLabel(duration: SymptomDuration): String = stringResource(durationLabelRes(duration))

fun durationLabelRes(duration: SymptomDuration): StringResource = when (duration) {
    SymptomDuration.UNDER_1H -> Res.string.symptom_duration_under_1h
    SymptomDuration.H1_TO_3 -> Res.string.symptom_duration_h1_to_3
    SymptomDuration.H3_TO_6 -> Res.string.symptom_duration_h3_to_6
    SymptomDuration.H6_TO_12 -> Res.string.symptom_duration_h6_to_12
    SymptomDuration.H3_TO_12 -> Res.string.symptom_duration_h3_to_12
    SymptomDuration.OVER_12H -> Res.string.symptom_duration_over_12h
}
