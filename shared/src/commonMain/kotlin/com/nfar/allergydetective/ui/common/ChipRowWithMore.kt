package com.nfar.allergydetective.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.nfar.allergydetective.ui.theme.AppColors
import kotlinx.coroutines.launch

/**
 * A sideways row of chips that shows when there is more (user note 22.11.2026: there will be many favourites and
 * quick picks). While the row can scroll on, its end fades into the card and a small arrow sits there; a tap on the
 * arrow scrolls on by about two chips. In right-to-left layout the arrow sits on the left.
 */
@Composable
fun ChipRowWithMore(
    state: LazyListState = rememberLazyListState(),
    /** Colour the end fades into (the card by default); transparent for no fade. */
    fadeColor: Color = AppColors.extra.home.card,
    content: LazyListScope.() -> Unit,
) {
    val home = AppColors.extra.home
    val scope = rememberCoroutineScope()
    val step = with(LocalDensity.current) { 200.dp.toPx() }
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    Box(modifier = Modifier.fillMaxWidth()) {
        LazyRow(
            state = state,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            // Room after the last chip, so it is not hidden under the arrow.
            contentPadding = PaddingValues(end = 40.dp),
            content = content,
        )
        if (state.canScrollForward) {
            val fade = listOf(fadeColor.copy(alpha = 0f), fadeColor)
            Box(
                contentAlignment = Alignment.CenterEnd,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(64.dp)
                    .height(48.dp)
                    .background(Brush.horizontalGradient(if (rtl) fade.reversed() else fade)),
            ) {
                Surface(
                    onClick = { scope.launch { state.animateScrollBy(step) } },
                    shape = CircleShape,
                    color = home.badge,
                    contentColor = home.accent,
                    modifier = Modifier.size(32.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}
