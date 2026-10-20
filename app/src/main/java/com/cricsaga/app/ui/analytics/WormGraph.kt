package com.cricsaga.app.ui.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricsaga.app.domain.model.DeliveryRecord
import com.cricsaga.app.ui.theme.*
import kotlin.math.max

data class CumulativePoint(
    val ballIndex: Int,
    val overFormatted: String,
    val cumulativeRuns: Int,
    val isWicket: Boolean = false
)

@Composable
fun WormGraph(
    teamAName: String,
    teamBName: String,
    innings1Deliveries: List<DeliveryRecord>,
    innings2Deliveries: List<DeliveryRecord>,
    targetRuns: Int? = null,
    totalOvers: Int = 20,
    modifier: Modifier = Modifier
) {
    // Generate cumulative curve for Innings 1
    val curve1 = if (innings1Deliveries.isNotEmpty()) {
        var running = 0
        innings1Deliveries.mapIndexed { idx, d ->
            running += d.totalRunsThisBall
            CumulativePoint(
                ballIndex = idx + 1,
                overFormatted = "${d.overNumber}.${d.ballInOver}",
                cumulativeRuns = running,
                isWicket = d.dismissal != null
            )
        }
    } else {
        // Fallback realistic progression for Innings 1 preview
        listOf(
            CumulativePoint(6, "1.0", 8),
            CumulativePoint(12, "2.0", 17, isWicket = true),
            CumulativePoint(18, "3.0", 29),
            CumulativePoint(24, "4.0", 38),
            CumulativePoint(30, "5.0", 52),
            CumulativePoint(36, "6.0", 64, isWicket = true),
            CumulativePoint(48, "8.0", 82),
            CumulativePoint(60, "10.0", 101),
            CumulativePoint(72, "12.0", 119, isWicket = true),
            CumulativePoint(90, "15.0", 146),
            CumulativePoint(120, "20.0", 182)
        )
    }

    // Generate cumulative curve for Innings 2
    val curve2 = if (innings2Deliveries.isNotEmpty()) {
        var running = 0
        innings2Deliveries.mapIndexed { idx, d ->
            running += d.totalRunsThisBall
            CumulativePoint(
                ballIndex = idx + 1,
                overFormatted = "${d.overNumber}.${d.ballInOver}",
                cumulativeRuns = running,
                isWicket = d.dismissal != null
            )
        }
    } else {
        // Fallback realistic chase curve for Innings 2 preview
        listOf(
            CumulativePoint(6, "1.0", 10),
            CumulativePoint(12, "2.0", 22),
            CumulativePoint(18, "3.0", 31, isWicket = true),
            CumulativePoint(24, "4.0", 45),
            CumulativePoint(30, "5.0", 61),
            CumulativePoint(36, "6.0", 70),
            CumulativePoint(48, "8.0", 89, isWicket = true),
            CumulativePoint(60, "10.0", 112),
            CumulativePoint(72, "12.0", 135),
            CumulativePoint(90, "15.0", 168)
        )
    }

    val maxBalls = totalOvers * 6
    val maxScore = max(
        max(curve1.maxOfOrNull { it.cumulativeRuns } ?: 100, curve2.maxOfOrNull { it.cumulativeRuns } ?: 100),
        targetRuns ?: 150
    ) + 20

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
                        text = "WORM COMPARISON GRAPH",
                        style = SectionLabel,
                        color = TextPrimary
                    )
                    Text(
                        text = "Cumulative Runs vs Overs (${teamAName} vs ${teamBName})",
                        style = BodyCaption,
                        color = TextTertiary
                    )
                }

                if (targetRuns != null) {
                    Surface(
                        color = amberColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "TARGET $targetRuns",
                            style = MicroTag,
                            color = amberColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Graph Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ElevatedSurfaceDark)
                    .border(1.dp, CardBorderDark, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val bottomPadding = 24f
                    val topPadding = 16f
                    val chartHeight = h - bottomPadding - topPadding

                    // Horizontal reference grid lines
                    val steps = 3
                    for (i in 0..steps) {
                        val y = topPadding + chartHeight - (i.toFloat() / steps) * chartHeight
                        val runVal = ((i.toFloat() / steps) * maxScore).toInt()
                        drawLine(
                            color = Color(0x33374151),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1f
                        )
                        drawIntoCanvas { canvas ->
                            val textPaint = android.graphics.Paint().apply {
                                color = android.graphics.Color.rgb(107, 114, 128)
                                textSize = 20f
                                isAntiAlias = true
                            }
                            canvas.nativeCanvas.drawText("$runVal", 4f, y - 4f, textPaint)
                        }
                    }

                    // Target line (if present)
                    if (targetRuns != null) {
                        val targetY = topPadding + chartHeight - ((targetRuns.toFloat() / maxScore.toFloat()) * chartHeight)
                        drawLine(
                            color = amberColor.copy(alpha = 0.6f),
                            start = Offset(0f, targetY),
                            end = Offset(w, targetY),
                            strokeWidth = 2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    }

                    // Helper to map (ballIndex, runs) to canvas (x, y)
                    fun toOffset(ballIdx: Int, runs: Int): Offset {
                        val x = (ballIdx.toFloat() / maxBalls.toFloat()) * w
                        val y = topPadding + chartHeight - ((runs.toFloat() / maxScore.toFloat()) * chartHeight)
                        return Offset(x, y)
                    }

                    // Draw Innings 1 Curve
                    if (curve1.isNotEmpty()) {
                        val path1 = Path().apply {
                            moveTo(0f, topPadding + chartHeight)
                            curve1.forEach { pt ->
                                val off = toOffset(pt.ballIndex, pt.cumulativeRuns)
                                lineTo(off.x, off.y)
                            }
                        }
                        drawPath(path = path1, color = Color(0xFF60A5FA), style = Stroke(width = 3.5f))

                        // Draw wicket dots for Innings 1
                        curve1.filter { it.isWicket }.forEach { pt ->
                            val off = toOffset(pt.ballIndex, pt.cumulativeRuns)
                            drawCircle(color = dismissalColor, radius = 5f, center = off)
                            drawCircle(color = Color.White, radius = 2f, center = off)
                        }
                    }

                    // Draw Innings 2 Curve
                    if (curve2.isNotEmpty()) {
                        val path2 = Path().apply {
                            moveTo(0f, topPadding + chartHeight)
                            curve2.forEach { pt ->
                                val off = toOffset(pt.ballIndex, pt.cumulativeRuns)
                                lineTo(off.x, off.y)
                            }
                        }
                        drawPath(path = path2, color = crimsonColor, style = Stroke(width = 3.5f))

                        // Draw wicket dots for Innings 2
                        curve2.filter { it.isWicket }.forEach { pt ->
                            val off = toOffset(pt.ballIndex, pt.cumulativeRuns)
                            drawCircle(color = dismissalColor, radius = 5f, center = off)
                            drawCircle(color = Color.White, radius = 2f, center = off)
                        }
                    }

                    // X-axis over markers (5, 10, 15, 20 ov)
                    val overInterval = when {
                        totalOvers <= 10 -> 2
                        totalOvers <= 15 -> 3
                        else -> 5
                    }
                    var curOver = overInterval
                    while (curOver <= totalOvers) {
                        val ballIdx = curOver * 6
                        val x = (ballIdx.toFloat() / maxBalls.toFloat()) * w
                        drawIntoCanvas { canvas ->
                            val textPaint = android.graphics.Paint().apply {
                                color = android.graphics.Color.rgb(156, 163, 175)
                                textSize = 22f
                                textAlign = android.graphics.Paint.Align.CENTER
                                isAntiAlias = true
                            }
                            canvas.nativeCanvas.drawText("${curOver}ov", x, h - 4f, textPaint)
                        }
                        curOver += overInterval
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(color = Color(0xFF60A5FA), label = "$teamAName (Inn 1)")
                    LegendItem(color = crimsonColor, label = "$teamBName (Inn 2)")
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(dismissalColor))
                    Text(text = "Fall of Wicket", style = MicroTag, color = TextTertiary)
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(14.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(1.5.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(text = label, style = MicroTag, color = TextPrimary)
    }
}
