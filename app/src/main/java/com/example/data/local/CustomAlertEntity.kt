package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_alerts")
data class CustomAlertEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val symbol: String,
    val alertType: String, // "RSI_BUY", "SUPERTREND_BUY", "PRICE_CROSS_UP", "PRICE_CROSS_DOWN", "PCR_SPIKE"
    val triggerCondition: String, // e.g., "RSI > 60 & Supertrend Bullish" or "Price >= 25,300"
    val thresholdPrice: Double = 0.0,
    val isActive: Boolean = true,
    val isTriggered: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val triggeredAt: Long? = null,
    val notes: String = ""
)
