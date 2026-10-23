package com.cricsaga.app.ui.redeem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricsaga.app.data.local.ProManager
import com.cricsaga.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RedeemCodeScreen(
    proManager: ProManager,
    onBackClick: () -> Unit = {}
) {
    val subscriptionState by proManager.subscriptionState.collectAsState()
    val scrollState = rememberScrollState()
    val hapticManager = com.cricsaga.app.ui.utils.LocalHapticManager.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pro Membership & Perks",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        hapticManager?.tapUiClick()
                        onBackClick()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Section 1: Hero Membership Status Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = if (subscriptionState.isActive) SlateSurfaceDark else MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    1.dp,
                    if (subscriptionState.isActive) BoundaryAmber.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                            .background(
                                if (subscriptionState.isActive) BoundaryAmber.copy(alpha = 0.15f)
                                else CrimsonPrimaryDark.copy(alpha = 0.12f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (subscriptionState.isActive) Icons.Default.WorkspacePremium else Icons.Default.SportsCricket,
                            contentDescription = null,
                            tint = if (subscriptionState.isActive) BoundaryAmber else CrimsonPrimaryDark,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (subscriptionState.isActive) "CRICSAGA PRO VERIFIED" else "GRASSROOTS ATHLETE TIER",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        letterSpacing = 0.5.sp,
                        color = if (subscriptionState.isActive) BoundaryAmber else MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (subscriptionState.isActive) {
                            "Verified Pro Athlete Access • Unlimited match analytics, official PDF exports, and golden broadcast badge enabled on your profile."
                        } else {
                            "Standard Community Athlete • Complete digital ball-by-ball scoring, squad rosters, and local match records are always free."
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    if (subscriptionState.isActive) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = BoundaryAmber.copy(alpha = 0.15f),
                            border = BorderStroke(0.5.dp, BoundaryAmber.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = BoundaryAmber, modifier = Modifier.size(14.dp))
                                Text(
                                    text = "ADMIN VERIFIED",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BoundaryAmber
                                )
                            }
                        }
                    }
                }
            }

            // Section 2: What's Included in Pro
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "CRICSAGA PRO CAPABILITIES",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = BoundaryAmber
                    )

                    ProPerkRow(
                        icon = Icons.Default.Block,
                        title = "100% Ad-Free Ground Scoring",
                        subtitle = "Clean, uninterrupted ball-by-ball scoring during intense matches."
                    )
                    ProPerkRow(
                        icon = Icons.Default.PictureAsPdf,
                        title = "Official Match PDF Scorecards",
                        subtitle = "Generate and share print-ready A4 scorecards for leagues and tournaments."
                    )
                    ProPerkRow(
                        icon = Icons.Default.ShowChart,
                        title = "Manhattan & Worm Analytics",
                        subtitle = "Broadcast-style run-rate wagon graphs and fall-of-wickets comparisons."
                    )
                    ProPerkRow(
                        icon = Icons.Default.Verified,
                        title = "Verified Athlete Golden Badge",
                        subtitle = "Prominent verification mark on your player profile and match lineups."
                    )
                    ProPerkRow(
                        icon = Icons.Default.Groups,
                        title = "Unlimited Squads & Tournaments",
                        subtitle = "Create and manage multiple club rosters across local turf circuits."
                    )
                    ProPerkRow(
                        icon = Icons.Default.Leaderboard,
                        title = "Priority Leaderboard Inclusion",
                        subtitle = "Top batter, bowler, and MVP standings across regional cricket rankings."
                    )
                }
            }

            // Section 3: Admin-Managed Policy Notice
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = BoundaryAmber,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Admin-Managed Pro Access",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "CricSaga Pro access is assigned directly by tournament coordinators and authorized CricSaga administrators. There are no paid in-app upgrades or recurring subscriptions.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ProPerkRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(BoundaryAmber.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BoundaryAmber,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 15.sp
            )
        }
    }
}
