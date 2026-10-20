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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricsaga.app.ui.theme.*
import com.cricsaga.app.ui.utils.HapticManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    themeManager: ThemeManager,
    hapticManager: HapticManager,
    onBackClick: () -> Unit
) {
    val themeMode by themeManager.themeMode.collectAsState()
    val isHapticsEnabled by hapticManager.isEnabled.collectAsState()

    var defaultOvers by remember { mutableIntStateOf(20) }
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var cacheClearedMessage by remember { mutableStateOf(false) }

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
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("App Version", style = BodyCaption, color = TextSecondary)
                            Text("1.0.0 (Production)", style = BodyCaption, color = TextPrimary)
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
