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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RedeemCodeScreen(
    proManager: ProManager,
    onBackClick: () -> Unit = {}
) {
    val subscriptionState by proManager.subscriptionState.collectAsState()
    val scrollState = rememberScrollState()
    val hapticManager = com.cricsaga.app.ui.utils.LocalHapticManager.current
    val coroutineScope = rememberCoroutineScope()

    var redeemCodeInput by remember { mutableStateOf("") }
    var isRedeeming by remember { mutableStateOf(false) }
    var redeemFeedback by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

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

            // Section 2: Available Pro Plans
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "AVAILABLE MEMBERSHIP TIERS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = BoundaryAmber
                )

                // 1 Month Plan (₹99)
                ProPlanCard(
                    title = "1 Month Access",
                    price = "₹99",
                    duration = "30 Days Access",
                    rateDetail = "₹3.30/day",
                    badge = null,
                    isHighlighted = false,
                    isCurrent = subscriptionState.isActive && subscriptionState.plan == ProPlan.ONE_MONTH
                )

                // 1 Year Plan (₹199 - Best Value)
                ProPlanCard(
                    title = "1 Year Pass",
                    price = "₹199",
                    duration = "365 Days Access",
                    rateDetail = "Save 83% vs Monthly",
                    badge = "BEST VALUE • 83% OFF",
                    isHighlighted = true,
                    isCurrent = subscriptionState.isActive && subscriptionState.plan == ProPlan.ONE_YEAR
                )

                // Lifetime Plan (₹1,999)
                ProPlanCard(
                    title = "Lifetime VIP Pass",
                    price = "₹1,999",
                    duration = "Unlimited Forever",
                    rateDetail = "Founder Status & All Future Features",
                    badge = "FOUNDER VIP",
                    isHighlighted = false,
                    isCurrent = subscriptionState.isActive && subscriptionState.plan == ProPlan.LIFETIME
                )
            }

            // Section 3: Redeem Tournament / Coordinator Code
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.ConfirmationNumber, contentDescription = null, tint = BoundaryAmber, modifier = Modifier.size(20.dp))
                        Text(
                            text = "Redeem Access Pass Code",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Enter a code provided by your tournament organizer, league coordinator, or CricSaga administrator.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )

                    OutlinedTextField(
                        value = redeemCodeInput,
                        onValueChange = {
                            redeemCodeInput = it.uppercase()
                            redeemFeedback = null
                        },
                        placeholder = { Text("e.g. CRIC-PRO-2026", fontSize = 12.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            val code = redeemCodeInput.trim()
                            if (code.isBlank()) return@Button
                            isRedeeming = true
                            hapticManager?.tapUiClick()
                            coroutineScope.launch {
                                val result = proManager.activateProWithCode(code)
                                isRedeeming = false
                                if (result.isSuccess) {
                                    hapticManager?.tapBoundaryFour()
                                    redeemFeedback = true to (result.message.ifBlank { "Pro Access Activated!" })
                                    redeemCodeInput = ""
                                } else {
                                    hapticManager?.tapWicket()
                                    redeemFeedback = false to result.message
                                }
                            }
                        },
                        enabled = redeemCodeInput.isNotBlank() && !isRedeeming,
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimaryDark),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isRedeeming) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("REDEEM CODE", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                        }
                    }

                    redeemFeedback?.let { (success, message) ->
                        Text(
                            text = message,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (success) ForestSixPrimary else DismissalRed,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Section 4: What's Included in Pro
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

            // Section 5: Admin-Managed Policy Notice
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
private fun ProPlanCard(
    title: String,
    price: String,
    duration: String,
    rateDetail: String,
    badge: String?,
    isHighlighted: Boolean,
    isCurrent: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = if (isHighlighted) SlateSurfaceDark else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            if (isHighlighted || isCurrent) 1.5.dp else 1.dp,
            if (isCurrent) ForestSixPrimary else if (isHighlighted) BoundaryAmber else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (badge != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = BoundaryAmber.copy(alpha = 0.15f),
                            border = BorderStroke(0.5.dp, BoundaryAmber)
                        ) {
                            Text(
                                text = badge,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = BoundaryAmber,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = price,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = if (isHighlighted) BoundaryAmber else CrimsonPrimaryDark
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = duration,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = rateDetail,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isHighlighted) BoundaryAmber.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isCurrent) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = ForestSixPrimary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "CURRENT ACTIVE TIER",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestSixPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
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
