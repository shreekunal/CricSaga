package com.cricsaga.app.ui.redeem

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricsaga.app.R
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.cricsaga_logo),
                            contentDescription = "CricSaga Logo",
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("CricSaga Pro Membership", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 17.sp)
                    }
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
                colors = CardDefaults.cardColors(containerColor = if (subscriptionState.isActive) Color(0xFF2C240E) else SlateSurfaceDark),
                shape = RoundedCornerShape(16.dp),
                border = if (subscriptionState.isActive) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BoundaryAmber)) else null
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.cricsaga_logo),
                        contentDescription = "Logo",
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (subscriptionState.isActive) {
                            "CRICSAGA PRO ACTIVE • ${subscriptionState.plan?.title ?: "MEMBER"}"
                        } else {
                            "GET CRICSAGA PRO"
                        },
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = if (subscriptionState.isActive) BoundaryAmber else Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (subscriptionState.isActive) {
                            "Validity: ${subscriptionState.formattedExpiry} (Redeemed Code: ${subscriptionState.redeemedCode})"
                        } else {
                            "Enter your single-use redeem code to unlock Pro perks directly without 3rd party fees."
                        },
                        color = MutedGrey,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // The 3 Plan Types
            Text(
                text = "AVAILABLE MEMBERSHIP TIERS",
                color = BoundaryAmber,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
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
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CardMembership, contentDescription = null, tint = BoundaryAmber, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "REDEEM ONE-TIME CODE",
                            color = BoundaryAmber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Each code is generated for a specific subscription and can only be used one time:",
                        color = MutedGrey,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = codeInput,
                        onValueChange = { codeInput = it.uppercase() },
                        placeholder = { Text("ENTER REDEEM CODE HERE", color = Color.Gray, fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BoundaryAmber,
                            unfocusedBorderColor = Color.DarkGray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

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
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isRedeeming) "VALIDATING CODE..." else "APPLY REDEEM CODE",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    if (feedbackMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSuccess) Color(0xFF142B1A) else Color(0xFF2B1414)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = feedbackMessage!!,
                                color = if (isSuccess) BoundaryGreen else DismissalRed,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }

            // Pro Features List
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("PRO MEMBER PRIVILEGES", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    ProPerkRow("100% Ad-Free experience across the entire app")
                    ProPerkRow("Export clean, unwatermarked PDF scorecards")
                    ProPerkRow("Instant WhatsApp Status match highlight poster maker")
                    ProPerkRow("Wagon wheel, spider chart & Manhattan run-rate analytics")
                    ProPerkRow("Unlimited historical match migration via companion tool")
                    ProPerkRow("Gold Verified Scorer & Player badge on public scorecards")
                }
            }

            // One-Time Security Note
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ElevatedSurfaceDark),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = BoundaryAmber, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Single-Use Security: Every issued code is verified against our database and permanently expires upon first redemption.",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
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
            containerColor = if (highlight) Color(0xFF2B1D1D) else SlateSurfaceDark
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (highlight) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CrimsonPrimaryDark)) else null
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (highlight) {
                Text(
                    text = "MOST POPULAR",
                    color = BoundaryAmber,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
            }
            Text(
                text = plan.title,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = plan.price,
                color = if (highlight) CrimsonPrimaryDark else BoundaryAmber,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = plan.durationText,
                color = MutedGrey,
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                color = CharcoalBackgroundDark,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = sampleCode,
                    color = Color.LightGray,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun ProPerkRow(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BoundaryGreen, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, color = Color.LightGray, fontSize = 12.sp)
    }
}
