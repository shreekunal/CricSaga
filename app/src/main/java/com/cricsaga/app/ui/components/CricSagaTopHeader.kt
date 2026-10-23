package com.cricsaga.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricsaga.app.ui.theme.*
import com.cricsaga.app.ui.utils.ImageUtils
import com.cricsaga.app.ui.utils.LocalHapticManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CricSagaTopHeader(
    isPro: Boolean,
    currentUserName: String,
    currentUserAvatarUrl: String?,
    unreadMessageCount: Int,
    isDarkTheme: Boolean,
    onThemeToggleClick: () -> Unit,
    onProfileClick: () -> Unit,
    onJoinPlusClick: () -> Unit,
    onMessagesClick: () -> Unit,
    onNotificationsClick: () -> Unit = {},
    onSettingsClick: () -> Unit
) {
    var showNotificationsDialog by remember { mutableStateOf(false) }
    val hapticManager = LocalHapticManager.current

    val initials = currentUserName.split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .uppercase()
        .ifEmpty { "CR" }

    val avatarBitmap = remember(currentUserAvatarUrl) {
        ImageUtils.loadBitmapFromPath(currentUserAvatarUrl)
    }

    TopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Avatar (Clickable to My Career)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.5.dp, CrimsonPrimaryDark, CircleShape)
                        .clickable {
                            hapticManager?.tapUiClick()
                            onProfileClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarBitmap != null) {
                        Image(
                            bitmap = avatarBitmap,
                            contentDescription = "My Profile",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = initials,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = CrimsonPrimaryDark
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Join Plus / Plus Active Pill Button
                Surface(
                    modifier = Modifier.clickable {
                        hapticManager?.tapUiClick()
                        onJoinPlusClick()
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = AmberFourPrimary.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, AmberFourPrimary.copy(alpha = 0.7f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = AmberFourPrimary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isPro) "PRO" else "PRO @99",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberFourPrimary
                        )
                    }
                }
            }
        },
        actions = {
            // Theme Switcher Toggle (Light / Dark)
            IconButton(onClick = {
                hapticManager?.tapUiClick()
                onThemeToggleClick()
            }) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "Toggle Theme",
                    tint = if (isDarkTheme) AmberFourPrimary else TextPrimaryLight
                )
            }

            // Direct Messages Icon with Unread Badge
            Box(contentAlignment = Alignment.TopEnd) {
                IconButton(onClick = {
                    hapticManager?.tapUiClick()
                    onMessagesClick()
                }) {
                    Icon(
                        Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "Messages",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                if (unreadMessageCount > 0) {
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp, end = 6.dp)
                            .size(16.dp)
                            .background(CrimsonPrimaryDark, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = unreadMessageCount.toString(),
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Notifications Icon
            IconButton(onClick = {
                hapticManager?.tapUiClick()
                showNotificationsDialog = true
                onNotificationsClick()
            }) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            // Settings Gear Icon
            IconButton(onClick = {
                hapticManager?.tapUiClick()
                onSettingsClick()
            }) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        )
    )

    if (showNotificationsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationsDialog = false },
            title = {
                Text(
                    text = "Match & Community Updates",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Match Result Verified",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Shivaji Park Warriors won by 4 wickets against Bandra Blasters.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Leaderboard Update",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "You are in the Top 5 grassroots all-rounders in Mumbai!",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotificationsDialog = false }) {
                    Text("OK", fontWeight = FontWeight.Bold, color = CrimsonPrimaryDark)
                }
            }
        )
    }
}
