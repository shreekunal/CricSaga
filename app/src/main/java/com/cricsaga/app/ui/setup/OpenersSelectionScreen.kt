package com.cricsaga.app.ui.setup

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

    Scaffold(
        containerColor = CharcoalBackgroundDark,
        topBar = {
            TopAppBar(
                title = { Text("Select Opening Players", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = {
                        hapticManager?.tapUiClick()
                        onBackClick()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Striker Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("OPENING STRIKER ($battingTeamName)", color = CrimsonPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(battingSquad.filter { it.userId != nonStrikerId }) { p ->
                            FilterChip(
                                selected = strikerId == p.userId,
                                onClick = {
                                    hapticManager?.tapUiClick()
                                    strikerId = p.userId
                                },
                                label = { Text(p.fullName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CrimsonPrimaryDark,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Non-Striker Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("OPENING NON-STRIKER ($battingTeamName)", color = BoundaryAmber, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(battingSquad.filter { it.userId != strikerId }) { p ->
                            FilterChip(
                                selected = nonStrikerId == p.userId,
                                onClick = {
                                    hapticManager?.tapUiClick()
                                    nonStrikerId = p.userId
                                },
                                label = { Text(p.fullName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BoundaryAmber,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }
                }
            }

            // Opening Bowler Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("OPENING BOWLER ($bowlingTeamName)", color = BoundaryGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(bowlingSquad) { b ->
                            FilterChip(
                                selected = openingBowlerId == b.userId,
                                onClick = {
                                    hapticManager?.tapUiClick()
                                    openingBowlerId = b.userId
                                },
                                label = { Text(b.fullName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BoundaryGreen,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    hapticManager?.tapUiClick()
                    onStartMatch(strikerId, nonStrikerId, openingBowlerId)
                },
                enabled = strikerId.isNotEmpty() && nonStrikerId.isNotEmpty() && openingBowlerId.isNotEmpty() && strikerId != nonStrikerId,
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimaryDark),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("LET'S PLAY! START SCORING", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
