package com.cricsaga.app.ui.setup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricsaga.app.data.local.entity.UserEntity
import com.cricsaga.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenersSelectionScreen(
    battingTeamName: String,
    bowlingTeamName: String,
    battingSquad: List<UserEntity>,
    bowlingSquad: List<UserEntity>,
    onBackClick: () -> Unit,
    onStartMatch: (strikerId: String, nonStrikerId: String, bowlerId: String) -> Unit
) {
    var strikerId by remember { mutableStateOf(battingSquad.getOrNull(0)?.userId ?: "") }
    var nonStrikerId by remember { mutableStateOf(battingSquad.getOrNull(1)?.userId ?: "") }
    var openingBowlerId by remember { mutableStateOf(bowlingSquad.getOrNull(0)?.userId ?: "") }
    val scrollState = rememberScrollState()
    val hapticManager = com.cricsaga.app.ui.utils.LocalHapticManager.current

    LaunchedEffect(battingSquad, bowlingSquad) {
        if (strikerId.isEmpty() && battingSquad.isNotEmpty()) {
            strikerId = battingSquad[0].userId
        }
        if (nonStrikerId.isEmpty() && battingSquad.size > 1) {
            nonStrikerId = battingSquad[1].userId
        }
        if (openingBowlerId.isEmpty() && bowlingSquad.isNotEmpty()) {
            openingBowlerId = bowlingSquad[0].userId
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Select Opening Players",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$battingTeamName vs $bowlingTeamName",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        hapticManager?.tapUiClick()
                        onBackClick()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Striker Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "OPENING STRIKER ($battingTeamName)",
                        color = CrimsonPrimaryDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 0.5.sp
                    )
                    if (battingSquad.isEmpty()) {
                        Text(
                            text = "No squad members found for $battingTeamName.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(battingSquad.filter { it.userId != nonStrikerId }) { p ->
                                val isSelected = strikerId == p.userId
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        hapticManager?.tapUiClick()
                                        strikerId = p.userId
                                    },
                                    label = {
                                        Text(
                                            text = p.fullName,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CrimsonPrimaryDark,
                                        selectedLabelColor = Color.White,
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        labelColor = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Non-Striker Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "OPENING NON-STRIKER ($battingTeamName)",
                        color = BoundaryAmber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 0.5.sp
                    )
                    if (battingSquad.size < 2) {
                        Text(
                            text = "At least 2 squad members needed to select non-striker.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(battingSquad.filter { it.userId != strikerId }) { p ->
                                val isSelected = nonStrikerId == p.userId
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        hapticManager?.tapUiClick()
                                        nonStrikerId = p.userId
                                    },
                                    label = {
                                        Text(
                                            text = p.fullName,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AmberFourPrimary,
                                        selectedLabelColor = Color.White,
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        labelColor = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Opening Bowler Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "OPENING BOWLER ($bowlingTeamName)",
                        color = Color(0xFF059669),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 0.5.sp
                    )
                    if (bowlingSquad.isEmpty()) {
                        Text(
                            text = "No squad members found for $bowlingTeamName.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(bowlingSquad) { b ->
                                val isSelected = openingBowlerId == b.userId
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        hapticManager?.tapUiClick()
                                        openingBowlerId = b.userId
                                    },
                                    label = {
                                        Text(
                                            text = b.fullName,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF059669),
                                        selectedLabelColor = Color.White,
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        labelColor = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                }
            }

            val canStart = strikerId.isNotEmpty() &&
                    nonStrikerId.isNotEmpty() &&
                    openingBowlerId.isNotEmpty() &&
                    strikerId != nonStrikerId

            Button(
                onClick = {
                    hapticManager?.tapUiClick()
                    onStartMatch(strikerId, nonStrikerId, openingBowlerId)
                },
                enabled = canStart,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CrimsonPrimaryDark,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = "LET'S PLAY! START SCORING",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (canStart) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
