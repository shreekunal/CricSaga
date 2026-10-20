package com.cricsaga.app.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricsaga.app.R
import com.cricsaga.app.ui.theme.*

// Demo accounts for instant 1-tap testing on physical device / ground
data class QuickSignAccount(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val team: String
)

@Composable
fun PhoneLoginScreen(
    onGoogleSignIn: (email: String, displayName: String) -> Unit = { _, _ -> },
    onContinueAsGuest: () -> Unit,
    onPhoneVerified: (phoneNumber: String) -> Unit = {}
) {
    var showAccountPicker by remember { mutableStateOf(false) }

    val presetAccounts = listOf(
        QuickSignAccount(
            id = "user_rohit",
            name = "Rohit Sharma",
            email = "rohit.sharma@cricsaga.app",
            role = "Opening Batsman",
            team = "Warriors CC"
        ),
        QuickSignAccount(
            id = "user_aman",
            name = "Aman Sharma",
            email = "aman.cricketer@cricsaga.app",
            role = "All-Rounder",
            team = "Metro Strikers"
        ),
        QuickSignAccount(
            id = "user_jasprit",
            name = "Jasprit Khan",
            email = "jasprit.khan@cricsaga.app",
            role = "Fast Bowler",
            team = "Royal Tigers"
        )
    )

    Scaffold(
        containerColor = CharcoalBackgroundDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Brand Emblem & Identity
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(28.dp))

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(SlateSurfaceDark)
                        .border(1.dp, CardBorderDark, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher),
                        contentDescription = "CricSaga",
                        modifier = Modifier.size(50.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "CRICSAGA",
                    style = ScoreMedium,
                    letterSpacing = 2.sp,
                    color = TextPrimary
                )

                Text(
                    text = "HAR RUN KI KAHANI",
                    style = SectionLabel,
                    color = CrimsonPrimaryDark,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Text(
                    text = "Broadcast-Grade Grassroots Cricket",
                    style = BodyCaption,
                    color = TextTertiary,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Sign-In Card Container
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Sign In to CricSaga",
                            style = PlayerNamePrimary,
                            color = TextPrimary
                        )

                        Text(
                            text = "Track your career stats, sync match scorecards, and build your digital player card with zero fees.",
                            style = BodyCaption,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                        )

                        // 1. Google 1-Tap Sign-In Button
                        Button(
                            onClick = {
                                showAccountPicker = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF1F1F1F)
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_google_logo),
                                    contentDescription = "Google",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Continue with Google",
                                    style = CtaLabel,
                                    color = Color(0xFF1F1F1F)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Divider with text
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = DividerDark,
                                thickness = 0.5.dp
                            )
                            Text(
                                text = "OR",
                                style = MicroTag,
                                color = TextTertiary,
                                modifier = Modifier.padding(horizontal = 10.dp)
                            )
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = DividerDark,
                                thickness = 0.5.dp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 2. Instant Pitch Mode (Guest Scorer)
                        OutlinedButton(
                            onClick = onContinueAsGuest,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = TextPrimary
                            ),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark)
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SportsCricket,
                                    contentDescription = null,
                                    tint = BoundaryAmber,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Continue as Guest Scorer",
                                    style = CtaLabel,
                                    color = TextPrimary
                                )
                            }
                        }

                        Text(
                            text = "Instant pitch mode with full offline scoring. No sign-in needed.",
                            style = BodyCaption,
                            color = TextTertiary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            // Bottom Section: Terms & Conditions
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "By continuing, you agree to CricSaga Grassroots Terms of Play & Privacy Policy",
                    style = BodyCaption,
                    color = TextTertiary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
        }

        // Google Account Selector Sheet
        if (showAccountPicker) {
            AlertDialog(
                onDismissRequest = { showAccountPicker = false },
                containerColor = SlateSurfaceDark,
                shape = RoundedCornerShape(14.dp),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_google_logo),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Choose an account",
                                style = PlayerNamePrimary,
                                color = TextPrimary
                            )
                            Text(
                                text = "to continue to CricSaga",
                                style = BodyCaption,
                                color = TextSecondary
                            )
                        }
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presetAccounts.forEach { acc ->
                            Surface(
                                onClick = {
                                    showAccountPicker = false
                                    onGoogleSignIn(acc.email, acc.name)
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = ElevatedSurfaceDark,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(CrimsonPrimaryDark),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = acc.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.joinToString("").take(2),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = acc.name,
                                            style = PlayerNameSecondary,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "${acc.email} • ${acc.team}",
                                            style = BodyCaption,
                                            color = TextTertiary
                                        )
                                    }
                                }
                            }
                        }

                        // Add new account option
                        Surface(
                            onClick = {
                                showAccountPicker = false
                                onGoogleSignIn("player.cricket@cricsaga.app", "Grassroots Player")
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = SlateSurfaceDark,
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Add another account",
                                    style = PlayerNameSecondary,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showAccountPicker = false }) {
                        Text(
                            text = "CANCEL",
                            style = MicroTag,
                            color = TextSecondary
                        )
                    }
                }
            )
        }
    }
}
