package com.luanmuc.openwrtmanager.util

import android.content.Context

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import com.luanmuc.openwrtmanager.ui.components.MiDimens
import com.luanmuc.openwrtmanager.ui.components.MiTheme

/**
 * Compose通用扩展函数
 * 提供常用的Modifier扩展
 */

/**
 * 小米风格卡片背景
 */
fun Modifier.miCardBackground(shape: Shape = MiTheme.shapes.cardShape): Modifier = this
    .clip(shape)
    .background(MiTheme.CardBackground)

/**
 * 可点击且无涟漪效果
 */
fun Modifier.clickableNoRipple(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    this.clickable(
        interactionSource = interactionSource,
        indication = null,
        enabled = enabled,
        onClick = onClick
    )
}

/**
 * 标准水平内边距
 */
fun Modifier.horizontalPadding(): Modifier = this
    .padding(horizontal = MiDimens.horizontalPadding)

/**
 * 标准垂直内边距
 */
fun Modifier.verticalPadding(): Modifier = this
    .padding(vertical = MiDimens.verticalPadding)

/**
 * 标准卡片内边距
 */
fun Modifier.cardPadding(): Modifier = this
    .padding(MiDimens.cardPadding)

/**
 * 项目间距
 */
fun Modifier.itemSpacingBottom(): Modifier = this
    .padding(bottom = MiDimens.itemSpacing)

/**
 * 顶部间距
 */
fun Modifier.topSpacing(spacing: Dp = MiDimens.itemSpacing): Modifier = this
    .padding(top = spacing)

/**
 * 底部间距
 */
fun Modifier.bottomSpacing(spacing: Dp = MiDimens.itemSpacing): Modifier = this
    .padding(bottom = spacing)

/**
 * 安全区域底部内边距
 */
fun Modifier.safeBottomPadding(): Modifier = composed {
    // 简单实现，实际项目中可以使用WindowInsets
    this.padding(bottom = MiDimens.horizontalPadding)
}

/**
 * 条件性应用Modifier
 */
fun Modifier.conditional(condition: Boolean, modifier: Modifier.() -> Modifier): Modifier {
    return if (condition) {
        then(modifier(Modifier))
    } else {
        this
    }
}

/**
 * 条件性应用Modifier（带else分支）
 */
fun <T> Modifier.conditionalLet(
    value: T?,
    modifier: Modifier.(T) -> Modifier
): Modifier {
    return if (value != null) {
        then(modifier(Modifier, value))
    } else {
        this
    }
}

/**
 * 应用多个Modifier
 */
fun Modifier.applyIf(condition: Boolean, block: Modifier.() -> Modifier): Modifier {
    return if (condition) block() else this
}

/**
 * 点击缩放效果（简单实现）
 */
fun Modifier.clickScale(
    onClick: () -> Unit
): Modifier = composed {
    // 简单实现，实际项目中可以使用animateContentSize
    this.clickable(onClick = onClick)
}

/**
 * 禁用状态透明度
 */
fun Modifier.disabledAlpha(disabled: Boolean): Modifier = this
    .conditional(disabled) {
        // 简单实现，实际项目中可以使用graphicsLayer
        this
    }

/**
 * 卡片点击效果
 */
fun Modifier.miCardClick(
    onClick: () -> Unit
): Modifier = composed {
    this
        .clip(MiTheme.shapes.cardShape)
        .clickable(onClick = onClick)
}

/**
 * 图标按钮点击效果
 */
fun Modifier.miIconButton(
    onClick: () -> Unit
): Modifier = composed {
    this
        .clip(MiTheme.shapes.iconShape)
        .clickable(onClick = onClick)
}

/**
 * 列表项点击效果
 */
fun Modifier.miListItemClick(
    onClick: () -> Unit
): Modifier = composed {
    this.clickable(onClick = onClick)
}

/**
 * 显示Toast提示
 */
fun Context.showToast(message: String, duration: Int = android.widget.Toast.LENGTH_SHORT) {
    android.widget.Toast.makeText(this, message, duration).show()
}

/**
 * 错误状态自动显示Toast的Composable
 */
@Composable
fun ErrorToastEffect(
    error: String?,
    onClear: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(error) {
        error?.let {
            context.showToast(it)
            onClear()
        }
    }
}

/**
 * 成功状态自动显示Toast的Composable
 */
@Composable
fun SuccessToastEffect(
    success: String?,
    onClear: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(success) {
        success?.let {
            context.showToast(it)
            onClear()
        }
    }
}
