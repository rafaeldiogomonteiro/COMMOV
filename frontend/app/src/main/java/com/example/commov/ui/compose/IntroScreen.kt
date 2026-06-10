package com.example.commov.ui.compose

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.commov.R
import kotlinx.coroutines.launch

private data class IntroSlide(
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
    @DrawableRes val iconRes: Int,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IntroScreen(
    onComplete: () -> Unit,
    onSkip: () -> Unit,
) {
    val slides = rememberIntroSlides()
    val pagerState = rememberPagerState(pageCount = { slides.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == slides.lastIndex
    val brandBlue = colorResource(R.color.velotask_blue)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brandBlue)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Text(
            text = stringResource(R.string.intro_skip),
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 12.dp, end = 20.dp)
                .clip(RoundedCornerShape(20.dp))
                .clickable(onClick = onSkip)
                .padding(horizontal = 14.dp, vertical = 8.dp),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Image(
                painter = painterResource(R.drawable.velotask_logo_intro),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .height(120.dp),
                contentScale = ContentScale.Fit,
            )

            Spacer(modifier = Modifier.height(28.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) { page ->
                IntroSlideContent(
                    slide = slides[page],
                    pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction,
                    isActive = pagerState.currentPage == page,
                )
            }

            IntroPageIndicator(
                pageCount = slides.size,
                currentPage = pagerState.currentPage,
                modifier = Modifier.padding(bottom = 28.dp),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .clickable {
                        if (isLastPage) {
                            onComplete()
                        } else {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(
                        if (isLastPage) R.string.intro_get_started else R.string.intro_next
                    ),
                    color = brandBlue,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@Composable
private fun rememberIntroSlides(): List<IntroSlide> {
    return listOf(
        IntroSlide(
            titleRes = R.string.intro_slide_welcome_title,
            subtitleRes = R.string.intro_slide_welcome_subtitle,
            iconRes = R.drawable.ic_home,
        ),
        IntroSlide(
            titleRes = R.string.intro_slide_projects_title,
            subtitleRes = R.string.intro_slide_projects_subtitle,
            iconRes = R.drawable.ic_projects,
        ),
        IntroSlide(
            titleRes = R.string.intro_slide_tasks_title,
            subtitleRes = R.string.intro_slide_tasks_subtitle,
            iconRes = R.drawable.ic_check_circle,
        ),
        IntroSlide(
            titleRes = R.string.intro_slide_team_title,
            subtitleRes = R.string.intro_slide_team_subtitle,
            iconRes = R.drawable.ic_settings,
        ),
    )
}

@Composable
private fun IntroSlideContent(
    slide: IntroSlide,
    pageOffset: Float,
    isActive: Boolean,
) {
    val parallax = pageOffset.coerceIn(-1f, 1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                translationX = parallax * 48f
                alpha = 1f - kotlin.math.abs(parallax) * 0.4f
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(slide.iconRes),
                contentDescription = null,
                modifier = Modifier.size(44.dp),
                colorFilter = ColorFilter.tint(Color.White),
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        AnimatedVisibility(
            visible = isActive,
            enter = fadeIn(tween(400)) + slideInVertically(tween(450)) { it / 4 },
            exit = fadeOut(tween(200)),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(slide.titleRes),
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 32.sp,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(slide.subtitleRes),
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun IntroPageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val selected = index == currentPage
            val width by animateFloatAsState(
                targetValue = if (selected) 24f else 8f,
                animationSpec = tween(250, easing = FastOutSlowInEasing),
                label = "dot_width_$index",
            )
            val alpha by animateFloatAsState(
                targetValue = if (selected) 1f else 0.45f,
                animationSpec = tween(250),
                label = "dot_alpha_$index",
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .height(8.dp)
                    .width(width.dp)
                    .alpha(alpha)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    }
}
