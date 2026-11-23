package com.nfar.allergydetective.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nfar.allergydetective.ui.theme.AppColors
import com.nfar.allergydetective.ui.theme.AppFonts

/**
 * Big page title in Playfair Display like the greeting on the start screen
 * (user note 22.11.2026: the style of the start screen should be found again in every screen).
 */
@Composable
fun PageTitle(text: String, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    val home = AppColors.extra.home
    Row(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontFamily = AppFonts.playfair,
                fontWeight = FontWeight.SemiBold,
                fontSize = 30.sp,
                lineHeight = 36.sp,
            ),
            color = home.ink,
            modifier = Modifier.weight(1f, fill = false),
        )
        if (icon != null) Icon(icon, contentDescription = null, tint = home.accent, modifier = Modifier.size(28.dp))
    }
}
