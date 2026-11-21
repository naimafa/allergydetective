package com.nfar.allergydetective.ui.home

import allergydetective.shared.generated.resources.home_background
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.paint
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Density
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.CompositionLocalProvider
import allergydetective.shared.generated.resources.home_reminders
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.IconButton
import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.home_brand
import allergydetective.shared.generated.resources.home_day_check_hint
import allergydetective.shared.generated.resources.home_greeting_day
import allergydetective.shared.generated.resources.home_greeting_evening
import allergydetective.shared.generated.resources.home_greeting_morning
import allergydetective.shared.generated.resources.home_log_meal_hint
import allergydetective.shared.generated.resources.home_log_symptom_hint
import allergydetective.shared.generated.resources.home_results_hint
import allergydetective.shared.generated.resources.home_tagline
import allergydetective.shared.generated.resources.tile_day_check
import allergydetective.shared.generated.resources.tile_meal
import allergydetective.shared.generated.resources.tile_results
import allergydetective.shared.generated.resources.tile_symptom
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.nfar.allergydetective.ui.theme.TileGradient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nfar.allergydetective.ui.theme.AppColors
import com.nfar.allergydetective.ui.theme.AppFonts
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

enum class HomeAction(val label: StringResource, val hint: StringResource, val icon: ImageVector) {
    LOG_MEAL(Res.string.tile_meal, Res.string.home_log_meal_hint, Icons.Filled.Restaurant),
    LOG_SYMPTOM(Res.string.tile_symptom, Res.string.home_log_symptom_hint, Icons.Filled.MonitorHeart),
    DAY_CHECK(Res.string.tile_day_check, Res.string.home_day_check_hint, Icons.Filled.Bedtime),
    RESULTS(Res.string.tile_results, Res.string.home_results_hint, Icons.Filled.QueryStats),
}

/**
 * Dashboard, home design of 21.11.2026: logo and name, a greeting by time of day with the tagline,
 * the progress card, "your day today" with three numbers and the four tiles with soft gradients.
 * The bell shows a red dot while today is still open.
 */
@Composable
fun HomeScreen(
    onAction: (HomeAction) -> Unit,
    /** "Your day today" opens the history. */
    onOpenToday: () -> Unit = {},
    modifier: Modifier = Modifier,
    state: HomeState = HomeState(),
    /** Optional card on top, e.g. "ill since ..." (self-test 18.11.). */
    banner: @Composable () -> Unit = {},
    /** The bell top right: opens the evening reminder in the profile. */
    onOpenReminders: () -> Unit = {},
) {
    // The cream page is drawn behind the whole screen (homeBackground in MainScreen),
    // so it also runs under the status bar and the navigation without a hard edge.
    // Phone test 21.11.: the picture shows the whole start screen with all four tiles at once. The system
    // font scale is therefore not applied here (1.25 on the test phone pushed the tiles off the screen);
    // the sizes follow the picture.
    val density = LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(density.density, minOf(density.fontScale, MAX_FONT_SCALE))) {
        BoxWithConstraints(modifier = modifier.fillMaxSize()) {
            // Text width inside a tile: screen padding 2 × 16, gap, tile padding on both sides, a small safety margin.
            val textWidth = (maxWidth - 32.dp - GRID) / 2 - TILE_PADDING * 2 - SAFETY_MARGIN
            val tileText = sharedTileStyles(textWidth)
            // The tiles share the height left below the cards, so nothing has to be scrolled. Only if a
            // banner or a very small screen leaves too little room the page scrolls as before.
            var topHeight by remember { mutableStateOf(0.dp) }
            val rowHeight = ((maxHeight - PAGE_PADDING * 2 - topHeight - SECTION_GAP - GRID) / 2).coerceAtLeast(MIN_TILE_HEIGHT)
            Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                // Dark mode: the drawn branch; light mode shows the leaf picture behind the page (homeBackground).
                if (!AppColors.extra.home.leafPhoto) LeafBranch(modifier = Modifier.align(Alignment.TopEnd).size(230.dp))
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = PAGE_PADDING),
                    verticalArrangement = Arrangement.spacedBy(SECTION_GAP),
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(SECTION_GAP),
                        modifier = Modifier.onSizeChanged { topHeight = with(density) { it.height.toDp() } },
                    ) {
                        Header(state.greeting, showDot = state.confirmedDays != null && state.todayOpen, onBell = onOpenReminders)
                        banner()
                        state.confirmedDays?.let {
                            HomeProgress(it)
                            HomeToday(state, onClick = onOpenToday)
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(GRID)) {
                        HomeAction.entries.chunked(2).forEach { row ->
                            // Both tiles of a row get the same height.
                            Row(horizontalArrangement = Arrangement.spacedBy(GRID), modifier = Modifier.height(IntrinsicSize.Min)) {
                                row.forEach { action ->
                                    ActionTile(
                                        action,
                                        onClick = { onAction(action) },
                                        modifier = Modifier.weight(1f).fillMaxHeight().heightIn(min = rowHeight),
                                        text = tileText,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private const val MAX_FONT_SCALE = 1.0f
private val PAGE_PADDING = 12.dp
private val SECTION_GAP = 12.dp

/**
 * Design of 21.11.2026: one even cream page. In light mode the leaf picture (docs/design, cut and
 * faded into the cream with BgCut.java) lies across the full width at the top, also under the status bar.
 */
@Composable
fun Modifier.homeBackground(): Modifier {
    val home = AppColors.extra.home
    val page = background(home.page)
    if (!home.leafPhoto) return page
    return page.paint(
        painterResource(Res.drawable.home_background),
        sizeToIntrinsics = false,
        alignment = Alignment.TopCenter,
        contentScale = ContentScale.FillWidth,
    )
}

@Composable
private fun Header(greeting: Greeting, showDot: Boolean, onBell: () -> Unit) {
    val home = AppColors.extra.home
    // Compact like the picture: logo row, greeting and tagline close together.
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            LeafLogo(Modifier.size(48.dp))
            Text(
                stringResource(Res.string.home_brand),
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = AppFonts.playfair, fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
                color = home.ink,
                modifier = Modifier.weight(1f),
            )
            // Bell with a red dot while today is still open; opens the evening reminder.
            IconButton(onClick = onBell) {
                Box {
                    Icon(
                        Icons.Outlined.Notifications,
                        contentDescription = stringResource(Res.string.home_reminders),
                        tint = home.ink,
                        modifier = Modifier.size(28.dp),
                    )
                    if (showDot) {
                        Box(
                            Modifier.align(Alignment.TopEnd).offset(x = 1.dp, y = (-1).dp).size(9.dp)
                                .background(home.dot, CircleShape),
                        )
                    }
                }
            }
        }
        // More room between logo row and greeting, as in the picture.
        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(
                when (greeting) {
                    Greeting.MORNING -> Res.string.home_greeting_morning
                    Greeting.DAY -> Res.string.home_greeting_day
                    Greeting.EVENING -> Res.string.home_greeting_evening
                },
            ),
            style = MaterialTheme.typography.headlineLarge.copy(fontFamily = AppFonts.playfair, fontWeight = FontWeight.SemiBold, fontSize = 36.sp, lineHeight = 42.sp),
            color = home.ink,
        )
        Text(
            stringResource(Res.string.home_tagline),
            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = AppFonts.playfair, fontSize = 17.sp, lineHeight = 22.sp),
            color = home.inkSoft,
        )
    }
}

/**
 * Phone test 19.11.: at font scale 1.25 the tiles shrank their texts one by one and looked uneven.
 * One size for all four titles and one for all four descriptions, the largest that fits:
 * titles 18 → 15 sp in one line, otherwise 18 → 13 sp in two lines; descriptions 15 → 11 sp in at most two lines
 * without breaking a word. Steps of 0.5 sp.
 */
@Composable
private fun sharedTileStyles(textWidth: Dp): TileTextStyles {
    val measurer = rememberTextMeasurer()
    val widthPx = with(LocalDensity.current) { textWidth.toPx() }
    val safetyPx = with(LocalDensity.current) { SAFETY_MARGIN.toPx() }
    val titles = HomeAction.entries.map { stringResource(it.label) }
    val hints = HomeAction.entries.map { stringResource(it.hint) }
    val title = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp, lineHeight = 23.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp)
    val hint = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 17.sp, letterSpacing = 0.sp, fontWeight = FontWeight.Normal)
    return remember(titles, hints, widthPx, safetyPx, title, hint) {
        fun oneLineIn(width: Float, text: String, style: TextStyle) =
            measurer.measure(text, style, maxLines = 1, softWrap = false).size.width <= width

        fun twoLinesIn(width: Float, text: String, style: TextStyle) =
            text.split(' ').all { oneLineIn(width, it, style) } &&
                !measurer.measure(text, style, maxLines = 2, constraints = Constraints(maxWidth = width.toInt())).hasVisualOverflow

        fun oneLine(text: String, style: TextStyle) = oneLineIn(widthPx, text, style)

        fun twoLines(text: String, style: TextStyle) = twoLinesIn(widthPx, text, style)

        fun sized(style: TextStyle, size: Float) =
            // Line height shrinks along with the font.
            style.copy(fontSize = size.sp, lineHeight = (size * style.lineHeight.value / style.fontSize.value).sp)

        fun largestFitting(texts: List<String>, style: TextStyle, minSize: Float, fits: (String, TextStyle) -> Boolean): TextStyle? {
            var size = style.fontSize.value
            while (size >= minSize) {
                val candidate = sized(style, size)
                if (texts.all { fits(it, candidate) }) return candidate
                size -= 0.5f
            }
            return null
        }

        fun fitting(texts: List<String>, style: TextStyle, minSize: Float, fits: (String, TextStyle) -> Boolean): TextStyle =
            largestFitting(texts, style, minSize, fits) ?: sized(style, minSize)

        // Phone test 21.11.: "Symptome erfassen" does not fit in one line at font scale 1.25, not even at 13 sp.
        // Then every title may wrap at a word into two lines instead of being cut off.
        // Second phone test 21.11.: like the picture, titles and descriptions in one line where possible
        // (down to 13 / 11 sp), so all four tiles fit on the screen.
        val oneLineTitle = largestFitting(titles, title, 13f, ::oneLine)
        val titleStyle = oneLineTitle ?: fitting(titles, title, 13f, ::twoLines)
        val titleLines = if (oneLineTitle != null) 1 else 2
        // The description stays clearly smaller than the title (phone test 19.11.: 15 sp hint under a shrunk title).
        val hintStart = minOf(hint.fontSize.value, titleStyle.fontSize.value - 3f)
        val hintBase = hint.copy(fontSize = hintStart.sp, lineHeight = (hintStart * 4f / 3f).sp)
        val hintStyle = largestFitting(hints, hintBase, 10.5f, ::oneLine) ?: fitting(hints, hintBase, 11f, ::twoLines)
        // Two lines are only reserved when one of the four descriptions needs them.
        // Lines against the real text width (without the safety margin used for the size).
        val lines = hints.maxOf { measurer.measure(it, hintStyle, maxLines = 2, constraints = Constraints(maxWidth = (widthPx + safetyPx).toInt())).lineCount }
        TileTextStyles(titleStyle, hintStyle, lines, titleLines)
    }
}

private val GRID = 12.dp
private val MIN_TILE_HEIGHT = 112.dp
private val TILE_PADDING = 12.dp
private val SAFETY_MARGIN = 4.dp

/**
 * Design of 21.11.2026: soft gradient, large icon top left, a chevron in a white circle top right,
 * title and description at the bottom. All four tiles alike (second picture: no button on the day
 * check tile; whether today is still open shows as the red dot on the bell).
 */
@Composable
private fun ActionTile(
    action: HomeAction,
    onClick: () -> Unit,
    modifier: Modifier,
    text: TileTextStyles,
) {
    val colors = tileColors(action)
    val shape = MaterialTheme.shapes.large
    Surface(
        onClick = onClick,
        shape = shape,
        color = Color.Transparent,
        contentColor = colors.content,
        border = BorderStroke(1.dp, AppColors.extra.home.cardBorder),
        modifier = modifier.shadow(elevation = 2.dp, shape = shape, clip = false),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .drawBehind { drawTileBackground(colors.gradient) }
                .padding(TILE_PADDING),
        ) {
            Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                TileIcon(action, colors, Modifier.size(52.dp))
                Spacer(Modifier.weight(1f))
                // Picture of 21.11.2026 (second version): the chevron sits in a small white circle.
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(30.dp).background(AppColors.extra.home.chevronCircle, CircleShape),
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = colors.content,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            Spacer(Modifier.weight(1f).heightIn(min = 6.dp))
            // Sizes shared by all four tiles (sharedTileStyles). The title wraps only when one of them does not fit
            // in one line; the description reserves the same number of lines everywhere.
            Text(stringResource(action.label), style = text.title, maxLines = text.titleLines, overflow = TextOverflow.Ellipsis)
            Text(
                stringResource(action.hint),
                style = text.hint,
                color = colors.content.copy(alpha = 0.85f),
                minLines = text.hintLines,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

/**
 * Design of 21.11.2026 (user feedback: "not just one colour"): a clear diagonal gradient from a light
 * corner top left to a deeper one bottom right, a soft light glow top left and a wave along the bottom.
 */
private fun DrawScope.drawTileBackground(gradient: TileGradient) {
    // Mostly top to bottom like the picture, slightly diagonal.
    drawRect(Brush.linearGradient(listOf(gradient.start, gradient.end), start = Offset(size.width * 0.2f, 0f), end = Offset(size.width * 0.8f, size.height)))
    drawRect(
        Brush.radialGradient(
            listOf(Color.White.copy(alpha = 0.35f), Color.Transparent),
            center = Offset(size.width * 0.15f, size.height * 0.1f),
            radius = size.maxDimension * 0.75f,
        ),
    )
    val wave = Path().apply {
        moveTo(0f, size.height * 0.78f)
        cubicTo(
            size.width * 0.35f, size.height * 0.62f,
            size.width * 0.65f, size.height * 0.98f,
            size.width, size.height * 0.68f,
        )
        lineTo(size.width, size.height)
        lineTo(0f, size.height)
        close()
    }
    drawPath(wave, Brush.verticalGradient(listOf(gradient.wave.copy(alpha = 0.25f), gradient.wave.copy(alpha = 0.7f)), startY = size.height * 0.6f, endY = size.height))
}

private class TileColors(val gradient: TileGradient, val content: Color, val accent: Color)

/** Icons as in the design picture: fork and knife, heart with pulse, moon with stars, bars with magnifier. */
@Composable
private fun TileIcon(action: HomeAction, colors: TileColors, modifier: Modifier) {
    when (action) {
        HomeAction.LOG_MEAL -> Icon(action.icon, contentDescription = null, tint = colors.accent, modifier = modifier)
        HomeAction.LOG_SYMPTOM -> HeartPulseIcon(colors.accent, modifier)
        HomeAction.DAY_CHECK -> MoonIcon(colors.accent, modifier)
        HomeAction.RESULTS -> PatternIcon(colors.accent, colors.gradient.start, modifier)
    }
}

/** Text sizes shared by the four tiles, and how many lines every description reserves. */
private class TileTextStyles(
    val title: TextStyle,
    val hint: TextStyle,
    val hintLines: Int,
    val titleLines: Int,
)

/** Food and results = sage, symptoms and day check = apricot (design of 21.11.2026). */
@Composable
private fun tileColors(action: HomeAction): TileColors {
    val home = AppColors.extra.home
    return when (action) {
        HomeAction.LOG_MEAL -> TileColors(home.mealTile, home.onSageTile, home.icon)
        HomeAction.LOG_SYMPTOM -> TileColors(home.symptomTile, home.onWarmTile, home.heart)
        HomeAction.DAY_CHECK -> TileColors(home.dayCheckTile, home.onWarmTile, home.moon)
        HomeAction.RESULTS -> TileColors(home.resultsTile, home.onSageTile, home.icon)
    }
}
