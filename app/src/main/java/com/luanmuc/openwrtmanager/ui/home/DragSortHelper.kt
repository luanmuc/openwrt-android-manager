package com.luanmuc.openwrtmanager.ui.home

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize

/**
 * 拖拽排序状态
 */
@Stable
class DragSortState {
    var isDragging by mutableStateOf(false)
    var draggingIndex by mutableStateOf(-1)
    var dragOffset by mutableStateOf(Offset.Zero)
    var targetIndex by mutableStateOf(-1)
}

/**
 * 拖拽排序Modifier
 */
fun Modifier.dragSort(
    state: DragSortState,
    index: Int,
    onDragEnd: (from: Int, to: Int) -> Unit
): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }
    
    Modifier.pointerInput(Unit) {
        detectDragGestures(
            onDragStart = {
                state.isDragging = true
                state.draggingIndex = index
                state.dragOffset = Offset.Zero
            },
            onDrag = { change, dragAmount ->
                change.consume()
                state.dragOffset = state.dragOffset.plus(dragAmount)
                
                // 计算目标位置（简化版：根据垂直偏移计算）
                val itemHeight = size.height.toFloat()
                if (itemHeight > 0) {
                    val offsetY = state.dragOffset.y
                    val moveBy = (offsetY / itemHeight).toInt()
                    var target = index + moveBy
                    // 限制范围
                    target = target.coerceIn(0, 100) // 假设最多100个卡片
                    state.targetIndex = target
                }
            },
            onDragEnd = {
                if (state.targetIndex >= 0 && state.targetIndex != state.draggingIndex) {
                    onDragEnd(state.draggingIndex, state.targetIndex)
                }
                state.isDragging = false
                state.draggingIndex = -1
                state.dragOffset = Offset.Zero
                state.targetIndex = -1
            },
            onDragCancel = {
                state.isDragging = false
                state.draggingIndex = -1
                state.dragOffset = Offset.Zero
                state.targetIndex = -1
            }
        )
    }
}
