package com.cricsaga.app

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricsaga.app.ui.theme.*

/**
 * MainActivity is the main entry point of CricSaga.
 *
 * In modern Android with Jetpack Compose:
 * - We extend [ComponentActivity].
 * - Instead of XML layouts (like setContentView(R.layout.activity_main)),
 *   we use [setContent] to build UI declaratively with Kotlin composable functions.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CricSagaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    StageZeroVerificationScreen()
                }
            }
        }
    }
}

/**
 * StageZeroVerificationScreen renders a confirmation card displaying the CricSaga brand
 * and physical device details to verify our end-to-end build, install, and run pipeline.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StageZeroVerificationScreen() {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "CricSaga",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                        Text(
                            text = "Har Run Ki Kahani",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedGrey
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CharcoalBackgroundDark
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Milestone Status Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "STAGE 0 VERIFIED",
                        color = CrimsonPrimaryDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Android System & Device Connected",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Minimal Kotlin + Jetpack Compose project successfully compiled and running natively on your hardware.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedGrey
                    )
                }
            }

            // Physical Device Information Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TARGET DEVICE INFO",
                        color = BoundaryAmber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    DeviceInfoRow("Device", "${Build.MANUFACTURER.uppercase()} ${Build.MODEL}")
                    DeviceInfoRow("Android Version", "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                    DeviceInfoRow("Build Mode", "Debug • Jetpack Compose 2.0")
                }
            }

            // CricHeroes-Parity Keypad Theme Preview
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SCORING THEME PREVIEW",
                        color = BoundaryGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "High-contrast keypad tokens verified for direct pitch sunlight:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedGrey
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Row 1: 0, 1, 2, 3
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        KeypadPreviewButton("0", ElevatedSurfaceDark, Color.White, Modifier.weight(1f))
                        KeypadPreviewButton("1", ElevatedSurfaceDark, Color.White, Modifier.weight(1f))
                        KeypadPreviewButton("2", ElevatedSurfaceDark, Color.White, Modifier.weight(1f))
                        KeypadPreviewButton("3", ElevatedSurfaceDark, Color.White, Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Row 2: 4, 6, Wide, No Ball
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        KeypadPreviewButton("4", BoundaryAmber, Color.Black, Modifier.weight(1f))
                        KeypadPreviewButton("6", BoundaryGreen, Color.Black, Modifier.weight(1f))
                        KeypadPreviewButton("Wd", ExtraBlue, Color.Black, Modifier.weight(1f))
                        KeypadPreviewButton("Nb", ExtraBlue, Color.Black, Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Row 3: Bye, Leg Bye, Out, Undo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        KeypadPreviewButton("B", ElevatedSurfaceDark, Color.White, Modifier.weight(1f))
                        KeypadPreviewButton("Lb", ElevatedSurfaceDark, Color.White, Modifier.weight(1f))
                        KeypadPreviewButton("OUT", DismissalRed, Color.White, Modifier.weight(1f))
                        KeypadPreviewButton("UNDO", ElevatedSurfaceDark, BoundaryAmber, Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = MutedGrey, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, color = Color.White, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun KeypadPreviewButton(
    label: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .background(backgroundColor, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}
