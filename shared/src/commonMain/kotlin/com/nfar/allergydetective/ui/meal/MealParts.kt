package com.nfar.allergydetective.ui.meal

import allergydetective.shared.generated.resources.Res
import allergydetective.shared.generated.resources.meal_remove
import allergydetective.shared.generated.resources.meal_remove_dish
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
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Cookie
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material.icons.outlined.RamenDining
import androidx.compose.material.icons.outlined.RoomService
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
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
import androidx.compose.ui.unit.dp
import com.nfar.allergydetective.domain.analysis.model.Portion
import com.nfar.allergydetective.domain.diary.IngredientInfo
import com.nfar.allergydetective.domain.time.MealSlot
import com.nfar.allergydetective.ui.theme.AppColors
import org.jetbrains.compose.resources.stringResource

/**
 * The meal kinds as pills with an icon (design picture 23.11.2026), always in the order of the day
 * (user 23.11.: the chosen one must not jump to the front). Selected shows only by its colour, no check mark.
 * A pill too narrow for icon and name at font 125 % leaves the icon out; the name is never shrunk.
 */
@Composable
fun MealSlotRow(slots: List<MealSlot>, selected: MealSlot?, onSelect: (MealSlot) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
        slots.forEach { slot ->
            val label = mealSlotLabel(slot)
            MealSlotPill(slot, label, slot == selected, { onSelect(slot) }, Modifier.weight(label.length + 4f))
        }
    }
}

@Composable
private fun MealSlotPill(slot: MealSlot, label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val scheme = MaterialTheme.colorScheme
    val style = MaterialTheme.typography.labelLarge
    val measurer = rememberTextMeasurer()
    val textWidth = remember(label, style) { measurer.measure(label, style, maxLines = 1, softWrap = false).size.width }
    val iconRoom = with(LocalDensity.current) { (20.dp + 6.dp).roundToPx() }
    Surface(
        onClick = onClick,
        selected = selected,
        shape = CircleShape,
        color = if (selected) scheme.primary else AppColors.extra.home.page,
        contentColor = if (selected) scheme.onPrimary else AppColors.extra.home.ink,
        border = if (selected) null else BorderStroke(1.dp, scheme.outlineVariant),
        modifier = modifier.heightIn(min = 42.dp),
    ) {
        BoxWithConstraints(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 6.dp)) {
            val room = constraints.maxWidth
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (textWidth + iconRoom <= room) {
                    Icon(mealSlotIcon(slot), contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                }
                Text(label, style = style, maxLines = 1, softWrap = false, textAlign = TextAlign.Center)
            }
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
 * each (user 23.11.: agreed), so its category shows as an emoji; own ingredients without a category get their
 * first letter in a sage circle.
 */
@Composable
fun IngredientBadge(ingredient: IngredientInfo, name: String) {
    val emoji = ingredient.categoryId?.let(CATEGORY_EMOJI::get)
    val density = LocalDensity.current
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp)) {
        if (emoji != null) {
            // In dp, not sp: the font scale of 125 % would push the picture out of its place.
            Text(emoji, fontSize = with(density) { 26.dp.toSp() }, lineHeight = with(density) { 32.dp.toSp() })
        } else {
            Surface(shape = CircleShape, color = AppColors.extra.home.badge, contentColor = AppColors.extra.home.ink, modifier = Modifier.size(34.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(name.take(1).uppercase(), style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
}

/** Seed categories (categories.json) → emoji. */
private val CATEGORY_EMOJI = mapOf(
    1L to "🍞", 2L to "🥛", 3L to "🧀", 4L to "🍖", 5L to "🐟", 6L to "🥚", 7L to "🥬",
    8L to "🍎", 9L to "🥜", 10L to "🫘", 11L to "🥤", 12L to "🍯", 13L to "🫒", 14L to "🌿",
)

private val SEGMENT_WIDTH = 40.dp
private val SWITCH_WIDTH = SEGMENT_WIDTH * 3
private val BADGE_WIDTH = 40.dp
private val DELETE_SIZE = 38.dp

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
                    modifier = Modifier.width(SEGMENT_WIDTH).heightIn(min = 36.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(label, style = MaterialTheme.typography.labelLarge, maxLines = 1, softWrap = false)
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
) {
    val nameStyle = MaterialTheme.typography.titleSmall.merge(
        TextStyle(fontWeight = FontWeight.Medium, hyphens = Hyphens.Auto, lineBreak = LineBreak.Paragraph),
    )
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        BoxWithConstraints {
            val nameRoom = maxWidth - BADGE_WIDTH - 10.dp - SWITCH_WIDTH - 6.dp - DELETE_SIZE - 8.dp
            if (nameRoom >= 76.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IngredientBadge(ingredient, name)
                    Text(name, style = nameStyle, modifier = Modifier.weight(1f).padding(start = 10.dp, end = 8.dp))
                    PortionSwitch(portion, onPortion)
                    Spacer(Modifier.width(6.dp))
                    DeleteButton(name, onRemove)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IngredientBadge(ingredient, name)
                        Text(name, style = nameStyle, modifier = Modifier.weight(1f).padding(start = 10.dp, end = 8.dp))
                        DeleteButton(name, onRemove)
                    }
                    PortionSwitch(portion, onPortion, Modifier.padding(start = BADGE_WIDTH + 10.dp))
                }
            }
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
        color = lerp(home.page, home.badge, 0.7f),
        contentColor = home.ink,
        modifier = Modifier.size(DELETE_SIZE).semantics { contentDescription = description },
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(20.dp))
        }
    }
}

/**
 * The chosen dish as a header over its ingredients (design picture 23.11.2026, user 23.11.: "mach das und wir
 * sehen später wie das wirkt"). The ✕ takes the dish out again.
 */
@Composable
fun DishHeader(name: String, onRemove: () -> Unit) {
    val home = AppColors.extra.home
    val density = LocalDensity.current
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = lerp(home.page, home.badge, 0.6f),
        contentColor = home.ink,
        border = BorderStroke(1.dp, home.cardBorder),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 12.dp, top = 6.dp, bottom = 6.dp)) {
            Text("🍲", fontSize = with(density) { 30.dp.toSp() }, lineHeight = with(density) { 36.dp.toSp() })
            Text(name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f).padding(start = 12.dp))
            IconButton(onClick = onRemove) {
                Icon(Icons.Outlined.Close, contentDescription = stringResource(Res.string.meal_remove_dish))
            }
        }
    }
}
