package com.nfar.allergydetective.ui.profile

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.outlined.HealthAndSafety
import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.allergies_none
import allergydetective.shared.generated.resources.allergies_section_title
import allergydetective.shared.generated.resources.common_save
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nfar.allergydetective.ui.common.NamePrefix
import com.nfar.allergydetective.ui.common.displayName
import org.jetbrains.compose.resources.stringResource

/**
 * Known allergies in the profile, folded by default (feedback 20.11.): one line with what is
 * selected, a tap shows the whole list with "Save". The onboarding keeps the open list.
 */
@Composable
fun KnownAllergiesSection(state: KnownAllergiesState, onToggle: (Long) -> Unit, onSave: () -> Unit, inGroup: Boolean = false) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val selectedNames = state.properties.filter { it.id in state.selected }.map { displayName(NamePrefix.PROPERTY, it.key) }
    // User note 22.11.2026: no white card inside the profile card; the fold lies directly on the card.
    Surface(
        shape = MaterialTheme.shapes.large,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.heightIn(min = 56.dp).padding(top = 8.dp, bottom = 4.dp),
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    // User 23.11.2026: an icon before every profile title.
                    if (inGroup) {
                        GroupSubtitle(stringResource(Res.string.allergies_section_title))
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.HealthAndSafety, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                            Text(stringResource(Res.string.allergies_section_title), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(start = 10.dp))
                        }
                    }
                    Text(
                        selectedNames.joinToString(", ").ifEmpty { stringResource(Res.string.allergies_none) },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = if (expanded) Int.MAX_VALUE else 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Icon(
                    if (expanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    KnownAllergiesContent(state, onToggle)
                    Button(
                        onClick = onSave,
                        enabled = !state.loading,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                    ) { Text(stringResource(Res.string.common_save)) }
                }
            }
        }
    }
}
