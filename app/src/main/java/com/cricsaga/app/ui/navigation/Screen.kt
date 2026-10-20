package com.cricsaga.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    // 5 Core Primary Bottom Bar Tabs
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Matches : Screen("matches", "Matches", Icons.Default.SportsCricket)
    object Discover : Screen("discover", "Discover", Icons.Default.Explore)
    object Tournaments : Screen("tournaments", "Tournaments", Icons.Default.EmojiEvents)
    object Profile : Screen("profile", "My Career", Icons.Default.Person)

    // Secondary / Nested Flow Screens
    object Teams : Screen("teams", "Teams", Icons.Default.Groups)
    object Redeem : Screen("redeem", "Redeem Pro", Icons.Default.Star)
    object MatchSetup : Screen("match_setup", "Match Setup")
    object OpenersSelection : Screen("openers_selection", "Select Openers")
    object LiveScoring : Screen("live_scoring", "Live Scoring")
    object FullScorecard : Screen("full_scorecard", "Scorecard")
}
