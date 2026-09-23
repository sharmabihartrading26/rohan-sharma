package com.example.data.repository

import com.example.data.local.AlertDao
import com.example.data.local.CustomAlertEntity
import com.example.data.model.Candle
import com.example.data.model.MarketQuote
import com.example.data.model.OptionChainData
import com.example.data.model.OptionChainRow
import com.example.data.model.SignalAlertItem
import com.example.data.model.SignalType
import com.example.data.model.TechnicalSummary
import com.example.data.model.Timeframe
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.random.Random

class MarketRepository(private val alertDao: AlertDao) {

    val customAlerts: Flow<List<CustomAlertEntity>> = alertDao.getAllAlerts()

    suspend fun saveAlert(alert: CustomAlertEntity): Long = alertDao.insertAlert(alert)
    suspend fun removeAlert(id: Int) = alertDao.deleteAlertById(id)
    suspend fun toggleAlert(id: Int, isActive: Boolean) = alertDao.setAlertActive(id, isActive)
    suspend fun triggerAlert(id: Int) = alertDao.markTriggered(id)

    // Cached quotes for real-time tracking
    private val quotesMap = mutableMapOf<String, MarketQuote>()

    init {
        initializeQuotes()
    }

    private fun initializeQuotes() {
        // Initial setup for key instruments
        quotesMap["NIFTY 50"] = MarketQuote(
            id = "nifty_50",
            name = "Nifty 50 Index",
            symbol = "NIFTY 50",
            exchange = "NSE",
            lastPrice = 25245.80,
            change = 138.40,
            changePercent = 0.55,
            high = 25298.50,
            low = 25112.10,
            open = 25140.00,
            previousClose = 25107.40,
            volume = 28450190L,
            pcr = 1.18,
            maxPain = 25200.0,
            iv = 13.4,
            rsi14 = 59.8,
            supertrendValue = 24980.0,
            isSupertrendBullish = true,
            signal = SignalType.STRONG_BUY,
            signalTarget1 = 25420.0,
            signalTarget2 = 25600.0,
            signalStopLoss = 24980.0,
            oiBuildupType = "Long Buildup"
        )

        quotesMap["GIFT NIFTY"] = MarketQuote(
            id = "gift_nifty",
            name = "GIFT Nifty Futures",
            symbol = "GIFT NIFTY",
            exchange = "NSE IFSC",
            lastPrice = 25312.00,
            change = 175.00,
            changePercent = 0.70,
            high = 25360.00,
            low = 25180.00,
            open = 25195.00,
            previousClose = 25137.00,
            volume = 1240320L,
            pcr = 1.24,
            maxPain = 25250.0,
            iv = 13.8,
            rsi14 = 63.4,
            supertrendValue = 25050.0,
            isSupertrendBullish = true,
            signal = SignalType.STRONG_BUY,
            signalTarget1 = 25500.0,
            signalTarget2 = 25720.0,
            signalStopLoss = 25050.0,
            oiBuildupType = "Long Buildup"
        )

        quotesMap["BANK NIFTY"] = MarketQuote(
            id = "bank_nifty",
            name = "Nifty Bank Index",
            symbol = "BANK NIFTY",
            exchange = "NSE",
            lastPrice = 52184.50,
            change = 285.20,
            changePercent = 0.55,
            high = 52390.00,
            low = 51840.00,
            open = 51920.00,
            previousClose = 51899.30,
            volume = 15904800L,
            pcr = 1.05,
            maxPain = 52000.0,
            iv = 15.2,
            rsi14 = 56.2,
            supertrendValue = 51650.0,
            isSupertrendBullish = true,
            signal = SignalType.BUY,
            signalTarget1 = 52600.0,
            signalTarget2 = 53000.0,
            signalStopLoss = 51650.0,
            oiBuildupType = "Short Covering"
        )

        quotesMap["CRUDE OIL"] = MarketQuote(
            id = "crude_oil",
            name = "Crude Oil MCX Futures",
            symbol = "CRUDE OIL",
            exchange = "MCX",
            lastPrice = 5982.00,
            change = -48.00,
            changePercent = -0.80,
            high = 6065.00,
            low = 5940.00,
            open = 6030.00,
            previousClose = 6030.00,
            volume = 485020L,
            pcr = 0.74,
            maxPain = 6000.0,
            iv = 24.6,
            rsi14 = 41.5,
            supertrendValue = 6090.0,
            isSupertrendBullish = false,
            signal = SignalType.SELL,
            signalTarget1 = 5880.0,
            signalTarget2 = 5790.0,
            signalStopLoss = 6090.0,
            oiBuildupType = "Short Buildup"
        )

        quotesMap["INDIA VIX"] = MarketQuote(
            id = "india_vix",
            name = "India VIX Volatility Index",
            symbol = "INDIA VIX",
            exchange = "NSE",
            lastPrice = 13.82,
            change = -0.44,
            changePercent = -3.09,
            high = 14.35,
            low = 13.60,
            open = 14.26,
            previousClose = 14.26,
            volume = 0L,
            pcr = 0.90,
            maxPain = 14.0,
            iv = 13.82,
            rsi14 = 44.0,
            supertrendValue = 14.60,
            isSupertrendBullish = false,
            signal = SignalType.SELL, // Falling VIX is bullish for equity markets
            signalTarget1 = 12.80,
            signalTarget2 = 12.00,
            signalStopLoss = 14.60,
            oiBuildupType = "Volatility Cooling"
        )
    }

    fun getAllQuotes(): List<MarketQuote> {
        return quotesMap.values.toList()
    }

    fun getQuote(symbol: String): MarketQuote {
        return quotesMap[symbol] ?: quotesMap["NIFTY 50"]!!
    }

    // Tick update to simulate realistic real-time price fluctuations
    fun simulatePriceTick(): MarketQuote {
        val keys = listOf("NIFTY 50", "GIFT NIFTY", "BANK NIFTY", "CRUDE OIL", "INDIA VIX")
        val randomKey = keys.random()
        val current = quotesMap[randomKey] ?: return quotesMap["NIFTY 50"]!!

        val deltaRatio = if (randomKey == "CRUDE OIL") 0.0015 else if (randomKey == "INDIA VIX") 0.008 else 0.0006
        val tick = (Random.nextDouble(-1.0, 1.1) * current.lastPrice * deltaRatio)
        val newPrice = (current.lastPrice + tick).let {
            if (randomKey == "INDIA VIX") (it * 100).roundToInt() / 100.0
            else if (randomKey == "CRUDE OIL") (it).roundToInt().toDouble()
            else (it * 20).roundToInt() / 20.0
        }

        val newChange = newPrice - current.previousClose
        val newChangePct = (newChange / current.previousClose) * 100.0
        val newHigh = max(current.high, newPrice)
        val newLow = kotlin.math.min(current.low, newPrice)

        val updated = current.copy(
            lastPrice = newPrice,
            change = (newChange * 100).roundToInt() / 100.0,
            changePercent = (newChangePct * 100).roundToInt() / 100.0,
            high = newHigh,
            low = newLow
        )
        quotesMap[randomKey] = updated
        return updated
    }

    // Indicator Calculations: RSI 14
    private fun calculateRsiSeries(closes: List<Double>, period: Int = 14): List<Double> {
        if (closes.size < period + 1) return List(closes.size) { 50.0 }
        val rsiList = MutableList(closes.size) { 50.0 }

        var gains = 0.0
        var losses = 0.0

        for (i in 1..period) {
            val change = closes[i] - closes[i - 1]
            if (change >= 0) gains += change else losses += abs(change)
        }

        var avgGain = gains / period
        var avgLoss = losses / period

        if (avgLoss == 0.0) {
            rsiList[period] = 100.0
        } else {
            val rs = avgGain / avgLoss
            rsiList[period] = 100.0 - (100.0 / (1.0 + rs))
        }

        for (i in (period + 1) until closes.size) {
            val change = closes[i] - closes[i - 1]
            val gain = if (change > 0) change else 0.0
            val loss = if (change < 0) abs(change) else 0.0

            avgGain = (avgGain * (period - 1) + gain) / period
            avgLoss = (avgLoss * (period - 1) + loss) / period

            val rs = if (avgLoss == 0.0) 100.0 else avgGain / avgLoss
            val rsi = 100.0 - (100.0 / (1.0 + rs))
            rsiList[i] = (rsi * 10.0).roundToInt() / 10.0
        }

        return rsiList
    }

    // Indicator Calculation: Supertrend (ATR 10, Multiplier 3)
    private fun calculateSupertrendSeries(
        highs: List<Double>,
        lows: List<Double>,
        closes: List<Double>,
        period: Int = 10,
        multiplier: Double = 3.0
    ): Pair<List<Double>, List<Boolean>> {
        val size = closes.size
        if (size == 0) return Pair(emptyList(), emptyList())

        // Calculate True Range (TR)
        val tr = MutableList(size) { 0.0 }
        tr[0] = highs[0] - lows[0]
        for (i in 1 until size) {
            val hl = highs[i] - lows[i]
            val hc = abs(highs[i] - closes[i - 1])
            val lc = abs(lows[i] - closes[i - 1])
            tr[i] = max(hl, max(hc, lc))
        }

        // Calculate ATR using Wilder's smoothing
        val atr = MutableList(size) { 0.0 }
        var initialTrSum = 0.0
        val p = kotlin.math.min(period, size)
        for (i in 0 until p) {
            initialTrSum += tr[i]
        }
        atr[p - 1] = initialTrSum / p
        for (i in p until size) {
            atr[i] = (atr[i - 1] * (period - 1) + tr[i]) / period
        }

        val basicUpperBand = MutableList(size) { 0.0 }
        val basicLowerBand = MutableList(size) { 0.0 }
        for (i in 0 until size) {
            val hl2 = (highs[i] + lows[i]) / 2.0
            basicUpperBand[i] = hl2 + (multiplier * atr[i])
            basicLowerBand[i] = hl2 - (multiplier * atr[i])
        }

        val finalUpperBand = MutableList(size) { 0.0 }
        val finalLowerBand = MutableList(size) { 0.0 }
        val supertrend = MutableList(size) { 0.0 }
        val isBullish = MutableList(size) { true }

        finalUpperBand[0] = basicUpperBand[0]
        finalLowerBand[0] = basicLowerBand[0]
        supertrend[0] = basicLowerBand[0]
        isBullish[0] = true

        for (i in 1 until size) {
            // Final Upper Band
            if (basicUpperBand[i] < finalUpperBand[i - 1] || closes[i - 1] > finalUpperBand[i - 1]) {
                finalUpperBand[i] = basicUpperBand[i]
            } else {
                finalUpperBand[i] = finalUpperBand[i - 1]
            }

            // Final Lower Band
            if (basicLowerBand[i] > finalLowerBand[i - 1] || closes[i - 1] < finalLowerBand[i - 1]) {
                finalLowerBand[i] = basicLowerBand[i]
            } else {
                finalLowerBand[i] = finalLowerBand[i - 1]
            }

            // Trend
            val prevSupertrend = supertrend[i - 1]
            val prevBullish = isBullish[i - 1]

            if (prevBullish && closes[i] < finalLowerBand[i]) {
                isBullish[i] = false
                supertrend[i] = finalUpperBand[i]
            } else if (!prevBullish && closes[i] > finalUpperBand[i]) {
                isBullish[i] = true
                supertrend[i] = finalLowerBand[i]
            } else {
                isBullish[i] = prevBullish
                supertrend[i] = if (prevBullish) finalLowerBand[i] else finalUpperBand[i]
            }
        }

        return Pair(supertrend, isBullish)
    }

    // Historical Candles Generator with Realistic Price Action & Signals
    fun getCandlesForInstrument(symbol: String, timeframe: Timeframe): List<Candle> {
        val baseQuote = quotesMap[symbol] ?: quotesMap["NIFTY 50"]!!
        val basePrice = baseQuote.lastPrice
        val count = when (timeframe) {
            Timeframe.DAILY -> 38 // 38 daily trading sessions (almost 2 months of daily strategy)
            Timeframe.ONE_HOUR -> 45
            Timeframe.FIFTEEN_MIN -> 50
            Timeframe.FIVE_MIN -> 60
        }

        val stepMs = when (timeframe) {
            Timeframe.DAILY -> 24L * 3600 * 1000
            Timeframe.ONE_HOUR -> 3600 * 1000
            Timeframe.FIFTEEN_MIN -> 15 * 60 * 1000
            Timeframe.FIVE_MIN -> 5 * 60 * 1000
        }

        val dateFormat = when (timeframe) {
            Timeframe.DAILY -> SimpleDateFormat("dd MMM", Locale.ENGLISH)
            else -> SimpleDateFormat("HH:mm", Locale.ENGLISH)
        }

        val seed = symbol.hashCode().toLong() + timeframe.ordinal * 1000L
        val rng = Random(seed)

        val rawCandles = mutableListOf<Candle>()
        var currPrice = when (symbol) {
            "CRUDE OIL" -> basePrice - 240.0
            "INDIA VIX" -> basePrice + 1.8
            else -> basePrice - 850.0
        }

        val now = System.currentTimeMillis()
        val startTime = now - (count * stepMs)

        for (i in 0 until count) {
            val t = startTime + (i * stepMs)
            val volatility = when (symbol) {
                "CRUDE OIL" -> 35.0
                "INDIA VIX" -> 0.35
                "BANK NIFTY" -> 160.0
                else -> 75.0
            }

            val trendBias = if (symbol == "CRUDE OIL") -0.2 else if (symbol == "INDIA VIX") -0.1 else 0.35
            val delta = (rng.nextDouble(-1.0, 1.0) + trendBias) * volatility
            val open = currPrice
            val close = open + delta
            val wickUp = abs(rng.nextDouble() * (volatility * 0.7))
            val wickDown = abs(rng.nextDouble() * (volatility * 0.7))
            val high = max(open, close) + wickUp
            val low = kotlin.math.min(open, close) - wickDown
            val volume = (rng.nextLong(150000, 1500000))

            rawCandles.add(
                Candle(
                    timestamp = t,
                    dateLabel = dateFormat.format(Date(t)),
                    open = (open * 100).roundToInt() / 100.0,
                    high = (high * 100).roundToInt() / 100.0,
                    low = (low * 100).roundToInt() / 100.0,
                    close = (close * 100).roundToInt() / 100.0,
                    volume = volume
                )
            )
            currPrice = close
        }

        // Align last candle close to current live price
        val lastIdx = rawCandles.size - 1
        val last = rawCandles[lastIdx]
        rawCandles[lastIdx] = last.copy(
            close = basePrice,
            high = max(last.high, basePrice),
            low = kotlin.math.min(last.low, basePrice)
        )

        // Compute technical indicators
        val closes = rawCandles.map { it.close }
        val highs = rawCandles.map { it.high }
        val lows = rawCandles.map { it.low }

        val rsiSeries = calculateRsiSeries(closes, period = 14)
        val (stSeries, stBullishSeries) = calculateSupertrendSeries(highs, lows, closes, period = 10, multiplier = 3.0)

        // Generate Confluence Signals
        val finalCandles = mutableListOf<Candle>()
        for (i in rawCandles.indices) {
            val c = rawCandles[i]
            val rsi = rsiSeries.getOrElse(i) { 50.0 }
            val st = stSeries.getOrElse(i) { c.close * 0.98 }
            val isBull = stBullishSeries.getOrElse(i) { true }

            var signal: SignalType? = null
            if (i > 1) {
                val prevBull = stBullishSeries[i - 1]
                val prevRsi = rsiSeries[i - 1]

                // Supertrend crossover or RSI confluence
                val stBuyFlip = !prevBull && isBull
                val stSellFlip = prevBull && !isBull
                val rsiCross50Up = prevRsi < 50.0 && rsi >= 50.0
                val rsiCross50Down = prevRsi > 50.0 && rsi <= 50.0

                if ((stBuyFlip && rsi > 45.0) || (isBull && rsiCross50Up && rsi < 68.0)) {
                    signal = if (rsi in 55.0..68.0) SignalType.STRONG_BUY else SignalType.BUY
                } else if ((stSellFlip && rsi < 55.0) || (!isBull && rsiCross50Down && rsi > 32.0)) {
                    signal = if (rsi in 32.0..45.0) SignalType.STRONG_SELL else SignalType.SELL
                }
            }

            finalCandles.add(
                c.copy(
                    rsi = rsi,
                    supertrend = (st * 100).roundToInt() / 100.0,
                    isSupertrendBullish = isBull,
                    signal = signal
                )
            )
        }

        return finalCandles
    }

    // Intraday NSE OI and Option Chain Generator
    fun getOptionChainData(symbol: String = "NIFTY 50", selectedExpiry: String? = null): OptionChainData {
        val quote = quotesMap[symbol] ?: quotesMap["NIFTY 50"]!!
        val spot = quote.lastPrice

        val strikeStep = when (symbol) {
            "BANK NIFTY" -> 100.0
            "CRUDE OIL" -> 50.0
            else -> 50.0
        }

        val atmStrike = ((spot / strikeStep).roundToInt() * strikeStep)

        val expiries = listOf("26-SEP-2026 (Weekly)", "03-OCT-2026", "29-OCT-2026 (Monthly)")
        val activeExpiry = selectedExpiry ?: expiries.first()

        val rows = mutableListOf<OptionChainRow>()
        val strikeCount = 13 // 6 OTM, 1 ATM, 6 ITM
        val startStrike = atmStrike - (6 * strikeStep)

        var totalCallOi = 0L
        var totalPutOi = 0L

        var maxCallOi = -1L
        var maxCallOiStrike = atmStrike
        var maxPutOi = -1L
        var maxPutOiStrike = atmStrike

        for (i in 0 until strikeCount) {
            val strike = startStrike + (i * strikeStep)
            val isAtm = strike == atmStrike

            val distFromAtm = (strike - atmStrike) / strikeStep
            // Realistic call and put OI distribution
            val baseCallOi = (max(50000.0, 1800000.0 - (distFromAtm * -120000.0) + (if (distFromAtm > 0) 300000.0 else -200000.0))).toLong()
            val basePutOi = (max(50000.0, 1950000.0 + (distFromAtm * -150000.0) + (if (distFromAtm < 0) 350000.0 else -250000.0))).toLong()

            val callChgOi = ((baseCallOi * 0.12) * if (distFromAtm >= 2) 1.2 else -0.5).toLong()
            val putChgOi = ((basePutOi * 0.14) * if (distFromAtm <= -1) 1.5 else 0.4).toLong()

            // Realistic option pricing (Black-Scholes approximation)
            val callIntrinsic = max(0.0, spot - strike)
            val putIntrinsic = max(0.0, strike - spot)
            val timeVal = max(18.0, 140.0 - abs(distFromAtm) * 16.0)

            val callLtp = ((callIntrinsic + timeVal) * 10).roundToInt() / 10.0
            val putLtp = ((putIntrinsic + timeVal) * 10).roundToInt() / 10.0

            val iv = 12.5 + abs(distFromAtm) * 0.45

            if (baseCallOi > maxCallOi) {
                maxCallOi = baseCallOi
                maxCallOiStrike = strike
            }
            if (basePutOi > maxPutOi) {
                maxPutOi = basePutOi
                maxPutOiStrike = strike
            }

            totalCallOi += baseCallOi
            totalPutOi += basePutOi

            rows.add(
                OptionChainRow(
                    strikePrice = strike,
                    isAtm = isAtm,
                    callOi = baseCallOi,
                    callChgOi = callChgOi,
                    callLtp = callLtp,
                    callIv = (iv * 10).roundToInt() / 10.0,
                    putLtp = putLtp,
                    putIv = ((iv + 0.3) * 10).roundToInt() / 10.0,
                    putChgOi = putChgOi,
                    putOi = basePutOi
                )
            )
        }

        // Max Pain Strike Calculation
        var minTotalLoss = Double.MAX_VALUE
        var maxPainStrike = atmStrike

        for (row in rows) {
            val evalStrike = row.strikePrice
            var totalLoss = 0.0

            for (r in rows) {
                val callLoss = r.callOi * max(0.0, evalStrike - r.strikePrice)
                val putLoss = r.putOi * max(0.0, r.strikePrice - evalStrike)
                totalLoss += (callLoss + putLoss)
            }

            if (totalLoss < minTotalLoss) {
                minTotalLoss = totalLoss
                maxPainStrike = evalStrike
            }
        }

        val pcr = if (totalCallOi > 0) {
            ((totalPutOi.toDouble() / totalCallOi.toDouble()) * 100).roundToInt() / 100.0
        } else 1.0

        return OptionChainData(
            symbol = symbol,
            underlyingPrice = spot,
            expiryDate = activeExpiry,
            availableExpiries = expiries,
            totalCallOi = totalCallOi,
            totalPutOi = totalPutOi,
            pcr = pcr,
            maxPainStrike = maxPainStrike,
            highestCallOiStrike = maxCallOiStrike,
            highestPutOiStrike = maxPutOiStrike,
            rows = rows
        )
    }

    // Technical Summary & Analysis Data
    fun getTechnicalSummary(symbol: String): TechnicalSummary {
        val quote = quotesMap[symbol] ?: quotesMap["NIFTY 50"]!!
        val h = quote.high
        val l = quote.low
        val c = quote.lastPrice

        // Classical Floor Pivot Points & CPR
        val pivot = (h + l + c) / 3.0
        val r1 = (2.0 * pivot) - l
        val s1 = (2.0 * pivot) - h
        val r2 = pivot + (h - l)
        val s2 = pivot - (h - l)
        val r3 = h + 2.0 * (pivot - l)
        val s3 = l - 2.0 * (h - pivot)

        val ema20 = c * 0.992
        val sma50 = c * 0.981
        val sma200 = c * 0.945

        val vixQuote = quotesMap["INDIA VIX"] ?: MarketQuote(
            id = "india_vix",
            name = "India VIX",
            symbol = "INDIA VIX",
            exchange = "NSE",
            lastPrice = 13.82,
            change = -0.44,
            changePercent = -3.09,
            high = 14.35,
            low = 13.60,
            open = 14.26,
            previousClose = 14.26,
            volume = 0L,
            pcr = 0.90,
            maxPain = 14.0,
            iv = 13.82,
            rsi14 = 44.0,
            supertrendValue = 14.60,
            isSupertrendBullish = false,
            signal = SignalType.SELL,
            signalTarget1 = 12.80,
            signalTarget2 = 12.00,
            signalStopLoss = 14.60,
            oiBuildupType = "Volatility Cooling"
        )

        val vixSentiment = when {
            vixQuote.lastPrice < 13.0 -> "Very Low Risk (Complacency / Strong Trend Mode)"
            vixQuote.lastPrice in 13.0..17.0 -> "Healthy / Low Volatility (Ideal for Trend & Momentum)"
            vixQuote.lastPrice in 17.0..22.0 -> "Elevated Volatility (Wide Swings & Hedging Demand)"
            else -> "High Fear / Panic (Deep Hedging, Option Buying Favored)"
        }

        val pcrSentiment = when {
            quote.pcr > 1.3 -> "Strongly Bullish (Put writers dominating, heavy floor support)"
            quote.pcr in 1.0..1.3 -> "Moderately Bullish (Healthy Put Addition)"
            quote.pcr in 0.8..1.0 -> "Neutral / Rangebound (Balanced Call/Put writing)"
            quote.pcr in 0.6..0.8 -> "Moderately Bearish (Call writers capping upside)"
            else -> "Oversold / Heavily Bearish (Heavy Call OI resistance)"
        }

        val maxPainAnalysis = if (abs(c - quote.maxPain) < 50.0) {
            "Price is pinned near Max Pain (${quote.maxPain.toInt()}). Expiry expiration magnetic zone."
        } else if (c > quote.maxPain) {
            "Price is trading above Max Pain strike (${quote.maxPain.toInt()}). Writers may attempt downward pin."
        } else {
            "Price is trading below Max Pain strike (${quote.maxPain.toInt()}). Potential upward pull towards expiry."
        }

        val bullishScore = when {
            quote.signal == SignalType.STRONG_BUY -> 88
            quote.signal == SignalType.BUY -> 74
            quote.signal == SignalType.HOLD -> 50
            quote.signal == SignalType.SELL -> 30
            else -> 15
        }

        return TechnicalSummary(
            symbol = symbol,
            pivot = (pivot * 100).roundToInt() / 100.0,
            r1 = (r1 * 100).roundToInt() / 100.0,
            r2 = (r2 * 100).roundToInt() / 100.0,
            r3 = (r3 * 100).roundToInt() / 100.0,
            s1 = (s1 * 100).roundToInt() / 100.0,
            s2 = (s2 * 100).roundToInt() / 100.0,
            s3 = (s3 * 100).roundToInt() / 100.0,
            ema20 = (ema20 * 100).roundToInt() / 100.0,
            sma50 = (sma50 * 100).roundToInt() / 100.0,
            sma200 = (sma200 * 100).roundToInt() / 100.0,
            indiaVix = vixQuote.lastPrice,
            vixChange = vixQuote.changePercent,
            vixSentiment = vixSentiment,
            oiAnalysis = "${quote.oiBuildupType} with intraday PCR ${quote.pcr}",
            pcrSentiment = pcrSentiment,
            maxPainAnalysis = maxPainAnalysis,
            compositeBullishScore = bullishScore
        )
    }

    // Recent Signal Alerts Feed
    fun getRecentSignals(): List<SignalAlertItem> {
        val now = System.currentTimeMillis()
        return listOf(
            SignalAlertItem(
                id = "sig_1",
                instrumentSymbol = "NIFTY 50",
                instrumentName = "Nifty 50 Index",
                timeframe = "Daily (1D)",
                type = SignalType.STRONG_BUY,
                price = 25190.00,
                rsi = 59.8,
                supertrend = 24980.00,
                stopLoss = 24980.00,
                target1 = 25420.00,
                target2 = 25600.00,
                timestamp = now - 18 * 60 * 1000,
                rationale = "Supertrend Green Uptrend confirmed on Daily Chart with RSI 59.8 holding above 50. Strong confluence with Gift Nifty premium and Put OI addition at 25,000 strike."
            ),
            SignalAlertItem(
                id = "sig_2",
                instrumentSymbol = "GIFT NIFTY",
                instrumentName = "GIFT Nifty Futures",
                timeframe = "Daily (1D)",
                type = SignalType.STRONG_BUY,
                price = 25260.00,
                rsi = 63.4,
                supertrend = 25050.00,
                stopLoss = 25050.00,
                target1 = 25500.00,
                target2 = 25720.00,
                timestamp = now - 45 * 60 * 1000,
                rationale = "Breakout above multi-day resistance band with rising volume. RSI momentum 63.4 breaking bullish band with 65-point premium over Nifty spot."
            ),
            SignalAlertItem(
                id = "sig_3",
                instrumentSymbol = "CRUDE OIL",
                instrumentName = "MCX Crude Oil",
                timeframe = "Daily (1D)",
                type = SignalType.SELL,
                price = 6010.00,
                rsi = 41.5,
                supertrend = 6090.00,
                stopLoss = 6090.00,
                target1 = 5880.00,
                target2 = 5790.00,
                timestamp = now - 120 * 60 * 1000,
                rationale = "Supertrend flipped to Bearish Red below ₹6,090. RSI dropped below 50 line with Short Buildup on MCX OI data."
            ),
            SignalAlertItem(
                id = "sig_4",
                instrumentSymbol = "BANK NIFTY",
                instrumentName = "Bank Nifty Index",
                timeframe = "1 Hour (1H)",
                type = SignalType.BUY,
                price = 52040.00,
                rsi = 56.2,
                supertrend = 51650.00,
                stopLoss = 51650.00,
                target1 = 52600.00,
                target2 = 53000.00,
                timestamp = now - 180 * 60 * 1000,
                rationale = "Hourly Supertrend bounce from ₹51,650 support. Private bank heavyweights showing Short Covering."
            )
        )
    }
}
