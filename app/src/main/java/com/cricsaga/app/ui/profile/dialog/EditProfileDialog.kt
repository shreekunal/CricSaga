package com.cricsaga.app.ui.profile.dialog

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
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.window.Dialog
import com.cricsaga.app.data.local.entity.UserEntity
import com.cricsaga.app.domain.model.BattingStyle
import com.cricsaga.app.domain.model.BowlingStyle
import com.cricsaga.app.domain.model.PlayingRole
import com.cricsaga.app.ui.theme.*
import com.cricsaga.app.ui.utils.ImageUtils

@Composable
fun EditProfileDialog(
    user: UserEntity,
    onDismiss: () -> Unit,
    onSave: (UserEntity) -> Unit
) {
    val context = LocalContext.current
    var fullName by remember { mutableStateOf(user.fullName) }
    var jerseyNumber by remember { mutableStateOf(user.jerseyNumber) }
    var city by remember { mutableStateOf(user.city) }
    var selectedRole by remember { mutableStateOf(user.playingRole) }
    var selectedBattingStyle by remember { mutableStateOf(user.battingStyle) }
    var selectedBowlingStyle by remember { mutableStateOf(user.bowlingStyle) }
    var profilePhotoUrl by remember { mutableStateOf(user.profilePhotoUrl) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = ImageUtils.saveProfileImageFromUri(context, uri, user.userId)
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

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SlateSurfaceDark),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(CardBorderDark)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Edit Cricket Profile",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
                Text(
                    text = "Update your digital cricket details and photo",
                    fontSize = 12.sp,
                    color = TextTertiary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 440.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Profile Photo Uploader Row
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(ElevatedSurfaceDark)
                                .border(1.dp, CardBorderDark, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val bitmap = remember(profilePhotoUrl) {
                                ImageUtils.loadBitmapFromPath(profilePhotoUrl)
                            }

                            Box(
                                modifier = Modifier
                                    .size(60.dp)
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
                                    text = "Profile Photo",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = if (profilePhotoUrl != null) "Custom photo loaded" else "Athletic initials shown",
                                    fontSize = 11.sp,
                                    color = TextTertiary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            photoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimaryDark.copy(alpha = 0.2f)),
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
                                            fontWeight = FontWeight.Bold,
                                            color = CrimsonPrimaryDark
                                        )
                                    }

                                    if (profilePhotoUrl != null) {
                                        OutlinedButton(
                                            onClick = {
                                                ImageUtils.deleteProfileImage(profilePhotoUrl)
                                                profilePhotoUrl = null
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = null,
                                                tint = DismissalRed,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Remove",
                                                fontSize = 11.sp,
                                                color = DismissalRed
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it; errorMessage = null },
                            label = { Text("Full Name *") },
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
                            Text(text = errorMessage!!, color = DismissalRed, fontSize = 11.sp)
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
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

                    // Playing Role Selector
                    item {
                        Text(
                            text = "PRIMARY PLAYING ROLE",
                            style = SectionLabel,
                            color = TextTertiary,
                            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PlayingRole.values().forEach { role ->
                                val isSelected = selectedRole == role
                                val roleLabel = when (role) {
                                    PlayingRole.BATSMAN -> "BAT"
                                    PlayingRole.BOWLER -> "BOWL"
                                    PlayingRole.ALL_ROUNDER -> "ALL"
                                    PlayingRole.WICKET_KEEPER -> "WK"
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) CrimsonPrimaryDark else ElevatedSurfaceDark)
                                        .border(
                                            1.dp,
                                            if (isSelected) CrimsonPrimaryDark else CardBorderDark,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable { selectedRole = role }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = roleLabel,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isSelected) Color.White else TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    // Batting Style Selector
                    item {
                        Text(
                            text = "BATTING HAND",
                            style = SectionLabel,
                            color = TextTertiary,
                            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                BattingStyle.RIGHT_HAND to "Right Hand Bat (RHB)",
                                BattingStyle.LEFT_HAND to "Left Hand Bat (LHB)"
                            ).forEach { (style, label) ->
                                val isSelected = selectedBattingStyle == style
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) CrimsonPrimaryDark.copy(alpha = 0.15f) else ElevatedSurfaceDark)
                                        .border(
                                            1.dp,
                                            if (isSelected) CrimsonPrimaryDark else CardBorderDark,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable { selectedBattingStyle = style }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedBattingStyle = style },
                                        colors = RadioButtonDefaults.colors(selectedColor = CrimsonPrimaryDark),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (style == BattingStyle.RIGHT_HAND) "RHB" else "LHB",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = if (isSelected) TextPrimary else TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    // Bowling Style Selector
                    item {
                        Text(
                            text = "BOWLING STYLE",
                            style = SectionLabel,
                            color = TextTertiary,
                            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                        )
                        listOf(
                            BowlingStyle.RIGHT_ARM_FAST to "Right Arm Fast / Medium",
                            BowlingStyle.RIGHT_ARM_SPIN to "Right Arm Spin / Leg Spin",
                            BowlingStyle.LEFT_ARM_FAST to "Left Arm Fast / Seam",
                            BowlingStyle.LEFT_ARM_SPIN to "Left Arm Spin / Orthodox"
                        ).forEach { (bStyle, bLabel) ->
                            val isSelected = selectedBowlingStyle == bStyle
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) CrimsonPrimaryDark.copy(alpha = 0.12f) else ElevatedSurfaceDark)
                                    .border(
                                        1.dp,
                                        if (isSelected) CrimsonPrimaryDark else CardBorderDark,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { selectedBowlingStyle = bStyle }
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedBowlingStyle = bStyle },
                                    colors = RadioButtonDefaults.colors(selectedColor = CrimsonPrimaryDark),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = bLabel,
                                    fontSize = 12.sp,
                                    color = if (isSelected) TextPrimary else TextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("CANCEL", color = TextSecondary, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (fullName.trim().isEmpty()) {
                                errorMessage = "Name cannot be empty"
                            } else {
                                val updated = user.copy(
                                    fullName = fullName.trim(),
                                    jerseyNumber = jerseyNumber.trim().ifEmpty { "00" },
                                    city = city.trim(),
                                    playingRole = selectedRole,
                                    battingStyle = selectedBattingStyle,
                                    bowlingStyle = selectedBowlingStyle,
                                    profilePhotoUrl = profilePhotoUrl
                                )
                                onSave(updated)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimaryDark),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("SAVE CHANGES", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
