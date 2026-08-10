package com.luanmuc.openwrtmanager.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.luanmuc.openwrtmanager.ui.components.MiDimens
import com.luanmuc.openwrtmanager.ui.components.MiTheme

/**
 * 骨架屏动画修饰符
 */
fun Modifier.shimmer(
    color: Color = MiTheme.Divider,
    highlightColor: Color = MiTheme.CardBackground
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )
    
    val brush = Brush.linearGradient(
        colors = listOf(
            color,
            highlightColor,
            color
        ),
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )
    
    background(brush = brush)
}

/**
 * 骨架屏卡片组件
 */
@Composable
fun ShimmerCard(
    modifier: Modifier = Modifier,
    height: Int = 100
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(RoundedCornerShape(MiDimens.cardRadius))
            .shimmer()
    )
}

/**
 * 骨架屏圆形组件
 */
@Composable
fun ShimmerCircle(
    size: Int = 40,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .shimmer()
    )
}

/**
 * 骨架屏矩形组件
 */
@Composable
fun ShimmerRect(
    width: Int = 100,
    height: Int = 16,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(width.dp)
            .height(height.dp)
            .clip(RoundedCornerShape(4.dp))
            .shimmer()
    )
}

/**
 * 列表项骨架屏
 */
@Composable
fun ShimmerListItem(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MiDimens.horizontalPadding, vertical = MiDimens.verticalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MiDimens.itemSpacing)
    ) {
        ShimmerCircle(size = 40)
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ShimmerRect(width = 120, height = 14)
            ShimmerRect(width = 80, height = 12)
        }
        
        ShimmerRect(width = 60, height = 14)
    }
}

/**
 * 卡片骨架屏
 */
@Composable
fun ShimmerCardItem(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(MiDimens.cardRadius))
            .background(MiTheme.CardBackground)
            .padding(MiDimens.cardPadding),
        verticalArrangement = Arrangement.spacedBy(MiDimens.itemSpacing)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MiDimens.itemSpacing)
        ) {
            ShimmerCircle(size = 32)
            ShimmerRect(width = 100, height = 16)
        }
        
        ShimmerRect(width = 200, height = 12)
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ShimmerRect(width = 80, height = 14)
            ShimmerRect(width = 60, height = 14)
        }
    }
}

/**
 * 页面骨架屏
 */
@Composable
fun ShimmerScreen(
    itemCount: Int = 5,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(MiDimens.horizontalPadding),
        verticalArrangement = Arrangement.spacedBy(MiDimens.itemSpacing)
    ) {
        repeat(itemCount) {
            ShimmerCardItem()
        }
    }
}

/**
 * 首页卡片骨架屏
 */
@Composable
fun ShimmerDashboardCard(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(MiDimens.cardRadius))
            .background(MiTheme.CardBackground)
            .padding(MiDimens.cardPadding),
        verticalArrangement = Arrangement.spacedBy(MiDimens.itemSpacing)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShimmerRect(width = 80, height = 16)
            ShimmerCircle(size = 24)
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        ShimmerRect(width = 150, height = 24)
        
        ShimmerRect(width = 100, height = 12)
    }
}

/**
 * 设备列表骨架屏
 */
@Composable
fun ShimmerDeviceList(
    count: Int = 8,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        repeat(count) {
            ShimmerListItem()
        }
    }
}

/**
 * 插件列表骨架屏
 */
@Composable
fun ShimmerPluginList(
    count: Int = 6,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MiDimens.itemSpacing)
    ) {
        repeat(count) {
            ShimmerCardItem()
        }
    }
}