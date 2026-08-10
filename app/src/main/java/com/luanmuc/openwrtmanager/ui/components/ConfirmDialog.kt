package com.luanmuc.openwrtmanager.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.luanmuc.openwrtmanager.ui.theme.MiDimens
import com.luanmuc.openwrtmanager.ui.theme.MiTheme

/**
 * 确认对话框组件
 * 用于危险操作的二次确认
 */
@Composable
fun ConfirmDialog(
    show: Boolean,
    title: String,
    message: String,
    confirmText: String = "确认",
    cancelText: String = "取消",
    icon: ImageVector? = null,
    isDangerous: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isDangerous) MiTheme.Error else MiTheme.Primary
                    )
                }
            },
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MiTheme.TextPrimary
                )
            },
            text = {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MiTheme.TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirm()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDangerous) MiTheme.Error else MiTheme.Primary
                    )
                ) {
                    Text(text = confirmText)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MiTheme.TextSecondary
                    )
                ) {
                    Text(text = cancelText)
                }
            },
            containerColor = MiTheme.CardBackground,
            shape = RoundedCornerShape(MiDimens.cardRadius)
        )
    }
}

/**
 * 删除确认对话框
 */
@Composable
fun DeleteConfirmDialog(
    show: Boolean,
    itemName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ConfirmDialog(
        show = show,
        title = "确认删除",
        message = "确定要删除「$itemName」吗？此操作不可撤销。",
        confirmText = "删除",
        cancelText = "取消",
        isDangerous = true,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

/**
 * 重置确认对话框
 */
@Composable
fun ResetConfirmDialog(
    show: Boolean,
    title: String = "确认重置",
    message: String = "确定要重置吗？所有自定义设置将丢失。",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ConfirmDialog(
        show = show,
        title = title,
        message = message,
        confirmText = "重置",
        cancelText = "取消",
        isDangerous = true,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

/**
 * 退出确认对话框
 */
@Composable
fun ExitConfirmDialog(
    show: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ConfirmDialog(
        show = show,
        title = "确认退出",
        message = "确定要退出应用吗？",
        confirmText = "退出",
        cancelText = "取消",
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

/**
 * 信息对话框
 */
@Composable
fun InfoDialog(
    show: Boolean,
    title: String,
    message: String,
    confirmText: String = "知道了",
    icon: ImageVector? = null,
    onDismiss: () -> Unit
) {
    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MiTheme.Primary
                    )
                }
            },
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MiTheme.TextPrimary
                )
            },
            text = {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MiTheme.TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MiTheme.Primary
                    )
                ) {
                    Text(text = confirmText)
                }
            },
            containerColor = MiTheme.CardBackground,
            shape = RoundedCornerShape(MiDimens.cardRadius)
        )
    }
}

/**
 * 成功对话框
 */
@Composable
fun SuccessDialog(
    show: Boolean,
    title: String = "操作成功",
    message: String = "",
    onDismiss: () -> Unit
) {
    InfoDialog(
        show = show,
        title = title,
        message = message,
        icon = androidx.compose.material.icons.Icons.Default.CheckCircle,
        onDismiss = onDismiss
    )
}

/**
 * 错误对话框
 */
@Composable
fun ErrorDialog(
    show: Boolean,
    title: String = "操作失败",
    message: String = "",
    onDismiss: () -> Unit
) {
    InfoDialog(
        show = show,
        title = title,
        message = message,
        icon = androidx.compose.material.icons.Icons.Default.Error,
        onDismiss = onDismiss
    )
}

/**
 * 输入对话框
 */
@Composable
fun InputDialog(
    show: Boolean,
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    confirmText: String = "确认",
    cancelText: String = "取消",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MiTheme.TextPrimary
                )
            },
            text = {
                Column {
                    androidx.compose.material3.OutlinedTextField(
                        value = value,
                        onValueChange = onValueChange,
                        placeholder = {
                            Text(
                                text = placeholder,
                                color = MiTheme.TextTertiary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MiTheme.Primary,
                            unfocusedBorderColor = MiTheme.Divider
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirm()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MiTheme.Primary
                    )
                ) {
                    Text(text = confirmText)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MiTheme.TextSecondary
                    )
                ) {
                    Text(text = cancelText)
                }
            },
            containerColor = MiTheme.CardBackground,
            shape = RoundedCornerShape(MiDimens.cardRadius)
        )
    }
}