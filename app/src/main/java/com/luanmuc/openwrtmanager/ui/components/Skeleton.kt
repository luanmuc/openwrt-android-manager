package com.luanmuc.openwrtmanager.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.luanmuc.openwrtmanager.ui.components.MiTheme

/**
 * 骨架屏组件
 */
object Skeleton {
    // 骨架屏颜色在shimmerBrush函数中动态获取，支持深色模式

    @Composable
    fun shimmerBrush(): Brush {
        val transition = rememberInfiniteTransition(label = "shimmer")
        val translateAnim by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmer"
        )

        return Brush.linearGradient(
            colors = listOf(MiTheme.Divider, MiTheme.CardBackground, MiTheme.Divider),
            start = Offset.Zero,
            end = Offset(x = translateAnim, y = translateAnim)
        )
    }

    @Composable
    fun SkeletonItem(
        modifier: Modifier = Modifier,
        shape: RoundedCornerShape = RoundedCornerShape(8.dp)
    ) {
        Box(
            modifier = modifier
                .clip(shape)
                .background(shimmerBrush())
        )
    }

    @Composable
    fun CardSkeleton() {
        MiCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SkeletonItem(
                    modifier = Modifier
                        .height(20.dp)
                        .width(120.dp)
                )
                SkeletonItem(
                    modifier = Modifier
                        .height(16.dp)
                        .width(200.dp)
                )
                SkeletonItem(
                    modifier = Modifier
                        .height(16.dp)
                        .width(150.dp)
                )
            }
        }
    }

    @Composable
    fun ListItemSkeleton() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SkeletonItem(
                modifier = Modifier
                    .size(48.dp),
                shape = RoundedCornerShape(12.dp)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SkeletonItem(
                    modifier = Modifier
                        .height(16.dp)
                        .width(120.dp)
                )
                SkeletonItem(
                    modifier = Modifier
                        .height(14.dp)
                        .width(80.dp)
                )
            }
        }
    }

    @Composable
    fun HomeSkeleton() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 顶部状态卡片
            SkeletonItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(16.dp)
            )

            // 网速卡片
            SkeletonItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = RoundedCornerShape(16.dp)
            )

            // 功能宫格
            SkeletonItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                shape = RoundedCornerShape(16.dp)
            )

            // 系统状态卡片
            SkeletonItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}
