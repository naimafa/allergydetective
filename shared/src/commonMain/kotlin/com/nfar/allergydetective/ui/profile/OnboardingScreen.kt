package com.nfar.allergydetective.ui.profile

import allergydetective.shared.generated.resources.welcome_title
import com.nfar.allergydetective.ui.theme.TileGradient
import com.nfar.allergydetective.ui.theme.AppFonts
import com.nfar.allergydetective.ui.theme.AppColors
import com.nfar.allergydetective.ui.home.homeBackground
import com.nfar.allergydetective.ui.home.AllergyDetectiveLogo
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Brush
import androidx.compose.material3.Surface
import androidx.compose.foundation.BorderStroke
import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.home_brand
import allergydetective.shared.generated.resources.onboarding_dishes_later
import allergydetective.shared.generated.resources.onboarding_dishes_start
import allergydetective.shared.generated.resources.onboarding_dishes_text
import allergydetective.shared.generated.resources.onboarding_dishes_title
import allergydetective.shared.generated.resources.onboarding_how_check
import allergydetective.shared.generated.resources.onboarding_how_meal
import allergydetective.shared.generated.resources.onboarding_how_symptom
import allergydetective.shared.generated.resources.onboarding_how_title
import allergydetective.shared.generated.resources.onboarding_next
import allergydetective.shared.generated.resources.onboarding_patience_text
import allergydetective.shared.generated.resources.onboarding_leave_out_text
import allergydetective.shared.generated.resources.onboarding_patience_title
import allergydetective.shared.generated.resources.onboarding_purpose
import allergydetective.shared.generated.resources.onboarding_purpose_title
import allergydetective.shared.generated.resources.onboarding_swipe_hint
import allergydetective.shared.generated.resources.onboarding_welcome
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private const val PAGE_WELCOME = 0
private const val PAGE_HOW = 1
private const val PAGE_PATIENCE = 2
private const val PAGE_ALLERGIES = 3
private const val PAGE_DISHES = 4

/**
 * First start (TRD 3.1, self-test 18.11.): a few short pages that introduce the app,
 * the known allergies, and at the end the tip to save frequent dishes.
 * [onFinished] gets true when the user wants to add dishes right away.
 * [replay] shows the intro again from the profile, without the allergies page.
 *
 * User note 21.11.2026: swipe from page to page with dots below instead of a "Weiter" button.
 * On the first start the allergies page is the gate: swiping past it works only after saving.
 */
@Composable
fun OnboardingScreen(
    onFinished: (openDishes: Boolean) -> Unit,
    replay: Boolean = false,
    viewModel: KnownAllergiesViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pages = if (replay) {
        listOf(PAGE_WELCOME, PAGE_HOW, PAGE_PATIENCE, PAGE_DISHES)
    } else {
        listOf(PAGE_WELCOME, PAGE_HOW, PAGE_PATIENCE, PAGE_ALLERGIES, PAGE_DISHES)
    }
    var allergiesSaved by remember { mutableStateOf(replay) }
    // Before the allergies are saved the pager ends on the allergies page.
    val reachable = if (allergiesSaved) pages.size else pages.indexOf(PAGE_ALLERGIES) + 1
    val pager = rememberPagerState { reachable }
    val scope = rememberCoroutineScope()
    // Saving the allergies completes the onboarding; the dishes tip follows.
    LaunchedEffect(state.saved) {
        if (state.saved) {
            viewModel.onSavedHandled()
            allergiesSaved = true
        }
    }
    LaunchedEffect(allergiesSaved) {
        if (allergiesSaved && !replay) pager.animateScrollToPage(pages.indexOf(PAGE_DISHES))
    }
    // User 23.11.2026: "die Einführungsseite ist so kahl". The look of the start screen: leaf picture, logo, Playfair
    // titles, a big round picture per page, texts in cream cards and the three steps as coloured tiles.
    val home = AppColors.extra.home
    Column(modifier = Modifier.fillMaxSize().homeBackground().safeDrawingPadding().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AllergyDetectiveLogo(Modifier.size(40.dp))
            Text(stringResource(Res.string.home_brand), style = MaterialTheme.typography.titleLarge.copy(fontFamily = AppFonts.playfair), color = home.ink)
        }
        HorizontalPager(state = pager, modifier = Modifier.weight(1f).fillMaxWidth().padding(top = 16.dp)) { index ->
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                when (pages[index]) {
                    PAGE_WELCOME -> {
                        PagePicture { AllergyDetectiveLogo(Modifier.size(84.dp)) }
                        PageTitle(stringResource(Res.string.welcome_title))
                        PageText(stringResource(Res.string.onboarding_welcome))
                        InfoCard(stringResource(Res.string.onboarding_purpose_title), stringResource(Res.string.onboarding_purpose))
                    }
                    PAGE_HOW -> {
                        PageTitle(stringResource(Res.string.onboarding_how_title))
                        StepTile(1, "🍽️", Res.string.onboarding_how_meal, home.mealTile)
                        StepTile(2, "🤒", Res.string.onboarding_how_symptom, home.symptomTile)
                        StepTile(3, "🌙", Res.string.onboarding_how_check, home.dayCheckTile)
                    }
                    PAGE_PATIENCE -> {
                        PagePicture { Text("⏳", fontSize = 56.sp) }
                        PageTitle(stringResource(Res.string.onboarding_patience_title))
                        // Seven days, three of them done: the first stretch at a glance.
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            repeat(7) { day ->
                                Box(Modifier.size(22.dp).background(if (day < 3) home.accent else home.progressTrack, CircleShape))
                            }
                        }
                        PageText(stringResource(Res.string.onboarding_patience_text))
                        // User 24.11.2026: leaving food out now and then is what makes patterns visible.
                        PageText(stringResource(Res.string.onboarding_leave_out_text))
                    }
                    PAGE_ALLERGIES -> {
                        KnownAllergiesContent(state, viewModel::onToggle)
                        Button(
                            onClick = viewModel::onSave,
                            enabled = !state.loading,
                            shape = CircleShape,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                        ) { Text(stringResource(Res.string.onboarding_next)) }
                    }
                    PAGE_DISHES -> {
                        PagePicture { Text("🍲", fontSize = 56.sp) }
                        PageTitle(stringResource(Res.string.onboarding_dishes_title))
                        PageText(stringResource(Res.string.onboarding_dishes_text))
                        Button(onClick = { onFinished(true) }, shape = CircleShape, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                            Text(stringResource(Res.string.onboarding_dishes_start))
                        }
                        OutlinedButton(onClick = { onFinished(false) }, shape = CircleShape, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                            Text(stringResource(Res.string.onboarding_dishes_later))
                        }
                    }
                }
            }
        }
        // Dots below, each can be tapped too. On the first page a short hint says that swiping goes on.
        if (pager.currentPage == 0) {
            Text(
                stringResource(Res.string.onboarding_swipe_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 4.dp),
            )
        }
        PageDots(
            current = pager.currentPage,
            count = pages.size,
            reachable = reachable,
            onSelect = { scope.launch { pager.animateScrollToPage(it) } },
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
    }
}

/** A big round sage picture at the top of a page. */
@Composable
private fun PagePicture(content: @Composable () -> Unit) {
    val home = AppColors.extra.home
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.padding(top = 8.dp).size(140.dp).background(Brush.linearGradient(listOf(home.mealTile.start, home.mealTile.end)), CircleShape),
    ) { content() }
}

@Composable
private fun PageTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.headlineSmall.copy(fontFamily = AppFonts.playfair),
        color = AppColors.extra.home.ink,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun PageText(text: String) {
    Text(text, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
}

/** Title and text in a cream card, like the cards of the other screens. */
@Composable
private fun InfoCard(title: String, text: String) {
    val home = AppColors.extra.home
    Surface(shape = MaterialTheme.shapes.large, color = home.card, border = BorderStroke(1.dp, home.cardBorder), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = home.ink)
            Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** One step of "So funktioniert es" as a tile in the colour of its tile on the start screen. */
@Composable
private fun StepTile(number: Int, emoji: String, text: StringResource, tile: TileGradient) {
    val home = AppColors.extra.home
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(listOf(tile.start, tile.end)), MaterialTheme.shapes.large)
            .padding(14.dp),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(48.dp).background(home.chevronCircle, CircleShape)) {
            Text(emoji, fontSize = 24.sp)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text("$number", style = MaterialTheme.typography.labelLarge, color = home.accent)
            Text(stringResource(text), style = MaterialTheme.typography.bodyLarge, color = home.ink)
        }
    }
}

/** Where the user is in the intro: the current dot wide, the others small; dots not yet reachable are faint. */
@Composable
private fun PageDots(current: Int, count: Int, reachable: Int, onSelect: (Int) -> Unit, modifier: Modifier) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        repeat(count) { index ->
            val scheme = MaterialTheme.colorScheme
            val color = when {
                index == current -> AppColors.extra.home.accent
                index < reachable -> scheme.outline
                else -> scheme.outlineVariant
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(32.dp).clickable(enabled = index < reachable && index != current) { onSelect(index) },
            ) {
                Box(Modifier.size(width = if (index == current) 22.dp else 10.dp, height = 10.dp).background(color, CircleShape))
            }
        }
    }
}
