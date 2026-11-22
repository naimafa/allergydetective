package com.nfar.allergydetective.ui.favorites

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

/**
 * Reordering a LazyColumn by dragging a handle (user note 21.11.2026: "die Liste der Gerichte mit dem Finger
 * nach oben oder unten verschieben"). Items are found by their Long key; while dragging, the item follows the
 * finger and changes place with the neighbour it passes the middle of.
 */
@Stable
class DragToReorderState(
    private val list: LazyListState,
    private val onMove: (from: Long, to: Long) -> Unit,
    private val onDone: () -> Unit,
) {
    var draggedKey by mutableStateOf<Long?>(null)
        private set
    var offset by mutableFloatStateOf(0f)
        private set

    internal fun start(key: Long) {
        draggedKey = key
        offset = 0f
    }

    internal fun drag(dy: Float) {
        val key = draggedKey ?: return
        offset += dy
        val items = list.layoutInfo.visibleItemsInfo
        val current = items.firstOrNull { it.key == key } ?: return
        val middle = current.offset + offset + current.size / 2f
        val target = items.firstOrNull { it.key is Long && it.key != key && middle >= it.offset && middle <= it.offset + it.size }
            ?: return
        onMove(key, target.key as Long)
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
fun rememberDragToReorderState(list: LazyListState, onMove: (Long, Long) -> Unit, onDone: () -> Unit): DragToReorderState =
    remember(list) { DragToReorderState(list, onMove, onDone) }

/** On the item: follows the finger and lifts slightly while dragged. */
fun Modifier.reorderableItem(state: DragToReorderState, key: Long): Modifier {
    val dragging = state.draggedKey == key
    return if (!dragging) this else this
        .zIndex(1f)
        .graphicsLayer { translationY = state.offset }
        .shadow(8.dp, shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp))
}

/** On the handle: drag to move the item. */
fun Modifier.dragHandle(state: DragToReorderState, key: Long): Modifier = pointerInput(key) {
    detectDragGestures(
        onDragStart = { state.start(key) },
        onDragEnd = { state.end() },
        onDragCancel = { state.end() },
        onDrag = { change, amount ->
            change.consume()
            state.drag(amount.y)
        },
    )
}
