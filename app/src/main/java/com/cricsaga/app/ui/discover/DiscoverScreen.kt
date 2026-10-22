package com.cricsaga.app.ui.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricsaga.app.ui.theme.*

data class LookingForItem(
    val id: String,
    val type: String,
    val title: String,
    val teamOrOrganizer: String,
    val timing: String,
    val location: String,
    val note: String,
    val contactPhone: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    isPro: Boolean,
    lookingForList: List<LookingForItem> = emptyList(),
    onPostRequirement: (type: String, title: String, organizer: String, timing: String, location: String, note: String, phone: String) -> Unit = { _, _, _, _, _, _, _ -> },
    onContactDirectChat: (title: String, organizer: String, phone: String) -> Unit = { _, _, _ -> },
    onContactWhatsApp: (String, String) -> Unit = { _, _ -> },
    onRankingsClick: () -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var showPostDialog by remember { mutableStateOf(false) }

    val filterOptions = listOf("All", "Urgent / Today", "Players", "Opponents", "Scorers", "Umpires")

    val filteredList = remember(lookingForList, selectedFilter, searchQuery) {
        lookingForList.filter { item ->
            val matchesFilter = when (selectedFilter) {
                "All" -> true
                "Urgent / Today" -> item.timing.contains("Tonight", ignoreCase = true) ||
                        item.timing.contains("Today", ignoreCase = true) ||
                        item.note.contains("Urgent", ignoreCase = true)
                "Players" -> item.type.contains("Player", ignoreCase = true)
                "Opponents" -> item.type.contains("Opponent", ignoreCase = true)
                "Scorers" -> item.type.contains("Scorer", ignoreCase = true)
                "Umpires" -> item.type.contains("Umpire", ignoreCase = true)
                else -> true
            }

            val matchesSearch = if (searchQuery.isBlank()) true else {
                item.title.contains(searchQuery, ignoreCase = true) ||
                        item.teamOrOrganizer.contains(searchQuery, ignoreCase = true) ||
                        item.location.contains(searchQuery, ignoreCase = true) ||
                        item.note.contains(searchQuery, ignoreCase = true)
            }

            matchesFilter && matchesSearch
        }
    }

    val hapticManager = com.cricsaga.app.ui.utils.LocalHapticManager.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    hapticManager?.tapUiClick()
                    showPostDialog = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = "Post Requirement", tint = Color.White) },
                text = { Text("POST REQUIREMENT", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp) },
                containerColor = CrimsonPrimaryDark,
                shape = RoundedCornerShape(24.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                Text(
                    text = "Discover",
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Grassroots Matchmaker",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                placeholder = { Text("Search by player role, team, or location...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CrimsonPrimaryDark,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                filterOptions.forEach { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            hapticManager?.tapUiClick()
                            selectedFilter = filter
                        },
                        label = {
                            Text(
                                text = filter,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (filter == "Urgent / Today") DarkAlertWicket else CrimsonPrimaryDark,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Regional Leaderboards Scouting Banner
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clickable { onRankingsClick() },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, AmberFourPrimary.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = AmberFourPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Regional Rankings & Leaderboards",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Scout top batters, bowlers & club standings",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "EXPLORE >",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberFourPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No Matchmaking Posts Found",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Need players for tonight's match, an opponent team, or an umpire? Post your requirement above!",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList, key = { it.id }) { item ->
                        MatchmakingCard(
                            item = item,
                            onMessageClick = {
                                onContactDirectChat(item.title, item.teamOrOrganizer, item.contactPhone)
                            },
                            onWhatsAppClick = {
                                val message = "Hi! I saw your post on CricSaga: \"${item.title}\" for ${item.teamOrOrganizer}. I am interested in connecting!"
                                onContactWhatsApp(item.contactPhone, message)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showPostDialog) {
        PostRequirementDialog(
            onDismiss = { showPostDialog = false },
            onPost = { type, title, organizer, timing, location, note, phone ->
                onPostRequirement(type, title, organizer, timing, location, note, phone)
                showPostDialog = false
            }
        )
    }
}

@Composable
fun MatchmakingCard(
    item: LookingForItem,
    onMessageClick: () -> Unit,
    onWhatsAppClick: () -> Unit
) {
    val hapticManager = com.cricsaga.app.ui.utils.LocalHapticManager.current
    val isUrgent = item.timing.contains("Tonight", ignoreCase = true) || item.timing.contains("Today", ignoreCase = true)

    val tagColor = when {
        isUrgent -> DarkAlertWicket
        item.type.contains("Opponent", ignoreCase = true) -> CrimsonPrimaryDark
        item.type.contains("Player", ignoreCase = true) -> AmberFourPrimary
        item.type.contains("Scorer", ignoreCase = true) -> ForestSixPrimary
        else -> DarkBrandPrimary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = tagColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = item.type.uppercase(),
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        color = tagColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                if (isUrgent) {
                    Surface(
                        color = DarkAlertWicket.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "URGENT",
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = DarkAlertWicket,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Posted by ${item.teamOrOrganizer}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(5.dp))
                Text(item.timing, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(3.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(5.dp))
                Text(item.location, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (item.note.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.note,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        hapticManager?.tapUiClick()
                        onMessageClick()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("MESSAGE", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                }

                Button(
                    onClick = {
                        hapticManager?.tapUiClick()
                        onWhatsAppClick()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestSixPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("WHATSAPP", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun PostRequirementDialog(
    onDismiss: () -> Unit,
    onPost: (type: String, title: String, organizer: String, timing: String, location: String, note: String, phone: String) -> Unit
) {
    val types = listOf("Player", "Opponent", "Scorer", "Umpire")
    var selectedType by remember { mutableStateOf("Player") }
    var titleText by remember { mutableStateOf("") }
    var organizerText by remember { mutableStateOf("") }
    var timingText by remember { mutableStateOf("") }
    var locationText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var contactPhoneText by remember { mutableStateOf("") }
    val hapticManager = com.cricsaga.app.ui.utils.LocalHapticManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Post Requirement", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Requirement Category", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    types.forEach { t ->
                        FilterChip(
                            selected = selectedType == t,
                            onClick = {
                                hapticManager?.tapUiClick()
                                selectedType = t
                            },
                            label = { Text(t, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CrimsonPrimaryDark,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = titleText,
                    onValueChange = { titleText = it },
                    label = { Text("Title (e.g. Need 2 Batsmen for Tonight)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = organizerText,
                    onValueChange = { organizerText = it },
                    label = { Text("Team / Organizer Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = timingText,
                    onValueChange = { timingText = it },
                    label = { Text("Timing (e.g. Tonight 9 PM - 11 PM)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = locationText,
                    onValueChange = { locationText = it },
                    label = { Text("Venue / Ground Location") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Notes (Ball type, match format, fees)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                OutlinedTextField(
                    value = contactPhoneText,
                    onValueChange = { contactPhoneText = it },
                    label = { Text("WhatsApp Contact Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    hapticManager?.tapUiClick()
                    onPost(
                        selectedType,
                        titleText.trim().ifEmpty { "Cricket Requirement • $selectedType" },
                        organizerText.trim().ifEmpty { "Cricket Team" },
                        timingText.trim().ifEmpty { "Weekend Match" },
                        locationText.trim().ifEmpty { "Local Turf Ground" },
                        noteText.trim(),
                        contactPhoneText.trim().ifEmpty { "9820000000" }
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimaryDark),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("POST REQUIREMENT", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
