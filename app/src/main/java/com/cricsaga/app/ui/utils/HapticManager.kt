package com.cricsaga.app.ui.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Tactile Haptic Feedback Engine — Phase 5.3
 *
 * Provides instant thumb confirmation for pitch scorers without requiring
 * continuous visual eye-contact with the screen during live overs.
 */
class HapticManager(context: Context) {

    private val prefs = context.getSharedPreferences("cricsaga_haptics", Context.MODE_PRIVATE)
    private val _isEnabled = MutableStateFlow(prefs.getBoolean("haptics_enabled", true))
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun setHapticsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("haptics_enabled", enabled).apply()
        _isEnabled.value = enabled
    }

    fun toggleHaptics() {
        setHapticsEnabled(!_isEnabled.value)
    }

    /**
     * Crisp micro-tap (12ms) for Dot Ball / Singles (0, 1, 2, 3 runs)
     */
    fun tapDotOrSingle() {
        if (!_isEnabled.value || vibrator?.hasVibrator() != true) return
        vibrate(VibrationEffect.createOneShot(12, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    /**
     * Rapid double pulse for Boundary (4 runs): 15ms - 30ms gap - 15ms
     */
    fun tapFour() {
        if (!_isEnabled.value || vibrator?.hasVibrator() != true) return
        val timings = longArrayOf(0, 15, 30, 15)
        val amplitudes = intArrayOf(0, 200, 0, 255)
        vibrateWaveform(timings, amplitudes)
    }

    /**
     * Deep double punch for Maximum (6 runs): 25ms - 40ms gap - 25ms
     */
    fun tapSix() {
        if (!_isEnabled.value || vibrator?.hasVibrator() != true) return
        val timings = longArrayOf(0, 25, 40, 25)
        val amplitudes = intArrayOf(0, 255, 0, 255)
        vibrateWaveform(timings, amplitudes)
    }

    /**
     * Sharp warning pulse (75ms firm buzz) for Fall of Wicket
     */
    fun tapWicket() {
        if (!_isEnabled.value || vibrator?.hasVibrator() != true) return
        vibrate(VibrationEffect.createOneShot(75, 255))
    }

    /**
     * Triple warning buzz for Undo Action: 15ms - 20ms - 15ms - 20ms - 15ms
     */
    fun tapUndo() {
        if (!_isEnabled.value || vibrator?.hasVibrator() != true) return
        val timings = longArrayOf(0, 15, 20, 15, 20, 15)
        val amplitudes = intArrayOf(0, 180, 0, 180, 0, 220)
        vibrateWaveform(timings, amplitudes)
    }

    /**
     * Rhythmic pulse for Over Completion: 30ms - 40ms - 30ms
     */
    fun tapOverComplete() {
        if (!_isEnabled.value || vibrator?.hasVibrator() != true) return
        val timings = longArrayOf(0, 30, 40, 30)
        val amplitudes = intArrayOf(0, 200, 0, 200)
        vibrateWaveform(timings, amplitudes)
    }

    /**
     * Generic light UI click (8ms)
     */
    fun tapUiClick() {
        if (!_isEnabled.value || vibrator?.hasVibrator() != true) return
        vibrate(VibrationEffect.createOneShot(8, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun vibrate(effect: VibrationEffect) {
        try {
            vibrator?.vibrate(effect)
        } catch (_: Exception) {}
    }

    private fun vibrateWaveform(timings: LongArray, amplitudes: IntArray) {
        try {
            val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
            vibrator?.vibrate(effect)
        } catch (_: Exception) {
            try {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(timings, -1)
            } catch (_: Exception) {}
        }
    }
}
