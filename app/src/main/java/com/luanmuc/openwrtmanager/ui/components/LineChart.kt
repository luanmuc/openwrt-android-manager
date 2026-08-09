package com.luanmuc.openwrtmanager.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.luanmuc.openwrtmanager.ui.components.MiTheme
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max
import kotlin.math.min

/**
 * 折线图组件
 * 用于显示实时数据趋势
 */
@Composable
fun LineChart(
    data: List<Float>,
    modifier: Modifier = Modifier,
    color: Color = MiColors.Primary,
    label: String = "",
    unit: String = "",
    showGrid: Boolean = true
) {
    Column(
        modifier = modifier
    ) {
        // 标签和当前值
        if (label.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MiColors.TextSecondary
                )
                if (data.isNotEmpty()) {
                    Text(
                        text = "${String.format("%.1f", data.last())}$unit",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
            }
        }

        // 图表
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            if (data.isEmpty()) return@Canvas

            val width = size.width
            val height = size.height
            val padding = 16.dp.toPx()

            // 计算数据范围
            val maxValue = data.maxOrNull() ?: 0f
            val minValue = data.minOrNull() ?: 0f
            val range = maxValue - minValue

            val chartWidth = width - padding * 2
            val chartHeight = height - padding * 2

            // 绘制网格
            if (showGrid) {
                val gridLines = 4
                for (i in 0..gridLines) {
                    val y = padding + (chartHeight / gridLines) * i
                    drawLine(
                        color = MiTheme.Divider,
                        start = Offset(padding, y),
                        end = Offset(width - padding, y),
                        strokeWidth = 1f
                    )
                }
            }

            // 绘制折线
            if (data.size > 1) {
                val path = Path()
                val pointSpacing = chartWidth / (data.size - 1)

                data.forEachIndexed { index, value ->
                    val x = padding + pointSpacing * index
                    val y = if (range > 0) {
                        padding + chartHeight - ((value - minValue) / range) * chartHeight
                    } else {
                        padding + chartHeight / 2
                    }

                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }

                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )

                // 绘制填充区域
                val fillPath = Path().apply {
                    addPath(path)
                    lineTo(padding + pointSpacing * (data.size - 1), height - padding)
                    lineTo(padding, height - padding)
                    close()
                }

                drawPath(
                    path = fillPath,
                    color = color.copy(alpha = 0.1f)
                )
            }
        }
    }
}

/**
 * 双折线图组件
 * 用于同时显示两个数据系列（如下载/上传速度）
 */
@Composable
fun DualLineChart(
    data1: List<Float>,
    data2: List<Float>,
    modifier: Modifier = Modifier,
    color1: Color = MiColors.Primary,
    color2: Color = MiColors.Success,
    label1: String = "",
    label2: String = "",
    unit: String = ""
) {
    Column(
        modifier = modifier
    ) {
        // 图例
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (label1.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Canvas(
                        modifier = Modifier.size(8.dp)
                    ) {
                        drawCircle(color = color1)
                    }
                    Text(
                        text = label1,
                        fontSize = 12.sp,
                        color = MiColors.TextSecondary
                    )
                    if (data1.isNotEmpty()) {
                        Text(
                            text = "${String.format("%.1f", data1.last())}$unit",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = color1
                        )
                    }
                }
            }

            if (label2.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Canvas(
                        modifier = Modifier.size(8.dp)
                    ) {
                        drawCircle(color = color2)
                    }
                    Text(
                        text = label2,
                        fontSize = 12.sp,
                        color = MiColors.TextSecondary
                    )
                    if (data2.isNotEmpty()) {
                        Text(
                            text = "${String.format("%.2f", data2.last())}$unit",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = color2
                        )
                    }
                }
            }
        }

        // 图表
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            val width = size.width
            val height = size.height
            val padding = 16.dp.toPx()

            val chartWidth = width - padding * 2
            val chartHeight = height - padding * 2

            // 计算数据范围
            val allData = data1 + data2
            val maxValue = allData.maxOrNull() ?: 0f
            val minValue = allData.minOrNull() ?: 0f
            val range = maxValue - minValue

            // 绘制网格
            val gridLines = 4
            for (i in 0..gridLines) {
                val y = padding + (chartHeight / gridLines) * i
                drawLine(
                    color = MiTheme.Divider,
                    start = Offset(padding, y),
                    end = Offset(width - padding, y),
                    strokeWidth = 1f
                )
            }

            // 绘制第一条线
            if (data1.size > 1) {
                val path = Path()
                val pointSpacing = chartWidth / (data1.size - 1)

                data1.forEachIndexed { index, value ->
                    val x = padding + pointSpacing * index
                    val y = if (range > 0) {
                        padding + chartHeight - ((value - minValue) / range) * chartHeight
                    } else {
                        padding + chartHeight / 2
                    }

                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }

                drawPath(
                    path = path,
                    color = color1,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )
            }

            // 绘制第二条线
            if (data2.size > 1) {
                val path = Path()
                val pointSpacing = chartWidth / (data2.size - 1)

                data2.forEachIndexed { index, value ->
                    val x = padding + pointSpacing * index
                    val y = if (range > 0) {
                        padding + chartHeight - ((value - minValue) / range) * chartHeight
                    } else {
                        padding + chartHeight / 2
                    }

                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }

                drawPath(
                    path = path,
                    color = color2,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )
            }
        }
    }
}
