package com.sohyun.image

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.util.VelocityTracker
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * image touch state
 * 1) scroll image : change scale and scroll wherever.
 * 2) zoom in/out image : manage scale state and
 * */
@Composable
fun rememberTouchableState(minScale: Float = 1f, maxScale: Float = 3f) =
    remember { TouchableState(minScale, maxScale) }

@Stable
class TouchableState(private val minScale: Float = 1f, private val maxScale: Float = 3f) {
    private val TAG = "TouchableState()"
    private var imageSize = Size.Zero
    private var layoutSize = Size.Zero

    private var _scale = Animatable(minScale).apply {
        updateBounds(minScale, maxScale)
    }

    val scale: Float
        get() = _scale.value

    private var _offsetX = Animatable(0f)
    val offsetX: Float
        get() = _offsetX.value

    private var _offsetY = Animatable(0f)
    val offsetY: Float
        get() = _offsetY.value

    fun setImageSize(size: Size) {
        Log.d(TAG, "setImageSize() $size ")
        imageSize = size
        updateImageSize()
    }

    fun setScreenSize(size: Size) {
        Log.d(TAG, "setScreenSize() $size ")
        layoutSize = size
        updateImageSize()
    }

    private val velocityTracker = VelocityTracker()
    private val velocityDecay = exponentialDecay<Float>()
    private var shouldFling = true

    private var shouldConsumeEvent: Boolean? = null

    private var realImageSize = Size.Zero
    private fun updateImageSize() {
        if ((imageSize == Size.Zero) || (layoutSize == Size.Zero)) {
            realImageSize = Size.Zero
            return
        }

        val imageAspectRatio = imageSize.width / imageSize.height
        val layoutAspectRatio = layoutSize.width / layoutSize.height

        realImageSize = if (imageAspectRatio > layoutAspectRatio) {
            imageSize * (layoutSize.width / imageSize.width)
        } else {
            imageSize * (layoutSize.height / imageSize.height)
        }
    }

    // if tab double, zoom in and out
    suspend fun zoomIn() {
        val midScale = (minScale + maxScale) / 2f
        when {
            _scale.value < midScale -> {
                _scale.animateTo(midScale)
            }
            _scale.value < maxScale -> {
                _scale.animateTo(maxScale)
            }
            _scale.value == maxScale -> {
                _offsetX.snapTo(0f)
                _offsetY.snapTo(0f)

                _scale.animateTo(minScale)
            }
        }
    }

    fun startGesture() {
        Log.d(TAG, "startGesture()")
        shouldConsumeEvent = null
    }

    // if do zoom or scroll, consume gesture. else not consume.
    // for example, when image is inside horizontal pager, we have to divide what we consume.
    // if not, cannot scroll to next page on horizontal pager.
    fun canConsume(pan: Offset, zoom: Float) : Boolean {
        Log.d(TAG, "canConsume()")
        return shouldConsumeEvent ?: run {
            var consume = true
            if (zoom == minScale) {
                if (scale == minScale) {
                    consume = false
                } else {
                    val ratio = (abs(pan.x) / abs(pan.y))
                    if (ratio > 3) {   // Horizontal drag // TODO 왜 horizontal만 체크하는거지?
                        if ((pan.x < 0) && (_offsetX.value == _offsetX.lowerBound)) {
                            // Drag R to L when right edge of the content is shown.
                            consume = false
                        }
                        if ((pan.x > 0) && (_offsetX.value == _offsetX.upperBound)) {
                            // Drag L to R when left edge of the content is shown.
                            consume = false
                        }
                    }
                }
            }
            shouldConsumeEvent = consume
            consume
        }
    }

    // when we load image or set page, reset image state.
    suspend fun reset() = coroutineScope {
        Log.d(TAG, "reset()")
        launch { _scale.snapTo(1f) }
        _offsetX.updateBounds(0f, 0f)
        launch { _offsetX.snapTo(0f) }
        _offsetY.updateBounds(0f, 0f)
        launch { _offsetY.snapTo(0f) }
    }

    suspend fun changeGesture(zoom: Float, pan: Offset, timeMillis: Long) = coroutineScope {
        Log.d(TAG, "changeGesture() $zoom, (${pan.x},${pan.y})")
        _scale.snapTo(_scale.value * zoom)

        val boundX = java.lang.Float.max(
            (realImageSize.width * _scale.value - layoutSize.width),
            0f
        ) / 2f
        _offsetX.updateBounds(-boundX, boundX)
        launch {
            _offsetX.snapTo(_offsetX.value + pan.x)
        }

        val boundY = java.lang.Float.max(
            (realImageSize.height * _scale.value - layoutSize.height),
            0f
        ) / 2f
        _offsetY.updateBounds(-boundY, boundY)

        launch {
            _offsetY.snapTo(_offsetY.value + pan.y)
        }

        velocityTracker.addPosition(timeMillis, position = pan)

        if (zoom != 1f) {
            shouldFling = false
        }
    }

    suspend fun onEndGesture() = coroutineScope {
        Log.d(TAG, "onEndGesture()")
        if (shouldFling) {
            val velocity = velocityTracker.calculateVelocity()
            launch {
                _offsetX.animateDecay(velocity.x, velocityDecay)
            }
            launch {
                _offsetY.animateDecay(velocity.y, velocityDecay)
            }
        }

        shouldFling = true

        if (_scale.value < 1f) {
            launch {
                _scale.animateTo(1f)
            }
        }
    }
}
