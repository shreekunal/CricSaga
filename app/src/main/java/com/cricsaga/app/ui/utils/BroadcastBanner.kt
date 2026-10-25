package com.cricsaga.app.ui.utils

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricsaga.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Locale
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BroadcastEvent(
    val tag: String,
    val title: String,
    val detail: String = "",
    val icon: ImageVector = Icons.Default.EmojiEvents,
    val accentColor: Color = DarkAccentFour
)

object BroadcastNotifier {
    private val _currentEvent = MutableStateFlow<BroadcastEvent?>(null)
    val currentEvent: StateFlow<BroadcastEvent?> = _currentEvent.asStateFlow()

    fun postMilestone(playerName: String, milestone: String, context: String = "") {
        _currentEvent.value = BroadcastEvent(
            tag = "MILESTONE",
            title = "$playerName reaches $milestone",
            detail = context,
            icon = Icons.Default.EmojiEvents,
            accentColor = DarkAccentFour
        )
    }

    fun postInningsBreak(target: Int, rrr: Double) {
        _currentEvent.value = BroadcastEvent(
            tag = "INNINGS BREAK",
            title = "Target Set: $target Runs",
            detail = "Required Run Rate: ${String.format(Locale.US, "%.2f", rrr)} per over",
            icon = Icons.Default.Info,
            accentColor = DarkBrandPrimary
        )
    }

    fun postResult(summary: String, potm: String? = null) {
        _currentEvent.value = BroadcastEvent(
            tag = "RESULT",
            title = summary,
            detail = if (potm != null) "Player of the Match: $potm" else "",
            icon = Icons.Default.EmojiEvents,
            accentColor = DarkAccentSix
        )
    }

    fun postRevert(message: String) {
        _currentEvent.value = BroadcastEvent(
            tag = "REVERTED",
            title = message,
            detail = "Scorecard adjusted",
            icon = Icons.AutoMirrored.Filled.Undo,
            accentColor = DarkAlertWicket
        )
    }

    fun dismiss() {
        _currentEvent.value = null
    }
}

/**
 * Floating High-Contrast Broadcast Toast Overlay Banner
 */
@Composable
fun BroadcastBannerHost(modifier: Modifier = Modifier) {
    val currentEvent by BroadcastNotifier.currentEvent.collectAsState()

    LaunchedEffect(currentEvent) {
        if (currentEvent != null) {
            delay(3000)
            BroadcastNotifier.dismiss()
        }
    }

    AnimatedVisibility(
        visible = currentEvent != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(tween(250)),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(tween(250)),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        currentEvent?.let { event ->
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SlateSurfaceDark,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, event.accentColor.copy(alpha = 0.6f), RoundedCornerShape(10.dp)),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(event.accentColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = event.icon,
                            contentDescription = null,
                            tint = event.accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = event.tag,
                                style = MicroTag,
                                color = event.accentColor
                            )
                        }
                        Text(
                            text = event.title,
                            style = PlayerNameSecondary,
                            color = TextPrimary
                        )
                        if (event.detail.isNotEmpty()) {
                            Text(
                                text = event.detail,
                                style = BodyCaption,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
