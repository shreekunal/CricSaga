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
import com.cricsaga.app.data.local.ProPlan
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
                        text = "CricSaga Pro Membership",
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
                        text = if (subscriptionState.isActive) "CRICSAGA PRO ACTIVE" else "GRASSROOTS ATHLETE TIER",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        letterSpacing = 0.5.sp,
                        color = if (subscriptionState.isActive) BoundaryAmber else MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (subscriptionState.isActive) {
                            "Verified Pro Athlete Access • Plan: ${subscriptionState.plan?.title ?: "Pro"} • Status: ${subscriptionState.formattedExpiry}"
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
                                    text = "VERIFIED ATHLETE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BoundaryAmber
                                )
                            }
                        }
                    }
                }
            }

            // Section 2: Available Membership Tiers (Vertical Tiles)
            Text(
                text = "AVAILABLE MEMBERSHIP TIERS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = BoundaryAmber,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            )

            // Vertical Tile 1: 1 Month Plan (₹99)
            ProPlanVerticalTile(
                title = "1 Month Access",
                price = "₹99",
                priceUnit = "/ month",
                duration = "30 Days Full Access",
                rateDetail = "₹3.30 / day • Great for tournaments",
                badge = "POPULAR STARTER",
                isHighlighted = false,
                isCurrent = subscriptionState.isActive && subscriptionState.plan == ProPlan.ONE_MONTH,
                features = listOf(
                    "100% Ad-Free Ground Scoring",
                    "Print-Ready Match PDF Scorecards",
                    "Unlimited Match Roster Management",
                    "Full Ball-by-Ball Match Sync"
                )
            )

            // Vertical Tile 2: 1 Year Plan (₹199 - Best Value)
            ProPlanVerticalTile(
                title = "1 Year Pass",
                price = "₹199",
                priceUnit = "/ year",
                duration = "365 Days Access",
                rateDetail = "Save 83% • Only ₹16.50 / month",
                badge = "BEST VALUE • 83% OFF",
                isHighlighted = true,
                isCurrent = subscriptionState.isActive && subscriptionState.plan == ProPlan.ONE_YEAR,
                features = listOf(
                    "Everything in Monthly Access",
                    "Broadcast Manhattan & Worm Analytics",
                    "Golden Verified Badge on Lineups & Profile",
                    "Priority Regional Leaderboard Standings",
                    "Unlimited Club Squads & Tournaments"
                )
            )

            // Vertical Tile 3: Lifetime VIP Pass (₹1,999)
            ProPlanVerticalTile(
                title = "Lifetime VIP Pass",
                price = "₹1,999",
                priceUnit = "one-time",
                duration = "Unlimited Forever",
                rateDetail = "Founder Status • Never Pay Again",
                badge = "FOUNDER VIP",
                isHighlighted = false,
                isCurrent = subscriptionState.isActive && subscriptionState.plan == ProPlan.LIFETIME,
                features = listOf(
                    "All Pro & Broadcast Features Forever",
                    "VIP Founder Profile Badge",
                    "All Future AI Commentary & Video Highlights",
                    "Multi-Tournament Coordinator Tools",
                    "Direct Support from CricSaga Team"
                )
            )

            // Section 3: What's Included in Pro
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

            // Section 4: Admin-Managed Policy Notice
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
                            text = "CricSaga Pro access is managed and assigned directly by tournament coordinators and authorized CricSaga administrators. There are no paid in-app upgrades or recurring subscriptions.",
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
private fun ProPlanVerticalTile(
    title: String,
    price: String,
    priceUnit: String,
    duration: String,
    rateDetail: String,
    badge: String?,
    isHighlighted: Boolean,
    isCurrent: Boolean,
    features: List<String>
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (isHighlighted) SlateSurfaceDark else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            if (isCurrent) 2.dp else if (isHighlighted) 1.5.dp else 1.dp,
            if (isCurrent) ForestSixPrimary else if (isHighlighted) BoundaryAmber else MaterialTheme.colorScheme.outlineVariant
        ),
        shadowElevation = if (isHighlighted) 4.dp else 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header: Title and Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (badge != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isHighlighted) BoundaryAmber else BoundaryAmber.copy(alpha = 0.15f),
                        border = BorderStroke(0.5.dp, BoundaryAmber)
                    ) {
                        Text(
                            text = badge,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isHighlighted) Color(0xFF1E1E1E) else BoundaryAmber,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Price in large bold font
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = price,
                    fontWeight = FontWeight.Black,
                    fontSize = 32.sp,
                    color = if (isHighlighted) BoundaryAmber else CrimsonPrimaryDark
                )
                Text(
                    text = priceUnit,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            Text(
                text = "$duration • $rateDetail",
                fontSize = 11.sp,
                color = if (isHighlighted) BoundaryAmber.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Feature Bullet Points
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                features.forEach { feature ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isHighlighted) BoundaryAmber else ForestSixPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = feature,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }

            // Current Plan status chip if active
            if (isCurrent) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ForestSixPrimary.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, ForestSixPrimary.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = ForestSixPrimary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CURRENT ACTIVE MEMBERSHIP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestSixPrimary
                        )
                    }
                }
            }
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
