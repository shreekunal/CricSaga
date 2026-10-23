package com.cricsaga.app.ui.teams.dialog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cricsaga.app.data.local.entity.UserEntity
import com.cricsaga.app.domain.model.PlayingRole
import com.cricsaga.app.ui.theme.*
import com.cricsaga.app.ui.utils.LocalHapticManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlayerToTeamDialog(
    teamName: String,
    currentSquadUserIds: Set<String>,
    allUsers: List<UserEntity>,
    onDismiss: () -> Unit,
    onAddExistingUser: (userId: String) -> Unit,
    onCreateAndAddNewPlayer: (fullName: String, email: String, role: PlayingRole, jerseyNumber: String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val hapticManager = LocalHapticManager.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            color = SlateSurfaceDark,
            tonalElevation = 8.dp,
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Add Player",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Assign squad member to $teamName",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = {
                        hapticManager?.tapUiClick()
                        onDismiss()
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tabs: Existing vs Register New
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = ElevatedSurfaceDark,
                    contentColor = TextPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = CrimsonPrimaryDark,
                            height = 3.dp
                        )
                    },
                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = {
                            hapticManager?.tapUiClick()
                            selectedTab = 0
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.PersonSearch,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (selectedTab == 0) CrimsonPrimaryDark else TextTertiary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Existing Athlete",
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            hapticManager?.tapUiClick()
                            selectedTab = 1
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (selectedTab == 1) CrimsonPrimaryDark else TextTertiary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Register New",
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tab Content
                if (selectedTab == 0) {
                    ExistingAthleteTab(
                        currentSquadUserIds = currentSquadUserIds,
                        allUsers = allUsers,
                        onSelectUser = { userId ->
                            hapticManager?.tapUiClick()
                            onAddExistingUser(userId)
                            onDismiss()
                        },
                        onSwitchToRegister = {
                            hapticManager?.tapUiClick()
                            selectedTab = 1
                        }
                    )
                } else {
                    RegisterNewAthleteTab(
                        onDismiss = onDismiss,
                        onRegister = { name, email, role, jersey ->
                            hapticManager?.tapUiClick()
                            onCreateAndAddNewPlayer(name, email, role, jersey)
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ExistingAthleteTab(
    currentSquadUserIds: Set<String>,
    allUsers: List<UserEntity>,
    onSelectUser: (userId: String) -> Unit,
    onSwitchToRegister: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    // Filter out users already in this squad, only after user enters at least 2 search characters (to protect servers and avoid upfront list clutter)
    val trimmedQuery = searchQuery.trim().lowercase()
    val isSearchActive = trimmedQuery.length >= 2

    val availableUsers = remember(allUsers, currentSquadUserIds, trimmedQuery) {
        if (!isSearchActive) {
            emptyList()
        } else {
            allUsers.filter { user ->
                user.userId !in currentSquadUserIds &&
                        (user.fullName.lowercase().contains(trimmedQuery) ||
                                (user.email?.lowercase()?.contains(trimmedQuery) == true) ||
                                user.phoneNumber.contains(trimmedQuery))
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by name, Gmail, or phone...", fontSize = 13.sp) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(18.dp))
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextTertiary, modifier = Modifier.size(16.dp))
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = CrimsonPrimaryDark,
                unfocusedBorderColor = CardBorderDark,
                focusedContainerColor = ElevatedSurfaceDark,
                unfocusedContainerColor = ElevatedSurfaceDark
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (!isSearchActive) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonSearch,
                        contentDescription = null,
                        tint = CrimsonPrimaryDark.copy(alpha = 0.8f),
                        modifier = Modifier.size(42.dp)
                    )
                    Text(
                        text = "Search Athletes",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Type at least 2 characters of their name, Gmail, or phone to find registered players.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = onSwitchToRegister,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonPrimaryDark),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonPrimaryDark.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Or Register New Athlete")
                    }
                }
            }
        } else if (availableUsers.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "No athlete matches \"$searchQuery\"",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    OutlinedButton(
                        onClick = onSwitchToRegister,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonPrimaryDark),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonPrimaryDark),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Register Athlete with Gmail")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableUsers, key = { it.userId }) { user ->
                    AthleteItemCard(
                        user = user,
                        onClick = { onSelectUser(user.userId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AthleteItemCard(
    user: UserEntity,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ElevatedSurfaceDark),
        shape = RoundedCornerShape(10.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Avatar initials circle
                val initials = user.fullName.split(" ")
                    .mapNotNull { it.firstOrNull()?.toString() }
                    .take(2)
                    .joinToString("")
                    .uppercase()
                    .ifEmpty { "P" }

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(CrimsonPrimaryDark.copy(alpha = 0.2f))
                        .border(1.dp, CrimsonPrimaryDark.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = CrimsonPrimaryDark
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = user.fullName,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (user.jerseyNumber.isNotBlank()) {
                            Surface(
                                color = CharcoalBackgroundDark,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "#${user.jerseyNumber}",
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    val subtitle = user.email?.takeIf { it.isNotBlank() }
                        ?: user.phoneNumber.takeIf { it.isNotBlank() }
                        ?: "Registered Athlete"

                    Text(
                        text = subtitle,
                        color = TextTertiary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Role badge & Add action
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val (roleLabel, roleColor) = when (user.playingRole) {
                    PlayingRole.BATSMAN -> "BAT" to BoundaryGreen
                    PlayingRole.BOWLER -> "BOWL" to CrimsonPrimaryDark
                    PlayingRole.ALL_ROUNDER -> "ALL" to BoundaryAmber
                    PlayingRole.WICKET_KEEPER -> "WK" to ExtraBlue
                }

                Surface(
                    color = roleColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = roleLabel,
                        color = roleColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    color = BoundaryGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, BoundaryGreen.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.GroupAdd, contentDescription = null, tint = BoundaryGreen, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add",
                            color = BoundaryGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RegisterNewAthleteTab(
    onDismiss: () -> Unit,
    onRegister: (fullName: String, email: String, role: PlayingRole, jerseyNumber: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var jerseyNumber by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(PlayingRole.ALL_ROUNDER) }

    val isEmailValid = email.isBlank() || (email.contains("@") && email.contains("."))
    val canSubmit = name.trim().isNotBlank() && email.trim().isNotBlank() && isEmailValid

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Gmail Single Source of Truth banner
            Surface(
                color = ExtraBlue.copy(alpha = 0.15f),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, ExtraBlue.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = null,
                        tint = BoundaryAmber,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Gmail is our single source of truth. When this athlete logs in via Google with this email, their team roster links automatically without needing a phone number upfront.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 15.sp
                    )
                }
            }

            // Athlete Full Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name *", fontSize = 12.sp) },
                placeholder = { Text("e.g. Rohit Sharma", fontSize = 13.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = CrimsonPrimaryDark,
                    unfocusedBorderColor = CardBorderDark,
                    focusedContainerColor = ElevatedSurfaceDark,
                    unfocusedContainerColor = ElevatedSurfaceDark
                )
            )

            // Gmail Address
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Athlete's Gmail / Email *", fontSize = 12.sp) },
                placeholder = { Text("e.g. rohit.sharma@gmail.com", fontSize = 13.sp) },
                isError = email.isNotBlank() && !isEmailValid,
                supportingText = {
                    if (email.isNotBlank() && !isEmailValid) {
                        Text("Enter a valid email address", color = DismissalRed, fontSize = 11.sp)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = CrimsonPrimaryDark,
                    unfocusedBorderColor = CardBorderDark,
                    focusedContainerColor = ElevatedSurfaceDark,
                    unfocusedContainerColor = ElevatedSurfaceDark
                )
            )

            // Jersey Number
            OutlinedTextField(
                value = jerseyNumber,
                onValueChange = { if (it.length <= 3 && it.all { ch -> ch.isDigit() }) jerseyNumber = it },
                label = { Text("Jersey Number (Optional)", fontSize = 12.sp) },
                placeholder = { Text("e.g. 45", fontSize = 13.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = CrimsonPrimaryDark,
                    unfocusedBorderColor = CardBorderDark,
                    focusedContainerColor = ElevatedSurfaceDark,
                    unfocusedContainerColor = ElevatedSurfaceDark
                )
            )

            // Playing Role Selection
            Text(
                text = "Playing Role:",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    PlayingRole.BATSMAN to "BAT",
                    PlayingRole.BOWLER to "BOWL",
                    PlayingRole.ALL_ROUNDER to "ALL",
                    PlayingRole.WICKET_KEEPER to "WK"
                ).forEach { (role, label) ->
                    val isSelected = selectedRole == role
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) CrimsonPrimaryDark else ElevatedSurfaceDark)
                            .border(1.dp, if (isSelected) CrimsonPrimaryDark else CardBorderDark, RoundedCornerShape(6.dp))
                            .clickable { selectedRole = role }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (isSelected) Color.White else TextSecondary
                        )
                    }
                }
            }
        }

        // Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderDark)
            ) {
                Text("CANCEL")
            }

            Button(
                onClick = {
                    if (canSubmit) {
                        onRegister(
                            name.trim(),
                            email.trim().lowercase(),
                            selectedRole,
                            jerseyNumber.trim().ifEmpty { "18" }
                        )
                    }
                },
                enabled = canSubmit,
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CrimsonPrimaryDark,
                    disabledContainerColor = ElevatedSurfaceDark
                )
            ) {
                Text(
                    text = "ADD TO TEAM",
                    fontWeight = FontWeight.Bold,
                    color = if (canSubmit) Color.White else TextTertiary
                )
            }
        }
    }
}
