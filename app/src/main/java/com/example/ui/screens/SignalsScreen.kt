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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.local.CustomAlertEntity
import com.example.data.model.MarketQuote
import com.example.data.model.SignalAlertItem
import com.example.data.model.SignalType
import com.example.ui.components.SignalBadge
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignalsScreen(
    quotes: List<MarketQuote>,
    signals: List<SignalAlertItem>,
    customAlerts: List<CustomAlertEntity>,
    onAddCustomAlert: (String, String, String, Double, String) -> Unit,
    onDeleteAlert: (Int) -> Unit,
    onToggleAlert: (Int, Boolean) -> Unit,
    onTriggerNotification: (Context, MarketQuote) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf("ALL") }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredSignals = when (selectedFilter) {
        "BUY ONLY" -> signals.filter { it.type.isBullish }
        "SELL ONLY" -> signals.filter { !it.type.isBullish }
        "NIFTY" -> signals.filter { it.instrumentSymbol.contains("NIFTY") }
        "CRUDE OIL" -> signals.filter { it.instrumentSymbol == "CRUDE OIL" }
        else -> signals
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)
    ) {

        // Header Actions: Push Notification Test & Create Custom Alert
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.weight(1f).testTag("create_custom_alert_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = TerminalCyan)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAlert,
                        contentDescription = null,
                        tint = DarkSurfaceCard,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "New Alert Rule",
                        color = DarkSurfaceCard,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                OutlinedButton(
                    onClick = {
                        val q = quotes.firstOrNull() ?: return@OutlinedButton
                        onTriggerNotification(context, q)
                    },
                    modifier = Modifier.testTag("push_notif_test_btn"),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BullishGreen)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = BullishGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Push Test", color = BullishGreen, fontSize = 12.sp)
                }
            }
        }

        // Custom Watchlist Alerts (Persisted in Room)
        if (customAlerts.isNotEmpty()) {
            item {
                Text(
                    text = "MY ACTIVE PRICE & INDICATOR ALERTS (${customAlerts.size})",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            items(customAlerts) { alert ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, DarkBorder, RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = alert.symbol,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = alert.triggerCondition,
                                    color = TerminalCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            if (alert.notes.isNotBlank()) {
                                Text(
                                    text = alert.notes,
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = alert.isActive,
                                onCheckedChange = { onToggleAlert(alert.id, it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = TerminalCyan,
                                    checkedTrackColor = TerminalCyan.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { onDeleteAlert(alert.id) },
                                modifier = Modifier.size(32.dp).testTag("delete_alert_${alert.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Alert",
                                    tint = BearishRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Live Indicator Signal Feed Header & Filters
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "RSI & SUPERTREND STRATEGY SIGNALS",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Filter Chips
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("ALL", "BUY ONLY", "SELL ONLY", "NIFTY", "CRUDE OIL").forEach { f ->
                    val isSel = f == selectedFilter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSel) TerminalCyan else DarkSurfaceVariant)
                            .clickable { selectedFilter = f }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = f,
                            color = if (isSel) DarkSurfaceCard else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Signal Cards
        items(filteredSignals) { sig ->
            val dateStr = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(sig.timestamp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, if (sig.type.isBullish) BullishGreen.copy(alpha = 0.5f) else BearishRed.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = sig.instrumentSymbol,
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "${sig.instrumentName} • ${sig.timeframe}",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }

                        SignalBadge(signal = sig.type)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = sig.rationale,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Trade parameters
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("TRIGGER", color = TextMuted, fontSize = 9.sp)
                            Text("₹${"%,.1f".format(sig.price)}", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Column {
                            Text("RSI(14)", color = TextMuted, fontSize = 9.sp)
                            Text("${sig.rsi}", color = TerminalCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Column {
                            Text("SL", color = TextMuted, fontSize = 9.sp)
                            Text("₹${"%,.1f".format(sig.stopLoss)}", color = BearishRed, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Column {
                            Text("TARGET 1", color = TextMuted, fontSize = 9.sp)
                            Text("₹${"%,.1f".format(sig.target1)}", color = BullishGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Supertrend Level: ₹${"%,.1f".format(sig.supertrend)}",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = dateStr,
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }

    // Modal Dialog: Add Custom Alert
    if (showAddDialog) {
        var alertSymbol by remember { mutableStateOf("NIFTY 50") }
        var condition by remember { mutableStateOf("RSI > 60 & Supertrend Buy") }
        var priceInput by remember { mutableStateOf("25300") }
        var noteInput by remember { mutableStateOf("Breakout alert") }
        var expandedSymbol by remember { mutableStateOf(false) }

        val conditions = listOf(
            "RSI > 60 & Supertrend Buy",
            "RSI < 40 & Supertrend Sell",
            "Price Crosses Above",
            "Price Crosses Below",
            "PCR Crosses > 1.25",
            "Supertrend Flip Alert"
        )

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = DarkSurfaceCard,
            title = {
                Text(
                    text = "Create Price or Signal Alert",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Set custom thresholds for RSI, Supertrend, or Price triggers. Push notifications will be dispatched instantly when met.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    // Symbol dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedSymbol,
                        onExpandedChange = { expandedSymbol = !expandedSymbol }
                    ) {
                        OutlinedTextField(
                            value = alertSymbol,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Instrument", color = TextMuted) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSymbol) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedSymbol,
                            onDismissRequest = { expandedSymbol = false }
                        ) {
                            listOf("NIFTY 50", "GIFT NIFTY", "BANK NIFTY", "CRUDE OIL").forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s) },
                                    onClick = {
                                        alertSymbol = s
                                        expandedSymbol = false
                                    }
                                )
                            }
                        }
                    }

                    // Condition selection
                    OutlinedTextField(
                        value = condition,
                        onValueChange = { condition = it },
                        label = { Text("Trigger Strategy / Condition", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Target Price
                    OutlinedTextField(
                        value = priceInput,
                        onValueChange = { priceInput = it },
                        label = { Text("Threshold Price / Level", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Notes
                    OutlinedTextField(
                        value = noteInput,
                        onValueChange = { noteInput = it },
                        label = { Text("Alert Note / Memo", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = priceInput.toDoubleOrNull() ?: 0.0
                        onAddCustomAlert(alertSymbol, "CUSTOM_ALERT", condition, parsed, noteInput)
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TerminalCyan)
                ) {
                    Text("Save Alert", color = DarkSurfaceCard, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}
