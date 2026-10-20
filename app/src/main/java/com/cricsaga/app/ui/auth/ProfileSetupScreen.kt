package com.cricsaga.app.ui.auth

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
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
import com.cricsaga.app.data.local.entity.UserEntity
import com.cricsaga.app.domain.model.BattingStyle
import com.cricsaga.app.domain.model.BowlingStyle
import com.cricsaga.app.domain.model.PlayingRole
import com.cricsaga.app.ui.theme.*
import com.cricsaga.app.ui.utils.ImageUtils
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    phoneNumber: String = "",
    initialFullName: String = "",
    initialEmail: String = "",
    onProfileCreated: (UserEntity) -> Unit
) {
    val context = LocalContext.current
    var fullName by remember { mutableStateOf(initialFullName) }
    var contactNumber by remember { mutableStateOf(phoneNumber) }
    var jerseyNumber by remember { mutableStateOf("18") }
    var city by remember { mutableStateOf("") }
    var profilePhotoUrl by remember { mutableStateOf<String?>(null) }
    var selectedRole by remember { mutableStateOf(PlayingRole.ALL_ROUNDER) }
    var selectedBattingStyle by remember { mutableStateOf(BattingStyle.RIGHT_HAND) }
    var selectedBowlingStyle by remember { mutableStateOf(BowlingStyle.RIGHT_ARM_MEDIUM) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val tempId = if (initialEmail.isNotEmpty()) initialEmail.replace("@", "_").replace(".", "_") else "new_user"
            val savedPath = ImageUtils.saveProfileImageFromUri(context, uri, tempId)
            if (savedPath != null) {
                profilePhotoUrl = savedPath
            }
        }
    }

    val initials = remember(fullName) {
        fullName.trim().split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .map { it.first().uppercaseChar() }
            .joinToString("")
            .ifEmpty { "P" }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Create Cricket Profile",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 18.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CharcoalBackgroundDark)
            )
        },
        bottomBar = {
            Surface(
                color = CharcoalBackgroundDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        if (fullName.trim().isEmpty()) {
                            errorMessage = "Please enter your full name"
                        } else {
                            val userId = if (initialEmail.isNotEmpty()) {
                                "user_" + initialEmail.replace("@", "_").replace(".", "_")
                            } else {
                                UUID.randomUUID().toString()
                            }
                            val newUser = UserEntity(
                                userId = userId,
                                phoneNumber = contactNumber.trim(),
                                fullName = fullName.trim(),
                                profilePhotoUrl = profilePhotoUrl,
                                playingRole = selectedRole,
                                battingStyle = selectedBattingStyle,
                                bowlingStyle = selectedBowlingStyle,
                                city = city.trim(),
                                jerseyNumber = jerseyNumber.trim().ifEmpty { "18" }
                            )
                            onProfileCreated(newUser)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimaryDark)
                ) {
                    Text(
                        text = "SAVE & ENTER CRICSAGA",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        },
        containerColor = CharcoalBackgroundDark
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Welcome to CricSaga! Set up your digital grassroots passport to track career stats, score live matches, and lead leaderboards.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // 1. Basic Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "PLAYER DETAILS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = BoundaryAmber,
                            letterSpacing = 1.sp
                        )

                        // Avatar photo uploader
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(ElevatedSurfaceDark)
                                .border(1.dp, CardBorderDark, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val bitmap = remember(profilePhotoUrl) {
                                ImageUtils.loadBitmapFromPath(profilePhotoUrl)
                            }

                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(SlateSurfaceDark)
                                    .border(1.5.dp, CrimsonPrimaryDark, CircleShape)
                                    .clickable {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap,
                                        contentDescription = "Profile Photo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text(
                                        text = initials,
                                        style = ScoreSmall,
                                        color = TextPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Athlete Passport Photo",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Tap to pick from gallery (optional)",
                                    fontSize = 11.sp,
                                    color = TextTertiary
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = CrimsonPrimaryDark,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (profilePhotoUrl != null) "Change" else "Upload",
                                    fontSize = 11.sp,
                                    color = CrimsonPrimaryDark
                                )
                            }
                        }

                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it; errorMessage = null },
                            label = { Text("Full Name * (e.g. Rohit Verma)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CrimsonPrimaryDark,
                                unfocusedBorderColor = CardBorderDark,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        OutlinedTextField(
                            value = contactNumber,
                            onValueChange = { contactNumber = it },
                            label = { Text("WhatsApp / Contact (Optional)") },
                            placeholder = { Text("For match invites & roster contact") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CrimsonPrimaryDark,
                                unfocusedBorderColor = CardBorderDark,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        if (errorMessage != null) {
                            Text(
                                text = errorMessage!!,
                                color = DismissalRed,
                                fontSize = 11.sp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = jerseyNumber,
                                onValueChange = { if (it.length <= 3) jerseyNumber = it },
                                label = { Text("Jersey #") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CrimsonPrimaryDark,
                                    unfocusedBorderColor = CardBorderDark,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            OutlinedTextField(
                                value = city,
                                onValueChange = { city = it },
                                label = { Text("City / Turf") },
                                singleLine = true,
                                modifier = Modifier.weight(2f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CrimsonPrimaryDark,
                                    unfocusedBorderColor = CardBorderDark,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                        }
                    }
                }
            }

            // 2. Playing Role Selection
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "PRIMARY PLAYING ROLE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = BoundaryAmber,
                            letterSpacing = 1.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val roles = listOf(
                                PlayingRole.BATSMAN to "Batter",
                                PlayingRole.BOWLER to "Bowler",
                                PlayingRole.ALL_ROUNDER to "All-Rounder",
                                PlayingRole.WICKET_KEEPER to "WK-Batter"
                            )
                            roles.forEach { (role, label) ->
                                val isSelected = selectedRole == role
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) CrimsonPrimaryDark else ElevatedSurfaceDark)
                                        .clickable { selectedRole = role }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Batting & Bowling Styles
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "BATTING STYLE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = BoundaryAmber,
                            letterSpacing = 1.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                BattingStyle.RIGHT_HAND to "Right-hand Bat",
                                BattingStyle.LEFT_HAND to "Left-hand Bat"
                            ).forEach { (style, label) ->
                                val isSelected = selectedBattingStyle == style
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) CrimsonPrimaryDark else ElevatedSurfaceDark)
                                        .clickable { selectedBattingStyle = style }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else TextSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "BOWLING STYLE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = BoundaryAmber,
                            letterSpacing = 1.sp
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            val bowlingStyles = listOf(
                                BowlingStyle.RIGHT_ARM_FAST to "Right-Arm Fast",
                                BowlingStyle.RIGHT_ARM_MEDIUM to "Right-Arm Medium",
                                BowlingStyle.RIGHT_ARM_SPIN to "Right-Arm Spin (Off/Leg Break)",
                                BowlingStyle.LEFT_ARM_FAST to "Left-Arm Fast",
                                BowlingStyle.LEFT_ARM_SPIN to "Left-Arm Spin"
                            )
                            bowlingStyles.forEach { (bStyle, bLabel) ->
                                val isSelected = selectedBowlingStyle == bStyle
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) ElevatedSurfaceDark else Color.Transparent)
                                        .clickable { selectedBowlingStyle = bStyle }
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedBowlingStyle = bStyle },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = CrimsonPrimaryDark,
                                            unselectedColor = TextTertiary
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = bLabel,
                                        fontSize = 13.sp,
                                        color = if (isSelected) TextPrimary else TextSecondary,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
