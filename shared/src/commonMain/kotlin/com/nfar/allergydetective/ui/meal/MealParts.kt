package com.nfar.allergydetective.ui.meal
import androidx.compose.foundation.layout.widthIn
import allergydetective.shared.generated.resources.meal_portion_option_large
import allergydetective.shared.generated.resources.meal_portion_option_large_spice
import allergydetective.shared.generated.resources.meal_portion_option_medium
import allergydetective.shared.generated.resources.meal_portion_option_medium_spice
import allergydetective.shared.generated.resources.meal_portion_option_small
import allergydetective.shared.generated.resources.meal_portion_option_small_spice
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.meal_portion_word
import allergydetective.shared.generated.resources.meal_remove
import allergydetective.shared.generated.resources.meal_dish_collapse
import allergydetective.shared.generated.resources.meal_dishes_chosen
import allergydetective.shared.generated.resources.meal_remove_dish
import allergydetective.shared.generated.resources.meal_dish_expand
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BakeryDining
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Cookie
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material.icons.outlined.RamenDining
import androidx.compose.material.icons.outlined.RoomService
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nfar.allergydetective.domain.analysis.model.Portion
import com.nfar.allergydetective.domain.diary.IngredientInfo
import com.nfar.allergydetective.domain.time.MealSlot
import com.nfar.allergydetective.ui.theme.AppColors
import org.jetbrains.compose.resources.stringResource

/**
 * The meal kinds as pills with an icon (design picture 23.11.2026), always in the order of the day
 * (user 23.11.: the chosen one must not jump to the front). Selected shows only by its colour, no check mark.
 * When one pill is too narrow for icon and name (font 125 %), all leave the icon out; the name is never shrunk.
 */
@Composable
fun MealSlotGrid(rows: List<List<MealSlot>>, selected: MealSlot?, onSelect: (MealSlot) -> Unit) {
    val style = MaterialTheme.typography.labelLarge
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val labels = rows.map { row -> row.map { mealSlotLabel(it) } }
    val textWidths = remember(labels, style) {
        labels.map { row -> row.map { measurer.measure(it, style, maxLines = 1, softWrap = false).size.width } }
    }
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val full = constraints.maxWidth
        // Phone test 23.11.: at font 125 % the lower row had no room for icons while the upper one had. Either
        // every pill shows its icon or none does.
        val showIcons = with(density) {
            val gap = GRID_GAP.roundToPx()
            val padding = (PILL_PADDING * 2).roundToPx()
            val iconRoom = (18.dp + 6.dp).roundToPx()
            labels.indices.all { r ->
                val weights = labels[r].map { it.length + 4f }
                val room = full - gap * (labels[r].size - 1)
                labels[r].indices.all { i -> textWidths[r][i] + iconRoom + padding <= room * weights[i] / weights.sum() }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            rows.forEachIndexed { r, row ->
                Row(horizontalArrangement = Arrangement.spacedBy(GRID_GAP), modifier = Modifier.fillMaxWidth()) {
                    row.forEachIndexed { i, slot ->
                        val label = labels[r][i]
                        MealSlotPill(
                            slot, label, showIcons, slot == selected, { onSelect(slot) },
                            Modifier.weight(label.length + if (showIcons) 4f else 2f),
                        )
                    }
                }
            }
        }
    }
}

private val GRID_GAP = 6.dp
private val PILL_PADDING = 6.dp

@Composable
private fun MealSlotPill(slot: MealSlot, label: String, showIcon: Boolean, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        selected = selected,
        shape = CircleShape,
        color = if (selected) scheme.primary else AppColors.extra.home.page,
        contentColor = if (selected) scheme.onPrimary else AppColors.extra.home.ink,
        border = if (selected) null else BorderStroke(1.dp, scheme.outlineVariant),
        modifier = modifier.heightIn(min = 36.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = PILL_PADDING),
        ) {
            if (showIcon) {
                Icon(mealSlotIcon(slot), contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
            }
            Text(label, style = MaterialTheme.typography.labelLarge, maxLines = 1, softWrap = false, textAlign = TextAlign.Center)
        }
    }
}

private fun mealSlotIcon(slot: MealSlot): ImageVector = when (slot) {
    MealSlot.BREAKFAST -> Icons.Outlined.BakeryDining
    MealSlot.LUNCH -> Icons.Outlined.RamenDining
    MealSlot.DINNER -> Icons.Outlined.RoomService
    MealSlot.SNACK -> Icons.Outlined.Cookie
    MealSlot.DRINK -> Icons.Outlined.LocalDrink
}

/**
 * The picture of an ingredient (design picture 23.11.2026). There are hundreds of ingredients and no photo for
 * each (user 23.11.: agreed), so it shows a picture of its kind; own ingredients without a category get their
 * first letter in a sage circle.
 */
@Composable
fun IngredientBadge(ingredient: IngredientInfo, name: String) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp)) {
        when (val picture = ingredientPicture(ingredient)) {
            IngredientPicture.OilBottle -> OilBottle(Modifier.size(34.dp))
            IngredientPicture.MixedVegetables -> MixedVegetables()
            is IngredientPicture.Emoji -> EmojiText(picture.emoji, 26.dp)
            null -> Surface(shape = CircleShape, color = AppColors.extra.home.badge, contentColor = AppColors.extra.home.ink, modifier = Modifier.size(34.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(name.take(1).uppercase(), style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
}

private sealed interface IngredientPicture {
    data object OilBottle : IngredientPicture
    data object MixedVegetables : IngredientPicture
    data class Emoji(val emoji: String) : IngredientPicture
}

/**
 * User 23.11.2026: oils as a vessel full of oil, olives as the olive branch, every vegetable as a picture
 * of several vegetables. The rest by category.
 */
private fun ingredientPicture(ingredient: IngredientInfo): IngredientPicture? {
    KEY_EMOJI[ingredient.key]?.let { return IngredientPicture.Emoji(it) }
    return when (ingredient.categoryId) {
        OILS_FATS -> IngredientPicture.OilBottle
        VEGETABLES -> IngredientPicture.MixedVegetables
        else -> ingredient.categoryId?.let(CATEGORY_EMOJI::get)?.let(IngredientPicture::Emoji)
    }
}

/** Emoji in dp, not sp: the font scale of 125 % would push the picture out of its place. */
@Composable
private fun EmojiText(emoji: String, size: Dp, modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    Text(emoji, fontSize = with(density) { size.toSp() }, lineHeight = with(density) { (size * 1.25f).toSp() }, modifier = modifier)
}

/** Carrot and tomato behind, broccoli in front: "Gemüse" as one picture. */
@Composable
private fun MixedVegetables() {
    Box(modifier = Modifier.size(40.dp)) {
        EmojiText("🥕", 18.dp, Modifier.align(Alignment.TopStart).rotate(-20f))
        EmojiText("🍅", 17.dp, Modifier.align(Alignment.TopEnd))
        EmojiText("🥦", 21.dp, Modifier.align(Alignment.BottomCenter))
    }
}

/** A glass bottle full of golden oil with a cork; no emoji shows oil. */
@Composable
private fun OilBottle(modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val glass = Color(0xFFDCE6D2)
        val outline = Color(0xFF8FA486)
        // Body and shoulders.
        val body = Path().apply {
            moveTo(w * 0.40f, h * 0.22f)
            lineTo(w * 0.60f, h * 0.22f)
            lineTo(w * 0.60f, h * 0.34f)
            cubicTo(w * 0.78f, h * 0.40f, w * 0.80f, h * 0.48f, w * 0.80f, h * 0.56f)
            lineTo(w * 0.80f, h * 0.88f)
            quadraticTo(w * 0.80f, h * 0.96f, w * 0.72f, h * 0.96f)
            lineTo(w * 0.28f, h * 0.96f)
            quadraticTo(w * 0.20f, h * 0.96f, w * 0.20f, h * 0.88f)
            lineTo(w * 0.20f, h * 0.56f)
            cubicTo(w * 0.20f, h * 0.48f, w * 0.22f, h * 0.40f, w * 0.40f, h * 0.34f)
            close()
        }
        drawPath(body, glass)
        // Oil up to the shoulders.
        clipPath(body) {
            drawRect(
                Brush.verticalGradient(listOf(Color(0xFFF2C94C), Color(0xFFC99A1E)), startY = h * 0.44f, endY = h * 0.96f),
                topLeft = Offset(0f, h * 0.44f),
                size = Size(w, h * 0.56f),
            )
            drawRect(Color.White.copy(alpha = 0.35f), topLeft = Offset(w * 0.27f, h * 0.48f), size = Size(w * 0.07f, h * 0.38f))
        }
        drawPath(body, outline, style = Stroke(width = w * 0.045f))
        // Cork.
        drawRoundRect(Color(0xFFB07A45), topLeft = Offset(w * 0.37f, h * 0.06f), size = Size(w * 0.26f, h * 0.18f), cornerRadius = CornerRadius(w * 0.05f))
    }
}

private const val VEGETABLES = 7L
private const val OILS_FATS = 13L

/** Single ingredients with their own picture. */
private val KEY_EMOJI = mapOf(
    "olives" to "🫒", "margarine" to "🧈",
    // Catalog gaps (user 25.11.2026): where one picture says it better than the category's.
    "biscuits" to "🍪", "waffles" to "🧇", "doughnut" to "🍩", "cream_cake" to "🍰", "pancakes_crepes" to "🥞",
    "lye_pastry" to "🥨", "flatbread" to "🫓", "tortilla_wrap" to "🌯", "rice_cakes" to "🍘", "pudding" to "🍮",
    "frankfurters" to "🌭", "broccoli" to "🥦", "cabbage" to "🥬", "sweet_potato" to "🍠", "fries" to "🍟",
    "grapes" to "🍇", "melon" to "🍈", "coconut" to "🥥", "hot_chocolate" to "☕", "milk_coffee" to "☕",
    "popcorn" to "🍿", "fruit_gums" to "🍬", "sorbet" to "🍧", "instant_noodles" to "🍜", "fish_fingers" to "🐟",
)

/** Seed categories (categories.json) → emoji. */
private val CATEGORY_EMOJI = mapOf(
    1L to "🌾", 2L to "🥛", 3L to "🧀", 4L to "🍖", 5L to "🐟", 6L to "🥚",
    8L to "🍎", 9L to "🥜", 10L to "🫘", 11L to "🥤", 12L to "🍯", 14L to "🧂", 15L to "🌿", 16L to "🍝", 17L to "🍞",
)

private val SEGMENT_WIDTH = 44.dp
private val SWITCH_WIDTH = SEGMENT_WIDTH * 3
private val BADGE_WIDTH = 40.dp
private val DELETE_SIZE = 40.dp

/**
 * User 24.11.2026: the portion as a small field "M ▾" in the same line as the name (two lines per entry looked
 * restless). A tap opens S / M / L, each with what it means.
 */
@Composable
fun PortionLine(
    portion: Portion,
    onPortion: (Portion) -> Unit,
    modifier: Modifier = Modifier,
    /** User 25.11.2026: spices, herbs, oils, nuts and seeds are not eaten by bites or glasses but by pinch or spoon. */
    smallAmounts: Boolean = false,
) {
    var open by remember { mutableStateOf(false) }
    val scheme = MaterialTheme.colorScheme
    val description = "${stringResource(Res.string.meal_portion_word)}: ${portionLabel(portion)}"
    Box(modifier = modifier) {
        Surface(
            onClick = { open = true },
            shape = CircleShape,
            color = AppColors.extra.home.page,
            contentColor = AppColors.extra.home.ink,
            border = BorderStroke(1.dp, scheme.outlineVariant),
            modifier = Modifier.heightIn(min = 36.dp).semantics { contentDescription = description },
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 12.dp, end = 6.dp)) {
                Text(portionLabel(portion), style = MaterialTheme.typography.labelMedium)
                Icon(Icons.Outlined.ArrowDropDown, contentDescription = null, modifier = Modifier.size(20.dp))
            }
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }, containerColor = AppColors.extra.home.card, modifier = Modifier.widthIn(min = 260.dp)) {
            Portion.entries.forEach { option ->
                DropdownMenuItem(
                    // User 24.11.2026: the list looked untidy. Letter in its own column, the word above, the amount small below.
                    text = {
                        val (word, amount) = stringResource(
                            when (option) {
                                Portion.SMALL -> if (smallAmounts) Res.string.meal_portion_option_small_spice else Res.string.meal_portion_option_small
                                Portion.MEDIUM -> if (smallAmounts) Res.string.meal_portion_option_medium_spice else Res.string.meal_portion_option_medium
                                Portion.LARGE -> if (smallAmounts) Res.string.meal_portion_option_large_spice else Res.string.meal_portion_option_large
                            },
                        ).split(" – ", limit = 2).let { it[0] to it.getOrElse(1) { "" } }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(portionLabel(option), style = MaterialTheme.typography.titleMedium, color = scheme.primary, modifier = Modifier.width(28.dp))
                            Column {
                                Text(word, style = MaterialTheme.typography.bodyMedium, fontWeight = if (option == portion) FontWeight.Bold else FontWeight.Normal)
                                Text(amount, style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
                            }
                        }
                    },
                    leadingIcon = {
                        if (option == portion) Icon(Icons.Outlined.Check, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(20.dp))
                        else Spacer(Modifier.size(20.dp))
                    },
                    onClick = {
                        open = false
                        onPortion(option)
                    },
                )
            }
        }
    }
}

/**
 * S · M · L in one outlined pill (design picture 23.11.2026); the chosen size is filled green, without a
 * check mark (user 23.11.: "nur die Farbe soll gewählt zeigen").
 */
@Composable
fun PortionSwitch(selected: Portion, onSelect: (Portion) -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = CircleShape,
        color = AppColors.extra.home.page,
        border = BorderStroke(1.dp, scheme.outlineVariant),
        modifier = modifier.width(SWITCH_WIDTH),
    ) {
        Row {
            Portion.entries.forEach { portion ->
                val chosen = portion == selected
                val label = portionLabel(portion)
                Surface(
                    onClick = { onSelect(portion) },
                    selected = chosen,
                    shape = CircleShape,
                    color = if (chosen) scheme.primary else AppColors.extra.home.page,
                    contentColor = if (chosen) scheme.onPrimary else AppColors.extra.home.ink,
                    modifier = Modifier.width(SEGMENT_WIDTH).heightIn(min = 30.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 1, softWrap = false)
                    }
                }
            }
        }
    }
}

/**
 * One ingredient: picture, name, S/M/L and the bin in one line (design picture 23.11.2026). User 23.11.: the bin
 * sits close to S/M/L; a long name breaks into a second line, and only when even that leaves too little room
 * S/M/L moves below the name. Raw / cooked follows below where it is asked.
 */
@Composable
fun IngredientRow(
    ingredient: IngredientInfo,
    name: String,
    portion: Portion,
    onPortion: (Portion) -> Unit,
    onRemove: () -> Unit,
    preparation: @Composable (() -> Unit)?,
    smallAmounts: Boolean = false,
) {
    val nameStyle = MaterialTheme.typography.bodyMedium.merge(
        TextStyle(fontWeight = FontWeight.Medium, hyphens = Hyphens.Auto, lineBreak = LineBreak.Paragraph),
    )
    // User 24.11.2026: name over the whole width with the bin, S/M/L in its own line below ("Portion"), like a dish.
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IngredientBadge(ingredient, name)
            Text(name, style = nameStyle, modifier = Modifier.weight(1f).padding(start = 10.dp, end = 8.dp))
            PortionLine(portion, onPortion, smallAmounts = smallAmounts)
            Spacer(Modifier.width(4.dp))
            DeleteButton(name, onRemove)
        }
        preparation?.let {
            Box(modifier = Modifier.padding(start = BADGE_WIDTH + 10.dp, top = 2.dp)) { it() }
        }
    }
}

@Composable
private fun DeleteButton(name: String, onClick: () -> Unit) {
    val home = AppColors.extra.home
    val description = "${stringResource(Res.string.meal_remove)}: $name"
    Surface(
        onClick = onClick,
        shape = CircleShape,
        // User 24.11.2026: the same plain bin everywhere, no circle.
        color = Color.Transparent,
        contentColor = home.ink,
        modifier = Modifier.size(DELETE_SIZE).semantics { contentDescription = description },
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(20.dp))
        }
    }
}


