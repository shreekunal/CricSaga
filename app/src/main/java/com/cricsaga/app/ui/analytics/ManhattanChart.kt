package com.cricsaga.app.ui.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricsaga.app.domain.model.DeliveryRecord
import com.cricsaga.app.ui.theme.*
import kotlin.math.max

data class OverBarData(
    val overNumber: Int,
    val runs: Int,
    val wickets: Int
)

@Composable
fun ManhattanChart(
    battingTeamName: String,
    deliveries: List<DeliveryRecord>,
    totalOvers: Int = 20,
    modifier: Modifier = Modifier
) {
    val overStats = if (deliveries.isNotEmpty()) {
        val grouped = deliveries.groupBy { it.overNumber }
        val maxOver = deliveries.maxOfOrNull { it.overNumber } ?: 0
        (0..maxOver).map { overIdx ->
            val list = grouped[overIdx] ?: emptyList()
            OverBarData(
                overNumber = overIdx + 1,
                runs = list.sumOf { it.totalRunsThisBall },
                wickets = list.count { it.dismissal != null }
            )
        }
    } else {
        emptyList()
    }

    val maxRuns = max(overStats.maxOfOrNull { it.runs } ?: 10, 16)
    val totalRuns = overStats.sumOf { it.runs }
    val totalWickets = overStats.sumOf { it.wickets }
    val highestOver = overStats.maxByOrNull { it.runs }

    // Resolve Composable color tokens before entering non-composable Canvas drawing scope
    val amberColor = BoundaryAmber
    val crimsonColor = CrimsonPrimaryDark
    val dismissalColor = DismissalRed

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MANHATTAN OVER GRAPH",
                        style = SectionLabel,
                        color = TextPrimary
                    )
                    Text(
                        text = "$battingTeamName • Runs & Wickets per Over",
                        style = BodyCaption,
                        color = TextTertiary
                    )
                }

                Surface(
                    color = crimsonColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "$totalRuns/$totalWickets (${overStats.size} ov)",
                        style = MicroTag,
                        color = crimsonColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (overStats.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ElevatedSurfaceDark)
                        .border(1.dp, CardBorderDark, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.BarChart,
                            contentDescription = null,
                            tint = TextTertiary.copy(alpha = 0.5f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No overs bowled yet",
                            style = BodyCaption,
                            color = TextSecondary
                        )
                        Text(
                            text = "Over-by-over Manhattan runs and wickets will appear once live scoring begins.",
                            style = MicroTag,
                            color = TextTertiary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 2.dp)
                        )
                    }
                }
            } else {
                // Graph Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ElevatedSurfaceDark)
                        .border(1.dp, CardBorderDark, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val bottomPadding = 24f
                    val topPadding = 20f
                    val chartHeight = h - bottomPadding - topPadding

                    // Draw grid reference lines
                    val steps = 3
                    for (i in 0..steps) {
                        val y = topPadding + chartHeight - (i.toFloat() / steps) * chartHeight
                        drawLine(
                            color = Color(0x33374151),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1f
                        )
                    }

                    val barCount = overStats.size
                    val slotWidth = w / barCount
                    val barWidth = (slotWidth * 0.65f).coerceAtMost(28f)

                    overStats.forEachIndexed { index, bar ->
                        val barHeight = ((bar.runs.toFloat() / maxRuns.toFloat()) * chartHeight).coerceAtLeast(4f)
                        val x = index * slotWidth + (slotWidth - barWidth) / 2f
                        val y = topPadding + chartHeight - barHeight

                        val barColor = when {
                            bar.runs >= 15 -> amberColor
                            bar.runs >= 10 -> crimsonColor
                            bar.runs >= 6 -> Color(0xFF3B82F6)
                            else -> Color(0xFF6B7280)
                        }

                        // Draw bar
                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(4f, 4f)
                        )

                        // Wicket indicators
                        if (bar.wickets > 0) {
                            for (wIdx in 0 until bar.wickets) {
                                val dotY = y - 8f - (wIdx * 10f)
                                drawCircle(
                                    color = dismissalColor,
                                    radius = 4f,
                                    center = Offset(x + barWidth / 2f, dotY)
                                )
                            }
                        }

                        // X-axis label (Over number)
                        drawIntoCanvas { canvas ->
                            val textPaint = android.graphics.Paint().apply {
                                color = android.graphics.Color.rgb(156, 163, 175)
                                textSize = 22f
                                textAlign = android.graphics.Paint.Align.CENTER
                                isAntiAlias = true
                            }
                            canvas.nativeCanvas.drawText(
                                "${bar.overNumber}",
                                x + barWidth / 2f,
                                h - 4f,
                                textPaint
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer Legend & Key Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendIndicator(color = amberColor, label = "15+ Runs")
                    LegendIndicator(color = crimsonColor, label = "10+ Runs")
                    LegendIndicator(color = dismissalColor, label = "Wicket")
                }

                if (highestOver != null) {
                    Text(
                        text = "Peak: Ov ${highestOver.overNumber} (${highestOver.runs}r)",
                        style = MicroTag,
                        color = amberColor
                    )
                }
            }
        }
    }
}
}

@Composable
private fun LegendIndicator(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, style = MicroTag, color = TextTertiary)
    }
}
