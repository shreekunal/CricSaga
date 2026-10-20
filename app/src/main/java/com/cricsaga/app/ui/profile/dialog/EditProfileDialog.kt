package com.cricsaga.app.ui.profile.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.cricsaga.app.data.local.entity.UserEntity
import com.cricsaga.app.domain.model.BattingStyle
import com.cricsaga.app.domain.model.BowlingStyle
import com.cricsaga.app.domain.model.PlayingRole
import com.cricsaga.app.ui.theme.*

@Composable
fun EditProfileDialog(
    user: UserEntity,
    onDismiss: () -> Unit,
    onSave: (UserEntity) -> Unit
) {
    var fullName by remember { mutableStateOf(user.fullName) }
    var jerseyNumber by remember { mutableStateOf(user.jerseyNumber) }
    var city by remember { mutableStateOf(user.city) }
    var selectedRole by remember { mutableStateOf(user.playingRole) }
    var selectedBattingStyle by remember { mutableStateOf(user.battingStyle) }
    var selectedBowlingStyle by remember { mutableStateOf(user.bowlingStyle) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                    text = "Update your digital cricket details",
                    fontSize = 12.sp,
                    color = TextTertiary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
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

                    item {
                        Text(
                            text = "PRIMARY ROLE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = BoundaryAmber,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                PlayingRole.BATSMAN to "Batter",
                                PlayingRole.BOWLER to "Bowler",
                                PlayingRole.ALL_ROUNDER to "All-Round",
                                PlayingRole.WICKET_KEEPER to "WK-Bat"
                            ).forEach { (role, label) ->
                                val isSelected = selectedRole == role
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) CrimsonPrimaryDark else ElevatedSurfaceDark)
                                        .clickable { selectedRole = role }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "BATTING STYLE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = BoundaryAmber,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                                        .padding(vertical = 8.dp),
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

                    item {
                        Text(
                            text = "BOWLING STYLE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = BoundaryAmber,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        listOf(
                            BowlingStyle.RIGHT_ARM_FAST to "Right-Arm Fast",
                            BowlingStyle.RIGHT_ARM_MEDIUM to "Right-Arm Medium",
                            BowlingStyle.RIGHT_ARM_SPIN to "Right-Arm Spin",
                            BowlingStyle.LEFT_ARM_FAST to "Left-Arm Fast",
                            BowlingStyle.LEFT_ARM_SPIN to "Left-Arm Spin"
                        ).forEach { (bStyle, bLabel) ->
                            val isSelected = selectedBowlingStyle == bStyle
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) ElevatedSurfaceDark else Color.Transparent)
                                    .clickable { selectedBowlingStyle = bStyle }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedBowlingStyle = bStyle },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = CrimsonPrimaryDark,
                                        unselectedColor = TextTertiary
                                    ),
                                    modifier = Modifier.size(20.dp)
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
                                    bowlingStyle = selectedBowlingStyle
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
