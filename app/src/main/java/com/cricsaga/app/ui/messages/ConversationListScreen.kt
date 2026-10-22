package com.cricsaga.app.ui.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.Close
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
import com.cricsaga.app.data.local.entity.UserEntity
import com.cricsaga.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ConversationSummary(
    val otherUser: UserEntity,
    val lastMessage: String,
    val lastTimestamp: Long,
    val isChallenge: Boolean = false,
    val unreadCount: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationListScreen(
    conversations: List<ConversationSummary>,
    allUsers: List<UserEntity>,
    onConversationClick: (UserEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showNewChatDialog by remember { mutableStateOf(false) }
    var newChatSearchQuery by remember { mutableStateOf("") }

    val filteredConversations = remember(conversations, searchQuery) {
        if (searchQuery.isBlank()) conversations
        else {
            conversations.filter {
                it.otherUser.fullName.contains(searchQuery, ignoreCase = true) ||
                        it.otherUser.city.contains(searchQuery, ignoreCase = true) ||
                        it.lastMessage.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val hapticManager = com.cricsaga.app.ui.utils.LocalHapticManager.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Messages",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Direct Player Conversations",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        hapticManager?.tapUiClick()
                        showNewChatDialog = true
                    }) {
                        Icon(
                            Icons.Default.AddComment,
                            contentDescription = "New Message",
                            tint = CrimsonPrimaryDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search conversations...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
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

            if (filteredConversations.isEmpty()) {
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
                            Icon(
                                Icons.AutoMirrored.Filled.Chat,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = CrimsonPrimaryDark
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Messages Yet",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Message any player directly from their profile, the cricket feed, or start a new chat below.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    hapticManager?.tapUiClick()
                                    showNewChatDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimaryDark),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("START NEW CHAT", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredConversations, key = { it.otherUser.userId }) { conv ->
                        ConversationItemRow(
                            conversation = conv,
                            onClick = {
                                hapticManager?.tapUiClick()
                                onConversationClick(conv.otherUser)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showNewChatDialog) {
        val selectableUsers = remember(allUsers, newChatSearchQuery) {
            if (newChatSearchQuery.isBlank()) allUsers
            else allUsers.filter {
                it.fullName.contains(newChatSearchQuery, ignoreCase = true) ||
                        it.city.contains(newChatSearchQuery, ignoreCase = true)
            }
        }

        AlertDialog(
            onDismissRequest = { showNewChatDialog = false },
            title = {
                Text("Start New Conversation", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp)) {
                    OutlinedTextField(
                        value = newChatSearchQuery,
                        onValueChange = { newChatSearchQuery = it },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        placeholder = { Text("Search players by name...", fontSize = 12.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(selectableUsers, key = { it.userId }) { user ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        hapticManager?.tapUiClick()
                                        showNewChatDialog = false
                                        onConversationClick(user)
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(CrimsonPrimaryDark.copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val initials = user.fullName.split(" ")
                                        .mapNotNull { it.firstOrNull()?.toString() }
                                        .take(2)
                                        .joinToString("")
                                        .uppercase()
                                        .ifEmpty { "CR" }
                                    Text(initials, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CrimsonPrimaryDark)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(user.fullName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                    val roleText = user.playingRole.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
                                    Text("$roleText • ${user.city.ifEmpty { "Grassroots" }}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showNewChatDialog = false }) {
                    Text("CANCEL", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

@Composable
fun ConversationItemRow(
    conversation: ConversationSummary,
    onClick: () -> Unit
) {
    val user = conversation.otherUser
    val initials = user.fullName.split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .uppercase()
        .ifEmpty { "CR" }

    val formattedTime = remember(conversation.lastTimestamp) {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        sdf.format(Date(conversation.lastTimestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(CrimsonPrimaryDark.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(initials, fontWeight = FontWeight.Black, fontSize = 15.sp, color = CrimsonPrimaryDark)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = user.fullName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = formattedTime,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                val previewPrefix = if (conversation.isChallenge) "Match Challenge: " else ""
                Text(
                    text = "$previewPrefix${conversation.lastMessage}",
                    fontSize = 12.sp,
                    color = if (conversation.isChallenge) AmberFourPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    fontWeight = if (conversation.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
                )
            }

            if (conversation.unreadCount > 0) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(CrimsonPrimaryDark, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = conversation.unreadCount.toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
