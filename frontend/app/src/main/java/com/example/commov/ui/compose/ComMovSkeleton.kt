package com.example.commov.ui.compose

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.commov.R

@Composable
private fun skeletonBrush(): Brush {
    val base = colorResource(R.color.skeleton_base)
    val highlight = colorResource(R.color.skeleton_highlight)
    val transition = rememberInfiniteTransition(label = "skeleton")
    val translate by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "skeleton_translate"
    )
    return Brush.linearGradient(
        colors = listOf(base, highlight, base),
        start = Offset(translate - 300f, translate - 300f),
        end = Offset(translate, translate)
    )
}

@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(6.dp)
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(skeletonBrush())
    )
}

@Composable
private fun Modifier.skeletonCard(radius: Dp = 10.dp): Modifier {
    val shape = RoundedCornerShape(radius)
    return clip(shape)
        .background(colorResource(R.color.dashboard_card))
        .border(1.dp, colorResource(R.color.dashboard_card_stroke), shape)
}

@Composable
fun DashboardScreenSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth(0.72f)
                .height(24.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .height(120.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(2) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .skeletonCard(10.dp)
                        .padding(13.dp)
                ) {
                    SkeletonBox(
                        modifier = Modifier
                            .fillMaxWidth(0.65f)
                            .height(12.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    SkeletonBox(
                        modifier = Modifier
                            .width(48.dp)
                            .height(28.dp)
                    )
                    SkeletonBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .height(8.dp),
                        shape = RoundedCornerShape(4.dp)
                    )
                }
            }
        }
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .height(56.dp)
                .skeletonCard(10.dp)
        )
        SkeletonBox(
            modifier = Modifier
                .padding(top = 24.dp)
                .width(120.dp)
                .height(18.dp)
        )
        repeat(3) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .height(66.dp)
                    .skeletonCard(8.dp)
                    .padding(horizontal = 10.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SkeletonBox(
                    modifier = Modifier.size(30.dp),
                    shape = CircleShape
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp)
                ) {
                    SkeletonBox(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(14.dp)
                    )
                    SkeletonBox(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .fillMaxWidth(0.45f)
                            .height(12.dp)
                    )
                }
                SkeletonBox(
                    modifier = Modifier
                        .width(52.dp)
                        .height(20.dp),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }
    }
}

@Composable
fun ProjectsScreenSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth(0.55f)
                .height(26.dp)
        )
        SkeletonBox(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(0.85f)
                .height(15.dp)
        )
        SkeletonBox(
            modifier = Modifier
                .padding(top = 22.dp)
                .fillMaxWidth()
                .height(46.dp),
            shape = RoundedCornerShape(6.dp)
        )
        repeat(4) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp)
                    .skeletonCard(10.dp)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SkeletonBox(
                        modifier = Modifier.size(42.dp),
                        shape = CircleShape
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 12.dp)
                    ) {
                        SkeletonBox(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(17.dp)
                        )
                        SkeletonBox(
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .width(80.dp)
                                .height(13.dp)
                        )
                    }
                }
                SkeletonBox(
                    modifier = Modifier
                        .padding(top = 14.dp)
                        .fillMaxWidth()
                        .height(13.dp)
                )
                Row(
                    modifier = Modifier.padding(top = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    repeat(3) {
                        SkeletonBox(
                            modifier = Modifier.size(28.dp),
                            shape = CircleShape
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminScreenSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(26.dp)
        )
        SkeletonBox(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(0.75f)
                .height(15.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 22.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(2) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp)
                        .skeletonCard(10.dp)
                        .padding(13.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SkeletonBox(
                            modifier = Modifier
                                .weight(1f)
                                .height(11.dp)
                        )
                        SkeletonBox(
                            modifier = Modifier.size(32.dp),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                    SkeletonBox(
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .width(36.dp)
                            .height(26.dp)
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(2) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp)
                        .skeletonCard(10.dp)
                        .padding(13.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SkeletonBox(
                            modifier = Modifier
                                .weight(1f)
                                .height(11.dp)
                        )
                        SkeletonBox(
                            modifier = Modifier.size(32.dp),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                    SkeletonBox(
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .width(36.dp)
                            .height(26.dp)
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SkeletonBox(
                modifier = Modifier
                    .weight(1f)
                    .height(18.dp)
            )
            SkeletonBox(
                modifier = Modifier
                    .width(120.dp)
                    .height(40.dp),
                shape = RoundedCornerShape(8.dp)
            )
        }
        repeat(4) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .height(72.dp)
                    .skeletonCard(10.dp)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SkeletonBox(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                ) {
                    SkeletonBox(
                        modifier = Modifier
                            .fillMaxWidth(0.55f)
                            .height(15.dp)
                    )
                    SkeletonBox(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .fillMaxWidth(0.35f)
                            .height(12.dp)
                    )
                }
                SkeletonBox(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape
                )
            }
        }
    }
}

@Composable
fun TaskDetailScreenSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        SkeletonBox(
            modifier = Modifier
                .width(90.dp)
                .height(26.dp),
            shape = RoundedCornerShape(13.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .skeletonCard(12.dp)
                .padding(16.dp)
        ) {
            SkeletonBox(
                modifier = Modifier
                    .width(120.dp)
                    .height(14.dp)
            )
            SkeletonBox(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .fillMaxWidth()
                    .height(8.dp),
                shape = RoundedCornerShape(4.dp)
            )
        }
        SkeletonBox(
            modifier = Modifier
                .padding(top = 18.dp)
                .width(140.dp)
                .height(16.dp)
        )
        repeat(2) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .skeletonCard(8.dp)
                    .padding(14.dp)
            ) {
                SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(14.dp)
                )
                SkeletonBox(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(0.25f)
                        .height(12.dp)
                )
            }
        }
        SkeletonBox(
            modifier = Modifier
                .padding(top = 22.dp)
                .width(100.dp)
                .height(16.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .height(64.dp)
                .skeletonCard(8.dp)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SkeletonBox(
                modifier = Modifier.size(40.dp),
                shape = CircleShape
            )
            SkeletonBox(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .width(120.dp)
                    .height(14.dp)
            )
        }
        SkeletonBox(
            modifier = Modifier
                .padding(top = 22.dp)
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun CreateFormScreenSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        repeat(3) { section ->
            SkeletonBox(
                modifier = Modifier
                    .padding(top = if (section == 0) 0.dp else 18.dp)
                    .width(100.dp)
                    .height(11.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .skeletonCard(8.dp)
                    .padding(14.dp)
            ) {
                repeat(if (section == 2) 3 else 2) { field ->
                    if (field > 0) {
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                    SkeletonBox(
                        modifier = Modifier
                            .width(80.dp)
                            .height(12.dp)
                    )
                    SkeletonBox(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .fillMaxWidth()
                            .height(if (section == 0 && field == 1) 80.dp else 46.dp),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }
        SkeletonBox(
            modifier = Modifier
                .padding(top = 24.dp)
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp)
        )
    }
}

@Composable
fun SettingsScreenSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth(0.45f)
                .height(26.dp)
        )
        SkeletonBox(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(0.7f)
                .height(15.dp)
        )
        repeat(3) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = if (it == 0) 24.dp else 14.dp)
                    .skeletonCard(8.dp)
                    .padding(16.dp)
            ) {
                SkeletonBox(
                    modifier = Modifier
                        .width(90.dp)
                        .height(12.dp)
                )
                SkeletonBox(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth(0.5f)
                        .height(18.dp)
                )
                SkeletonBox(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .fillMaxWidth(0.65f)
                        .height(14.dp)
                )
                if (it == 1) {
                    Row(
                        modifier = Modifier.padding(top = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SkeletonBox(
                            modifier = Modifier.size(72.dp),
                            shape = CircleShape
                        )
                        Column(modifier = Modifier.padding(start = 16.dp)) {
                            SkeletonBox(
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(16.dp)
                            )
                            SkeletonBox(
                                modifier = Modifier
                                    .padding(top = 6.dp)
                                    .width(90.dp)
                                    .height(14.dp)
                            )
                        }
                    }
                    SkeletonBox(
                        modifier = Modifier
                            .padding(top = 14.dp)
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                if (it == 2) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        repeat(2) {
                            SkeletonBox(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(58.dp),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
