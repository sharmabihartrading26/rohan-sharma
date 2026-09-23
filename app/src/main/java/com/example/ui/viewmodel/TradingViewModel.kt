package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.CustomAlertEntity
import com.example.data.model.Candle
import com.example.data.model.MarketQuote
import com.example.data.model.OptionChainData
import com.example.data.model.SignalAlertItem
import com.example.data.model.SignalType
import com.example.data.model.TechnicalSummary
import com.example.data.model.Timeframe
import com.example.data.repository.MarketRepository
import com.example.service.NotificationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TradingViewModel(
    private val repository: MarketRepository
) : ViewModel() {

    private val _quotes = MutableStateFlow<List<MarketQuote>>(repository.getAllQuotes())
    val quotes: StateFlow<List<MarketQuote>> = _quotes.asStateFlow()

    private val _selectedSymbol = MutableStateFlow("NIFTY 50")
    val selectedSymbol: StateFlow<String> = _selectedSymbol.asStateFlow()

    private val _selectedTimeframe = MutableStateFlow(Timeframe.DAILY)
    val selectedTimeframe: StateFlow<Timeframe> = _selectedTimeframe.asStateFlow()

    private val _candles = MutableStateFlow<List<Candle>>(emptyList())
    val candles: StateFlow<List<Candle>> = _candles.asStateFlow()

    private val _optionChain = MutableStateFlow(repository.getOptionChainData("NIFTY 50"))
    val optionChain: StateFlow<OptionChainData> = _optionChain.asStateFlow()

    private val _technicalSummary = MutableStateFlow(repository.getTechnicalSummary("NIFTY 50"))
    val technicalSummary: StateFlow<TechnicalSummary> = _technicalSummary.asStateFlow()

    private val _signals = MutableStateFlow<List<SignalAlertItem>>(repository.getRecentSignals())
    val signals: StateFlow<List<SignalAlertItem>> = _signals.asStateFlow()

    val customAlerts: StateFlow<List<CustomAlertEntity>> = repository.customAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLiveTickerRunning = MutableStateFlow(true)
    val isLiveTickerRunning: StateFlow<Boolean> = _isLiveTickerRunning.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        loadDataForSelected()
        startLiveMarketSimulation()
    }

    private fun loadDataForSelected() {
        val sym = _selectedSymbol.value
        val tf = _selectedTimeframe.value
        _candles.value = repository.getCandlesForInstrument(sym, tf)
        _optionChain.value = repository.getOptionChainData(sym)
        _technicalSummary.value = repository.getTechnicalSummary(sym)
    }

    fun selectSymbol(symbol: String) {
        if (_selectedSymbol.value == symbol) return
        _selectedSymbol.value = symbol
        loadDataForSelected()
    }

    fun selectTimeframe(timeframe: Timeframe) {
        if (_selectedTimeframe.value == timeframe) return
        _selectedTimeframe.value = timeframe
        _candles.value = repository.getCandlesForInstrument(_selectedSymbol.value, timeframe)
    }

    fun selectOptionChainExpiry(expiry: String) {
        _optionChain.value = repository.getOptionChainData(_selectedSymbol.value, expiry)
    }

    fun toggleLiveTicker() {
        _isLiveTickerRunning.value = !_isLiveTickerRunning.value
    }

    fun clearToastMessage() {
        _toastMessage.value = null
    }

    private fun startLiveMarketSimulation() {
        viewModelScope.launch {
            while (true) {
                delay(2500)
                if (_isLiveTickerRunning.value) {
                    val updated = repository.simulatePriceTick()
                    _quotes.value = repository.getAllQuotes()

                    // If currently selected symbol updated, refresh candle head
                    if (updated.symbol == _selectedSymbol.value) {
                        val currentList = _candles.value
                        if (currentList.isNotEmpty()) {
                            val last = currentList.last()
                            val updatedLast = last.copy(
                                close = updated.lastPrice,
                                high = kotlin.math.max(last.high, updated.lastPrice),
                                low = kotlin.math.min(last.low, updated.lastPrice)
                            )
                            _candles.value = currentList.dropLast(1) + updatedLast
                        }
                    }
                }
            }
        }
    }

    fun addCustomAlert(symbol: String, alertType: String, condition: String, thresholdPrice: Double, notes: String) {
        viewModelScope.launch {
            repository.saveAlert(
                CustomAlertEntity(
                    symbol = symbol,
                    alertType = alertType,
                    triggerCondition = condition,
                    thresholdPrice = thresholdPrice,
                    notes = notes
                )
            )
            _toastMessage.value = "Alert created for $symbol ($condition)"
        }
    }

    fun deleteAlert(id: Int) {
        viewModelScope.launch {
            repository.removeAlert(id)
            _toastMessage.value = "Alert deleted"
        }
    }

    fun toggleAlertStatus(id: Int, isActive: Boolean) {
        viewModelScope.launch {
            repository.toggleAlert(id, isActive)
        }
    }

    fun triggerTestPushNotification(context: Context, quote: MarketQuote) {
        val title = "${quote.symbol}: ${quote.signal.label} ALERT!"
        val desc = "Price: ₹${"%,.2f".format(quote.lastPrice)} | RSI 14: ${quote.rsi14} | Supertrend: ${if (quote.isSupertrendBullish) "Bullish Green (₹${"%,.0f".format(quote.supertrendValue)})" else "Bearish Red (₹${"%,.0f".format(quote.supertrendValue)})"} | Target: ₹${"%,.0f".format(quote.signalTarget1)}"
        NotificationHelper.sendSignalNotification(
            context = context,
            title = title,
            message = desc,
            signalType = quote.signal
        )
        _toastMessage.value = "Push notification triggered for ${quote.symbol}"
    }

    class Factory(private val repository: MarketRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TradingViewModel::class.java)) {
                return TradingViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
