package com.cricsaga.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.cricsaga.app.data.local.AuthManager
import com.cricsaga.app.data.local.ProManager
import com.cricsaga.app.ui.navigation.AppNavigation
import com.cricsaga.app.ui.scoring.ScoringViewModel
import com.cricsaga.app.ui.splash.SplashScreen
import com.cricsaga.app.ui.theme.CricSagaTheme

class MainActivity : ComponentActivity() {

    private val viewModel: ScoringViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val proManager = ProManager(applicationContext)
        val authManager = AuthManager(applicationContext)
        setContent {
            CricSagaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var showSplash by remember { mutableStateOf(true) }

                    Crossfade(
                        targetState = showSplash,
                        animationSpec = tween(400),
                        label = "AppStartCrossfade"
                    ) { isSplash ->
                        if (isSplash) {
                            SplashScreen(
                                onAnimationFinish = { showSplash = false }
                            )
                        } else {
                            AppNavigation(
                                scoringViewModel = viewModel,
                                proManager = proManager,
                                authManager = authManager
                            )
                        }
                    }
                }
            }
        }
    }
}
