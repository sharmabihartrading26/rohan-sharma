package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MarketQuote
import com.example.data.model.SignalType
import com.example.data.model.Timeframe
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BearishRedContainer
import com.example.ui.theme.BullishGreen
import com.example.ui.theme.BullishGreenContainer
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.TerminalCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SignalBadge(
    signal: SignalType,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val (bg, textColor, border) = when (signal) {
        SignalType.STRONG_BUY -> Triple(BullishGreenContainer, BullishGreen, BullishGreen.copy(alpha = 0.6f))
        SignalType.BUY -> Triple(BullishGreenContainer.copy(alpha = 0.7f), BullishGreen, BullishGreen.copy(alpha = 0.4f))
        SignalType.HOLD -> Triple(DarkSurfaceVariant, TextSecondary, DarkBorder)
        SignalType.SELL -> Triple(BearishRedContainer.copy(alpha = 0.7f), BearishRed, BearishRed.copy(alpha = 0.4f))
        SignalType.STRONG_SELL -> Triple(BearishRedContainer, BearishRed, BearishRed.copy(alpha = 0.6f))
    }

    val icon = when {
        signal.isBullish -> Icons.Default.TrendingUp
        signal == SignalType.HOLD -> null
        else -> Icons.Default.TrendingDown
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(6.dp))
            .padding(horizontal = if (compact) 6.dp else 10.dp, vertical = if (compact) 3.dp else 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(if (compact) 12.dp else 15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = signal.label,
            color = textColor,
            fontSize = if (compact) 10.sp else 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            maxLines = 1
        )
    }
}

@Composable
fun InstrumentQuoteCard(
    quote: MarketQuote,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPositive = quote.change >= 0
    val changeColor = if (isPositive) BullishGreen else BearishRed

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) TerminalCyan else DarkBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .testTag("quote_card_${quote.symbol.replace(" ", "_")}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) DarkSurfaceCard else DarkSurfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = quote.symbol,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )
                    Text(
                        text = "${quote.exchange} • ${quote.oiBuildupType}",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                SignalBadge(signal = quote.signal, compact = true)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = if (quote.symbol == "INDIA VIX") {
                            "%.2f".format(quote.lastPrice)
                        } else {
                            "₹${"%,.2f".format(quote.lastPrice)}"
                        },
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Icon(
                        imageVector = if (isPositive) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = changeColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${if (isPositive) "+" else ""}${"%.2f".format(quote.change)} (${if (isPositive) "+" else ""}${"%.2f".format(quote.changePercent)}%)",
                        color = changeColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Mini Indicators Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkBorder.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "RSI: ${quote.rsi14}",
                    color = if (quote.rsi14 >= 60) BullishGreen else if (quote.rsi14 <= 40) BearishRed else TerminalCyan,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "ST: ${if (quote.isSupertrendBullish) "🟢" else "🔴"} ${quote.supertrendValue.toInt()}",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "PCR: ${quote.pcr}",
                    color = if (quote.pcr >= 1.0) BullishGreen else BearishRed,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun PcrAnalysisBar(
    pcr: Double,
    modifier: Modifier = Modifier
) {
    val fillRatio = (pcr / 2.0).coerceIn(0.1, 0.95).toFloat()
    val sentimentText = when {
        pcr > 1.3 -> "Strongly Bullish (Heavy Put Writing Support)"
        pcr in 1.0..1.3 -> "Moderately Bullish (Call Buying / Put Building)"
        pcr in 0.8..1.0 -> "Neutral Rangebound"
        else -> "Bearish (Call Resistance Heavy)"
    }
    val sentimentColor = if (pcr >= 1.0) BullishGreen else BearishRed

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Put-Call Ratio (PCR)",
                color = TextSecondary,
                fontSize = 12.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "%.2f".format(pcr),
                    color = sentimentColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(sentimentColor)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Progress bar comparing Put vs Call side
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(BearishRed.copy(alpha = 0.5f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fillRatio)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(BullishGreen, TerminalCyan)
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Calls (Resistance)", color = BearishRed, fontSize = 10.sp)
            Text(text = sentimentText, color = TextMuted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = "Puts (Support)", color = BullishGreen, fontSize = 10.sp)
        }
    }
}

@Composable
fun TimeframeChipGroup(
    selectedTimeframe: Timeframe,
    onTimeframeSelected: (Timeframe) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Timeframe.values().forEach { tf ->
            val isSelected = tf == selectedTimeframe
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) TerminalCyan else Color.Transparent)
                    .clickable { onTimeframeSelected(tf) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("timeframe_${tf.code}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tf.code,
                    color = if (isSelected) Color(0xFF070B14) else TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}
