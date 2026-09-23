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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MarketQuote
import com.example.data.model.OptionChainData
import com.example.data.model.OptionChainRow
import com.example.ui.components.PcrAnalysisBar
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
fun OptionChainScreen(
    quotes: List<MarketQuote>,
    selectedSymbol: String,
    optionChainData: OptionChainData,
    onSelectSymbol: (String) -> Unit,
    onSelectExpiry: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val supportedSymbols = listOf("NIFTY 50", "BANK NIFTY", "CRUDE OIL")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {

        // Instrument Selection
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                supportedSymbols.forEach { sym ->
                    val isSelected = sym == selectedSymbol
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) TerminalCyan else DarkSurfaceVariant)
                            .border(1.dp, if (isSelected) TerminalCyan else DarkBorder, RoundedCornerShape(8.dp))
                            .clickable { onSelectSymbol(sym) }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                            .testTag("opt_chain_sym_${sym.replace(" ", "_")}")
                    ) {
                        Text(
                            text = sym,
                            color = if (isSelected) Color(0xFF070B14) else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Expiry Date Selector
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(optionChainData.availableExpiries) { exp ->
                    val isSelected = exp == optionChainData.expiryDate
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) DarkBorder else DarkSurfaceVariant)
                            .border(1.dp, if (isSelected) TerminalCyan else Color.Transparent, RoundedCornerShape(6.dp))
                            .clickable { onSelectExpiry(exp) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .testTag("expiry_${exp.take(6)}")
                    ) {
                        Text(
                            text = exp,
                            color = if (isSelected) TerminalCyan else TextMuted,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Option Chain Insights Summary Card
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
                            Text("SPOT PRICE", color = TextMuted, fontSize = 10.sp)
                            Text(
                                "₹${"%,.2f".format(optionChainData.underlyingPrice)}",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("MAX PAIN", color = TextMuted, fontSize = 10.sp)
                            Text(
                                "₹${"%,.0f".format(optionChainData.maxPainStrike)}",
                                color = AccentGold,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("OI PCR", color = TextMuted, fontSize = 10.sp)
                            Text(
                                "%.2f".format(optionChainData.pcr),
                                color = if (optionChainData.pcr >= 1.0) BullishGreen else BearishRed,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    PcrAnalysisBar(pcr = optionChainData.pcr)

                    Spacer(modifier = Modifier.height(8.dp))

                    // Major Resistance & Major Support Strikes
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkSurfaceVariant)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("MAJOR RESISTANCE (Max CE OI)", color = BearishRed, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                "₹${"%,.0f".format(optionChainData.highestCallOiStrike)} CE",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("MAJOR SUPPORT (Max PE OI)", color = BullishGreen, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                "₹${"%,.0f".format(optionChainData.highestPutOiStrike)} PE",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Table Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .background(DarkSurfaceVariant)
                    .border(1.dp, DarkBorder, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Call Side
                Text(
                    text = "CALL OI",
                    color = BearishRed,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1.1f),
                    textAlign = TextAlign.Start
                )
                Text(
                    text = "C-LTP",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(0.9f),
                    textAlign = TextAlign.End
                )

                // Strike Center
                Text(
                    text = "STRIKE",
                    color = TerminalCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1.1f),
                    textAlign = TextAlign.Center
                )

                // Put Side
                Text(
                    text = "P-LTP",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(0.9f),
                    textAlign = TextAlign.Start
                )
                Text(
                    text = "PUT OI",
                    color = BullishGreen,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1.1f),
                    textAlign = TextAlign.End
                )
            }
        }

        // Strike Rows
        items(optionChainData.rows) { row ->
            val isAtm = row.isAtm
            val isMaxCall = row.strikePrice == optionChainData.highestCallOiStrike
            val isMaxPut = row.strikePrice == optionChainData.highestPutOiStrike
            val isMaxPain = row.strikePrice == optionChainData.maxPainStrike

            val rowBg = when {
                isAtm -> TerminalCyan.copy(alpha = 0.15f)
                else -> DarkSurfaceCard
            }

            val rowBorder = when {
                isAtm -> TerminalCyan
                else -> DarkBorder.copy(alpha = 0.5f)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(rowBg)
                    .border(if (isAtm) 1.5.dp else 0.5.dp, rowBorder)
                    .padding(vertical = 8.dp, horizontal = 4.dp)
                    .testTag("strike_row_${row.strikePrice.toInt()}"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Call OI & Chg
                Column(modifier = Modifier.weight(1.1f)) {
                    Text(
                        text = "${(row.callOi / 1000)}k",
                        color = if (isMaxCall) AccentGold else TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = if (isMaxCall) FontWeight.ExtraBold else FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${if (row.callChgOi >= 0) "+" else ""}${(row.callChgOi / 1000)}k",
                        color = if (row.callChgOi >= 0) BullishGreen else BearishRed,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Call LTP
                Text(
                    text = "₹${"%.1f".format(row.callLtp)}",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.weight(0.9f),
                    textAlign = TextAlign.End
                )

                // Strike Price Center (with badges for ATM, Max Pain, etc.)
                Column(
                    modifier = Modifier.weight(1.1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${row.strikePrice.toInt()}",
                        color = if (isAtm) TerminalCyan else TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    if (isAtm) {
                        Text(
                            text = "ATM",
                            color = TerminalCyan,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    } else if (isMaxPain) {
                        Text(
                            text = "PAIN",
                            color = AccentGold,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Put LTP
                Text(
                    text = "₹${"%.1f".format(row.putLtp)}",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.weight(0.9f),
                    textAlign = TextAlign.Start
                )

                // Put OI & Chg
                Column(
                    modifier = Modifier.weight(1.1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "${(row.putOi / 1000)}k",
                        color = if (isMaxPut) AccentGold else TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = if (isMaxPut) FontWeight.ExtraBold else FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${if (row.putChgOi >= 0) "+" else ""}${(row.putChgOi / 1000)}k",
                        color = if (row.putChgOi >= 0) BullishGreen else BearishRed,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
