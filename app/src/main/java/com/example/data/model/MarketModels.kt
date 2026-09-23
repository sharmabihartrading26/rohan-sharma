package com.example.data.model

enum class SignalType(val label: String, val isBullish: Boolean) {
    STRONG_BUY("STRONG BUY", true),
    BUY("BUY", true),
    HOLD("NEUTRAL / HOLD", false),
    SELL("SELL", false),
    STRONG_SELL("STRONG SELL", false)
}

enum class Timeframe(val code: String, val label: String) {
    DAILY("1D", "Daily Chart"),
    ONE_HOUR("1H", "1 Hour"),
    FIFTEEN_MIN("15m", "15 Min"),
    FIVE_MIN("5m", "5 Min")
}

data class MarketQuote(
    val id: String,
    val name: String,
    val symbol: String,
    val exchange: String, // "NSE", "NSE IFSC", "MCX"
    val lastPrice: Double,
    val change: Double,
    val changePercent: Double,
    val high: Double,
    val low: Double,
    val open: Double,
    val previousClose: Double,
    val volume: Long,
    val pcr: Double,
    val maxPain: Double,
    val iv: Double,
    val rsi14: Double,
    val supertrendValue: Double,
    val isSupertrendBullish: Boolean,
    val signal: SignalType,
    val signalTarget1: Double,
    val signalTarget2: Double,
    val signalStopLoss: Double,
    val oiBuildupType: String // e.g., "Long Buildup", "Short Covering"
)

data class Candle(
    val timestamp: Long,
    val dateLabel: String,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long,
    val rsi: Double = 50.0,
    val supertrend: Double = 0.0,
    val isSupertrendBullish: Boolean = true,
    val signal: SignalType? = null
)

data class OptionChainRow(
    val strikePrice: Double,
    val isAtm: Boolean,
    val callOi: Long,
    val callChgOi: Long,
    val callLtp: Double,
    val callIv: Double,
    val putLtp: Double,
    val putIv: Double,
    val putChgOi: Long,
    val putOi: Long
)

data class OptionChainData(
    val symbol: String,
    val underlyingPrice: Double,
    val expiryDate: String,
    val availableExpiries: List<String>,
    val totalCallOi: Long,
    val totalPutOi: Long,
    val pcr: Double,
    val maxPainStrike: Double,
    val highestCallOiStrike: Double,
    val highestPutOiStrike: Double,
    val rows: List<OptionChainRow>
)

data class SignalAlertItem(
    val id: String,
    val instrumentSymbol: String,
    val instrumentName: String,
    val timeframe: String,
    val type: SignalType,
    val price: Double,
    val rsi: Double,
    val supertrend: Double,
    val stopLoss: Double,
    val target1: Double,
    val target2: Double,
    val timestamp: Long,
    val rationale: String
)

data class TechnicalSummary(
    val symbol: String,
    val pivot: Double,
    val r1: Double,
    val r2: Double,
    val r3: Double,
    val s1: Double,
    val s2: Double,
    val s3: Double,
    val ema20: Double,
    val sma50: Double,
    val sma200: Double,
    val indiaVix: Double,
    val vixChange: Double,
    val vixSentiment: String,
    val oiAnalysis: String,
    val pcrSentiment: String,
    val maxPainAnalysis: String,
    val compositeBullishScore: Int // 0 to 100
)
