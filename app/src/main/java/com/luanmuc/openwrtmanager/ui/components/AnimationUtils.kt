package com.luanmuc.openwrtmanager.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * 动画工具类
 * 提供常用的动画效果
 */
object AnimationUtils {
    
    // 标准动画时长
    const val SHORT_DURATION = 150
    const val MEDIUM_DURATION = 300
    const val LONG_DURATION = 500
    
    /**
     * 淡入动画规格
     */
    fun fadeInSpec(duration: Int = MEDIUM_DURATION) = fadeIn(
        animationSpec = tween(
            durationMillis = duration,
            easing = FastOutSlowInEasing
        )
    )
    
    /**
     * 淡出动画规格
     */
    fun fadeOutSpec(duration: Int = MEDIUM_DURATION) = fadeOut(
        animationSpec = tween(
            durationMillis = duration,
            easing = FastOutSlowInEasing
        )
    )
    
    /**
     * 垂直展开动画规格
     */
    fun expandVerticallySpec(duration: Int = MEDIUM_DURATION) = expandVertically(
        animationSpec = tween(
            durationMillis = duration,
            easing = FastOutSlowInEasing
        ),
        expandFrom = Alignment.Top
    )
    
    /**
     * 垂直收缩动画规格
     */
    fun shrinkVerticallySpec(duration: Int = MEDIUM_DURATION) = shrinkVertically(
        animationSpec = tween(
            durationMillis = duration,
            easing = FastOutSlowInEasing
        ),
        shrinkTowards = Alignment.Top
    )
    
    /**
     * 水平滑入动画规格
     */
    fun slideInHorizontallySpec(duration: Int = MEDIUM_DURATION, from: Int = 300) = 
        slideInHorizontally(
            animationSpec = tween(
                durationMillis = duration,
                easing = FastOutSlowInEasing
            ),
            initialOffsetX = { from }
        )
    
    /**
     * 水平滑出动画规格
     */
    fun slideOutHorizontallySpec(duration: Int = MEDIUM_DURATION, to: Int = -300) = 
        slideOutHorizontally(
            animationSpec = tween(
                durationMillis = duration,
                easing = FastOutSlowInEasing
            ),
            targetOffsetX = { to }
        )
    
    /**
     * 垂直滑入动画规格（从底部）
     */
    fun slideInVerticallySpec(duration: Int = MEDIUM_DURATION, from: Int = 300) = 
        slideInVertically(
            animationSpec = tween(
                durationMillis = duration,
                easing = FastOutSlowInEasing
            ),
            initialOffsetY = { from }
        )
    
    /**
     * 垂直滑出动画规格（向底部）
     */
    fun slideOutVerticallySpec(duration: Int = MEDIUM_DURATION, to: Int = 300) = 
        slideOutVertically(
            animationSpec = tween(
                durationMillis = duration,
                easing = FastOutSlowInEasing
            ),
            targetOffsetY = { to }
        )
}

/**
 * 淡入淡出可见性组件
 */
@Composable
fun FadeVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    duration: Int = AnimationUtils.MEDIUM_DURATION,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = AnimationUtils.fadeInSpec(duration),
        exit = AnimationUtils.fadeOutSpec(duration),
        modifier = modifier
    ) {
        content()
    }
}

/**
 * 垂直展开/收缩可见性组件
 */
@Composable
fun ExpandVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    duration: Int = AnimationUtils.MEDIUM_DURATION,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = AnimationUtils.expandVerticallySpec(duration) + AnimationUtils.fadeInSpec(duration),
        exit = AnimationUtils.shrinkVerticallySpec(duration) + AnimationUtils.fadeOutSpec(duration),
        modifier = modifier
    ) {
        content()
    }
}

/**
 * 水平滑入/滑出可见性组件
 */
@Composable
fun SlideHorizontalVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    duration: Int = AnimationUtils.MEDIUM_DURATION,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = AnimationUtils.slideInHorizontallySpec(duration) + AnimationUtils.fadeInSpec(duration),
        exit = AnimationUtils.slideOutHorizontallySpec(duration) + AnimationUtils.fadeOutSpec(duration),
        modifier = modifier
    ) {
        content()
    }
}

/**
 * 垂直滑入/滑出可见性组件（从底部）
 */
@Composable
fun SlideVerticalVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    duration: Int = AnimationUtils.MEDIUM_DURATION,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = AnimationUtils.slideInVerticallySpec(duration) + AnimationUtils.fadeInSpec(duration),
        exit = AnimationUtils.slideOutVerticallySpec(duration) + AnimationUtils.fadeOutSpec(duration),
        modifier = modifier
    ) {
        content()
    }
}

/**
 * 交叉淡入淡出组件
 */
@Composable
fun Crossfade(
    targetState: Boolean,
    modifier: Modifier = Modifier,
    duration: Int = AnimationUtils.MEDIUM_DURATION,
    contentWhenTrue: @Composable () -> Unit,
    contentWhenFalse: @Composable () -> Unit
) {
    androidx.compose.animation.Crossfade(
        targetState = targetState,
        animationSpec = tween(
            durationMillis = duration,
            easing = FastOutSlowInEasing
        ),
        modifier = modifier
    ) { state ->
        if (state) {
            contentWhenTrue()
        } else {
            contentWhenFalse()
        }
    }
}

/**
 * 带淡入动画的内容
 */
@Composable
fun FadeInContent(
    modifier: Modifier = Modifier,
    delay: Int = 0,
    duration: Int = AnimationUtils.MEDIUM_DURATION,
    content: @Composable () -> Unit
) {
    var visible by androidx.compose.runtime.remember { mutableStateOf(false) }
    
    androidx.compose.runtime.LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delay.toLong())
        visible = true
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = AnimationUtils.fadeInSpec(duration),
        exit = fadeOut(),
        modifier = modifier
    ) {
        content()
    }
}

/**
 * 带滑入动画的内容（从底部）
 */
@Composable
fun SlideInContent(
    modifier: Modifier = Modifier,
    delay: Int = 0,
    duration: Int = AnimationUtils.MEDIUM_DURATION,
    content: @Composable () -> Unit
) {
    var visible by androidx.compose.runtime.remember { mutableStateOf(false) }
    
    androidx.compose.runtime.LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delay.toLong())
        visible = true
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = AnimationUtils.slideInVerticallySpec(duration) + AnimationUtils.fadeInSpec(duration),
        exit = AnimationUtils.slideOutVerticallySpec(duration) + AnimationUtils.fadeOutSpec(duration),
        modifier = modifier
    ) {
        content()
    }
}