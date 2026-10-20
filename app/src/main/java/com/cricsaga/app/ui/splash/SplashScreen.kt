package com.cricsaga.app.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricsaga.app.R
import com.cricsaga.app.ui.theme.CharcoalBackgroundDark
import com.cricsaga.app.ui.theme.CrimsonPrimaryDark
import com.cricsaga.app.ui.theme.TextPrimary
import com.cricsaga.app.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onAnimationFinish: () -> Unit
) {
    // 1.5 Second (1500ms) Smooth Native Animation Sequence
    val transitionState = remember { MutableTransitionState(false) }

    LaunchedEffect(Unit) {
        transitionState.targetState = true
        delay(1500) // Exactly 1.5 seconds
        onAnimationFinish()
    }

    val transition = updateTransition(transitionState, label = "SplashTransition")

    // Logo scale: smooth spring from 0.82f to 1.0f
    val logoScale by transition.animateFloat(
        transitionSpec = {
            tween(durationMillis = 700, easing = FastOutSlowInEasing)
        },
        label = "LogoScale"
    ) { state ->
        if (state) 1f else 0.82f
    }

    // Logo alpha: smooth fade in 0f to 1f
    val logoAlpha by transition.animateFloat(
        transitionSpec = {
            tween(durationMillis = 600, easing = LinearOutSlowInEasing)
        },
        label = "LogoAlpha"
    ) { state ->
        if (state) 1f else 0f
    }

    // Logo horizontal slide offset (simulating the speed ball entering from the left)
    val logoOffsetX by transition.animateFloat(
        transitionSpec = {
            tween(durationMillis = 700, easing = FastOutSlowInEasing)
        },
        label = "LogoOffsetX"
    ) { state ->
        if (state) 0f else -30f
    }

    // Brand text alpha: fades in smoothly after 300ms
    val textAlpha by transition.animateFloat(
        transitionSpec = {
            tween(durationMillis = 600, delayMillis = 300, easing = FastOutSlowInEasing)
        },
        label = "TextAlpha"
    ) { state ->
        if (state) 1f else 0f
    }

    // Tagline alpha: fades in quietly after 500ms
    val taglineAlpha by transition.animateFloat(
        transitionSpec = {
            tween(durationMillis = 500, delayMillis = 500, easing = FastOutSlowInEasing)
        },
        label = "TaglineAlpha"
    ) { state ->
        if (state) 1f else 0f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CharcoalBackgroundDark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Speed Ball Logo
            Image(
                painter = painterResource(id = R.drawable.cricsaga_logo),
                contentDescription = "CricSaga",
                modifier = Modifier
                    .size(92.dp)
                    .offset(x = logoOffsetX.dp)
                    .scale(logoScale)
                    .alpha(logoAlpha)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Clean Brand Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.alpha(textAlpha)
            ) {
                Text(
                    text = "CRIC",
                    fontWeight = FontWeight.Black,
                    fontSize = 26.sp,
                    letterSpacing = 1.sp,
                    color = CrimsonPrimaryDark
                )
                Text(
                    text = "SAGA",
                    fontWeight = FontWeight.Black,
                    fontSize = 26.sp,
                    letterSpacing = 1.sp,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Subdued Tagline
            Text(
                text = "HAR RUN KI KAHANI",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp,
                modifier = Modifier.alpha(taglineAlpha)
            )
        }
    }
}
