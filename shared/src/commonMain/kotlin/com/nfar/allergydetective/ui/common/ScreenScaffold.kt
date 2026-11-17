package com.nfar.allergydetective.ui.common

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.common_back
import allergydetective.shared.generated.resources.common_saved
import allergydetective.shared.generated.resources.common_undo
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

/** Common frame of the logging screens: title, back arrow and a snackbar host. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenScaffold(
    title: String,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    // Text instead of an icon until the design phase adds an icon set.
                    TextButton(onClick = onBack) { Text(stringResource(Res.string.common_back)) }
                },
            )
        },
        bottomBar = bottomBar,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = content,
    )
}

@Composable
fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
}

/**
 * Shows "Saved · Undo" once per saved [entryId] (UX 6: snackbar instead of a dialog).
 * [onUndo] runs when the user taps undo, [onShown] in any case afterwards.
 */
@Composable
fun SavedSnackbarEffect(
    entryId: Long?,
    snackbarHostState: SnackbarHostState,
    onUndo: (Long) -> Unit,
    onShown: () -> Unit,
) {
    LaunchedEffect(entryId) {
        val id = entryId ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = getString(Res.string.common_saved),
            actionLabel = getString(Res.string.common_undo),
            duration = SnackbarDuration.Short,
        )
        if (result == SnackbarResult.ActionPerformed) onUndo(id)
        onShown()
    }
}
