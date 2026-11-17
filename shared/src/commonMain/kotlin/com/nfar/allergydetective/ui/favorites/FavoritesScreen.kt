package com.nfar.allergydetective.ui.favorites

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.favorites_empty
import allergydetective.shared.generated.resources.history_delete
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nfar.allergydetective.ui.meal.ingredientName
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FavoritesScreen(modifier: Modifier = Modifier, viewModel: FavoritesViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.refresh() }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (!state.loading && state.favorites.isEmpty()) {
            item { Text(stringResource(Res.string.favorites_empty)) }
        }
        items(state.favorites, key = { it.id }) { favorite ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(favorite.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        favorite.items.mapNotNull { state.ingredients[it.ingredientId] }.map { ingredientName(it) }.joinToString(", "),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                TextButton(onClick = { viewModel.onDelete(favorite.id) }) { Text(stringResource(Res.string.history_delete)) }
            }
        }
    }
}
