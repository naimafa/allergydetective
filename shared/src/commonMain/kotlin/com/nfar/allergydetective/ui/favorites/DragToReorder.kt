package com.nfar.allergydetective.ui.favorites

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

/**
 * Reordering a LazyColumn by dragging a handle (user note 21.11.2026: "die Liste der Gerichte mit dem Finger
 * nach oben oder unten verschieben"). Items are found by their key; while dragging, the item follows the
 * finger and changes place with the neighbour it passes the middle of. Only items whose key [accepts] are
 * places to move to (dishes: Long ids; symptom areas, user 25.11.2026: their group keys).
 */
@Stable
class DragToReorderState(
    private val list: LazyListState,
    private val onMove: (from: Any, to: Any) -> Unit,
    private val onDone: () -> Unit,
    private val accepts: (Any) -> Boolean,
) {
    var draggedKey by mutableStateOf<Any?>(null)
        private set
    var offset by mutableFloatStateOf(0f)
        private set

    internal fun start(key: Any) {
        draggedKey = key
        offset = 0f
    }

    internal fun drag(dy: Float) {
        val key = draggedKey ?: return
        offset += dy
        val items = list.layoutInfo.visibleItemsInfo
        val current = items.firstOrNull { it.key == key } ?: return
        val middle = current.offset + offset + current.size / 2f
        val target = items.firstOrNull { accepts(it.key) && it.key != key && middle >= it.offset && middle <= it.offset + it.size }
            ?: return
        onMove(key, target.key)
        // The item now sits where the target was; keep it under the finger.
        offset -= (target.offset - current.offset)
    }

    internal fun end() {
        if (draggedKey != null) onDone()
        draggedKey = null
        offset = 0f
    }
}

@Composable
fun rememberDragToReorderState(list: LazyListState, onMove: (Long, Long) -> Unit, onDone: () -> Unit): DragToReorderState {
    val move by rememberUpdatedState(onMove)
    val done by rememberUpdatedState(onDone)
    return remember(list) { DragToReorderState(list, { from, to -> move(from as Long, to as Long) }, { done() }, accepts = { it is Long }) }
}

/** For items keyed by text: [accepts] says which keys can be moved to. */
@Composable
fun rememberDragToReorderState(
    list: LazyListState,
    onMove: (String, String) -> Unit,
    onDone: () -> Unit,
    accepts: (String) -> Boolean,
): DragToReorderState {
    val move by rememberUpdatedState(onMove)
    val done by rememberUpdatedState(onDone)
    val ok by rememberUpdatedState(accepts)
    return remember(list) { DragToReorderState(list, { from, to -> move(from as String, to as String) }, { done() }, accepts = { it is String && ok(it) }) }
}

/** On the item: follows the finger and lifts slightly while dragged. */
fun Modifier.reorderableItem(state: DragToReorderState, key: Any): Modifier {
    val dragging = state.draggedKey == key
    return if (!dragging) this else this
        .zIndex(1f)
        .graphicsLayer { translationY = state.offset }
        .shadow(8.dp, shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp))
}

/**
 * On the handle: hold, then drag to move the item. User 25.11.2026: dragging started at the first touch and
 * moved dishes by accident while scrolling; now it waits for a long press and confirms it with a short buzz.
 */
@Composable
fun Modifier.dragHandle(state: DragToReorderState, key: Any): Modifier {
    val haptics = LocalHapticFeedback.current
    return pointerInput(key) {
        detectDragGesturesAfterLongPress(
        onDragStart = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            state.start(key)
        },
        onDragEnd = { state.end() },
        onDragCancel = { state.end() },
        onDrag = { change, amount ->
            change.consume()
            state.drag(amount.y)
        },
        )
    }
}
