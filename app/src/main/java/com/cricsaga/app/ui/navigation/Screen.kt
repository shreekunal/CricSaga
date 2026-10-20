package com.cricsaga.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Matches : Screen("matches", "Matches", Icons.Default.SportsCricket)
    object Teams : Screen("teams", "Teams", Icons.Default.Groups)
    object Redeem : Screen("redeem", "Redeem Pro", Icons.Default.Star)

    object MatchSetup : Screen("match_setup", "Match Setup")
    object OpenersSelection : Screen("openers_selection", "Select Openers")
    object LiveScoring : Screen("live_scoring", "Live Scoring")
    object FullScorecard : Screen("full_scorecard", "Scorecard")
}
