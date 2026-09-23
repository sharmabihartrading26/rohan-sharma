package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.AppDatabase
import com.example.data.repository.MarketRepository
import com.example.service.NotificationHelper
import com.example.ui.screens.AnalysisScreen
import com.example.ui.screens.ChartScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.OptionChainScreen
import com.example.ui.screens.SignalsScreen
import com.example.ui.theme.BullishGreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TerminalCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.TradingViewModel

enum class NavigationTab(val label: String, val icon: ImageVector, val tag: String) {
    DASHBOARD("Terminal", Icons.Default.Dashboard, "tab_dashboard"),
    CHART("Live Chart", Icons.Default.ShowChart, "tab_chart"),
    OPTION_CHAIN("Option Chain", Icons.Default.TableChart, "tab_option_chain"),
    SIGNALS("Signals", Icons.Default.NotificationsActive, "tab_signals"),
    ANALYSIS("Technicals", Icons.Default.Analytics, "tab_analysis")
}

class MainActivity : ComponentActivity() {

    private val viewModel: TradingViewModel by viewModels {
        val db = AppDatabase.getInstance(applicationContext)
        val repository = MarketRepository(db.alertDao())
        TradingViewModel.Factory(repository)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        NotificationHelper.createNotificationChannel(applicationContext)

        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                val snackbarHostState = remember { SnackbarHostState() }
                var currentTab by remember { mutableStateOf(NavigationTab.DASHBOARD) }

                // Request notification permission on Android 13+
                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        Toast.makeText(context, "Signal push notifications enabled", Toast.LENGTH_SHORT).show()
                    }
                }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                // ViewModel States
                val quotes by viewModel.quotes.collectAsStateWithLifecycle()
                val selectedSymbol by viewModel.selectedSymbol.collectAsStateWithLifecycle()
                val selectedTimeframe by viewModel.selectedTimeframe.collectAsStateWithLifecycle()
                val candles by viewModel.candles.collectAsStateWithLifecycle()
                val optionChainData by viewModel.optionChain.collectAsStateWithLifecycle()
                val technicalSummary by viewModel.technicalSummary.collectAsStateWithLifecycle()
                val signals by viewModel.signals.collectAsStateWithLifecycle()
                val customAlerts by viewModel.customAlerts.collectAsStateWithLifecycle()
                val isLiveTickerRunning by viewModel.isLiveTickerRunning.collectAsStateWithLifecycle()
                val toastMsg by viewModel.toastMessage.collectAsStateWithLifecycle()

                LaunchedEffect(toastMsg) {
                    toastMsg?.let {
                        snackbarHostState.showSnackbar(it)
                        viewModel.clearToastMessage()
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = DarkBackground,
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(TerminalCyan),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "RSI",
                                            color = DarkBackground,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "RSI SuperTrend Pro",
                                            color = TextPrimary,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isLiveTickerRunning) BullishGreen else Color.Gray)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isLiveTickerRunning) "NSE & MCX Live" else "Paused",
                                                color = if (isLiveTickerRunning) BullishGreen else TextMuted,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            },
                            actions = {
                                IconButton(
                                    onClick = {
                                        val q = quotes.find { it.symbol == selectedSymbol } ?: quotes.first()
                                        viewModel.triggerTestPushNotification(context, q)
                                    },
                                    modifier = Modifier.testTag("top_bar_push_notif_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = "Test Notification",
                                        tint = TerminalCyan
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = DarkSurface
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = DarkSurface,
                            tonalElevation = 8.dp,
                            modifier = Modifier.border(width = 0.5.dp, color = DarkBorder)
                        ) {
                            NavigationTab.values().forEach { tab ->
                                val selected = currentTab == tab
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { currentTab = tab },
                                    icon = {
                                        Icon(
                                            imageVector = tab.icon,
                                            contentDescription = tab.label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = tab.label,
                                            fontSize = 10.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = DarkBackground,
                                        selectedTextColor = TerminalCyan,
                                        indicatorColor = TerminalCyan,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextMuted
                                    ),
                                    modifier = Modifier.testTag(tab.tag)
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(DarkBackground)
                    ) {
                        when (currentTab) {
                            NavigationTab.DASHBOARD -> {
                                DashboardScreen(
                                    quotes = quotes,
                                    selectedSymbol = selectedSymbol,
                                    isLiveTickerRunning = isLiveTickerRunning,
                                    signals = signals,
                                    onSelectSymbol = { viewModel.selectSymbol(it) },
                                    onToggleLiveTicker = { viewModel.toggleLiveTicker() },
                                    onNavigateToChart = {
                                        viewModel.selectSymbol(it)
                                        currentTab = NavigationTab.CHART
                                    },
                                    onNavigateToOptionChain = {
                                        viewModel.selectSymbol(it)
                                        currentTab = NavigationTab.OPTION_CHAIN
                                    },
                                    onTriggerNotificationTest = { ctx, q ->
                                        viewModel.triggerTestPushNotification(ctx, q)
                                    }
                                )
                            }

                            NavigationTab.CHART -> {
                                ChartScreen(
                                    quotes = quotes,
                                    selectedSymbol = selectedSymbol,
                                    selectedTimeframe = selectedTimeframe,
                                    candles = candles,
                                    onSelectSymbol = { viewModel.selectSymbol(it) },
                                    onSelectTimeframe = { viewModel.selectTimeframe(it) },
                                    onTriggerNotification = { ctx, q ->
                                        viewModel.triggerTestPushNotification(ctx, q)
                                    }
                                )
                            }

                            NavigationTab.OPTION_CHAIN -> {
                                OptionChainScreen(
                                    quotes = quotes,
                                    selectedSymbol = selectedSymbol,
                                    optionChainData = optionChainData,
                                    onSelectSymbol = { viewModel.selectSymbol(it) },
                                    onSelectExpiry = { viewModel.selectOptionChainExpiry(it) }
                                )
                            }

                            NavigationTab.SIGNALS -> {
                                SignalsScreen(
                                    quotes = quotes,
                                    signals = signals,
                                    customAlerts = customAlerts,
                                    onAddCustomAlert = { sym, type, cond, thresh, notes ->
                                        viewModel.addCustomAlert(sym, type, cond, thresh, notes)
                                    },
                                    onDeleteAlert = { viewModel.deleteAlert(it) },
                                    onToggleAlert = { id, active -> viewModel.toggleAlertStatus(id, active) },
                                    onTriggerNotification = { ctx, q ->
                                        viewModel.triggerTestPushNotification(ctx, q)
                                    }
                                )
                            }

                            NavigationTab.ANALYSIS -> {
                                AnalysisScreen(
                                    quotes = quotes,
                                    selectedSymbol = selectedSymbol,
                                    technicalSummary = technicalSummary,
                                    onSelectSymbol = { viewModel.selectSymbol(it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
