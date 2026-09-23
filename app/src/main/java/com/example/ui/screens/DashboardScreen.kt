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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MarketQuote
import com.example.data.model.SignalAlertItem
import com.example.data.model.SignalType
import com.example.ui.components.InstrumentQuoteCard
import com.example.ui.components.PcrAnalysisBar
import com.example.ui.components.SignalBadge
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BullishGreen
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.TerminalCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    quotes: List<MarketQuote>,
    selectedSymbol: String,
    isLiveTickerRunning: Boolean,
    signals: List<SignalAlertItem>,
    onSelectSymbol: (String) -> Unit,
    onToggleLiveTicker: () -> Unit,
    onNavigateToChart: (String) -> Unit,
    onNavigateToOptionChain: (String) -> Unit,
    onTriggerNotificationTest: (Context, MarketQuote) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentQuote = quotes.find { it.symbol == selectedSymbol } ?: quotes.firstOrNull() ?: return
    val giftNifty = quotes.find { it.symbol == "GIFT NIFTY" }
    val nifty50 = quotes.find { it.symbol == "NIFTY 50" }
    val crudeOil = quotes.find { it.symbol == "CRUDE OIL" }
    val indiaVix = quotes.find { it.symbol == "INDIA VIX" }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)
    ) {

        // Top Status Header: Live Market Status Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurfaceVariant)
                    .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isLiveTickerRunning) BullishGreen else BearishRed)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isLiveTickerRunning) "NSE & MCX LIVE FEED ACTIVE" else "LIVE SIMULATION PAUSED",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "RSI + Supertrend Daily & Intraday Signals",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleLiveTicker,
                        modifier = Modifier.size(34.dp).testTag("toggle_live_ticker")
                    ) {
                        Icon(
                            imageVector = if (isLiveTickerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Toggle Ticker",
                            tint = TerminalCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Horizontal Ticker Carousel
        item {
            Text(
                text = "CORE MARKETS & INDICES",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(quotes) { q ->
                    InstrumentQuoteCard(
                        quote = q,
                        isSelected = q.symbol == selectedSymbol,
                        onClick = { onSelectSymbol(q.symbol) },
                        modifier = Modifier.width(220.dp)
                    )
                }
            }
        }

        // Featured Daily Strategy Signal Card for Selected Symbol
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.5.dp, if (currentQuote.signal.isBullish) BullishGreen else BearishRed, RoundedCornerShape(14.dp))
                    .testTag("featured_strategy_card"),
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
                                imageVector = Icons.Default.ShowChart,
                                contentDescription = null,
                                tint = TerminalCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "DAILY CHART STRATEGY",
                                color = TerminalCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        SignalBadge(signal = currentQuote.signal)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "${currentQuote.symbol} • ${currentQuote.name}",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Confluence triggered by Daily RSI (14) at ${currentQuote.rsi14} and Supertrend (10, 3) at ₹${"%,.1f".format(currentQuote.supertrendValue)} with ${currentQuote.oiBuildupType}.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Targets & Stop Loss Grid
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("STOP LOSS", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                "₹${"%,.1f".format(currentQuote.signalStopLoss)}",
                                color = BearishRed,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Column {
                            Text("TARGET 1", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                "₹${"%,.1f".format(currentQuote.signalTarget1)}",
                                color = BullishGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Column {
                            Text("TARGET 2", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                "₹${"%,.1f".format(currentQuote.signalTarget2)}",
                                color = BullishGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onNavigateToChart(currentQuote.symbol) },
                            modifier = Modifier.weight(1f).testTag("open_live_chart_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = TerminalCyan)
                        ) {
                            Text("View Live Chart", color = DarkSurfaceCard, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { onTriggerNotificationTest(context, currentQuote) },
                            modifier = Modifier.testTag("test_push_notif_btn"),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCyan)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Test Notification",
                                tint = TerminalCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Send Alert", color = TerminalCyan, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Live Market Analysis Matrix: PCR + India VIX + Max Pain
        item {
            Text(
                text = "REAL-TIME MARKET PULSE & OI",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {

                    // PCR Bar
                    PcrAnalysisBar(pcr = currentQuote.pcr)

                    Spacer(modifier = Modifier.height(14.dp))

                    // Grid: Max Pain & India VIX
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Max Pain Box
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceVariant)
                                .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("MAX PAIN STRIKE", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "₹${"%,.0f".format(currentQuote.maxPain)}",
                                    color = AccentGold,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (currentQuote.lastPrice > currentQuote.maxPain) "Spot > Max Pain" else "Spot < Max Pain",
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        // India VIX Box
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceVariant)
                                .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("INDIA VIX", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    indiaVix?.let { "%.2f".format(it.lastPrice) } ?: "13.82",
                                    color = TerminalCyan,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Low Volatility Regime",
                                    color = BullishGreen,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // GIFT Nifty vs NIFTY 50 Spread
                    if (giftNifty != null && nifty50 != null) {
                        val spread = giftNifty.lastPrice - nifty50.lastPrice
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkBorder.copy(alpha = 0.3f))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "GIFT Nifty Premium Spread:",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "${if (spread >= 0) "+" else ""}${"%.1f".format(spread)} pts (${if (spread >= 0) "Bullish Premium" else "Discount"})",
                                color = if (spread >= 0) BullishGreen else BearishRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Recent Buy/Sell Alert Feed
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LATEST SIGNALS & ALERTS",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Auto-push enabled",
                    color = BullishGreen,
                    fontSize = 11.sp
                )
            }
        }

        items(signals) { sig ->
            val dateStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(sig.timestamp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(10.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = sig.instrumentSymbol,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = sig.timeframe,
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                        SignalBadge(signal = sig.type, compact = true)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = sig.rationale,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Triggered at ₹${"%,.1f".format(sig.price)} | RSI: ${sig.rsi}",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = dateStr,
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
