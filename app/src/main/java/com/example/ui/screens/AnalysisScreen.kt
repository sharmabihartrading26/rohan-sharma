package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MarketQuote
import com.example.data.model.TechnicalSummary
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

@Composable
fun AnalysisScreen(
    quotes: List<MarketQuote>,
    selectedSymbol: String,
    technicalSummary: TechnicalSummary,
    onSelectSymbol: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentQuote = quotes.find { it.symbol == selectedSymbol } ?: quotes.firstOrNull() ?: return
    val supportedSymbols = listOf("NIFTY 50", "GIFT NIFTY", "BANK NIFTY", "CRUDE OIL")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)
    ) {

        // Instrument Selector
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                supportedSymbols.forEach { sym ->
                    val isSel = sym == selectedSymbol
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) TerminalCyan else DarkSurfaceVariant)
                            .border(1.dp, if (isSel) TerminalCyan else DarkBorder, RoundedCornerShape(8.dp))
                            .clickable { onSelectSymbol(sym) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("analysis_symbol_${sym.replace(" ", "_")}")
                    ) {
                        Text(
                            text = sym,
                            color = if (isSel) Color(0xFF070B14) else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Composite Technical Health Score
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
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = TerminalCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "COMPOSITE TECHNICAL SCORE",
                                color = TerminalCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        SignalBadge(signal = currentQuote.signal, compact = true)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "${technicalSummary.compositeBullishScore}/100",
                            color = if (technicalSummary.compositeBullishScore >= 70) BullishGreen else if (technicalSummary.compositeBullishScore <= 40) BearishRed else AccentGold,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = if (technicalSummary.compositeBullishScore >= 70) "STRONG BULLISH MOMENTUM" else if (technicalSummary.compositeBullishScore <= 40) "BEARISH PRESSURE" else "NEUTRAL / CONSOLIDATION",
                            color = if (technicalSummary.compositeBullishScore >= 70) BullishGreen else if (technicalSummary.compositeBullishScore <= 40) BearishRed else AccentGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { technicalSummary.compositeBullishScore / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (technicalSummary.compositeBullishScore >= 70) BullishGreen else if (technicalSummary.compositeBullishScore <= 40) BearishRed else AccentGold,
                        trackColor = DarkSurfaceVariant
                    )
                }
            }
        }

        // India VIX & Volatility Impact Analysis
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
                                imageVector = Icons.Default.Analytics,
                                contentDescription = null,
                                tint = AccentGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "INDIA VIX & VOLATILITY REGIME",
                                color = AccentGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = "${technicalSummary.indiaVix} (${if (technicalSummary.vixChange >= 0) "+" else ""}${"%.2f".format(technicalSummary.vixChange)}%)",
                            color = if (technicalSummary.vixChange <= 0) BullishGreen else BearishRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = technicalSummary.vixSentiment,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "When India VIX trades below 15, options premiums are reasonable and equity markets favor steady trend following and long call momentum. A sudden VIX spike above 18 signals violent swings and hedging demand.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Intraday NSE Open Interest (OI) & Max Pain Insights
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "INTRADAY NSE OI & MAX PAIN INSIGHTS",
                        color = TerminalCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    PcrAnalysisBar(pcr = currentQuote.pcr)

                    Spacer(modifier = Modifier.height(10.dp))

                    // OI Sentiment description
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant)
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "OI Sentiment: ${technicalSummary.oiAnalysis}",
                                color = BullishGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = technicalSummary.pcrSentiment,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Max Pain details
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant)
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "Max Pain Expiry Magnet: ₹${"%,.0f".format(currentQuote.maxPain)}",
                                color = AccentGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = technicalSummary.maxPainAnalysis,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Pivot Points & Key Levels (Floor Pivots R1-R3, S1-S3, CPR)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "DAILY PIVOT POINTS & KEY LEVELS",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Resistance Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("R3", color = BearishRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("₹${"%,.1f".format(technicalSummary.r3)}", color = TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                        Column {
                            Text("R2", color = BearishRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("₹${"%,.1f".format(technicalSummary.r2)}", color = TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                        Column {
                            Text("R1", color = BearishRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("₹${"%,.1f".format(technicalSummary.r1)}", color = TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Pivot Level (CPR)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(TerminalCyan.copy(alpha = 0.15f))
                            .border(1.dp, TerminalCyan.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("CENTRAL PIVOT (CPR):", color = TerminalCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "₹${"%,.1f".format(technicalSummary.pivot)}",
                                color = TerminalCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Support Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("S1", color = BullishGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("₹${"%,.1f".format(technicalSummary.s1)}", color = TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                        Column {
                            Text("S2", color = BullishGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("₹${"%,.1f".format(technicalSummary.s2)}", color = TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                        Column {
                            Text("S3", color = BullishGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("₹${"%,.1f".format(technicalSummary.s3)}", color = TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}
