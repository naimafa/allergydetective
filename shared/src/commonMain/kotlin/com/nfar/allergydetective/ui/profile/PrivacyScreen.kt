package com.nfar.allergydetective.ui.profile

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.privacy_backup_text
import allergydetective.shared.generated.resources.privacy_backup_title
import allergydetective.shared.generated.resources.privacy_delete_text
import allergydetective.shared.generated.resources.privacy_delete_title
import allergydetective.shared.generated.resources.privacy_export_text
import allergydetective.shared.generated.resources.privacy_export_title
import allergydetective.shared.generated.resources.privacy_intro
import allergydetective.shared.generated.resources.privacy_local_text
import allergydetective.shared.generated.resources.privacy_local_title
import allergydetective.shared.generated.resources.privacy_notifications_text
import allergydetective.shared.generated.resources.privacy_notifications_title
import allergydetective.shared.generated.resources.privacy_title
import allergydetective.shared.generated.resources.privacy_voice_text
import allergydetective.shared.generated.resources.privacy_voice_title
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nfar.allergydetective.ui.common.ScreenScaffold
import com.nfar.allergydetective.ui.common.SectionTitle
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Privacy details on their own screen (user note 21.11.2026: in the profile it would be overloaded).
 * Only what the app really does, checked in the code on 21.11.2026: no internet permission (manifest), encrypted
 * database (SQLCipher, key in the Android Keystore), cloud backup off (allowBackup=false), voice input through the
 * phone's speech recognition (offline preferred), export only when the user shares it, notifications only for the
 * evening reminder. The binding legal text for the stores is still to be written and checked separately.
 */
@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    ScreenScaffold(
        title = stringResource(Res.string.privacy_title),
        onBack = onBack,
        snackbarHostState = remember { SnackbarHostState() },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(Res.string.privacy_intro), style = MaterialTheme.typography.bodyLarge)
            Topic(Res.string.privacy_local_title, Res.string.privacy_local_text)
            Topic(Res.string.privacy_backup_title, Res.string.privacy_backup_text)
            Topic(Res.string.privacy_voice_title, Res.string.privacy_voice_text)
            Topic(Res.string.privacy_export_title, Res.string.privacy_export_text)
            Topic(Res.string.privacy_notifications_title, Res.string.privacy_notifications_text)
            Topic(Res.string.privacy_delete_title, Res.string.privacy_delete_text)
        }
    }
}

@Composable
private fun Topic(title: StringResource, text: StringResource) {
    ProfileCard {
        SectionTitle(stringResource(title))
        Text(stringResource(text), style = MaterialTheme.typography.bodyMedium)
    }
}
