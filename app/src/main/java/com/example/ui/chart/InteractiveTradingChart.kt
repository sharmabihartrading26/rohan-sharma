package com.example.ui.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Candle
import com.example.data.model.SignalType
import com.example.ui.components.SignalBadge
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BullishGreen
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeutralBar
import com.example.ui.theme.TerminalCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.max
import kotlin.math.min

@Composable
fun InteractiveTradingChart(
    candles: List<Candle>,
    symbol: String,
    modifier: Modifier = Modifier
) {
    if (candles.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(380.dp)
                .background(DarkSurfaceVariant, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading chart data...", color = TextSecondary)
        }
        return
    }

    var selectedIndex by remember(candles) { mutableStateOf<Int?>(candles.size - 1) }
    val inspectedCandle = selectedIndex?.let { if (it in candles.indices) candles[it] else candles.last() } ?: candles.last()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            .testTag("interactive_trading_chart"),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            // Header: Inspected Candle Details HUD
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = inspectedCandle.dateLabel,
                            color = TerminalCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (inspectedCandle.close >= inspectedCandle.open) "▲ BULLISH" else "▼ BEARISH",
                            color = if (inspectedCandle.close >= inspectedCandle.open) BullishGreen else BearishRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (inspectedCandle.signal != null) {
                        SignalBadge(signal = inspectedCandle.signal, compact = true)
                    } else {
                        Text(
                            text = if (inspectedCandle.isSupertrendBullish) "ST: 🟢 BULLISH" else "ST: 🔴 BEARISH",
                            color = if (inspectedCandle.isSupertrendBullish) BullishGreen else BearishRed,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "O: ${"%,.1f".format(inspectedCandle.open)}",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "H: ${"%,.1f".format(inspectedCandle.high)}",
                        color = BullishGreen,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "L: ${"%,.1f".format(inspectedCandle.low)}",
                        color = BearishRed,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "C: ${"%,.1f".format(inspectedCandle.close)}",
                        color = if (inspectedCandle.close >= inspectedCandle.open) BullishGreen else BearishRed,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "RSI(14): ${inspectedCandle.rsi}",
                        color = if (inspectedCandle.rsi > 70) BearishRed else if (inspectedCandle.rsi < 30) BullishGreen else TerminalCyan,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Supertrend: ${"%,.1f".format(inspectedCandle.supertrend)}",
                        color = if (inspectedCandle.isSupertrendBullish) BullishGreen else BearishRed,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Chart Canvas: Candlesticks + Supertrend + Buy/Sell Indicators + RSI Subchart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(310.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(candles) {
                            detectTapGestures { offset ->
                                val candleWidth = size.width / candles.size
                                val index = (offset.x / candleWidth).toInt().coerceIn(0, candles.size - 1)
                                selectedIndex = index
                            }
                        }
                        .pointerInput(candles) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                val candleWidth = size.width / candles.size
                                val index = (change.position.x / candleWidth).toInt().coerceIn(0, candles.size - 1)
                                selectedIndex = index
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height

                    // Main Price Chart occupies top 70%, RSI Subchart occupies bottom 26%, gap 4%
                    val mainChartH = h * 0.70f
                    val rsiChartY = h * 0.74f
                    val rsiChartH = h * 0.24f

                    // Calculate price min/max across candles and supertrend
                    var minPrice = Double.MAX_VALUE
                    var maxPrice = Double.MIN_VALUE
                    for (c in candles) {
                        minPrice = min(minPrice, min(c.low, c.supertrend))
                        maxPrice = max(maxPrice, max(c.high, c.supertrend))
                    }
                    val pricePad = (maxPrice - minPrice) * 0.05
                    val lowBound = minPrice - pricePad
                    val highBound = maxPrice + pricePad
                    val priceRange = max(1.0, highBound - lowBound)

                    fun priceToY(price: Double): Float {
                        val norm = (price - lowBound) / priceRange
                        return (mainChartH - (norm * mainChartH)).toFloat()
                    }

                    fun rsiToY(rsi: Double): Float {
                        val norm = (rsi.coerceIn(0.0, 100.0) / 100.0)
                        return (rsiChartY + rsiChartH - (norm * rsiChartH)).toFloat()
                    }

                    // Background Grid Lines for Price
                    val gridSteps = 4
                    for (i in 0..gridSteps) {
                        val gy = (mainChartH / gridSteps) * i
                        drawLine(
                            color = DarkBorder.copy(alpha = 0.5f),
                            start = Offset(0f, gy),
                            end = Offset(w, gy),
                            strokeWidth = 1f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )
                    }

                    // Grid lines for RSI (30, 50, 70)
                    drawLine(
                        color = BearishRed.copy(alpha = 0.5f),
                        start = Offset(0f, rsiToY(70.0)),
                        end = Offset(w, rsiToY(70.0)),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                    )
                    drawLine(
                        color = NeutralBar.copy(alpha = 0.6f),
                        start = Offset(0f, rsiToY(50.0)),
                        end = Offset(w, rsiToY(50.0)),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                    )
                    drawLine(
                        color = BullishGreen.copy(alpha = 0.5f),
                        start = Offset(0f, rsiToY(30.0)),
                        end = Offset(w, rsiToY(30.0)),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                    )

                    // Draw RSI panel boundary
                    drawRect(
                        color = DarkSurfaceVariant.copy(alpha = 0.4f),
                        topLeft = Offset(0f, rsiChartY),
                        size = Size(w, rsiChartH)
                    )

                    val candleWidth = w / candles.size
                    val bodyWidth = max(2.5f, candleWidth * 0.7f)

                    // 1. Draw Supertrend Line & Cloud
                    val stPath = Path()
                    var firstSt = true

                    for (i in candles.indices) {
                        val c = candles[i]
                        val cx = (i * candleWidth) + (candleWidth / 2f)
                        val sy = priceToY(c.supertrend)

                        if (firstSt) {
                            stPath.moveTo(cx, sy)
                            firstSt = false
                        } else {
                            stPath.lineTo(cx, sy)
                        }
                    }

                    // Draw Supertrend segments with Green / Red color depending on trend
                    for (i in 0 until candles.size - 1) {
                        val c1 = candles[i]
                        val c2 = candles[i + 1]
                        val x1 = (i * candleWidth) + (candleWidth / 2f)
                        val y1 = priceToY(c1.supertrend)
                        val x2 = ((i + 1) * candleWidth) + (candleWidth / 2f)
                        val y2 = priceToY(c2.supertrend)

                        val stColor = if (c2.isSupertrendBullish) BullishGreen else BearishRed
                        drawLine(
                            color = stColor,
                            start = Offset(x1, y1),
                            end = Offset(x2, y2),
                            strokeWidth = 2.5f,
                            cap = StrokeCap.Round
                        )
                    }

                    // 2. Draw Candlesticks & Signal Badges
                    for (i in candles.indices) {
                        val c = candles[i]
                        val cx = (i * candleWidth) + (candleWidth / 2f)
                        val isBullishCandle = c.close >= c.open
                        val candleColor = if (isBullishCandle) BullishGreen else BearishRed

                        val highY = priceToY(c.high)
                        val lowY = priceToY(c.low)
                        val openY = priceToY(c.open)
                        val closeY = priceToY(c.close)

                        // Wick
                        drawLine(
                            color = candleColor,
                            start = Offset(cx, highY),
                            end = Offset(cx, lowY),
                            strokeWidth = 1.2f
                        )

                        // Candle body
                        val topY = min(openY, closeY)
                        val bottomY = max(openY, closeY)
                        val bodyH = max(2f, bottomY - topY)

                        drawRoundRect(
                            color = candleColor,
                            topLeft = Offset(cx - (bodyWidth / 2f), topY),
                            size = Size(bodyWidth, bodyH),
                            cornerRadius = CornerRadius(1.5f, 1.5f)
                        )

                        // 3. Draw BUY / SELL Indicator Arrows on chart!
                        if (c.signal != null) {
                            val isBuy = c.signal == SignalType.STRONG_BUY || c.signal == SignalType.BUY
                            val signalColor = if (isBuy) BullishGreen else BearishRed
                            val arrowY = if (isBuy) lowY + 14f else highY - 14f

                            // Marker Triangle
                            val arrowPath = Path()
                            if (isBuy) {
                                arrowPath.moveTo(cx, arrowY - 7f)
                                arrowPath.lineTo(cx - 5f, arrowY)
                                arrowPath.lineTo(cx + 5f, arrowY)
                                arrowPath.close()
                            } else {
                                arrowPath.moveTo(cx, arrowY + 7f)
                                arrowPath.lineTo(cx - 5f, arrowY)
                                arrowPath.lineTo(cx + 5f, arrowY)
                                arrowPath.close()
                            }
                            drawPath(arrowPath, color = signalColor)

                            // Signal Tag Dot
                            drawCircle(
                                color = signalColor,
                                radius = 2.5f,
                                center = Offset(cx, if (isBuy) arrowY + 4f else arrowY - 4f)
                            )
                        }
                    }

                    // 4. Draw RSI Curve in Sub-chart
                    val rsiPath = Path()
                    var firstRsi = true
                    for (i in candles.indices) {
                        val c = candles[i]
                        val cx = (i * candleWidth) + (candleWidth / 2f)
                        val ry = rsiToY(c.rsi)
                        if (firstRsi) {
                            rsiPath.moveTo(cx, ry)
                            firstRsi = false
                        } else {
                            rsiPath.lineTo(cx, ry)
                        }
                    }
                    drawPath(
                        path = rsiPath,
                        color = TerminalCyan,
                        style = Stroke(width = 2f, cap = StrokeCap.Round)
                    )

                    // 5. Crosshair Line if inspected
                    selectedIndex?.let { idx ->
                        if (idx in candles.indices) {
                            val scrubX = (idx * candleWidth) + (candleWidth / 2f)
                            drawLine(
                                color = TextPrimary.copy(alpha = 0.8f),
                                start = Offset(scrubX, 0f),
                                end = Offset(scrubX, h),
                                strokeWidth = 1.2f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                            )
                            // Circle on candle close
                            val c = candles[idx]
                            val cy = priceToY(c.close)
                            drawCircle(
                                color = TerminalCyan,
                                radius = 4f,
                                center = Offset(scrubX, cy)
                            )
                            // Circle on RSI point
                            val ry = rsiToY(c.rsi)
                            drawCircle(
                                color = TerminalCyan,
                                radius = 3.5f,
                                center = Offset(scrubX, ry)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Indicator Legend / Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(BullishGreen))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Supertrend BUY", color = TextSecondary, fontSize = 9.sp)

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(BearishRed))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Supertrend SELL", color = TextSecondary, fontSize = 9.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(TerminalCyan))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("RSI(14) Sub-chart", color = TerminalCyan, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
