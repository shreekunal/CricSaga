package com.cricsaga.app.ui.discover

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricsaga.app.R
import com.cricsaga.app.ui.theme.*

data class LookingForItem(
    val id: String,
    val type: String, // "OPPONENT", "PLAYER", "SCORER"
    val title: String,
    val teamOrOrganizer: String,
    val timing: String,
    val location: String,
    val note: String,
    val contactPhone: String
)

data class TurfItem(
    val id: String,
    val name: String,
    val location: String,
    val distance: String,
    val surfaceType: String,
    val hasLights: Boolean,
    val pricePerHour: String,
    val rating: String,
    val phone: String
)

data class OpenTourneyItem(
    val id: String,
    val name: String,
    val organizer: String,
    val prizePool: String,
    val entryFee: String,
    val format: String,
    val dates: String,
    val location: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    isPro: Boolean,
    onContactWhatsApp: (String, String) -> Unit = { _, _ -> }
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Looking For", "Grounds & Turfs", "Tournaments")

    // Sample Grassroots Community Data
    val lookingForList = listOf(
        LookingForItem(
            id = "lf_1",
            type = "OPPONENT",
            title = "Need Opponent for Weekend Friendly",
            teamOrOrganizer = "Warriors CC",
            timing = "Sunday • 7:00 AM - 11:00 AM",
            location = "Shivaji Park Ground #3, Dadar",
            note = "16 Overs Leather Ball. Umpire & pitch fees split 50-50.",
            contactPhone = "9820123456"
        ),
        LookingForItem(
            id = "lf_2",
            type = "PLAYER",
            title = "Need 2 Batsmen for Tonight Box Match",
            teamOrOrganizer = "Metro Knights",
            timing = "Tonight • 9:30 PM - 11:00 PM",
            location = "Champions Turf, Andheri East",
            note = "Fast-paced 8 overs box cricket. Immediate confirmation.",
            contactPhone = "9820987654"
        ),
        LookingForItem(
            id = "lf_3",
            type = "SCORER",
            title = "Certified Scorer Needed for Corporate League",
            teamOrOrganizer = "Corporate Cup Committee",
            timing = "Saturday & Sunday • Full Day",
            location = "Oval Maidan, Churchgate",
            note = "₹1,200 per match + Refreshments. CricSaga digital scoring.",
            contactPhone = "9820555777"
        )
    )

    val turfsList = listOf(
        TurfItem(
            id = "turf_1",
            name = "Apex Arena Box Cricket",
            location = "Andheri West, Mumbai",
            distance = "1.8 km",
            surfaceType = "FIFA Grade Astroturf",
            hasLights = true,
            pricePerHour = "₹900/hr",
            rating = "4.8 ★",
            phone = "9820111222"
        ),
        TurfItem(
            id = "turf_2",
            name = "Skyline Turf & Nets",
            location = "Kandivali East, Mumbai",
            distance = "4.2 km",
            surfaceType = "Astroturf + Practice Nets",
            hasLights = true,
            pricePerHour = "₹1,100/hr",
            rating = "4.9 ★",
            phone = "9820333444"
        ),
        TurfItem(
            id = "turf_3",
            name = "Shivaji Gymkhana Cricket Ground",
            location = "Dadar, Mumbai",
            distance = "6.5 km",
            surfaceType = "Natural Red Clay Turf",
            hasLights = false,
            pricePerHour = "₹2,500/session",
            rating = "4.7 ★",
            phone = "9820777888"
        )
    )

    val tourneysList = listOf(
        OpenTourneyItem(
            id = "trn_1",
            name = "Mumbai Grassroots Champions Trophy 2026",
            organizer = "Mumbai Grassroots Cricket Board",
            prizePool = "₹75,000 Cash + Trophy",
            entryFee = "₹4,500 per team",
            format = "15 Overs Heavy Tennis Ball (Knockout)",
            dates = "Nov 15 - Nov 20, 2026",
            location = "Shivaji Park, Mumbai"
        ),
        OpenTourneyItem(
            id = "trn_2",
            name = "Midnight Box Cricket League Season 4",
            organizer = "Turf Kings Association",
            prizePool = "₹30,000 Cash",
            entryFee = "₹2,200 per team",
            format = "8 Overs Box Cricket • Under Floodlights",
            dates = "Every Friday Night in November",
            location = "Champions Arena, Andheri"
        )
    )

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(CharcoalBackgroundDark)) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.cricsaga_logo),
                                contentDescription = "CricSaga Logo",
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Discover Cricket Hub",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 18.sp
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = CharcoalBackgroundDark)
                )

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = SlateSurfaceDark,
                    contentColor = CrimsonPrimaryDark,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = CrimsonPrimaryDark
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == index) Color.White else MutedGrey
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Minimal Ad Banner for Free tier
            if (!isPro) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = ElevatedSurfaceDark),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(0xFF333333),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "SPONSOR",
                                color = Color.Gray,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pro Sports Gear: Get 10% off match tennis balls. Code: SAGA10",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // "Looking For" Feed
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF231F14)),
                                shape = RoundedCornerShape(10.dp),
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BoundaryAmber))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "NEED PLAYERS OR OPPONENT?",
                                            color = BoundaryAmber,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "Post a request to reach 1,000+ local players instantly.",
                                            color = Color.LightGray,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Button(
                                        onClick = { /* Open Post Dialog */ },
                                        colors = ButtonDefaults.buttonColors(containerColor = BoundaryAmber),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("+ POST", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        items(lookingForList) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val badgeColor = when (item.type) {
                                            "OPPONENT" -> CrimsonPrimaryDark
                                            "PLAYER" -> BoundaryAmber
                                            else -> BoundaryGreen
                                        }
                                        Surface(
                                            color = badgeColor.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = item.type,
                                                color = badgeColor,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(text = item.teamOrOrganizer, color = MutedGrey, fontSize = 11.sp)
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = item.title,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = MutedGrey, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = item.timing, color = Color.LightGray, fontSize = 11.sp)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MutedGrey, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = item.location, color = Color.LightGray, fontSize = 11.sp)
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = item.note,
                                        color = Color.Gray,
                                        fontSize = 11.sp
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = { onContactWhatsApp(item.contactPhone, "Hi! I saw your requirement on CricSaga: ${item.title}") },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(vertical = 8.dp)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "CONTACT ON WHATSAPP",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Grounds & Turfs Directory
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(turfsList) { turf ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = turf.name,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Surface(
                                            color = Color(0x334CAF50),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = turf.rating,
                                                color = BoundaryGreen,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${turf.location} • ${turf.distance}",
                                        color = MutedGrey,
                                        fontSize = 11.sp
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            color = ElevatedSurfaceDark,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = turf.surfaceType,
                                                color = Color.LightGray,
                                                fontSize = 10.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        if (turf.hasLights) {
                                            Surface(
                                                color = ElevatedSurfaceDark,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "⚡ Floodlights",
                                                    color = BoundaryAmber,
                                                    fontSize = 10.sp,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Pricing", color = MutedGrey, fontSize = 10.sp)
                                            Text(
                                                text = turf.pricePerHour,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                        }

                                        Button(
                                            onClick = { onContactWhatsApp(turf.phone, "Hi, I'd like to book a slot at ${turf.name}") },
                                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimaryDark),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                        ) {
                                            Text("BOOK SLOT", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Open Tournaments Tab
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(tourneysList) { tourney ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = tourney.name,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Surface(
                                            color = Color(0x33FFB300),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "OPEN",
                                                color = BoundaryAmber,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "By ${tourney.organizer} • ${tourney.location}",
                                        color = MutedGrey,
                                        fontSize = 11.sp
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("Prize Pool", color = MutedGrey, fontSize = 10.sp)
                                            Text(tourney.prizePool, color = BoundaryGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Entry Fee", color = MutedGrey, fontSize = 10.sp)
                                            Text(tourney.entryFee, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "${tourney.format} • ${tourney.dates}",
                                        color = Color.LightGray,
                                        fontSize = 11.sp
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = { /* Register team flow */ },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimaryDark),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("REGISTER TEAM", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
