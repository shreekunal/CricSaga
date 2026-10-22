package com.cricsaga.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    // 5 Core Primary Bottom Bar Tabs
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Matches : Screen("matches", "Matches", Icons.Default.SportsCricket)
    object Discover : Screen("discover", "Discover", Icons.Default.Explore)
    object Rankings : Screen("rankings", "Rankings", Icons.Default.EmojiEvents)
    object Profile : Screen("profile", "My Career", Icons.Default.Person)

    // Authentication & Onboarding
    object PhoneLogin : Screen("phone_login", "Sign In")
    object ProfileSetup : Screen("profile_setup", "Create Profile")

    // Secondary / Direct Flow Screens
    object Messages : Screen("messages", "Messages", Icons.AutoMirrored.Filled.Chat)
    object Chat : Screen("chat/{userId}", "Direct Chat")
    object Teams : Screen("teams", "Teams", Icons.Default.Groups)
    object TeamDetail : Screen("team_detail", "Team Hub")
    object PlayerDetail : Screen("player_detail", "Player Profile")
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object Redeem : Screen("redeem", "Redeem Pro", Icons.Default.Star)
    object MatchSetup : Screen("match_setup", "Match Setup")
    object OpenersSelection : Screen("openers_selection", "Select Openers")
    object LiveScoring : Screen("live_scoring", "Live Scoring")
    object FullScorecard : Screen("full_scorecard", "Scorecard")
}
