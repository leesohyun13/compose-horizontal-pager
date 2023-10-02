package com.sohyun.image

import android.util.Log
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.node.LayoutAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize

// get view size without updating modifier
fun Modifier.detectLayoutSize(onChangeSize: (Size) -> Unit) = this then DetectedLayoutSizeModifierElement(onChangeSize)

data class DetectedLayoutSizeModifierElement(val onChangeSize: (Size) -> Unit) : ModifierNodeElement<SizeModifierNode>() {
    override fun create() = SizeModifierNode(onChangeSize)
    override fun update(node: SizeModifierNode) {
        node.onChangeSize = onChangeSize
    }
    override fun InspectorInfo.inspectableProperties() {
        name = "detectSize"
        properties["onChangeSize"] = onChangeSize
    }
}

class SizeModifierNode(var onChangeSize: (Size) -> Unit) : LayoutAwareModifierNode, Modifier.Node() {
    override fun onRemeasured(size: IntSize) {
        Log.d("SizeModifierNode", "size: ${size.toSize()}")
        onChangeSize(size.toSize())
    }
}

// debugging state
fun Modifier.touchable(touchState: TouchableState): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "touchable"
        properties["touchable"] = touchState
    }
) {
    this
}