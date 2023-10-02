package com.sohyun.pointerinputsample

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import com.sohyun.image.TouchableImageView
import com.sohyun.pointerinputsample.ui.theme.PointerInputSampleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PointerInputSampleTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeImageListScreen(
                        listOf(
                            "https://picsum.photos/id/237/400/300",
                            "https://picsum.photos/id/235/400/300",
                            "https://picsum.photos/id/233/400/300"
                        )
                    )
                }
            }
        }
    }
}

// horizontal image pager
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeImageListScreen(images: List<String> = listOf()) {
    val state = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f
    ) { images.size }

    Column {
        HorizontalPager(
            modifier = Modifier.fillMaxSize(),
            state = state,
            userScrollEnabled = true
        ) {
            val isVisible by remember {
                derivedStateOf {
                    val offset = state.getOffsetFractionForPage(it)
                    Log.d("HorizontalPager()", "page: $it -> $offset")
                    (-1.0f < offset) and (offset < 1.0f)
                }
            }

            TouchableImageView(
                modifier = Modifier.fillMaxSize(),
                imagePath = images[it],
                isVisible = isVisible,
                contentScale = ContentScale.Fit,
            )
        }
    }
}

// single image
@Composable
fun HomeImageScreen(
    imageUrl: String,
) {
    Column {
        TouchableImageView(
            modifier = Modifier.fillMaxSize(),
            imagePath = imageUrl,
            isVisible = true,
            contentScale = ContentScale.Fit
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeImageListScreenPreview() {
    PointerInputSampleTheme {
        HomeImageListScreen(
            listOf(
                "https://picsum.photos/id/237/400/300",
                "https://picsum.photos/id/235/400/300",
                "https://picsum.photos/id/233/400/300"
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeImageScreenPreview() {
    PointerInputSampleTheme {
        HomeImageScreen("https://picsum.photos/id/237/400/300")
    }
}