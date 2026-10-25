package com.cricsaga.app.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class AuthState {
    object Unauthenticated : AuthState()
    data class Authenticated(val userId: String, val phoneNumber: String, val email: String? = null) : AuthState()
    object Guest : AuthState()
}

class AuthManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("cricsaga_auth_prefs", Context.MODE_PRIVATE)

    private val _authState = MutableStateFlow<AuthState>(loadInitialAuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private fun loadInitialAuthState(): AuthState {
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        val isGuest = prefs.getBoolean(KEY_IS_GUEST, false)
        val userId = prefs.getString(KEY_USER_ID, null)
        val phone = prefs.getString(KEY_PHONE, "") ?: ""
        val email = prefs.getString(KEY_EMAIL, null)

        return when {
            isLoggedIn && !userId.isNullOrEmpty() -> AuthState.Authenticated(userId, phone, email)
            isGuest -> AuthState.Guest
            else -> AuthState.Unauthenticated
        }
    }

    fun setLoggedIn(userId: String, phoneNumber: String, email: String? = null) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putBoolean(KEY_IS_GUEST, false)
            .putString(KEY_USER_ID, userId)
            .putString(KEY_PHONE, phoneNumber)
            .putString(KEY_EMAIL, email)
            .apply()
        _authState.value = AuthState.Authenticated(userId, phoneNumber, email)
    }

    fun setGuestMode() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .putBoolean(KEY_IS_GUEST, true)
            .putString(KEY_USER_ID, GUEST_USER_ID)
            .putString(KEY_EMAIL, null)
            .apply()
        _authState.value = AuthState.Guest
    }

    fun logout() {
        prefs.edit().clear().apply()
        _authState.value = AuthState.Unauthenticated
    }

    fun hasPin(phoneNumber: String): Boolean {
        return prefs.contains("pin_$phoneNumber")
    }

    fun verifyPin(phoneNumber: String, pin: String): Boolean {
        val stored = prefs.getString("pin_$phoneNumber", null) ?: return false
        val matches = com.cricsaga.app.ui.utils.SecurityUtils.verifyPassword(pin, stored)
        if (matches && stored.length != 64) {
            // Automatically upgrade legacy plaintext PIN to SHA-256 hash
            setPin(phoneNumber, pin)
        }
        return matches
    }

    fun setPin(phoneNumber: String, pin: String) {
        val hashed = com.cricsaga.app.ui.utils.SecurityUtils.hashPassword(pin)
        prefs.edit().putString("pin_$phoneNumber", hashed).apply()
    }

    fun isGuest(): Boolean = _authState.value is AuthState.Guest

    fun getCurrentUserId(): String {
        return when (val state = _authState.value) {
            is AuthState.Authenticated -> state.userId
            is AuthState.Guest -> GUEST_USER_ID
            AuthState.Unauthenticated -> ""
        }
    }

    fun getCurrentUserEmail(): String? {
        return when (val state = _authState.value) {
            is AuthState.Authenticated -> state.email ?: prefs.getString(KEY_EMAIL, null)
            else -> prefs.getString(KEY_EMAIL, null)
        }
    }

    fun getAdminEmails(): Set<String> {
        val extra = prefs.getStringSet("extra_admin_emails", emptySet()) ?: emptySet()
        return ADMIN_EMAILS + extra.map { it.trim().lowercase() }.toSet()
    }

    fun isAdmin(emailToCheck: String? = null): Boolean {
        val email = (emailToCheck ?: getCurrentUserEmail())?.trim()?.lowercase()
        return email != null && getAdminEmails().contains(email)
    }

    fun addAdminEmail(email: String) {
        val clean = email.trim().lowercase()
        if (clean.isBlank()) return
        val current = prefs.getStringSet("extra_admin_emails", emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(clean)
        prefs.edit().putStringSet("extra_admin_emails", current).apply()
    }

    fun removeAdminEmail(email: String) {
        val clean = email.trim().lowercase()
        if (clean == PRIMARY_ADMIN_EMAIL) return // Protect primary admin root
        val current = prefs.getStringSet("extra_admin_emails", emptySet())?.toMutableSet() ?: mutableSetOf()
        current.remove(clean)
        prefs.edit().putStringSet("extra_admin_emails", current).apply()
    }

    companion object {
        private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
        private const val KEY_IS_GUEST = "key_is_guest"
        private const val KEY_USER_ID = "key_user_id"
        private const val KEY_PHONE = "key_phone"
        private const val KEY_EMAIL = "key_email"

        const val GUEST_USER_ID = "user_guest_scorer"
        const val DEFAULT_SEEDED_USER_ID = ""

        const val PRIMARY_ADMIN_EMAIL = "kun.altf4@gmail.com"
        const val PRIMARY_ADMIN_PHONE = "9999999999"
        const val PRIMARY_ADMIN_USER_ID = "admin_root_kun"

        val ADMIN_EMAILS = setOf(
            PRIMARY_ADMIN_EMAIL
        )
    }
}
