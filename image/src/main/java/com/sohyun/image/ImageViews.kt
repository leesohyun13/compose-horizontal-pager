package com.sohyun.image

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.google.android.material.progressindicator.CircularProgressIndicator

/**
 * load image url on view using coil
 *
 * @param imagePath image url
 * @param onLoading loading view
 * @param onError error view
 * @param contentDescription Text used by accessibility services to describe what this image represents
 * @param contentScale the aspect ratio scaling to be used
 * */
@Composable
fun ImageView(
    modifier: Modifier = Modifier,
    imagePath: String,
    imageSize: suspend (androidx.compose.ui.geometry.Size) -> Unit ={},
    onLoading: @Composable () -> Unit = { CircularProgressIndicator(LocalContext.current) },
    onError: (@Composable () -> Unit)? = null,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val model = ImageRequest.Builder(LocalContext.current)
        .data(imagePath)
        .crossfade(true)
        .build()

    SubcomposeAsyncImage(
        model = model,
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier,
    ) {
        val state = painter.state
        when {
            state is AsyncImagePainter.State.Loading || (state is AsyncImagePainter.State.Error && onError == null) -> {
                onLoading()
            }
            state is AsyncImagePainter.State.Error && onError != null -> {
                onError()
            }
            else -> {
                LaunchedEffect(this@SubcomposeAsyncImage.painter, imagePath) {
                    imageSize.invoke(this@SubcomposeAsyncImage.painter.intrinsicSize)
                }

                SubcomposeAsyncImageContent()
            }
        }
    }
}