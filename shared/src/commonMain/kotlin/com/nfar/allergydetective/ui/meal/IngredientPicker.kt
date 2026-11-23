package com.nfar.allergydetective.ui.meal

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.meal_search
import allergydetective.shared.generated.resources.ingredient_search
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import com.nfar.allergydetective.domain.diary.FavoriteInfo
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
    /** Dishes found by name (user 23.11.2026: the field says "Zutat oder Gericht"); a tap adds all their ingredients. */
    dishes: List<FavoriteInfo> = emptyList(),
    onAddDish: (FavoriteInfo) -> Unit = {},
    findsDishes: Boolean = false,
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
            // Only where dishes can be found does the field say "oder Gericht".
            label = { Text(stringResource(if (findsDishes) Res.string.meal_search else Res.string.ingredient_search)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        dishes.forEach { dish ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable { onAddDish(dish) }.padding(vertical = 12.dp),
            ) {
                Icon(Icons.Outlined.StarOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Text(dish.name, modifier = Modifier.padding(start = 10.dp))
            }
        }
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
