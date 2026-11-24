package com.nfar.allergydetective.ui.profile

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.paywall_backup
import allergydetective.shared.generated.resources.paywall_feature_entries
import allergydetective.shared.generated.resources.paywall_feature_patterns
import allergydetective.shared.generated.resources.paywall_feature_reports
import allergydetective.shared.generated.resources.paywall_keep
import allergydetective.shared.generated.resources.paywall_only_preview
import allergydetective.shared.generated.resources.paywall_or
import allergydetective.shared.generated.resources.paywall_per_month
import allergydetective.shared.generated.resources.paywall_preview_badge
import allergydetective.shared.generated.resources.paywall_price
import allergydetective.shared.generated.resources.paywall_thanks
import allergydetective.shared.generated.resources.paywall_title
import allergydetective.shared.generated.resources.paywall_unlock
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nfar.allergydetective.ui.common.ScreenScaffold
import com.nfar.allergydetective.ui.theme.AppColors
import com.nfar.allergydetective.ui.theme.AppFonts
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Payment page as a test view only (user 23.11.2026, picture "Deine 7-tägige Testphase ist vorbei"): the idea is
 * 7 days free without a subscription, afterwards the data can still be seen and exported but nothing new entered.
 * Nothing is locked and nothing is sold here; the page is reached only by a long press in the profile and its
 * buttons just say "Nur Vorschau". Real payments are decided later.
 */
@Composable
fun PaywallPreviewScreen(onBack: () -> Unit) {
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val previewOnly = stringResource(Res.string.paywall_only_preview)
    val showPreviewOnly: () -> Unit = { scope.launch { snackbar.showSnackbar(previewOnly) } }
    val home = AppColors.extra.home
    ScreenScaffold(title = "", onBack = onBack, snackbarHostState = snackbar) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.tertiaryContainer, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text(stringResource(Res.string.paywall_preview_badge), style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
            }
            Surface(shape = MaterialTheme.shapes.large, color = home.card, border = BorderStroke(1.dp, home.cardBorder)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(72.dp).background(home.badge, CircleShape)) {
                            Icon(Icons.Outlined.EventAvailable, contentDescription = null, tint = home.accent, modifier = Modifier.size(38.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(Res.string.paywall_title), style = MaterialTheme.typography.titleLarge.copy(fontFamily = AppFonts.playfair, fontWeight = FontWeight.Bold), color = home.ink)
                            Text(stringResource(Res.string.paywall_thanks), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Surface(shape = MaterialTheme.shapes.medium, color = lerp(home.page, home.badge, 0.45f)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                                Text(stringResource(Res.string.paywall_price), style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold), color = home.ink)
                                Text(stringResource(Res.string.paywall_per_month), style = MaterialTheme.typography.titleMedium, color = home.ink, modifier = Modifier.padding(start = 6.dp, bottom = 6.dp))
                            }
                            Feature(Res.string.paywall_feature_entries)
                            Feature(Res.string.paywall_feature_patterns)
                            Feature(Res.string.paywall_feature_reports)
                        }
                    }
                    Button(onClick = showPreviewOnly, shape = CircleShape, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                        Text(stringResource(Res.string.paywall_unlock), textAlign = TextAlign.Center)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = home.cardBorder)
                        Text(stringResource(Res.string.paywall_or), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(horizontal = 12.dp))
                        HorizontalDivider(modifier = Modifier.weight(1f), color = home.cardBorder)
                    }
                    OutlinedButton(onClick = showPreviewOnly, shape = CircleShape, border = BorderStroke(1.dp, home.accent), modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                        Icon(Icons.Outlined.VerifiedUser, contentDescription = null, modifier = Modifier.size(20.dp))
                        Text(stringResource(Res.string.paywall_backup), textAlign = TextAlign.Center, modifier = Modifier.padding(start = 8.dp))
                    }
                    Surface(shape = MaterialTheme.shapes.medium, color = lerp(home.page, home.badge, 0.3f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(14.dp)) {
                            Icon(Icons.Outlined.Info, contentDescription = null, modifier = Modifier.size(20.dp))
                            Text(stringResource(Res.string.paywall_keep), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 12.dp))
                        }
                    }
                }
            }
            Text(stringResource(Res.string.paywall_only_preview), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun Feature(text: StringResource) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = AppColors.extra.home.accent, modifier = Modifier.size(24.dp))
        Text(stringResource(text), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 12.dp))
    }
}
