package com.nfar.allergydetective.ui.meal

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.common_cancel
import allergydetective.shared.generated.resources.meal_scan_add
import allergydetective.shared.generated.resources.meal_scan_hint
import allergydetective.shared.generated.resources.meal_scan_text_label
import allergydetective.shared.generated.resources.meal_scan_text_read
import allergydetective.shared.generated.resources.meal_scan_text_title
import androidx.compose.material3.OutlinedTextField
import allergydetective.shared.generated.resources.meal_scan_none
import allergydetective.shared.generated.resources.meal_scan_title
import allergydetective.shared.generated.resources.meal_scan_unknown
import allergydetective.shared.generated.resources.meal_scan_unknown_hint
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource

/**
 * What the photo of a package gave (user 27.11.2026): the catalog ingredients found as written, all ticked, and the
 * names the catalog does not know, not ticked: a tick makes them a new ingredient. Nothing is guessed.
 */
@Composable
fun PackageScanDialog(scan: PackageScan, viewModel: LogMealViewModel) {
    var chosen by remember(scan) { mutableStateOf(scan.found.map { it.id }.toSet()) }
    var chosenNew by remember(scan) { mutableStateOf(emptySet<String>()) }
    val nothing = scan.found.isEmpty() && scan.unknown.isEmpty()
    AlertDialog(
        onDismissRequest = viewModel::onPackageDismiss,
        title = { Text(stringResource(Res.string.meal_scan_title)) },
        text = {
          // The hint stays in view: reading a photo is never perfect (user 27.11.2026), the user checks the list.
          Column {
            Text(
                stringResource(Res.string.meal_scan_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            Column(modifier = Modifier.heightIn(max = 340.dp).verticalScroll(rememberScrollState())) {
                if (nothing) Text(stringResource(Res.string.meal_scan_none))
                scan.found.forEach { ingredient ->
                    TickRow(ingredientName(ingredient).substringBefore(" / "), ingredient.id in chosen) {
                        chosen = if (ingredient.id in chosen) chosen - ingredient.id else chosen + ingredient.id
                    }
                }
                if (scan.unknown.isNotEmpty()) {
                    Text(
                        stringResource(Res.string.meal_scan_unknown),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                    Text(
                        stringResource(Res.string.meal_scan_unknown_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    scan.unknown.forEach { name ->
                        TickRow(name, name in chosenNew) {
                            chosenNew = if (name in chosenNew) chosenNew - name else chosenNew + name
                        }
                    }
                }
            }
          }
        },
        confirmButton = {
            if (nothing) {
                TextButton(onClick = viewModel::onPackageDismiss) { Text(stringResource(Res.string.common_cancel)) }
            } else {
                Button(
                    onClick = { viewModel.onPackageConfirm(chosen, scan.unknown.filter { it in chosenNew }) },
                    enabled = chosen.isNotEmpty() || chosenNew.isNotEmpty(),
                ) { Text(stringResource(Res.string.meal_scan_add)) }
            }
        },
        dismissButton = {
            if (!nothing) TextButton(onClick = viewModel::onPackageDismiss) { Text(stringResource(Res.string.common_cancel)) }
        },
    )
}

/**
 * The ingredient list as text (user 27.11.2026: text copied with Google Lens): a field with what is on the clipboard,
 * to check or change before it is read. It works the same on every platform.
 */
@Composable
fun PackageTextDialog(initialText: String, onRead: (String) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf(initialText) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.meal_scan_text_title)) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(stringResource(Res.string.meal_scan_text_label)) },
                minLines = 5,
                maxLines = 10,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            Button(onClick = { onRead(text) }, enabled = text.isNotBlank()) { Text(stringResource(Res.string.meal_scan_text_read)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.common_cancel)) } },
    )
}

@Composable
private fun TickRow(label: String, checked: Boolean, onToggle: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable(onClick = onToggle),
    ) {
        Checkbox(checked = checked, onCheckedChange = null)
        Text(label, modifier = Modifier.padding(start = 12.dp))
    }
}
