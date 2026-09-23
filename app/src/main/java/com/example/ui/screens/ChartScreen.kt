package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Candle
import com.example.data.model.MarketQuote
import com.example.data.model.Timeframe
import com.example.ui.chart.InteractiveTradingChart
import com.example.ui.components.SignalBadge
import com.example.ui.components.TimeframeChipGroup
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BullishGreen
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.TerminalCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ChartScreen(
    quotes: List<MarketQuote>,
    selectedSymbol: String,
    selectedTimeframe: Timeframe,
    candles: List<Candle>,
    onSelectSymbol: (String) -> Unit,
    onSelectTimeframe: (Timeframe) -> Unit,
    onTriggerNotification: (Context, MarketQuote) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentQuote = quotes.find { it.symbol == selectedSymbol } ?: quotes.firstOrNull() ?: return
    val isPositive = currentQuote.change >= 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)
    ) {

        // Instrument Switcher Row
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(quotes) { q ->
                    val isSelected = q.symbol == selectedSymbol
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) TerminalCyan else DarkSurfaceVariant)
                            .border(1.dp, if (isSelected) TerminalCyan else DarkBorder, RoundedCornerShape(8.dp))
                            .clickable { onSelectSymbol(q.symbol) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("chart_instrument_${q.symbol.replace(" ", "_")}")
                    ) {
                        Text(
                            text = q.symbol,
                            color = if (isSelected) Color(0xFF070B14) else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Live Price & Stats Bar
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = currentQuote.name,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            Text(
                                text = if (currentQuote.symbol == "INDIA VIX") "%.2f".format(currentQuote.lastPrice) else "₹${"%,.2f".format(currentQuote.lastPrice)}",
                                color = TextPrimary,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            SignalBadge(signal = currentQuote.signal)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${if (isPositive) "+" else ""}${"%.2f".format(currentQuote.change)} (${if (isPositive) "+" else ""}${"%.2f".format(currentQuote.changePercent)}%)",
                                color = if (isPositive) BullishGreen else BearishRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Timeframe selection row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Timeframe:",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        TimeframeChipGroup(
                            selectedTimeframe = selectedTimeframe,
                            onTimeframeSelected = onSelectTimeframe
                        )
                    }
                }
            }
        }

        // Interactive Candlestick + Supertrend + RSI Chart
        item {
            InteractiveTradingChart(
                candles = candles,
                symbol = selectedSymbol
            )
        }

        // RSI + Supertrend Strategy Confluence Breakdown Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = TerminalCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "INDICATOR CONFLUENCE RULES",
                                color = TerminalCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = selectedTimeframe.label,
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Indicator Rule 1: RSI (14)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "RSI (Relative Strength Index 14)",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Current: ${currentQuote.rsi14} • ${if (currentQuote.rsi14 >= 60) "Strong Momentum (Bullish)" else if (currentQuote.rsi14 <= 40) "Weak Momentum (Bearish)" else "Neutral Range (50 line)"}",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = if (currentQuote.rsi14 >= 50) "PASS ✅" else "FAIL ❌",
                            color = if (currentQuote.rsi14 >= 50) BullishGreen else BearishRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Indicator Rule 2: Supertrend (10, 3)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Supertrend (ATR 10, Multiplier 3.0)",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Trailing Band: ₹${"%,.1f".format(currentQuote.supertrendValue)} • ${if (currentQuote.isSupertrendBullish) "Green (Support / Longs Active)" else "Red (Resistance / Shorts Active)"}",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = if (currentQuote.isSupertrendBullish) "BULLISH 🟢" else "BEARISH 🔴",
                            color = if (currentQuote.isSupertrendBullish) BullishGreen else BearishRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Trade Plan Box
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkBorder.copy(alpha = 0.3f))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("STOP LOSS", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                            Text("₹${"%,.1f".format(currentQuote.signalStopLoss)}", color = BearishRed, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Column {
                            Text("TARGET 1 (1:1.5)", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                            Text("₹${"%,.1f".format(currentQuote.signalTarget1)}", color = BullishGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Column {
                            Text("TARGET 2 (1:2.5)", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                            Text("₹${"%,.1f".format(currentQuote.signalTarget2)}", color = BullishGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { onTriggerNotification(context, currentQuote) },
                        modifier = Modifier.fillMaxWidth().testTag("chart_trigger_push_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = TerminalCyan)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = DarkSurfaceCard,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Send Signal Push Alert to Phone",
                            color = DarkSurfaceCard,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
