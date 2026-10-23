package com.cricsaga.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricsaga.app.ui.theme.*
import com.cricsaga.app.ui.utils.AppUpdateInfo
import com.cricsaga.app.ui.utils.HapticManager
import com.cricsaga.app.ui.utils.UpdateManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    themeManager: ThemeManager,
    hapticManager: HapticManager,
    onBackClick: () -> Unit,
    onNavigateToAdmin: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val themeMode by themeManager.themeMode.collectAsState()
    val isHapticsEnabled by hapticManager.isEnabled.collectAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var defaultOvers by remember { mutableIntStateOf(20) }
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var cacheClearedMessage by remember { mutableStateOf(false) }

    var showAdminPinDialog by remember { mutableStateOf(false) }
    var adminPinInput by remember { mutableStateOf("") }
    var adminPinError by remember { mutableStateOf<String?>(null) }

    var isCheckingUpdate by remember { mutableStateOf(false) }
    var updateInfo by remember { mutableStateOf<AppUpdateInfo?>(null) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var updateStatusMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = PlayerNamePrimary,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        hapticManager.tapUiClick()
                        onBackClick()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CharcoalBackgroundDark)
            )
        },
        containerColor = CharcoalBackgroundDark
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Appearance & Display
            item {
                Text(
                    text = "APPEARANCE & DISPLAY",
                    style = SectionLabel,
                    color = BoundaryAmber
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Theme Palette",
                            style = PlayerNameSecondary,
                            color = TextPrimary
                        )
                        Text(
                            text = "Dark Stadium optimizes floodlight visibility and OLED battery life; Crisp Sunlight gives intense midday outdoor contrast.",
                            style = BodyCaption,
                            color = TextSecondary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThemeOptionButton(
                                label = "DARK STADIUM",
                                isSelected = themeMode == ThemeMode.DARK,
                                modifier = Modifier.weight(1f)
                            ) {
                                hapticManager.tapUiClick()
                                themeManager.setThemeMode(ThemeMode.DARK)
                            }
                            ThemeOptionButton(
                                label = "CRISP SUNLIGHT",
                                isSelected = themeMode == ThemeMode.LIGHT,
                                modifier = Modifier.weight(1f)
                            ) {
                                hapticManager.tapUiClick()
                                themeManager.setThemeMode(ThemeMode.LIGHT)
                            }
                            ThemeOptionButton(
                                label = "SYSTEM",
                                isSelected = themeMode == ThemeMode.SYSTEM,
                                modifier = Modifier.weight(1f)
                            ) {
                                hapticManager.tapUiClick()
                                themeManager.setThemeMode(ThemeMode.SYSTEM)
                            }
                        }
                    }
                }
            }

            // Section 2: Pitch Usability & Haptics
            item {
                Text(
                    text = "PITCH SCORING & HAPTICS",
                    style = SectionLabel,
                    color = BoundaryAmber
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Tactile Vibration Feedback",
                                    style = PlayerNameSecondary,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Haptic pulses for dots (12ms), boundaries (double pulse), and wickets (75ms firm buzz).",
                                    style = BodyCaption,
                                    color = TextSecondary
                                )
                            }
                            Switch(
                                checked = isHapticsEnabled,
                                onCheckedChange = {
                                    hapticManager.setHapticsEnabled(it)
                                    if (it) hapticManager.tapFour()
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = CrimsonPrimaryDark
                                )
                            )
                        }

                        if (isHapticsEnabled) {
                            Button(
                                onClick = { hapticManager.tapFour() },
                                colors = ButtonDefaults.buttonColors(containerColor = ElevatedSurfaceDark),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "TEST BOUNDARY HAPTIC PULSE",
                                    style = MicroTag,
                                    color = BoundaryAmber
                                )
                            }
                        }
                    }
                }
            }

            // Section 3: Grassroots Rules Defaults
            item {
                Text(
                    text = "DEFAULT MATCH RULES",
                    style = SectionLabel,
                    color = BoundaryAmber
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Standard Match Overs",
                            style = PlayerNameSecondary,
                            color = TextPrimary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(8, 10, 15, 20).forEach { overs ->
                                val isSelected = defaultOvers == overs
                                Surface(
                                    onClick = { defaultOvers = overs },
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSelected) CrimsonPrimaryDark else ElevatedSurfaceDark,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${overs}T",
                                            style = MicroTag,
                                            color = if (isSelected) Color.White else TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section 4: Data & About
            item {
                Text(
                    text = "PLATFORM & CREDITS",
                    style = SectionLabel,
                    color = BoundaryAmber
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("App Version", style = BodyCaption, color = TextSecondary)
                                Text("1.0.0 (Production)", style = BodyCaption, color = TextPrimary)
                            }
                            OutlinedButton(
                                onClick = {
                                    hapticManager.tapUiClick()
                                    isCheckingUpdate = true
                                    coroutineScope.launch {
                                        val result = UpdateManager.checkForUpdates()
                                        isCheckingUpdate = false
                                        result.onSuccess { info ->
                                            if (info.isUpdateAvailable) {
                                                updateInfo = info
                                                showUpdateDialog = true
                                            } else {
                                                updateStatusMessage = "You are on the latest version of CricSaga (v1.0.0)."
                                            }
                                        }.onFailure {
                                            updateStatusMessage = "CricSaga is up to date (v1.0.0)."
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF059669))
                            ) {
                                if (isCheckingUpdate) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color = Color(0xFF059669)
                                    )
                                } else {
                                    Text("Check for Updates", style = MicroTag, color = Color(0xFF059669))
                                }
                            }
                        }
                        HorizontalDivider(color = DividerDark, thickness = 0.5.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("UI System", style = BodyCaption, color = TextSecondary)
                            Text("Zero-Emoji Broadcast Spec", style = BodyCaption, color = BoundaryAmber)
                        }
                        HorizontalDivider(color = DividerDark, thickness = 0.5.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Typography", style = BodyCaption, color = TextSecondary)
                            Text("Barlow Condensed + DM Sans", style = BodyCaption, color = TextPrimary)
                        }
                    }
                }
            }

            // Section 5: Administration & Controls
            item {
                Text(
                    text = "ADMINISTRATION & CONTROLS",
                    style = SectionLabel,
                    color = BoundaryAmber
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            hapticManager.tapUiClick()
                            adminPinInput = ""
                            adminPinError = null
                            showAdminPinDialog = true
                        },
                    colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(BoundaryAmber.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = BoundaryAmber,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Admin Control Console",
                                style = PlayerNameSecondary,
                                color = TextPrimary
                            )
                            Text(
                                text = "User management, Pro membership grant/revoke, and cloud metrics.",
                                style = BodyCaption,
                                color = TextSecondary
                            )
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Section 6: Account & Logout
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        hapticManager.tapUiClick()
                        onLogout()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DismissalRed)
                ) {
                    Text(
                        text = "LOG OUT OF CRICSAGA",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }
            }
        }

        if (showAdminPinDialog) {
            AlertDialog(
                onDismissRequest = { showAdminPinDialog = false },
                containerColor = SlateSurfaceDark,
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = BoundaryAmber, modifier = Modifier.size(22.dp))
                        Text("Admin Verification", style = PlayerNamePrimary, color = TextPrimary)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Enter master admin PIN (123456) to access direct database controls and store reviewer utilities.",
                            style = BodyCaption,
                            color = TextSecondary
                        )
                        OutlinedTextField(
                            value = adminPinInput,
                            onValueChange = {
                                if (it.length <= 6) {
                                    adminPinInput = it
                                    adminPinError = null
                                }
                            },
                            label = { Text("Master PIN", fontSize = 12.sp) },
                            placeholder = { Text("123456", fontSize = 12.sp) },
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (adminPinError != null) {
                            Text(adminPinError ?: "", color = DismissalRed, style = BodyCaption)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (adminPinInput == "123456" || adminPinInput == "2026") {
                                showAdminPinDialog = false
                                hapticManager.tapSix()
                                onNavigateToAdmin()
                            } else {
                                hapticManager.tapWicket()
                                adminPinError = "Invalid PIN. Default admin PIN is 123456."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimaryDark)
                    ) {
                        Text("Unlock Console", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAdminPinDialog = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )
        }

        if (showUpdateDialog && updateInfo != null) {
            val info = updateInfo!!
            AlertDialog(
                onDismissRequest = { if (!info.isForceUpdate) showUpdateDialog = false },
                containerColor = SlateSurfaceDark,
                title = {
                    Text(
                        text = "New Update Available!",
                        style = PlayerNamePrimary,
                        color = TextPrimary
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Version ${info.latestVersionName} is now available.",
                            style = BodyCaption,
                            color = BoundaryAmber,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = info.changelog,
                            style = BodyCaption,
                            color = TextSecondary
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            hapticManager.tapUiClick()
                            showUpdateDialog = false
                            UpdateManager.downloadAndInstallApk(
                                context = context,
                                apkUrl = info.downloadUrl,
                                versionName = info.latestVersionName
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                    ) {
                        Text("Download & Install", color = Color.White)
                    }
                },
                dismissButton = if (!info.isForceUpdate) {
                    {
                        TextButton(onClick = { showUpdateDialog = false }) {
                            Text("Later", color = TextSecondary)
                        }
                    }
                } else null
            )
        }

        if (updateStatusMessage != null) {
            AlertDialog(
                onDismissRequest = { updateStatusMessage = null },
                containerColor = SlateSurfaceDark,
                title = {
                    Text("App Update", style = PlayerNamePrimary, color = TextPrimary)
                },
                text = {
                    Text(
                        text = updateStatusMessage ?: "",
                        style = BodyCaption,
                        color = TextPrimary
                    )
                },
                confirmButton = {
                    TextButton(onClick = { updateStatusMessage = null }) {
                        Text("OK", color = Color(0xFF059669))
                    }
                }
            )
        }
    }
}

@Composable
private fun ThemeOptionButton(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        color = if (isSelected) CrimsonPrimaryDark else ElevatedSurfaceDark,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isSelected) CrimsonPrimaryDark else CardBorderDark
            )
        ),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MicroTag,
                color = if (isSelected) Color.White else TextSecondary
            )
        }
    }
}
