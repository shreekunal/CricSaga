package com.cricsaga.app.data.local

import android.content.Context
import com.cricsaga.app.data.local.entity.RedeemCodeEntity
import com.cricsaga.app.data.remote.SupabaseClient
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

    fun syncUserProStatus(isPro: Boolean, planName: String? = null, expiresAt: Long? = null) {
        if (isPro) {
            val currentState = loadSubscriptionState()
            val resolvedPlan = planName?.let { runCatching { ProPlan.valueOf(it) }.getOrNull() }
                ?: (if (currentState.isActive && currentState.plan != null) currentState.plan else ProPlan.LIFETIME)
            val resolvedExpiresAt = expiresAt
                ?: (if (currentState.isActive && currentState.expiresAt > 0) currentState.expiresAt else (if (resolvedPlan == ProPlan.LIFETIME) Long.MAX_VALUE else System.currentTimeMillis() + 30L * 24 * 3600 * 1000))

            val now = System.currentTimeMillis()
            prefs.edit()
                .putBoolean("is_pro_active", true)
                .putString("pro_plan", resolvedPlan.name)
                .putString("redeemed_code", if (currentState.redeemedCode.isNotEmpty()) currentState.redeemedCode else "ADMIN_VERIFIED")
                .putLong("pro_activated_at", if (currentState.activatedAt > 0) currentState.activatedAt else now)
                .putLong("pro_expires_at", resolvedExpiresAt)
                .apply()

            _subscriptionState.value = ProSubscriptionState(
                isActive = true,
                plan = resolvedPlan,
                redeemedCode = if (currentState.redeemedCode.isNotEmpty()) currentState.redeemedCode else "ADMIN_VERIFIED",
                activatedAt = if (currentState.activatedAt > 0) currentState.activatedAt else now,
                expiresAt = resolvedExpiresAt
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
        // No hardcoded promotional codes seeded into local client SQLite.
        // Redeem codes are validated and provisioned securely via remote admin backend.
    }

    suspend fun activateProWithCode(code: String, userId: String = "user_me"): RedeemResult = withContext(Dispatchers.IO) {
        val cleanCode = code.trim().uppercase()
        if (cleanCode.isEmpty()) {
            return@withContext RedeemResult(false, null, "Please enter a redeem code.")
        }

        // Query the dedicated redeem_codes table
        var codeEntity = redeemCodeDao.getCode(cleanCode)

        if (codeEntity == null) {
            // Check cloud redeem_codes if not present locally
            try {
                val cloudRes = com.cricsaga.app.data.remote.SupabaseClient.redeemCode(cleanCode, userId)
                val cloudEntity = cloudRes.getOrNull()
                if (cloudEntity != null) {
                    redeemCodeDao.insertCode(cloudEntity)
                    codeEntity = cloudEntity
                }
            } catch (_: Exception) {}
        }

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
        val cleanCode = code.trim().uppercase()
        val entity = RedeemCodeEntity(
            code = cleanCode,
            plan = plan,
            durationDays = durationDays
        )
        redeemCodeDao.insertCode(entity)
        SupabaseClient.createRemoteRedeemCode(cleanCode, plan.name, durationDays)
    }

    suspend fun deleteRedeemCode(code: String) = withContext(Dispatchers.IO) {
        val cleanCode = code.trim().uppercase()
        redeemCodeDao.deleteCode(cleanCode)
        SupabaseClient.deleteRemoteRedeemCode(cleanCode)
    }
}
