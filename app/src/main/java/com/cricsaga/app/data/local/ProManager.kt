package com.cricsaga.app.data.local

import android.content.Context
import com.cricsaga.app.data.local.entity.RedeemCodeEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ProPlan(val title: String, val price: String, val durationText: String, val isBestValue: Boolean = false) {
    ONE_MONTH("1 Month", "₹99", "30 Days Access"),
    ONE_YEAR("1 Year", "₹199", "365 Days Access", isBestValue = true),
    LIFETIME("Lifetime", "₹1999", "Unlimited Forever")
}

data class ProSubscriptionState(
    val isActive: Boolean = false,
    val plan: ProPlan? = null,
    val redeemedCode: String = "",
    val activatedAt: Long = 0L,
    val expiresAt: Long = 0L
) {
    val isLifetime: Boolean get() = plan == ProPlan.LIFETIME
    val formattedExpiry: String
        get() {
            if (isLifetime) return "Lifetime Access"
            if (expiresAt <= 0) return "Not active"
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            return sdf.format(Date(expiresAt))
        }
}

data class RedeemResult(
    val isSuccess: Boolean,
    val plan: ProPlan? = null,
    val message: String
)

class ProManager(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("cricsaga_pro_prefs", Context.MODE_PRIVATE)
    private val database = CricSagaDatabase.getDatabase(appContext)
    private val redeemCodeDao = database.redeemCodeDao()

    private val _subscriptionState = MutableStateFlow(loadSubscriptionState())
    val subscriptionState: StateFlow<ProSubscriptionState> = _subscriptionState.asStateFlow()

    val isProFlow: StateFlow<Boolean> = MutableStateFlow(isProActive())

    init {
        // Initialized with admin-managed Pro status
    }

    private fun loadSubscriptionState(): ProSubscriptionState {
        val isActive = prefs.getBoolean("is_pro_active", false)
        val planName = prefs.getString("pro_plan", null)
        val plan = planName?.let { runCatching { ProPlan.valueOf(it) }.getOrNull() }
        val code = prefs.getString("redeemed_code", "") ?: ""
        val activatedAt = prefs.getLong("pro_activated_at", 0L)
        val expiresAt = prefs.getLong("pro_expires_at", 0L)

        // Check expiry if not lifetime
        if (isActive && plan != ProPlan.LIFETIME && expiresAt > 0 && System.currentTimeMillis() > expiresAt) {
            return ProSubscriptionState(isActive = false)
        }

        return ProSubscriptionState(
            isActive = isActive,
            plan = plan,
            redeemedCode = code,
            activatedAt = activatedAt,
            expiresAt = expiresAt
        )
    }

    fun isProActive(): Boolean {
        val state = loadSubscriptionState()
        return state.isActive
    }

    fun syncUserProStatus(isPro: Boolean) {
        if (isPro) {
            val now = System.currentTimeMillis()
            prefs.edit()
                .putBoolean("is_pro_active", true)
                .putString("pro_plan", ProPlan.LIFETIME.name)
                .putString("redeemed_code", "ADMIN_VERIFIED")
                .putLong("pro_activated_at", now)
                .putLong("pro_expires_at", Long.MAX_VALUE)
                .apply()

            _subscriptionState.value = ProSubscriptionState(
                isActive = true,
                plan = ProPlan.LIFETIME,
                redeemedCode = "ADMIN_VERIFIED",
                activatedAt = now,
                expiresAt = Long.MAX_VALUE
            )
            (isProFlow as MutableStateFlow).value = true
        } else {
            deactivatePro()
        }
    }

    fun activatePlan(plan: ProPlan) {
        val now = System.currentTimeMillis()
        val expiresAt = when (plan) {
            ProPlan.ONE_MONTH -> now + (30L * 24 * 60 * 60 * 1000)
            ProPlan.ONE_YEAR -> now + (365L * 24 * 60 * 60 * 1000)
            ProPlan.LIFETIME -> Long.MAX_VALUE
        }

        prefs.edit()
            .putBoolean("is_pro_active", true)
            .putString("pro_plan", plan.name)
            .putString("redeemed_code", "DIRECT_PRO")
            .putLong("pro_activated_at", now)
            .putLong("pro_expires_at", expiresAt)
            .apply()

        val newState = ProSubscriptionState(
            isActive = true,
            plan = plan,
            redeemedCode = "DIRECT_PRO",
            activatedAt = now,
            expiresAt = expiresAt
        )
        _subscriptionState.value = newState
        (isProFlow as MutableStateFlow).value = true
    }

    fun promoteUserToPro(plan: ProPlan) {
        activatePlan(plan)
    }

    fun deactivatePro() {
        prefs.edit()
            .putBoolean("is_pro_active", false)
            .remove("pro_plan")
            .remove("redeemed_code")
            .remove("pro_activated_at")
            .remove("pro_expires_at")
            .apply()

        _subscriptionState.value = ProSubscriptionState(isActive = false)
        (isProFlow as MutableStateFlow).value = false
    }

    suspend fun seedDefaultCodesIfEmpty() = withContext(Dispatchers.IO) {
        if (redeemCodeDao.getCount() == 0) {
            val initialCodes = listOf(
                // 1 Month Tier (₹99)
                RedeemCodeEntity("MONTH-99-A1", ProPlan.ONE_MONTH, 30),
                RedeemCodeEntity("MONTH-99-B2", ProPlan.ONE_MONTH, 30),
                RedeemCodeEntity("CRIC-MONTH-2026", ProPlan.ONE_MONTH, 30),

                // 1 Year Tier (₹199)
                RedeemCodeEntity("ANNUAL-199-A1", ProPlan.ONE_YEAR, 365),
                RedeemCodeEntity("ANNUAL-199-B2", ProPlan.ONE_YEAR, 365),
                RedeemCodeEntity("CRIC-PRO-2026", ProPlan.ONE_YEAR, 365),
                RedeemCodeEntity("PRO-SAGA-TURF", ProPlan.ONE_YEAR, 365),

                // Lifetime VIP Tier (₹1,999)
                RedeemCodeEntity("VIP-1999-A1", ProPlan.LIFETIME, 36500),
                RedeemCodeEntity("CRICSAGA-VIP", ProPlan.LIFETIME, 36500),
                RedeemCodeEntity("FOUNDER-1999", ProPlan.LIFETIME, 36500)
            )
            redeemCodeDao.insertCodes(initialCodes)
        }
    }

    suspend fun activateProWithCode(code: String, userId: String = "user_me"): RedeemResult = withContext(Dispatchers.IO) {
        val cleanCode = code.trim().uppercase()
        if (cleanCode.isEmpty()) {
            return@withContext RedeemResult(false, null, "Please enter a redeem code.")
        }

        // Query the dedicated redeem_codes table
        val codeEntity = redeemCodeDao.getCode(cleanCode)

        if (codeEntity == null) {
            return@withContext RedeemResult(false, null, "Invalid redeem code. Please verify the code or contact support.")
        }

        // Enforce ONE-TIME use: verify if already redeemed
        if (codeEntity.isRedeemed) {
            val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            val dateStr = codeEntity.redeemedAt?.let { sdf.format(Date(it)) } ?: "previously"
            return@withContext RedeemResult(
                false,
                null,
                "This code ($cleanCode) was already used on $dateStr. Each redeem code can only be used once!"
            )
        }

        val now = System.currentTimeMillis()
        val rowsUpdated = redeemCodeDao.markCodeRedeemed(cleanCode, userId, now)
        if (rowsUpdated <= 0) {
            return@withContext RedeemResult(false, null, "Code could not be redeemed. It may have already been used.")
        }

        val plan = codeEntity.plan
        val expiresAt = when (plan) {
            ProPlan.ONE_MONTH -> now + (30L * 24 * 60 * 60 * 1000)
            ProPlan.ONE_YEAR -> now + (365L * 24 * 60 * 60 * 1000)
            ProPlan.LIFETIME -> Long.MAX_VALUE
        }

        prefs.edit()
            .putBoolean("is_pro_active", true)
            .putString("pro_plan", plan.name)
            .putString("redeemed_code", cleanCode)
            .putLong("pro_activated_at", now)
            .putLong("pro_expires_at", expiresAt)
            .apply()

        val newState = ProSubscriptionState(
            isActive = true,
            plan = plan,
            redeemedCode = cleanCode,
            activatedAt = now,
            expiresAt = expiresAt
        )
        _subscriptionState.value = newState
        (isProFlow as MutableStateFlow).value = true

        return@withContext RedeemResult(
            true,
            plan,
            "Success! CricSaga Pro (${plan.title}) is now ACTIVE on your account!"
        )
    }

    fun getAllRedeemCodes(): kotlinx.coroutines.flow.Flow<List<RedeemCodeEntity>> = redeemCodeDao.getAllCodes()

    suspend fun getAllRedeemCodesList(): List<RedeemCodeEntity> = withContext(Dispatchers.IO) {
        redeemCodeDao.getAllCodesList()
    }

    suspend fun createCustomRedeemCode(code: String, plan: ProPlan, durationDays: Int = 30) = withContext(Dispatchers.IO) {
        val entity = RedeemCodeEntity(
            code = code.trim().uppercase(),
            plan = plan,
            durationDays = durationDays
        )
        redeemCodeDao.insertCode(entity)
    }

    suspend fun deleteRedeemCode(code: String) = withContext(Dispatchers.IO) {
        redeemCodeDao.deleteCode(code)
    }
}
