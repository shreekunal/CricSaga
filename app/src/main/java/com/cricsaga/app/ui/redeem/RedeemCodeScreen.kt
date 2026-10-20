package com.cricsaga.app.ui.redeem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
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
import com.cricsaga.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RedeemCodeScreen(proManager: ProManager) {
    var codeInput by remember { mutableStateOf("") }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }
    val isProActive by proManager.isProFlow.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CricSaga Pro", fontWeight = FontWeight.Bold, color = Color.White) },
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
                colors = CardDefaults.cardColors(containerColor = if (isProActive) Color(0xFF2C240E) else SlateSurfaceDark),
                shape = RoundedCornerShape(16.dp),
                border = if (isProActive) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BoundaryAmber)) else null
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Pro",
                        tint = if (isProActive) BoundaryAmber else Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isProActive) "CRICSAGA PRO ACTIVE" else "GET CRICSAGA PRO",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = if (isProActive) BoundaryAmber else Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isProActive) "All premium features unlocked for your account!" else "Redeem your direct code to unlock pro perks without app store commission.",
                        color = MutedGrey,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Redeem Code Input Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "REDEEM PRO MEMBERSHIP CODE",
                        color = BoundaryAmber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Enter your code (e.g. CRIC-PRO-2026 or PRO-SAGA-TURF):",
                        color = MutedGrey,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = codeInput,
                        onValueChange = { codeInput = it.uppercase() },
                        placeholder = { Text("ENTER CODE HERE", color = Color.Gray) },
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
                            val success = proManager.activateProWithCode(codeInput)
                            isSuccess = success
                            feedbackMessage = if (success) {
                                "Congratulations! CricSaga Pro is now ACTIVE."
                            } else {
                                "Invalid code. Please verify your redeem code."
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimaryDark),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("REDEEM CODE", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    if (feedbackMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = feedbackMessage!!,
                            color = if (isSuccess) BoundaryGreen else DismissalRed,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
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
                    Text("PRO INCLUDED PERKS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    ProPerkRow("100% Ad-Free scoring experience forever")
                    ProPerkRow("Export clean, unwatermarked PDF scorecards")
                    ProPerkRow("WhatsApp match summary graphic poster generator")
                    ProPerkRow("Wagon wheel, spider chart & Manhattan analytics")
                    ProPerkRow("Unlimited historical migration via companion app")
                }
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
