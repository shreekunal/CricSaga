package com.cricsaga.app.ui.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricsaga.app.data.local.entity.MatchEntity
import com.cricsaga.app.data.local.entity.PostEntity
import com.cricsaga.app.domain.model.MatchStatus
import com.cricsaga.app.ui.theme.*
import com.cricsaga.app.ui.utils.ImageUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class GrassrootsHeroItem(
    val playerName: String,
    val initials: String,
    val teamName: String,
    val performance: String,
    val context: String
)

data class LiveRadarItem(
    val matchId: String,
    val teamA: String,
    val teamB: String,
    val scoreText: String,
    val oversText: String,
    val crrText: String,
    val equationText: String,
    val isLive: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    isPro: Boolean,
    currentUserId: String,
    matches: List<MatchEntity>,
    posts: List<PostEntity>,
    followedUserIds: Set<String> = emptySet(),
    unreadMessageCount: Int = 0,
    isDarkTheme: Boolean = true,
    onThemeToggleClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onMessagesClick: () -> Unit = {},
    onRankingsClick: () -> Unit = {},
    onStartMatchClick: () -> Unit,
    onManageTeamsClick: () -> Unit,
    onProBadgeClick: () -> Unit,
    onMatchClick: (String) -> Unit,
    onPlayerProfileClick: (String) -> Unit,
    onToggleFollow: (String) -> Unit,
    onLikePostClick: (String) -> Unit,
    onCreatePost: (content: String, performanceTag: String?, imageUri: String?) -> Unit,
    onDirectMessageAuthor: (authorId: String, authorName: String) -> Unit
) {
    var selectedFeedTab by remember { mutableIntStateOf(0) } // 0: For You, 1: Following
    var showCreatePostDialog by remember { mutableStateOf(false) }
    val hapticManager = com.cricsaga.app.ui.utils.LocalHapticManager.current

    val activeRadarMatches = remember(matches) {
        val liveOrRecent = matches.filter { it.matchStatus == MatchStatus.LIVE || it.matchStatus == MatchStatus.COMPLETED }
        liveOrRecent.map { m ->
            val parts = m.matchTitle.split(" vs ")
            val teamA = parts.getOrNull(0) ?: "Team A"
            val teamB = parts.getOrNull(1) ?: "Team B"
            LiveRadarItem(
                matchId = m.matchId,
                teamA = teamA,
                teamB = teamB,
                scoreText = if (m.matchStatus == MatchStatus.COMPLETED) m.resultSummary?.take(18) ?: "Final" else "Live",
                oversText = "${m.totalOvers} ov",
                crrText = m.ballType.name.replace("_", " "),
                equationText = m.resultSummary ?: "${m.totalOvers} overs match",
                isLive = m.matchStatus == MatchStatus.LIVE
            )
        }
    }

    val displayPosts = remember(posts, selectedFeedTab, followedUserIds) {
        if (selectedFeedTab == 0) {
            posts
        } else {
            posts.filter { it.authorId in followedUserIds || it.authorId == currentUserId }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "CricSaga",
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            if (isPro) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = AmberFourPrimary.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "PRO",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp,
                                        color = AmberFourPrimary,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Grassroots Cricket Social Feed",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
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

                    // Theme Switcher Toggle
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    hapticManager?.tapUiClick()
                    showCreatePostDialog = true
                },
                icon = { Icon(Icons.Default.Edit, contentDescription = "Post", tint = Color.White) },
                text = { Text("POST", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp) },
                containerColor = CrimsonPrimaryDark,
                shape = RoundedCornerShape(24.dp)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Section 1: Compact Live Match & Scorer Ticker
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LIVE RADAR & SCORING",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        hapticManager?.tapUiClick()
                                        onRankingsClick()
                                    },
                                color = AmberFourPrimary.copy(alpha = 0.18f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = AmberFourPrimary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "RANKINGS",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AmberFourPrimary
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        hapticManager?.tapUiClick()
                                        onManageTeamsClick()
                                    },
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "TEAMS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        hapticManager?.tapBoundaryFour()
                                        onStartMatchClick()
                                    },
                                color = CrimsonPrimaryDark
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "SCORE MATCH",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (activeRadarMatches.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(activeRadarMatches, key = { it.matchId }) { item ->
                                CompactMatchRadarCard(item = item, onClick = { onMatchClick(item.matchId) })
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onStartMatchClick() },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.SportsCricket, contentDescription = null, modifier = Modifier.size(16.dp), tint = CrimsonPrimaryDark)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("No live matches right now. Tap to start scoring!", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // Section 2: Post Composition Quick Bar
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable {
                            hapticManager?.tapUiClick()
                            showCreatePostDialog = true
                        },
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
                                .size(34.dp)
                                .background(CrimsonPrimaryDark.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SportsCricket, contentDescription = null, modifier = Modifier.size(16.dp), tint = CrimsonPrimaryDark)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "What's happening on the pitch? Post an update...",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Section 3: Dual Feed Tabs (For You vs Following)
            item {
                TabRow(
                    selectedTabIndex = selectedFeedTab,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = CrimsonPrimaryDark,
                    divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), thickness = 0.5.dp) }
                ) {
                    Tab(
                        selected = selectedFeedTab == 0,
                        onClick = {
                            hapticManager?.tapUiClick()
                            selectedFeedTab = 0
                        },
                        text = {
                            Text(
                                text = "For You",
                                fontWeight = if (selectedFeedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedFeedTab == 0) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                        }
                    )
                    Tab(
                        selected = selectedFeedTab == 1,
                        onClick = {
                            hapticManager?.tapUiClick()
                            selectedFeedTab = 1
                        },
                        text = {
                            Text(
                                text = "Following",
                                fontWeight = if (selectedFeedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedFeedTab == 1) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Section 4: Feed Post Cards
            if (displayPosts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (selectedFeedTab == 0) "No Posts Yet" else "No Posts from Following",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (selectedFeedTab == 0) "Be the first cricketer to publish an update on the feed!"
                                else "Follow other players from their profile or the 'For You' feed to see their posts here.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(displayPosts, key = { it.postId }) { post ->
                    val isFollowing = post.authorId in followedUserIds
                    CricketPostCard(
                        post = post,
                        isMyPost = post.authorId == currentUserId,
                        isFollowing = isFollowing,
                        onAuthorClick = { onPlayerProfileClick(post.authorId) },
                        onToggleFollow = { onToggleFollow(post.authorId) },
                        onLikeClick = { onLikePostClick(post.postId) },
                        onMessageAuthor = { onDirectMessageAuthor(post.authorId, post.authorName) },
                        onMatchClick = { post.linkedMatchId?.let { onMatchClick(it) } }
                    )
                }
            }
        }
    }

    if (showCreatePostDialog) {
        CreatePostDialog(
            onDismiss = { showCreatePostDialog = false },
            onPost = { content, statTag, imageUri ->
                onCreatePost(content, statTag, imageUri)
                showCreatePostDialog = false
            }
        )
    }
}

@Composable
fun CompactMatchRadarCard(
    item: LiveRadarItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${item.teamA} vs ${item.teamB}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (item.isLive) {
                    Surface(color = CrimsonPrimaryDark, shape = RoundedCornerShape(4.dp)) {
                        Text("LIVE", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(item.equationText, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}

@Composable
fun CricketPostCard(
    post: PostEntity,
    isMyPost: Boolean,
    isFollowing: Boolean,
    onAuthorClick: () -> Unit,
    onToggleFollow: () -> Unit,
    onLikeClick: () -> Unit,
    onMessageAuthor: () -> Unit,
    onMatchClick: () -> Unit
) {
    val hapticManager = com.cricsaga.app.ui.utils.LocalHapticManager.current
    val formattedTime = remember(post.createdAt) {
        val diff = System.currentTimeMillis() - post.createdAt
        when {
            diff < 60000 -> "Just now"
            diff < 3600000 -> "${diff / 60000}m"
            diff < 86400000 -> "${diff / 3600000}h"
            else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(post.createdAt))
        }
    }

    val initials = post.authorName.split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .uppercase()
        .ifEmpty { "CR" }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Author Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(CrimsonPrimaryDark.copy(alpha = 0.2f), CircleShape)
                        .clickable(onClick = onAuthorClick),
                    contentAlignment = Alignment.Center
                ) {
                    Text(initials, fontWeight = FontWeight.Black, fontSize = 14.sp, color = CrimsonPrimaryDark)
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(onClick = onAuthorClick)
                    ) {
                        Text(
                            text = post.authorName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.Verified,
                            contentDescription = "Verified Player",
                            modifier = Modifier.size(14.dp),
                            tint = AmberFourPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• $formattedTime",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!post.authorTeamName.isNullOrBlank()) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = post.authorTeamName,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = "${post.authorRole} • ${post.authorCity}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (!isMyPost) {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                hapticManager?.tapUiClick()
                                onToggleFollow()
                            },
                        color = if (isFollowing) MaterialTheme.colorScheme.surfaceVariant else CrimsonPrimaryDark.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (isFollowing) "FOLLOWING" else "+ FOLLOW",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isFollowing) MaterialTheme.colorScheme.onSurfaceVariant else CrimsonPrimaryDark,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Text Content
            Text(
                text = post.content,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
            )

            // Performance Tag Highlight
            if (!post.performanceTag.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(enabled = post.linkedMatchId != null, onClick = onMatchClick),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.SportsCricket, contentDescription = null, modifier = Modifier.size(14.dp), tint = AmberFourPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = post.performanceTag,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberFourPrimary
                        )
                        if (post.linkedMatchId != null) {
                            Spacer(modifier = Modifier.weight(1f))
                            Text("VIEW SCORECARD >", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Attached Photo Image (if present)
            if (!post.imageUri.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                val context = LocalContext.current
                val bitmap = remember(post.imageUri) { ImageUtils.decodeUriToBitmap(context, Uri.parse(post.imageUri)) }
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = "Post Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Bar (Like, Comment, Repost, Message)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        hapticManager?.tapBoundaryFour()
                        onLikeClick()
                    }
                ) {
                    Icon(
                        imageVector = if (post.isLikedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        modifier = Modifier.size(16.dp),
                        tint = if (post.isLikedByMe) CrimsonPrimaryDark else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (post.likeCount > 0) post.likeCount.toString() else "",
                        fontSize = 11.sp,
                        color = if (post.isLikedByMe) CrimsonPrimaryDark else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Comment Button
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Comments",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (post.commentCount > 0) post.commentCount.toString() else "",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Repost Button
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = "Repost",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (post.repostCount > 0) post.repostCount.toString() else "",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Message / Share Button
                IconButton(
                    onClick = {
                        hapticManager?.tapUiClick()
                        onMessageAuthor()
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "Direct Message",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun CreatePostDialog(
    onDismiss: () -> Unit,
    onPost: (content: String, performanceTag: String?, imageUri: String?) -> Unit
) {
    var contentText by remember { mutableStateOf("") }
    var performanceTagText by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val hapticManager = com.cricsaga.app.ui.utils.LocalHapticManager.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Create Cricket Post", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = contentText,
                    onValueChange = { contentText = it },
                    placeholder = { Text("What happened on the pitch today? Share match highlights, thoughts, or banter...", fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = performanceTagText,
                    onValueChange = { performanceTagText = it },
                    label = { Text("Match Performance Tag (optional)") },
                    placeholder = { Text("e.g. 54* (28) • 4/18", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = {
                            hapticManager?.tapUiClick()
                            photoPickerLauncher.launch("image/*")
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (selectedImageUri != null) "Photo Attached" else "Attach Photo", fontSize = 11.sp)
                    }

                    if (selectedImageUri != null) {
                        TextButton(onClick = { selectedImageUri = null }) {
                            Text("Remove", fontSize = 11.sp, color = CrimsonPrimaryDark)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmed = contentText.trim()
                    if (trimmed.isNotEmpty()) {
                        hapticManager?.tapBoundaryFour()
                        onPost(
                            trimmed,
                            performanceTagText.trim().ifEmpty { null },
                            selectedImageUri?.toString()
                        )
                    }
                },
                enabled = contentText.trim().isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimaryDark),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("PUBLISH POST", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
