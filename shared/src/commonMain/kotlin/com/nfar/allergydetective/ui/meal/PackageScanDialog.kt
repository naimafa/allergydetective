package com.nfar.allergydetective.ui.meal

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.common_cancel
import allergydetective.shared.generated.resources.meal_scan_add
import allergydetective.shared.generated.resources.meal_scan_none
import allergydetective.shared.generated.resources.meal_scan_title
import allergydetective.shared.generated.resources.meal_scan_unknown
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource

/**
 * What the photo of a package gave (user 27.11.2026): the ingredients found, all ticked, to confirm before they go
 * into the meal; words that matched nothing are only listed, a photo of a list is never perfect.
 */
@Composable
fun PackageScanDialog(scan: PackageScan, viewModel: LogMealViewModel) {
    var chosen by remember(scan) { mutableStateOf(scan.found.map { it.id }.toSet()) }
    AlertDialog(
        onDismissRequest = viewModel::onPackageDismiss,
        title = { Text(stringResource(Res.string.meal_scan_title)) },
        text = {
            Column(modifier = Modifier.heightIn(max = 380.dp).verticalScroll(rememberScrollState())) {
                if (scan.found.isEmpty()) Text(stringResource(Res.string.meal_scan_none))
                scan.found.forEach { ingredient ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable {
                            chosen = if (ingredient.id in chosen) chosen - ingredient.id else chosen + ingredient.id
                        },
                    ) {
                        Checkbox(checked = ingredient.id in chosen, onCheckedChange = null)
                        Text(ingredientName(ingredient), modifier = Modifier.padding(start = 12.dp))
                    }
                }
                if (scan.notRecognized.isNotEmpty()) {
                    Text(
                        stringResource(Res.string.meal_scan_unknown, scan.notRecognized.joinToString(", ")),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        },
        confirmButton = {
            if (scan.found.isNotEmpty()) {
                Button(onClick = { viewModel.onPackageConfirm(chosen) }, enabled = chosen.isNotEmpty()) { Text(stringResource(Res.string.meal_scan_add)) }
            } else {
                TextButton(onClick = viewModel::onPackageDismiss) { Text(stringResource(Res.string.common_cancel)) }
            }
        },
        dismissButton = {
            if (scan.found.isNotEmpty()) TextButton(onClick = viewModel::onPackageDismiss) { Text(stringResource(Res.string.common_cancel)) }
        },
    )
}
