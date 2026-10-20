package com.cricsaga.app.ui.redeem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
fun RedeemCodeScreen(proManager: ProManager) {
    var codeInput by remember { mutableStateOf("") }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }
    var isRedeeming by remember { mutableStateOf(false) }
    val subscriptionState by proManager.subscriptionState.collectAsState()
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pro Membership",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 18.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CharcoalBackgroundDark)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Pro Member Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                shape = RoundedCornerShape(10.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = if (subscriptionState.isActive) BoundaryAmber else TextSecondary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (subscriptionState.isActive) {
                            "Pro Active • ${subscriptionState.plan?.title ?: "Member"}"
                        } else {
                            "Upgrade to Pro"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (subscriptionState.isActive) {
                            "Valid until: ${subscriptionState.formattedExpiry}"
                        } else {
                            "100% ad-free scoring, unwatermarked PDF exports, and detailed analytics."
                        },
                        color = TextTertiary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // The 3 Plan Types
            Text(
                text = "MEMBERSHIP TIERS",
                color = TextSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PlanCard(
                    plan = ProPlan.THREE_MONTHS,
                    modifier = Modifier.weight(1f),
                    sampleCode = "SEASON-99-A1"
                )
                PlanCard(
                    plan = ProPlan.ONE_YEAR,
                    modifier = Modifier.weight(1.1f),
                    sampleCode = "CRIC-PRO-2026",
                    highlight = true
                )
                PlanCard(
                    plan = ProPlan.LIFETIME,
                    modifier = Modifier.weight(1f),
                    sampleCode = "CRICSAGA-VIP"
                )
            }

            // Redeem Code Input Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                shape = RoundedCornerShape(10.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "REDEEM PRO CODE",
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Each code is single-use and activates the specified plan:",
                        color = TextTertiary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = codeInput,
                        onValueChange = { codeInput = it.uppercase() },
                        placeholder = { Text("ENTER CODE HERE", color = TextTertiary, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TextSecondary,
                            unfocusedBorderColor = CardBorderDark,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (!isRedeeming && codeInput.isNotBlank()) {
                                isRedeeming = true
                                coroutineScope.launch {
                                    val result = proManager.activateProWithCode(codeInput)
                                    isSuccess = result.isSuccess
                                    feedbackMessage = result.message
                                    isRedeeming = false
                                }
                            }
                        },
                        enabled = !isRedeeming,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimaryDark),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (isRedeeming) "Verifying..." else "Apply Code",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }

                    if (feedbackMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = feedbackMessage!!,
                            color = if (isSuccess) BoundaryGreen else DismissalRed,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Pro Features List
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                shape = RoundedCornerShape(10.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("PRO INCLUDED PERKS", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    ProPerkRow("100% Ad-Free experience across the entire app")
                    ProPerkRow("Export unwatermarked PDF scorecards")
                    ProPerkRow("Instant WhatsApp match summary poster maker")
                    ProPerkRow("Wagon wheel, spider chart & Manhattan run-rate analytics")
                    ProPerkRow("Unlimited historical migration via companion tool")
                }
            }

            // Single-Use Security Note
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Each issued code permanently expires upon first redemption.",
                    color = TextTertiary,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun PlanCard(
    plan: ProPlan,
    modifier: Modifier = Modifier,
    sampleCode: String = "",
    highlight: Boolean = false
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (highlight) ElevatedSurfaceDark else SlateSurfaceDark
        ),
        shape = RoundedCornerShape(8.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(if (highlight) CrimsonPrimaryDark else CardBorderDark)
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (highlight) {
                Text(
                    text = "BEST VALUE",
                    color = CrimsonPrimaryDark,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
            }
            Text(
                text = plan.title,
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = plan.price,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = plan.durationText,
                color = TextTertiary,
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ProPerkRow(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, color = TextSecondary, fontSize = 12.sp)
    }
}
