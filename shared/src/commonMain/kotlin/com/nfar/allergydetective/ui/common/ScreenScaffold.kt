package com.nfar.allergydetective.ui.common

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.common_back
import allergydetective.shared.generated.resources.common_saved
import allergydetective.shared.generated.resources.common_undo
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.nfar.allergydetective.ui.home.homeBackground
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

/**
 * Common frame of the logging screens: back arrow, big title in the start screen's style and a snackbar host.
 * User note 22.11.2026: every screen has the background of the start screen (cream page with the leaf picture).
 * [titleIcon] stands after the title, [actions] at the top right (e.g. "Jetzt" in the meal screen).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenScaffold(
    title: String,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
    titleIcon: ImageVector? = null,
    actions: @Composable RowScope.() -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = Modifier.homeBackground(),
        containerColor = Color.Transparent,
        topBar = {
            Column {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(Res.string.common_back))
                        }
                    },
                    actions = actions,
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, scrolledContainerColor = Color.Transparent),
                    // Phone test 22.11.: compact, so the three meal steps fit on the screen.
                    expandedHeight = 52.dp,
                )
                PageTitle(title, icon = titleIcon, modifier = Modifier.padding(bottom = 4.dp))
            }
        },
        bottomBar = bottomBar,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = content,
    )
}

@Composable
fun SectionTitle(text: String) {
    // Card title of the type scale (Theme.kt).
    Text(text, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 8.dp))
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
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(entryId) {
        val id = entryId ?: return@LaunchedEffect
        // UX 6: short vibration when something was saved.
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        val result = snackbarHostState.showSnackbar(
            message = getString(Res.string.common_saved),
            actionLabel = getString(Res.string.common_undo),
            duration = SnackbarDuration.Short,
        )
        if (result == SnackbarResult.ActionPerformed) onUndo(id)
        onShown()
    }
}
