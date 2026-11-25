package com.nfar.allergydetective.ui.meal

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.meal_prep_cooked
import allergydetective.shared.generated.resources.meal_prep_missing
import allergydetective.shared.generated.resources.meal_prep_raw
import allergydetective.shared.generated.resources.meal_variant_gluten_free
import allergydetective.shared.generated.resources.meal_variant_normal
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import com.nfar.allergydetective.ui.common.AppFilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.nfar.allergydetective.domain.analysis.model.Preparation
import org.jetbrains.compose.resources.stringResource

/**
 * Mandatory "raw / cooked" for ingredients with a heat-labile trigger (self-test 18.11.).
 * Nothing is preselected; until a choice is made a short reason is shown.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PreparationChoice(selected: Preparation?, onSelect: (Preparation) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppFilterChip(
                selected = selected == Preparation.RAW,
                onClick = { onSelect(Preparation.RAW) },
                label = { Text(stringResource(Res.string.meal_prep_raw)) },
                showCheck = false,
            )
            AppFilterChip(
                selected = selected == Preparation.COOKED,
                onClick = { onSelect(Preparation.COOKED) },
                label = { Text(stringResource(Res.string.meal_prep_cooked)) },
                showCheck = false,
            )
        }
        if (selected == null) {
            Text(
                stringResource(Res.string.meal_prep_missing),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

/**
 * "normal / glutenfrei" for products that are also sold gluten-free (user 25.11.2026). Normal is preselected; the
 * gluten-free version counts without gluten in the evaluation.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GlutenFreeChoice(glutenFree: Boolean, onChange: (Boolean) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AppFilterChip(
            selected = !glutenFree,
            onClick = { onChange(false) },
            label = { Text(stringResource(Res.string.meal_variant_normal)) },
            showCheck = false,
        )
        AppFilterChip(
            selected = glutenFree,
            onClick = { onChange(true) },
            label = { Text(stringResource(Res.string.meal_variant_gluten_free)) },
            showCheck = false,
        )
    }
}
