package com.luanmuc.openwrtmanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape

/**
 * 通用扩展函数
 * 减少重复代码，统一小米风格
 */

/**
 * 小米风格卡片修饰符
 * 包含圆角、背景色、内边距
 */
fun Modifier.miCard(
    shape: Shape = MiTheme.shapes.cardShape,
    padding: androidx.compose.ui.unit.Dp = MiDimens.cardPadding
): Modifier = this
    .clip(shape)
    .background(MiTheme.CardBackground)
    .padding(padding)

/**
 * 无涟漪点击效果
 * 小米风格的简洁点击反馈
 */
fun Modifier.miClickable(
    onClick: () -> Unit
): Modifier = composed {
    this.clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}

/**
 * 水平内边距
 */
fun Modifier.horizontalPadding(
    padding: androidx.compose.ui.unit.Dp = MiDimens.horizontalPadding
): Modifier = this.padding(horizontal = padding)

/**
 * 垂直内边距
 */
fun Modifier.verticalPadding(
    padding: androidx.compose.ui.unit.Dp = MiDimens.verticalPadding
): Modifier = this.padding(vertical = padding)

/**
 * 列表项间距
 */
fun Modifier.itemSpacing(
    spacing: androidx.compose.ui.unit.Dp = MiDimens.itemSpacing
): Modifier = this.padding(bottom = spacing)

/**
 * 小米风格分割线
 */
fun Modifier.miDivider(): Modifier = this
    .background(MiTheme.Divider)

/**
 * 小米风格按钮修饰符
 */
fun Modifier.miButton(
    shape: Shape = MiTheme.shapes.buttonShape,
): Modifier = this
    .clip(shape)
    .background(MiTheme.Primary)

/**
 * 小米风格输入框修饰符
 */
fun Modifier.miInput(
    shape: Shape = MiTheme.shapes.inputShape,
): Modifier = this
    .clip(shape)
    .background(MiTheme.CardBackground)