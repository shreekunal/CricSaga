package com.cricsaga.app.ui.redeem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    var selectedPlan by remember { mutableStateOf(ProPlan.ONE_YEAR) }
    var actionMessage by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()
    val hapticManager = com.cricsaga.app.ui.utils.LocalHapticManager.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pro Membership",
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
                color = if (subscriptionState.isActive) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    1.dp,
                    if (subscriptionState.isActive) BoundaryAmber else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(if (subscriptionState.isActive) BoundaryAmber.copy(alpha = 0.15f) else CrimsonPrimaryDark.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (subscriptionState.isActive) Icons.Default.WorkspacePremium else Icons.Default.Star,
                            contentDescription = null,
                            tint = if (subscriptionState.isActive) BoundaryAmber else CrimsonPrimaryDark,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (subscriptionState.isActive) "CRICSAGA PRO ACTIVE" else "ELEVATE YOUR CRICKET",
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        letterSpacing = 0.5.sp,
                        color = if (subscriptionState.isActive) BoundaryAmber else MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (subscriptionState.isActive) {
                            "Plan: ${subscriptionState.plan?.title ?: "Pro"} • Valid: ${subscriptionState.formattedExpiry}"
                        } else {
                            "Unlock full match analytics, official PDF scorecards, and verified golden player badges."
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 17.sp
                    )

                    if (subscriptionState.isActive) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = {
                                hapticManager?.tapUiClick()
                                proManager.deactivatePro()
                                actionMessage = "Membership deactivated for testing."
                            },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Text("Switch to Free Tier", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Section 2: Three Pricing Plans
            Text(
                text = "SELECT MEMBERSHIP PLAN",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MembershipPlanCard(
                    plan = ProPlan.ONE_MONTH,
                    isSelected = selectedPlan == ProPlan.ONE_MONTH,
                    tag = null,
                    modifier = Modifier.weight(1f),
                    onSelect = {
                        hapticManager?.tapUiClick()
                        selectedPlan = ProPlan.ONE_MONTH
                    }
                )

                MembershipPlanCard(
                    plan = ProPlan.ONE_YEAR,
                    isSelected = selectedPlan == ProPlan.ONE_YEAR,
                    tag = "SAVE 83%",
                    modifier = Modifier.weight(1.1f),
                    onSelect = {
                        hapticManager?.tapUiClick()
                        selectedPlan = ProPlan.ONE_YEAR
                    }
                )

                MembershipPlanCard(
                    plan = ProPlan.LIFETIME,
                    isSelected = selectedPlan == ProPlan.LIFETIME,
                    tag = "VIP",
                    modifier = Modifier.weight(1f),
                    onSelect = {
                        hapticManager?.tapUiClick()
                        selectedPlan = ProPlan.LIFETIME
                    }
                )
            }

            // Section 3: Action Button
            Button(
                onClick = {
                    hapticManager?.tapBoundaryFour()
                    proManager.activatePlan(selectedPlan)
                    actionMessage = "Congratulations! ${selectedPlan.title} (${selectedPlan.price}) is now ACTIVE on your profile."
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimaryDark)
            ) {
                Text(
                    text = "UPGRADE TO ${selectedPlan.title.uppercase()} • ${selectedPlan.price}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White
                )
            }

            actionMessage?.let { msg ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BoundaryGreen.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, BoundaryGreen.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = msg,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BoundaryGreen,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Section 4: What You Get (Features Quad)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "ALL PLANS INCLUDE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    FeatureRow("100% Ad-Free Live Scoring on Ground")
                    FeatureRow("Unlimited Official PDF Scorecard Downloads")
                    FeatureRow("Worm Graphs & Manhattan Run-Rate Charts")
                    FeatureRow("Verified Golden Athlete Profile Badge")
                    FeatureRow("Unlimited Squad & Player Roster Creations")
                    FeatureRow("Priority Grassroots Leaderboard Rankings")
                }
            }

            // Section 5: Admin Dashboard Notice Card
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
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Admin Dashboard Integration",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Tournament organizers and authorized admins can promote any registered cricketer to Pro directly from the CricSaga Admin Dashboard.",
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
private fun MembershipPlanCard(
    plan: ProPlan,
    isSelected: Boolean,
    tag: String?,
    modifier: Modifier = Modifier,
    onSelect: () -> Unit
) {
    val borderColor = if (isSelected) CrimsonPrimaryDark else MaterialTheme.colorScheme.outlineVariant
    val containerColor = if (isSelected) CrimsonPrimaryDark.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface

    Surface(
        modifier = modifier.clickable(onClick = onSelect),
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            tag?.let {
                Surface(
                    color = if (plan.isBestValue) CrimsonPrimaryDark else BoundaryAmber,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = it,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            } ?: run {
                Spacer(modifier = Modifier.height(17.dp))
            }

            Text(
                text = plan.title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = plan.price,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                color = if (isSelected) CrimsonPrimaryDark else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = plan.durationText,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun FeatureRow(label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = BoundaryGreen,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}
