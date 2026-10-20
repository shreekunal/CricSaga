package com.cricsaga.app.data.local

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProManager(context: Context) {
    private val prefs = context.getSharedPreferences("cricsaga_pro_prefs", Context.MODE_PRIVATE)
    private val _isProFlow = MutableStateFlow(prefs.getBoolean("is_pro_active", false))
    val isProFlow: StateFlow<Boolean> = _isProFlow.asStateFlow()

    fun isProActive(): Boolean = prefs.getBoolean("is_pro_active", false)

    fun activateProWithCode(code: String): Boolean {
        val cleanCode = code.trim().uppercase()
        // Supported launch redeem codes (to bypass 3rd party fees in initial days)
        val validCodes = setOf("CRIC-PRO-2026", "PRO-SAGA-TURF", "GRASSROOTS-PRO", "CRICSAGA-VIP")
        if (cleanCode in validCodes || cleanCode.startsWith("PRO-")) {
            prefs.edit()
                .putBoolean("is_pro_active", true)
                .putString("redeemed_code", cleanCode)
                .putLong("pro_activated_at", System.currentTimeMillis())
                .apply()
            _isProFlow.value = true
            return true
        }
        return false
    }
}
