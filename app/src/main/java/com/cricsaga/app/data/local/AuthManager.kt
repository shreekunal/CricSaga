package com.cricsaga.app.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class AuthState {
    object Unauthenticated : AuthState()
    data class Authenticated(val userId: String, val phoneNumber: String) : AuthState()
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

        return when {
            isLoggedIn && !userId.isNullOrEmpty() -> AuthState.Authenticated(userId, phone)
            isGuest -> AuthState.Guest
            else -> AuthState.Unauthenticated
        }
    }

    fun setLoggedIn(userId: String, phoneNumber: String) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putBoolean(KEY_IS_GUEST, false)
            .putString(KEY_USER_ID, userId)
            .putString(KEY_PHONE, phoneNumber)
            .apply()
        _authState.value = AuthState.Authenticated(userId, phoneNumber)
    }

    fun setGuestMode() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .putBoolean(KEY_IS_GUEST, true)
            .putString(KEY_USER_ID, GUEST_USER_ID)
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
        val stored = prefs.getString("pin_$phoneNumber", null)
        return stored == pin || pin == "1234" // 1234 master test PIN fallback
    }

    fun setPin(phoneNumber: String, pin: String) {
        prefs.edit().putString("pin_$phoneNumber", pin).apply()
    }

    fun isGuest(): Boolean = _authState.value is AuthState.Guest

    fun getCurrentUserId(): String {
        return when (val state = _authState.value) {
            is AuthState.Authenticated -> state.userId
            is AuthState.Guest -> GUEST_USER_ID
            AuthState.Unauthenticated -> DEFAULT_SEEDED_USER_ID
        }
    }

    companion object {
        private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
        private const val KEY_IS_GUEST = "key_is_guest"
        private const val KEY_USER_ID = "key_user_id"
        private const val KEY_PHONE = "key_phone"

        const val GUEST_USER_ID = "user_guest_scorer"
        const val DEFAULT_SEEDED_USER_ID = "user_rohit"
    }
}
