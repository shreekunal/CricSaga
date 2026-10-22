package com.cricsaga.app.ui.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricsaga.app.data.local.entity.ChatMessageEntity
import com.cricsaga.app.data.local.entity.UserEntity
import com.cricsaga.app.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    currentUserId: String,
    recipientUser: UserEntity,
    messages: List<ChatMessageEntity>,
    initialDraftText: String = "",
    onSendMessage: (text: String, isChallenge: Boolean, challengeDetails: String?) -> Unit,
    onViewProfileClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var messageInput by remember(initialDraftText) { mutableStateOf(initialDraftText) }
    var showChallengeDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val hapticManager = com.cricsaga.app.ui.utils.LocalHapticManager.current

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val initials = recipientUser.fullName.split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .uppercase()
        .ifEmpty { "CR" }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = {
                                hapticManager?.tapUiClick()
                                onViewProfileClick()
                            }),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(CrimsonPrimaryDark.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(initials, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CrimsonPrimaryDark)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = recipientUser.fullName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            val roleFormatted = recipientUser.playingRole.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
                            Text(
                                text = "$roleFormatted • ${recipientUser.city.ifEmpty { "Grassroots" }}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        hapticManager?.tapUiClick()
                        onBackClick()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    TextButton(onClick = {
                        hapticManager?.tapUiClick()
                        onViewProfileClick()
                    }) {
                        Text(
                            text = "PROFILE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = CrimsonPrimaryDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            hapticManager?.tapUiClick()
                            showChallengeDialog = true
                        }
                    ) {
                        Icon(
                            Icons.Default.SportsCricket,
                            contentDescription = "Send Match Challenge",
                            tint = AmberFourPrimary
                        )
                    }

                    OutlinedTextField(
                        value = messageInput,
                        onValueChange = { messageInput = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp),
                        placeholder = { Text("Message ${recipientUser.fullName.split(" ").firstOrNull() ?: ""}...", fontSize = 13.sp) },
                        maxLines = 4,
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CrimsonPrimaryDark,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    IconButton(
                        onClick = {
                            val trimmed = messageInput.trim()
                            if (trimmed.isNotEmpty()) {
                                hapticManager?.tapBoundaryFour()
                                onSendMessage(trimmed, false, null)
                                messageInput = ""
                                coroutineScope.launch {
                                    if (messages.isNotEmpty()) {
                                        listState.animateScrollToItem(messages.size)
                                    }
                                }
                            }
                        },
                        enabled = messageInput.trim().isNotEmpty()
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (messageInput.trim().isNotEmpty()) CrimsonPrimaryDark else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        if (messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Say hello to ${recipientUser.fullName}!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Coordinate weekend matches, turf slots, or cricket practice.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages, key = { it.messageId }) { msg ->
                    val isMine = msg.senderId == currentUserId
                    ChatMessageBubble(message = msg, isMine = isMine)
                }
            }
        }
    }

    if (showChallengeDialog) {
        var oversInput by remember { mutableStateOf("10") }
        var ballTypeInput by remember { mutableStateOf("Tennis Ball") }
        var venueInput by remember { mutableStateOf("Local Turf Ground") }
        var dateInput by remember { mutableStateOf("This Saturday, 8:00 PM") }

        AlertDialog(
            onDismissRequest = { showChallengeDialog = false },
            title = {
                Text("Send Match Challenge", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Challenge ${recipientUser.fullName} or their team to a match fixture:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = oversInput,
                        onValueChange = { oversInput = it },
                        label = { Text("Overs per side") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = ballTypeInput,
                        onValueChange = { ballTypeInput = it },
                        label = { Text("Ball Type (Tennis / Leather / Box)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = venueInput,
                        onValueChange = { venueInput = it },
                        label = { Text("Turf / Ground Venue") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = dateInput,
                        onValueChange = { dateInput = it },
                        label = { Text("Date & Timing") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        hapticManager?.tapBoundaryFour()
                        val challengeSummary = "$oversInput Overs • $ballTypeInput • $venueInput • $dateInput"
                        onSendMessage("Match Challenge: Would you be up for a $oversInput-over match fixture?", true, challengeSummary)
                        showChallengeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimaryDark),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("SEND CHALLENGE", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showChallengeDialog = false }) {
                    Text("CANCEL", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

@Composable
fun ChatMessageBubble(
    message: ChatMessageEntity,
    isMine: Boolean
) {
    val formattedTime = remember(message.timestamp) {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        sdf.format(Date(message.timestamp))
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
    ) {
        if (message.isChallenge && !message.challengeDetails.isNullOrBlank()) {
            Card(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .padding(vertical = 2.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isMine) CrimsonPrimaryDark else MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.SportsCricket,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (isMine) Color.White else AmberFourPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "MATCH CHALLENGE",
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            color = if (isMine) Color.White else AmberFourPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = message.challengeDetails,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = if (isMine) Color.White else MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = message.text,
                        fontSize = 12.sp,
                        color = if (isMine) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formattedTime,
                        fontSize = 10.sp,
                        color = if (isMine) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .padding(vertical = 2.dp),
                shape = RoundedCornerShape(
                    topStart = 14.dp,
                    topEnd = 14.dp,
                    bottomStart = if (isMine) 14.dp else 2.dp,
                    bottomEnd = if (isMine) 2.dp else 14.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isMine) CrimsonPrimaryDark else MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Text(
                        text = message.text,
                        fontSize = 13.sp,
                        color = if (isMine) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formattedTime,
                        fontSize = 10.sp,
                        color = if (isMine) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}
