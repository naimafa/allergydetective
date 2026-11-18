package com.nfar.allergydetective.ui.meal

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.meal_search
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.InputChip
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nfar.allergydetective.domain.diary.IngredientInfo
import org.jetbrains.compose.resources.stringResource

/**
 * Search field, matches and the chosen ingredients as chips (tap removes).
 * Used to put a dish or a custom ingredient together from known ingredients (self-test 18.11.).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IngredientPicker(
    query: String,
    results: List<IngredientInfo>,
    selected: List<IngredientInfo>,
    onQueryChange: (String) -> Unit,
    onAdd: (IngredientInfo) -> Unit,
    onRemove: (IngredientInfo) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (selected.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                selected.forEach { ingredient ->
                    InputChip(
                        selected = true,
                        onClick = { onRemove(ingredient) },
                        label = { Text("${ingredientName(ingredient)}  ✕") },
                    )
                }
            }
        }
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text(stringResource(Res.string.meal_search)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        results.filter { it !in selected }.forEach { candidate ->
            Text(
                ingredientName(candidate),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clickable { onAdd(candidate) }
                    .padding(vertical = 12.dp),
            )
        }
    }
}
