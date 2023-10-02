package com.sohyun.image

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.launch

/**
 * touchable image view
 *
 * we can use below functions:
 * 1) zoom
 * 2) movement
 * */
@Composable
fun TouchableImageView(
    modifier: Modifier = Modifier,
    imagePath: String,
    onError: (@Composable () -> Unit)? = null,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Fit,
    minScale: Float = 1f,
    maxScale: Float = 3f,
    isVisible: Boolean = false,
) {
    val state = rememberTouchableState(minScale, maxScale)
    val scope = rememberCoroutineScope()

    LaunchedEffect(isVisible) {
        state.reset()
    }

    ImageView(
        modifier = modifier.fillMaxSize()
            .clipToBounds()
            .touchable(state)
            .detectLayoutSize { state.setScreenSize(it) }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        scope.launch {
                            state.zoomIn()
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectTransformCustomGestures(
                    onGesture = { _, pan, zoom, _, timeMills ->
                        val canConsume = state.canConsume(pan, zoom)
                        if (canConsume) {
                            scope.launch {
                                state.changeGesture(zoom, pan, timeMills)
                            }
                        }
                        canConsume
                    },
                    onGestureStart = {
                        state.startGesture()
                    },
                    onGestureEnd = {
                        scope.launch {
                            state.onEndGesture()
                        }
                    }
                )
            }.graphicsLayer {
                scaleX = state.scale
                scaleY = state.scale
                translationX = state.offsetX
                translationY = state.offsetY
            },
        imageSize = { state.setImageSize(it) },
        imagePath = imagePath,
        onError = onError,
        contentDescription = contentDescription,
        contentScale = contentScale,
    )
}
