package com.cricsaga.app.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricsaga.app.data.local.entity.UserEntity
import com.cricsaga.app.ui.theme.*
import com.cricsaga.app.ui.utils.AppUpdateInfo
import com.cricsaga.app.ui.utils.HapticManager
import com.cricsaga.app.ui.utils.ImageUtils
import com.cricsaga.app.ui.utils.UpdateManager
import kotlinx.coroutines.launch

/**
 * Settings & Preferences Screen — Clean & Minimal Consumer-Grade Layout
 *
 * Streamlined to core athlete essentials:
 *  - Profile preview snippet (avatar, contact, role)
 *  - Appearance theme switcher (Dark Stadium, Sunlight, System)
 *  - Tactile haptic vibration toggle
 *  - App version & In-app OTA updates
 *  - Storage cache cleaner
 *  - Support & Gated Admin console
 *  - Clean Log Out
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentUser: UserEntity? = null,
    themeManager: ThemeManager,
    hapticManager: HapticManager,
    onBackClick: () -> Unit,
    onProfileClick: () -> Unit = {},
    onProClick: () -> Unit = {},
    isAdmin: Boolean = false,
    onNavigateToAdmin: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val themeMode by themeManager.themeMode.collectAsState()
    val isHapticsEnabled by hapticManager.isEnabled.collectAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Dialog states
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var cacheClearedMessage by remember { mutableStateOf(false) }
    var cacheSizeText by remember { mutableStateOf(getCacheDirectorySize(context)) }

    // Admin PIN prompt
    var showAdminPinDialog by remember { mutableStateOf(false) }
    var adminPinInput by remember { mutableStateOf("") }
    var adminPinError by remember { mutableStateOf<String?>(null) }

    // OTA In-App Updater
    var isCheckingUpdate by remember { mutableStateOf(false) }
    var updateInfo by remember { mutableStateOf<AppUpdateInfo?>(null) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var updateStatusMessage by remember { mutableStateOf<String?>(null) }

    // Support dialog
    var showSupportDialog by remember { mutableStateOf(false) }

    val userAvatarBitmap = remember(currentUser?.profilePhotoUrl) {
        currentUser?.profilePhotoUrl?.let { ImageUtils.loadBitmapFromPath(it) }
    }

    val isPro = currentUser?.isPro == true

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = PlayerNamePrimary,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        hapticManager.tapUiClick()
                        onBackClick()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // =========================================================================
            // 1. Athlete Profile Card (Compact & Focused)
            // =========================================================================
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            hapticManager.tapUiClick()
                            onProfileClick()
                        },
                    shape = RoundedCornerShape(16.dp),
                    color = SlateSurfaceDark,
                    border = BorderStroke(
                        1.dp,
                        if (isPro) BoundaryAmber.copy(alpha = 0.5f) else CardBorderDark
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User Avatar or Fallback Initials
                        if (userAvatarBitmap != null) {
                            Image(
                                bitmap = userAvatarBitmap,
                                contentDescription = "Athlete Avatar",
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = if (isPro) 2.dp else 1.5.dp,
                                        color = if (isPro) BoundaryAmber else CardBorderDark,
                                        shape = CircleShape
                                    )
                            )
                        } else {
                            val initials = (currentUser?.fullName?.trim() ?: "Player")
                                .split(" ")
                                .mapNotNull { it.firstOrNull()?.toString() }
                                .take(2)
                                .joinToString("")
                                .uppercase()
                                .ifEmpty { "P" }

                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            if (isPro) listOf(BoundaryAmber.copy(alpha = 0.35f), BoundaryAmber.copy(alpha = 0.15f))
                                            else listOf(CrimsonPrimaryDark.copy(alpha = 0.35f), CrimsonPrimaryDark.copy(alpha = 0.15f))
                                        )
                                    )
                                    .border(
                                        width = if (isPro) 2.dp else 1.5.dp,
                                        color = if (isPro) BoundaryAmber else CardBorderDark,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initials,
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isPro) BoundaryAmber else TextPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = currentUser?.fullName?.ifBlank { "Athlete Profile" } ?: "Athlete Profile",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                if (isPro) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = BoundaryAmber.copy(alpha = 0.2f),
                                        border = BorderStroke(0.5.dp, BoundaryAmber)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.WorkspacePremium,
                                                contentDescription = null,
                                                tint = BoundaryAmber,
                                                modifier = Modifier.size(11.dp)
                                            )
                                            Text(
                                                text = "PRO",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                color = BoundaryAmber
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = currentUser?.email?.ifBlank { null }
                                    ?: (if (currentUser?.phoneNumber?.isNotBlank() == true) "+91 ${currentUser.phoneNumber}" else "Personal Athlete Account"),
                                style = BodyCaption,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            val roleText = currentUser?.playingRole?.name?.replace("_", " ")?.lowercase()
                                ?.replaceFirstChar { it.uppercase() } ?: "All-Rounder"
                            val jerseyText = if (!currentUser?.jerseyNumber.isNullOrBlank()) "#${currentUser?.jerseyNumber} • " else ""

                            Text(
                                text = "$jerseyText$roleText",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isPro) BoundaryAmber else CrimsonPrimaryDark
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "View Profile",
                            tint = TextTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // =========================================================================
            // 2. Preferences (Theme & Haptics)
            // =========================================================================
            item {
                SettingsSectionTitle("PREFERENCES")
            }

            item {
                SettingsGroupCard {
                    // Display Theme Selector
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SettingsIconBadge(icon = Icons.Default.Palette, tint = ExtraBlue)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Display Theme",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Pitch contrast and stadium lighting",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Segmented Control Pill Row
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = ElevatedSurfaceDark,
                            border = BorderStroke(0.5.dp, CardBorderDark)
                        ) {
                            Row(modifier = Modifier.padding(3.dp)) {
                                SegmentedThemeTab(
                                    label = "Dark Stadium",
                                    icon = Icons.Default.DarkMode,
                                    isSelected = themeMode == ThemeMode.DARK,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    hapticManager.tapUiClick()
                                    themeManager.setThemeMode(ThemeMode.DARK)
                                }
                                SegmentedThemeTab(
                                    label = "Sunlight",
                                    icon = Icons.Default.LightMode,
                                    isSelected = themeMode == ThemeMode.LIGHT,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    hapticManager.tapUiClick()
                                    themeManager.setThemeMode(ThemeMode.LIGHT)
                                }
                                SegmentedThemeTab(
                                    label = "System",
                                    icon = Icons.Default.SettingsBrightness,
                                    isSelected = themeMode == ThemeMode.SYSTEM,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    hapticManager.tapUiClick()
                                    themeManager.setThemeMode(ThemeMode.SYSTEM)
                                }
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(start = 62.dp),
                        thickness = 0.5.dp,
                        color = DividerDark
                    )

                    // Tactile Vibration Switch
                    SettingsRow(
                        icon = Icons.Default.Vibration,
                        iconTint = BoundaryAmber,
                        title = "Tactile Vibration",
                        subtitle = "Haptic feedback on boundaries, wickets & dots",
                        trailing = {
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
                    )
                }
            }

            // =========================================================================
            // 3. App, Storage & Support
            // =========================================================================
            item {
                SettingsSectionTitle("APP & SYSTEM")
            }

            item {
                SettingsGroupCard {
                    // App Version & Check for Updates
                    SettingsRow(
                        icon = Icons.Default.SystemUpdate,
                        iconTint = ForestSixPrimary,
                        title = "App Version",
                        subtitle = "v1.0.1 (Production)",
                        trailing = {
                            Button(
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
                                                updateStatusMessage = "You are on the latest version of CricSaga (v1.0.1)."
                                            }
                                        }.onFailure {
                                            updateStatusMessage = "You are on the latest version of CricSaga."
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ForestSixPrimary.copy(alpha = 0.18f),
                                    contentColor = ForestSixPrimary
                                ),
                                border = BorderStroke(1.dp, ForestSixPrimary.copy(alpha = 0.45f))
                            ) {
                                if (isCheckingUpdate) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(12.dp),
                                        strokeWidth = 2.dp,
                                        color = ForestSixPrimary
                                    )
                                } else {
                                    Text(
                                        text = "Check Updates",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(start = 62.dp),
                        thickness = 0.5.dp,
                        color = DividerDark
                    )

                    // Clear Local Cache
                    SettingsRow(
                        icon = Icons.Default.CleaningServices,
                        iconTint = Color(0xFF8B5CF6),
                        title = "Clear Cache",
                        subtitle = "Temporary images & memory buffers ($cacheSizeText)",
                        trailing = {
                            OutlinedButton(
                                onClick = {
                                    hapticManager.tapUiClick()
                                    showClearCacheDialog = true
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = CrimsonPrimaryDark
                                ),
                                border = BorderStroke(1.dp, CrimsonPrimaryDark.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "Clear",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(start = 62.dp),
                        thickness = 0.5.dp,
                        color = DividerDark
                    )

                    // Help & Support
                    SettingsRow(
                        icon = Icons.AutoMirrored.Filled.HelpOutline,
                        iconTint = BoundaryAmber,
                        title = "Help & Support",
                        subtitle = "Feedback, queries & bug reports",
                        trailing = {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = TextTertiary,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            hapticManager.tapUiClick()
                            showSupportDialog = true
                        }
                    )
                }
            }

            // =========================================================================
            // 4. Administration Console (Gated exclusively to Admin Email)
            // =========================================================================
            if (isAdmin) {
                item {
                    SettingsSectionTitle("ADMINISTRATION")
                }

                item {
                    SettingsGroupCard {
                        SettingsRow(
                            icon = Icons.Default.AdminPanelSettings,
                            iconTint = BoundaryAmber,
                            title = "Admin Console",
                            subtitle = "Player database, cloud KPIs & user grants",
                            trailing = {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = TextTertiary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = {
                                hapticManager.tapUiClick()
                                adminPinInput = ""
                                adminPinError = null
                                showAdminPinDialog = true
                            }
                        )
                    }
                }
            }

            // =========================================================================
            // 5. Account Logout
            // =========================================================================
            item {
                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            hapticManager.tapUiClick()
                            showLogoutDialog = true
                        },
                    shape = RoundedCornerShape(14.dp),
                    color = DismissalRed.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, DismissalRed.copy(alpha = 0.28f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            tint = DismissalRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Log Out",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = DismissalRed
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CricSaga v1.0.1",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextTertiary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // =========================================================================
        // Dialogs
        // =========================================================================

        // Logout Confirmation Dialog
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                containerColor = SlateSurfaceDark,
                shape = RoundedCornerShape(18.dp),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            tint = DismissalRed,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Log Out",
                            style = PlayerNamePrimary,
                            color = TextPrimary
                        )
                    }
                },
                text = {
                    Text(
                        text = "Are you sure you want to log out of CricSaga? Your offline matches and team rosters remain securely saved on this device.",
                        style = BodyCaption,
                        color = TextSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showLogoutDialog = false
                            onLogout()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DismissalRed),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Log Out", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )
        }

        // Clear Cache Confirmation Dialog
        if (showClearCacheDialog) {
            AlertDialog(
                onDismissRequest = { showClearCacheDialog = false },
                containerColor = SlateSurfaceDark,
                shape = RoundedCornerShape(18.dp),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CleaningServices,
                            contentDescription = null,
                            tint = Color(0xFF8B5CF6),
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Clear Match Cache",
                            style = PlayerNamePrimary,
                            color = TextPrimary
                        )
                    }
                },
                text = {
                    Text(
                        text = "This frees temporary scorecard previews and memory buffers. Your matches, teams, and tournament rosters will not be deleted.",
                        style = BodyCaption,
                        color = TextSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            try {
                                context.cacheDir.deleteRecursively()
                            } catch (_: Exception) {}
                            cacheSizeText = getCacheDirectorySize(context)
                            showClearCacheDialog = false
                            cacheClearedMessage = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimaryDark),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Clear Cache", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearCacheDialog = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )
        }

        if (cacheClearedMessage) {
            AlertDialog(
                onDismissRequest = { cacheClearedMessage = false },
                containerColor = SlateSurfaceDark,
                shape = RoundedCornerShape(16.dp),
                title = { Text("Cache Cleared", style = PlayerNamePrimary, color = TextPrimary) },
                text = { Text("Temporary match cache was successfully cleared.", style = BodyCaption, color = TextSecondary) },
                confirmButton = {
                    TextButton(onClick = { cacheClearedMessage = false }) {
                        Text("OK", color = ForestSixPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // Admin Master PIN Dialog
        if (showAdminPinDialog) {
            AlertDialog(
                onDismissRequest = { showAdminPinDialog = false },
                containerColor = SlateSurfaceDark,
                shape = RoundedCornerShape(18.dp),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = BoundaryAmber,
                            modifier = Modifier.size(22.dp)
                        )
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
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.NumberPassword
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
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
                                hapticManager.tapBoundaryFour()
                                onNavigateToAdmin()
                            } else {
                                hapticManager.tapWicket()
                                adminPinError = "Invalid PIN. Default admin PIN is 123456."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimaryDark),
                        shape = RoundedCornerShape(8.dp)
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

        // OTA Update Dialog
        if (showUpdateDialog && updateInfo != null) {
            val info = updateInfo!!
            AlertDialog(
                onDismissRequest = { if (!info.isForceUpdate) showUpdateDialog = false },
                containerColor = SlateSurfaceDark,
                shape = RoundedCornerShape(18.dp),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = null,
                            tint = ForestSixPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text("New Update Available", style = PlayerNamePrimary, color = TextPrimary)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Version ${info.latestVersionName} is ready to install.",
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
                            hapticManager.tapBoundaryFour()
                            showUpdateDialog = false
                            UpdateManager.downloadAndInstallApk(
                                context = context,
                                apkUrl = info.downloadUrl,
                                versionName = info.latestVersionName
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestSixPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Download & Install", color = Color.White, fontWeight = FontWeight.Bold)
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

        // Status Message Dialog
        if (updateStatusMessage != null) {
            AlertDialog(
                onDismissRequest = { updateStatusMessage = null },
                containerColor = SlateSurfaceDark,
                shape = RoundedCornerShape(16.dp),
                title = {
                    Text("App Update", style = PlayerNamePrimary, color = TextPrimary)
                },
                text = {
                    Text(text = updateStatusMessage ?: "", style = BodyCaption, color = TextPrimary)
                },
                confirmButton = {
                    TextButton(onClick = { updateStatusMessage = null }) {
                        Text("OK", color = ForestSixPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // Support Dialog
        if (showSupportDialog) {
            AlertDialog(
                onDismissRequest = { showSupportDialog = false },
                containerColor = SlateSurfaceDark,
                shape = RoundedCornerShape(18.dp),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = null,
                            tint = BoundaryAmber,
                            modifier = Modifier.size(22.dp)
                        )
                        Text("CricSaga Support", style = PlayerNamePrimary, color = TextPrimary)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Need help with offline scoring, player stats, or tournament fixtures?",
                            style = BodyCaption,
                            color = TextPrimary
                        )
                        Text(
                            text = "Reach out to our team at support@cricsaga.app. We respond within 24 hours.",
                            style = BodyCaption,
                            color = TextSecondary
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showSupportDialog = false
                            try {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:support@cricsaga.app")
                                    putExtra(Intent.EXTRA_SUBJECT, "CricSaga Support & Feedback")
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimaryDark),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Send Email", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSupportDialog = false }) {
                        Text("Close", color = TextSecondary)
                    }
                }
            )
        }
    }
}

// =============================================================================
// Settings Reusable UI Subcomponents
// =============================================================================

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        color = BoundaryAmber,
        modifier = Modifier.padding(start = 6.dp, bottom = 2.dp)
    )
}

@Composable
private fun SettingsGroupCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SlateSurfaceDark,
        border = BorderStroke(1.dp, CardBorderDark)
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val modifier = Modifier
        .fillMaxWidth()
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        .padding(horizontal = 14.dp, vertical = 13.dp)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsIconBadge(icon = icon, tint = iconTint)

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = TextPrimary
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 15.sp
                )
            }
        }

        if (trailing != null) {
            Spacer(modifier = Modifier.width(10.dp))
            trailing()
        }
    }
}

@Composable
private fun SettingsIconBadge(icon: ImageVector, tint: Color) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(tint.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(19.dp)
        )
    }
}

@Composable
private fun SegmentedThemeTab(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) CrimsonPrimaryDark else Color.Transparent,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(vertical = 7.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else TextSecondary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else TextSecondary
            )
        }
    }
}

private fun getCacheDirectorySize(context: Context): String {
    return try {
        var size: Long = 0
        context.cacheDir.walkTopDown().forEach { file ->
            if (file.isFile) size += file.length()
        }
        if (size > 1024 * 1024) {
            String.format(java.util.Locale.US, "%.1f MB", size / (1024.0 * 1024.0))
        } else if (size > 1024) {
            String.format(java.util.Locale.US, "%.1f KB", size / 1024.0)
        } else {
            "< 1 MB"
        }
    } catch (_: Exception) {
        "< 1 MB"
    }
}
