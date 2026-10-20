package com.cricsaga.app.ui.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    val lookingForList = listOf(
        LookingForItem(
            id = "lf_1",
            type = "Opponent",
            title = "Weekend Match • 16 Overs",
            teamOrOrganizer = "Warriors CC",
            timing = "Sunday • 7:00 AM - 11:00 AM",
            location = "Shivaji Park Ground #3, Dadar",
            note = "Leather ball. Umpire and pitch fees split 50/50.",
            contactPhone = "9820123456"
        ),
        LookingForItem(
            id = "lf_2",
            type = "Players",
            title = "Need 2 Batsmen for Tonight",
            teamOrOrganizer = "Metro Knights",
            timing = "Tonight • 9:30 PM - 11:00 PM",
            location = "Champions Turf, Andheri East",
            note = "8 overs box cricket under floodlights.",
            contactPhone = "9820987654"
        ),
        LookingForItem(
            id = "lf_3",
            type = "Scorer",
            title = "Certified Scorer for Corporate League",
            teamOrOrganizer = "Corporate Cup Committee",
            timing = "Saturday & Sunday • Full Day",
            location = "Oval Maidan, Churchgate",
            note = "₹1,200 per match. Digital scoring on CricSaga.",
            contactPhone = "9820555777"
        )
    )

    val turfsList = listOf(
        TurfItem(
            id = "turf_1",
            name = "Apex Arena Box Cricket",
            location = "Andheri West, Mumbai",
            distance = "1.8 km",
            surfaceType = "Astroturf",
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
            surfaceType = "Astroturf + Nets",
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
            surfaceType = "Natural Red Clay",
            hasLights = false,
            pricePerHour = "₹2,500/slot",
            rating = "4.7 ★",
            phone = "9820777888"
        )
    )

    val tourneysList = listOf(
        OpenTourneyItem(
            id = "trn_1",
            name = "Mumbai Champions Trophy 2026",
            organizer = "Grassroots Cricket Board",
            prizePool = "₹75,000 Cash",
            entryFee = "₹4,500 / team",
            format = "15 Overs Heavy Tennis (Knockout)",
            dates = "Nov 15 - Nov 20, 2026",
            location = "Shivaji Park, Mumbai"
        ),
        OpenTourneyItem(
            id = "trn_2",
            name = "Midnight Box League Season 4",
            organizer = "Turf Kings Association",
            prizePool = "₹30,000 Cash",
            entryFee = "₹2,200 / team",
            format = "8 Overs Box Cricket • Floodlights",
            dates = "Fridays in November",
            location = "Champions Arena, Andheri"
        )
    )

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(CharcoalBackgroundDark)) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Discover",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 20.sp
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = CharcoalBackgroundDark)
                )

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = CharcoalBackgroundDark,
                    contentColor = CrimsonPrimaryDark,
                    divider = { HorizontalDivider(color = DividerDark, thickness = 0.5.dp) },
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
                                    color = if (selectedTab == index) TextPrimary else TextSecondary
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
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = SlateSurfaceDark,
                                shape = RoundedCornerShape(8.dp),
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark))
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
                                            text = "Need players or opponent?",
                                            color = TextPrimary,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "Post a request to reach local teams.",
                                            color = TextTertiary,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Button(
                                        onClick = { /* Post requirement */ },
                                        colors = ButtonDefaults.buttonColors(containerColor = ElevatedSurfaceDark),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Post", color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        items(lookingForList) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                                shape = RoundedCornerShape(10.dp),
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            color = ElevatedSurfaceDark,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = item.type.uppercase(),
                                                color = TextSecondary,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(text = item.teamOrOrganizer, color = TextTertiary, fontSize = 11.sp)
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = item.title,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = item.timing, color = TextSecondary, fontSize = 11.sp)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = item.location, color = TextSecondary, fontSize = 11.sp)
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = item.note,
                                        color = TextTertiary,
                                        fontSize = 11.sp
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = { onContactWhatsApp(item.contactPhone, "Hi! I saw your post on CricSaga: ${item.title}") },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = ElevatedSurfaceDark),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(vertical = 8.dp)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Message on WhatsApp",
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 12.sp,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Grounds & Turfs
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
                                shape = RoundedCornerShape(10.dp),
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = turf.name,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = turf.rating,
                                            color = TextSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${turf.location} • ${turf.distance}",
                                        color = TextTertiary,
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
                                                color = TextSecondary,
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
                                                    text = "Floodlights",
                                                    color = TextSecondary,
                                                    fontSize = 10.sp,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = turf.pricePerHour,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )

                                        Button(
                                            onClick = { onContactWhatsApp(turf.phone, "Hi, I'd like to book a slot at ${turf.name}") },
                                            colors = ButtonDefaults.buttonColors(containerColor = ElevatedSurfaceDark),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text("Book Slot", color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 11.sp)
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
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(tourneysList) { tourney ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                                shape = RoundedCornerShape(10.dp),
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = tourney.name,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "By ${tourney.organizer} • ${tourney.location}",
                                        color = TextTertiary,
                                        fontSize = 11.sp
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Prize: ${tourney.prizePool}", color = TextSecondary, fontSize = 11.sp)
                                        Text("Entry: ${tourney.entryFee}", color = TextSecondary, fontSize = 11.sp)
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${tourney.format} • ${tourney.dates}",
                                        color = TextTertiary,
                                        fontSize = 11.sp
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = { /* Register team */ },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = ElevatedSurfaceDark),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(vertical = 8.dp)
                                    ) {
                                        Text("Register Team", color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 12.sp)
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
