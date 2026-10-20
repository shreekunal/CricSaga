package com.cricsaga.app.ui.utils

import android.content.Context
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

val LocalHapticManager = staticCompositionLocalOf<HapticManager?> { null }

/**
 * Tactile Haptic Feedback Engine — Phase 5.3
 *
 * Provides instant thumb confirmation for pitch scorers and tactile UI feedback
 * across all screen touches and buttons.
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
     * Crisp tactile tap for Dot Ball / Singles (0, 1, 2, 3 runs)
     */
    fun tapDotOrSingle() {
        if (!_isEnabled.value || vibrator?.hasVibrator() != true) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        } catch (_: Exception) {
            vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    /**
     * Rapid double pulse for Boundary (4 runs): 45ms - 50ms gap - 45ms
     */
    fun tapFour() {
        if (!_isEnabled.value || vibrator?.hasVibrator() != true) return
        val timings = longArrayOf(0, 45, 50, 45)
        val amplitudes = intArrayOf(0, 220, 0, 255)
        vibrateWaveform(timings, amplitudes)
    }

    /**
     * Deep double punch for Maximum (6 runs): 60ms - 55ms gap - 65ms
     */
    fun tapSix() {
        if (!_isEnabled.value || vibrator?.hasVibrator() != true) return
        val timings = longArrayOf(0, 60, 55, 65)
        val amplitudes = intArrayOf(0, 255, 0, 255)
        vibrateWaveform(timings, amplitudes)
    }

    /**
     * Sharp warning pulse (100ms firm buzz) for Fall of Wicket
     */
    fun tapWicket() {
        if (!_isEnabled.value || vibrator?.hasVibrator() != true) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
            } else {
                vibrate(VibrationEffect.createOneShot(110, 255))
            }
        } catch (_: Exception) {
            vibrate(VibrationEffect.createOneShot(110, 255))
        }
    }

    /**
     * Triple warning buzz for Undo Action: 35ms - 35ms - 35ms - 35ms - 45ms
     */
    fun tapUndo() {
        if (!_isEnabled.value || vibrator?.hasVibrator() != true) return
        val timings = longArrayOf(0, 35, 35, 35, 35, 45)
        val amplitudes = intArrayOf(0, 200, 0, 200, 0, 255)
        vibrateWaveform(timings, amplitudes)
    }

    /**
     * Rhythmic pulse for Over Completion: 45ms - 50ms - 45ms
     */
    fun tapOverComplete() {
        if (!_isEnabled.value || vibrator?.hasVibrator() != true) return
        val timings = longArrayOf(0, 45, 50, 45)
        val amplitudes = intArrayOf(0, 220, 0, 240)
        vibrateWaveform(timings, amplitudes)
    }

    /**
     * Tactile UI click for button touches, tabs, chips, and navigations.
     */
    fun tapUiClick() {
        if (!_isEnabled.value || vibrator?.hasVibrator() != true) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        } catch (_: Exception) {
            vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    private fun vibrate(effect: VibrationEffect) {
        try {
            vibrator?.vibrate(effect)
        } catch (_: Exception) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val attrs = VibrationAttributes.Builder()
                        .setUsage(VibrationAttributes.USAGE_TOUCH)
                        .build()
                    vibrator?.vibrate(effect, attrs)
                }
            } catch (_: Exception) {}
        }
    }

    private fun vibrateWaveform(timings: LongArray, amplitudes: IntArray) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = try {
                    if (vibrator?.hasAmplitudeControl() == true) {
                        VibrationEffect.createWaveform(timings, amplitudes, -1)
                    } else {
                        VibrationEffect.createWaveform(timings, -1)
                    }
                } catch (_: Exception) {
                    VibrationEffect.createWaveform(timings, -1)
                }
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(timings, -1)
            }
        } catch (_: Exception) {
            try {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(timings, -1)
            } catch (_: Exception) {}
        }
    }
}
