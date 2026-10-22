package com.cricsaga.app.ui.home

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricsaga.app.data.local.entity.CommentEntity
import com.cricsaga.app.data.local.entity.MatchEntity
import com.cricsaga.app.data.local.entity.PostEntity
import com.cricsaga.app.domain.model.MatchStatus
import com.cricsaga.app.domain.model.PlayerRankingItem
import com.cricsaga.app.ui.theme.*
import com.cricsaga.app.ui.utils.ImageUtils
import kotlinx.coroutines.flow.Flow
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
    val ballType: String = "Tennis Light",
    val isLive: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    isPro: Boolean,
    currentUserId: String,
    currentUserName: String = "Rohit Sharma",
    currentUserAvatarUrl: String? = null,
    matches: List<MatchEntity>,
    posts: List<PostEntity>,
    topPlayersToFollow: List<PlayerRankingItem> = emptyList(),
    followedUserIds: Set<String> = emptySet(),
    unreadMessageCount: Int = 0,
    isDarkTheme: Boolean = true,
    onThemeToggleClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onMessagesClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onJoinPlusClick: () -> Unit = {},
    onRankingsClick: () -> Unit = {},
    onStartMatchClick: () -> Unit,
    onManageTeamsClick: () -> Unit,
    onProBadgeClick: () -> Unit,
    onMatchClick: (String) -> Unit,
    onPlayerProfileClick: (String) -> Unit,
    onToggleFollow: (String) -> Unit,
    onLikePostClick: (String) -> Unit,
    onCreatePost: (content: String, performanceTag: String?) -> Unit,
    onDirectMessageAuthor: (authorId: String, authorName: String) -> Unit,
    getCommentsForPost: ((String) -> Flow<List<CommentEntity>>)? = null,
    onAddComment: ((String, String) -> Unit)? = null,
    onDeletePost: ((String) -> Unit)? = null,
    onRepostPost: ((String) -> Unit)? = null
) {
    var selectedFeedTab by remember { mutableIntStateOf(0) } // 0: For You, 1: Following
    var showCreatePostDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var activeCommentPost by remember { mutableStateOf<PostEntity?>(null) }
    val hapticManager = com.cricsaga.app.ui.utils.LocalHapticManager.current

    val commentsForActivePost by produceState<List<CommentEntity>>(initialValue = emptyList(), activeCommentPost) {
        val currentPost = activeCommentPost
        if (currentPost != null && getCommentsForPost != null) {
            getCommentsForPost(currentPost.postId).collect { list ->
                value = list
            }
        } else {
            value = emptyList()
        }
    }

    val activeRadarMatches = remember(matches) {
        val liveOrRecent = matches.filter { it.matchStatus == MatchStatus.LIVE || it.matchStatus == MatchStatus.COMPLETED }
        liveOrRecent.map { m ->
            val parts = m.matchTitle.split(" vs ")
            val teamA = parts.getOrNull(0) ?: "Team A"
            val teamB = parts.getOrNull(1) ?: "Team B"
            val ballTypeStr = m.ballType.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
            LiveRadarItem(
                matchId = m.matchId,
                teamA = teamA,
                teamB = teamB,
                scoreText = if (m.matchStatus == MatchStatus.COMPLETED) m.resultSummary?.take(25) ?: "Match Completed" else "In Progress",
                oversText = "${m.totalOvers} ov",
                crrText = ballTypeStr,
                equationText = m.resultSummary ?: "${m.totalOvers} overs match",
                ballType = ballTypeStr,
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
            // Section 1: Clear Live Score Header & Sleek Actions
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Live Score",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                modifier = Modifier.clickable {
                                    hapticManager?.tapUiClick()
                                    onManageTeamsClick()
                                },
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp),
                                        tint = CrimsonPrimaryDark
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Teams",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier.clickable {
                                    hapticManager?.tapBoundaryFour()
                                    onStartMatchClick()
                                },
                                shape = RoundedCornerShape(16.dp),
                                color = CrimsonPrimaryDark
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Match",
                                        fontSize = 12.sp,
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

            // Section 1.5: Top Players to Follow Carousel
            if (topPlayersToFollow.isNotEmpty()) {
                item {
                    TopPlayersFollowCarousel(
                        players = topPlayersToFollow,
                        followedUserIds = followedUserIds,
                        onPlayerClick = onPlayerProfileClick,
                        onToggleFollow = onToggleFollow,
                        onViewRankingsClick = onRankingsClick
                    )
                }
            }

            // Section 2: Dual Feed Tabs (For You vs Following)
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
                        onMatchClick = { post.linkedMatchId?.let { onMatchClick(it) } },
                        onCommentClick = { activeCommentPost = post },
                        onRepostClick = { onRepostPost?.invoke(post.postId) },
                        onDeleteClick = { onDeletePost?.invoke(post.postId) }
                    )
                }
            }
        }
    }

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
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Challenge Request",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = CrimsonPrimaryDark
                            )
                            Text(
                                text = "Warriors challenged your squad to an 8-over weekend fixture.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Leaderboard Alert",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = AmberFourPrimary
                            )
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

    if (showCreatePostDialog) {
        CreatePostDialog(
            onDismiss = { showCreatePostDialog = false },
            onPost = { content, statTag ->
                onCreatePost(content, statTag)
                showCreatePostDialog = false
            }
        )
    }

    activeCommentPost?.let { post ->
        CommentsBottomSheet(
            post = post,
            comments = commentsForActivePost,
            onDismissRequest = { activeCommentPost = null },
            onAddComment = { text ->
                onAddComment?.invoke(post.postId, text)
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
            .width(320.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Teams, Ball Type & Live Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(CrimsonPrimaryDark.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.teamA.take(1).uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = CrimsonPrimaryDark
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "vs",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.teamB.take(1).uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${item.teamA} vs ${item.teamB}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                if (item.isLive) {
                    Surface(
                        color = CrimsonPrimaryDark,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .background(Color.White, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "LIVE",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                } else {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "FINAL",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body: Match Equation & Format Strip
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.equationText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${item.oversText} • ${item.ballType}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "View Scorecard",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TopPlayersFollowCarousel(
    players: List<PlayerRankingItem>,
    followedUserIds: Set<String>,
    onPlayerClick: (String) -> Unit,
    onToggleFollow: (String) -> Unit,
    onViewRankingsClick: () -> Unit
) {
    if (players.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Stars,
                        contentDescription = null,
                        tint = AmberFourPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TOP PLAYERS TO FOLLOW",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = "Discover top performers and connect with talent",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(
                onClick = onViewRankingsClick,
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
            ) {
                Text(
                    text = "See Rankings",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CrimsonPrimaryDark
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = CrimsonPrimaryDark,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(players, key = { it.userId }) { player ->
                val isFollowing = player.userId in followedUserIds
                TopPlayerFollowCard(
                    player = player,
                    isFollowing = isFollowing,
                    onCardClick = { onPlayerClick(player.userId) },
                    onToggleFollow = { onToggleFollow(player.userId) }
                )
            }
        }
    }
}

@Composable
fun TopPlayerFollowCard(
    player: PlayerRankingItem,
    isFollowing: Boolean,
    onCardClick: () -> Unit,
    onToggleFollow: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            color = when (player.rank) {
                                1 -> Color(0xFFD97706).copy(alpha = 0.2f)
                                2 -> Color(0xFF94A3B8).copy(alpha = 0.2f)
                                3 -> Color(0xFFB45309).copy(alpha = 0.2f)
                                else -> CrimsonPrimaryDark.copy(alpha = 0.15f)
                            },
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = player.initials,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (player.rank) {
                            1 -> Color(0xFFD97706)
                            2 -> Color(0xFF94A3B8)
                            3 -> Color(0xFFB45309)
                            else -> CrimsonPrimaryDark
                        }
                    )
                }
                if (player.isVerified) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified",
                        tint = AmberFourPrimary,
                        modifier = Modifier
                            .size(15.dp)
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = player.playerName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            Text(
                text = player.role,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = if (player.runs > 0) "${player.runs} Runs" else "${player.wickets} Wkts",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                onClick = onToggleFollow,
                shape = RoundedCornerShape(6.dp),
                color = if (isFollowing) MaterialTheme.colorScheme.surfaceVariant else CrimsonPrimaryDark,
                border = if (isFollowing) androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant) else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isFollowing) "Following" else "+ Follow",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isFollowing) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                    )
                }
            }
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
    onMatchClick: () -> Unit,
    onCommentClick: () -> Unit = {},
    onRepostClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null
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

                    Text(
                        text = "${post.authorRole} • ${post.authorCity}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!isMyPost) {
                    Surface(
                        modifier = Modifier.clickable {
                            hapticManager?.tapUiClick()
                            onToggleFollow()
                        },
                        shape = RoundedCornerShape(12.dp),
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
                } else if (onDeleteClick != null) {
                    IconButton(
                        onClick = {
                            hapticManager?.tapUiClick()
                            onDeleteClick()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Post",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
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
                        .clickable(enabled = post.linkedMatchId != null, onClick = onMatchClick),
                    shape = RoundedCornerShape(8.dp),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        hapticManager?.tapUiClick()
                        onCommentClick()
                    }
                ) {
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        hapticManager?.tapBoundaryFour()
                        onRepostClick?.invoke()
                    }
                ) {
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
    onPost: (content: String, performanceTag: String?) -> Unit
) {
    var contentText by remember { mutableStateOf("") }
    var performanceTagText by remember { mutableStateOf("") }
    val hapticManager = com.cricsaga.app.ui.utils.LocalHapticManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Create Cricket Post", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = contentText,
                    onValueChange = { contentText = it },
                    placeholder = { Text("What happened on the pitch today? Share match highlights, thoughts, or banter...", fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    shape = RoundedCornerShape(8.dp)
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Match Performance Tag",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = performanceTagText,
                        onValueChange = { performanceTagText = it },
                        placeholder = { Text("e.g. 54* (28) • 4/18", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
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
                            performanceTagText.trim().ifEmpty { null }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsBottomSheet(
    post: PostEntity,
    comments: List<CommentEntity>,
    onDismissRequest: () -> Unit,
    onAddComment: (String) -> Unit
) {
    var newCommentText by remember { mutableStateOf("") }
    val hapticManager = com.cricsaga.app.ui.utils.LocalHapticManager.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Comments (${comments.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "by ${post.authorName}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Post content snippet
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = post.content,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Comments List
            if (comments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No comments yet. Start the conversation!",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(comments, key = { it.commentId }) { comment ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            val authorInitials = comment.authorName.split(" ")
                                .mapNotNull { it.firstOrNull()?.toString() }
                                .take(2)
                                .joinToString("")
                                .uppercase()
                                .ifEmpty { "P" }

                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(CrimsonPrimaryDark.copy(alpha = 0.12f), CircleShape)
                                    .border(0.75.dp, CrimsonPrimaryDark.copy(alpha = 0.3f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = authorInitials,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CrimsonPrimaryDark
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = comment.authorName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = comment.text,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Add Comment Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newCommentText,
                    onValueChange = { newCommentText = it },
                    placeholder = { Text("Add a comment...", fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        val text = newCommentText.trim()
                        if (text.isNotEmpty()) {
                            hapticManager?.tapUiClick()
                            onAddComment(text)
                            newCommentText = ""
                        }
                    },
                    enabled = newCommentText.trim().isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Comment",
                        tint = if (newCommentText.trim().isNotEmpty()) CrimsonPrimaryDark else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}
