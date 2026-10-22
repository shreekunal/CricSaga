package com.cricsaga.app.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricsaga.app.R
import com.cricsaga.app.ui.theme.*

@Composable
fun PhoneLoginScreen(
    onSignIn: (phoneNumber: String, password: String, onError: (String) -> Unit) -> Unit,
    onSignUp: (fullName: String, phoneNumber: String, password: String, onError: (String) -> Unit) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Sign In, 1 = Sign Up
    val hapticManager = com.cricsaga.app.ui.utils.LocalHapticManager.current

    // Sign In fields
    var loginPhone by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginPasswordVisible by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }
    var isSigningIn by remember { mutableStateOf(false) }

    // Sign Up fields
    var regFullName by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regConfirmPassword by remember { mutableStateOf("") }
    var regPasswordVisible by remember { mutableStateOf(false) }
    var regConfirmPasswordVisible by remember { mutableStateOf(false) }
    var regError by remember { mutableStateOf<String?>(null) }
    var isRegistering by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Brand Emblem
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher),
                        contentDescription = "CricSaga",
                        modifier = Modifier.size(46.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "CRICSAGA",
                    style = ScoreMedium,
                    letterSpacing = 2.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "HAR RUN KI KAHANI",
                    style = SectionLabel,
                    color = CrimsonPrimaryDark,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Text(
                    text = "Grassroots Cricket Platform",
                    style = BodyCaption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Main Authentication Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Segmented Tab Selector
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp)
                            ) {
                                // Sign In Tab Button
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            hapticManager?.tapUiClick()
                                            selectedTab = 0
                                            loginError = null
                                        },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (selectedTab == 0) CrimsonPrimaryDark else Color.Transparent
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Sign In",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selectedTab == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Sign Up Tab Button
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            hapticManager?.tapUiClick()
                                            selectedTab = 1
                                            regError = null
                                        },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (selectedTab == 1) CrimsonPrimaryDark else Color.Transparent
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Sign Up",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selectedTab == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        if (selectedTab == 0) {
                            // ----------------- SIGN IN VIEW -----------------
                            Text(
                                text = "Welcome Back",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "Enter your registered mobile number and password",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                            )

                            // Phone Number Field
                            OutlinedTextField(
                                value = loginPhone,
                                onValueChange = {
                                    val filtered = it.filter { ch -> ch.isDigit() }.take(10)
                                    loginPhone = filtered
                                    loginError = null
                                },
                                label = { Text("Mobile Number", fontSize = 12.sp) },
                                placeholder = { Text("9876543210", fontSize = 12.sp) },
                                leadingIcon = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(start = 12.dp, end = 6.dp)
                                    ) {
                                        Text("+91", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .width(1.dp)
                                                .height(16.dp)
                                                .background(MaterialTheme.colorScheme.outlineVariant)
                                        )
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Password Field
                            OutlinedTextField(
                                value = loginPassword,
                                onValueChange = {
                                    loginPassword = it
                                    loginError = null
                                },
                                label = { Text("Password", fontSize = 12.sp) },
                                placeholder = { Text("Enter your password", fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                },
                                trailingIcon = {
                                    IconButton(onClick = { loginPasswordVisible = !loginPasswordVisible }) {
                                        Icon(
                                            imageVector = if (loginPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = if (loginPasswordVisible) "Hide password" else "Show password",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                },
                                visualTransformation = if (loginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Error Display
                            if (loginError != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = loginError ?: "",
                                    color = DismissalRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Sign In Button
                            Button(
                                onClick = {
                                    val phoneTrimmed = loginPhone.trim()
                                    val passTrimmed = loginPassword.trim()
                                    if (phoneTrimmed.length < 10) {
                                        loginError = "Please enter a valid 10-digit mobile number."
                                        return@Button
                                    }
                                    if (passTrimmed.isEmpty()) {
                                        loginError = "Please enter your password."
                                        return@Button
                                    }
                                    hapticManager?.tapUiClick()
                                    isSigningIn = true
                                    onSignIn(phoneTrimmed, passTrimmed) { error ->
                                        isSigningIn = false
                                        loginError = error
                                    }
                                },
                                enabled = !isSigningIn && loginPhone.isNotBlank() && loginPassword.isNotBlank(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimaryDark)
                            ) {
                                if (isSigningIn) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = "SIGN IN",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 1-Tap Quick Test Credential Chip
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = androidx.compose.foundation.BorderStroke(0.75.dp, CrimsonPrimaryDark.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        hapticManager?.tapUiClick()
                                        loginPhone = "9876543210"
                                        loginPassword = "123456"
                                        loginError = null
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = CrimsonPrimaryDark,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Test Account: Rohit Sharma (9876543210)",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = CrimsonPrimaryDark
                                    )
                                }
                            }

                        } else {
                            // ----------------- SIGN UP VIEW -----------------
                            Text(
                                text = "Create Grassroots Account",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "Join CricSaga with direct mobile authentication",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                            )

                            // Full Name Field
                            OutlinedTextField(
                                value = regFullName,
                                onValueChange = {
                                    regFullName = it
                                    regError = null
                                },
                                label = { Text("Full Name", fontSize = 12.sp) },
                                placeholder = { Text("e.g. Virat Kohli", fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Mobile Number Field
                            OutlinedTextField(
                                value = regPhone,
                                onValueChange = {
                                    val filtered = it.filter { ch -> ch.isDigit() }.take(10)
                                    regPhone = filtered
                                    regError = null
                                },
                                label = { Text("Mobile Number", fontSize = 12.sp) },
                                placeholder = { Text("10-digit number", fontSize = 12.sp) },
                                leadingIcon = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(start = 12.dp, end = 6.dp)
                                    ) {
                                        Text("+91", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .width(1.dp)
                                                .height(16.dp)
                                                .background(MaterialTheme.colorScheme.outlineVariant)
                                        )
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Password Field
                            OutlinedTextField(
                                value = regPassword,
                                onValueChange = {
                                    regPassword = it
                                    regError = null
                                },
                                label = { Text("Password", fontSize = 12.sp) },
                                placeholder = { Text("At least 4 characters", fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                },
                                trailingIcon = {
                                    IconButton(onClick = { regPasswordVisible = !regPasswordVisible }) {
                                        Icon(
                                            imageVector = if (regPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                },
                                visualTransformation = if (regPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Confirm Password Field
                            OutlinedTextField(
                                value = regConfirmPassword,
                                onValueChange = {
                                    regConfirmPassword = it
                                    regError = null
                                },
                                label = { Text("Confirm Password", fontSize = 12.sp) },
                                placeholder = { Text("Re-enter password", fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                },
                                trailingIcon = {
                                    IconButton(onClick = { regConfirmPasswordVisible = !regConfirmPasswordVisible }) {
                                        Icon(
                                            imageVector = if (regConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                },
                                visualTransformation = if (regConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Error Display
                            if (regError != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = regError ?: "",
                                    color = DismissalRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Create Account Button
                            Button(
                                onClick = {
                                    val name = regFullName.trim()
                                    val phone = regPhone.trim()
                                    val pass = regPassword.trim()
                                    val confPass = regConfirmPassword.trim()

                                    if (name.isEmpty()) {
                                        regError = "Please enter your full name."
                                        return@Button
                                    }
                                    if (phone.length < 10) {
                                        regError = "Please enter a valid 10-digit mobile number."
                                        return@Button
                                    }
                                    if (pass.length < 4) {
                                        regError = "Password must be at least 4 characters."
                                        return@Button
                                    }
                                    if (pass != confPass) {
                                        regError = "Passwords do not match."
                                        return@Button
                                    }

                                    hapticManager?.tapUiClick()
                                    isRegistering = true
                                    onSignUp(name, phone, pass) { error ->
                                        isRegistering = false
                                        regError = error
                                    }
                                },
                                enabled = !isRegistering && regFullName.isNotBlank() && regPhone.isNotBlank() && regPassword.isNotBlank(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimaryDark)
                            ) {
                                if (isRegistering) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = "CREATE ACCOUNT",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Footer Terms & Security Note
            Text(
                text = "Instant grassroots mobile authentication • CricSaga 2026",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}
