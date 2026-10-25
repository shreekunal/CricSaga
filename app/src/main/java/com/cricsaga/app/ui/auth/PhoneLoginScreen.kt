package com.cricsaga.app.ui.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cricsaga.app.R
import com.cricsaga.app.domain.model.PlayingRole
import com.cricsaga.app.ui.theme.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneLoginScreen(
    onGoogleSignInSuccess: suspend (
        account: GoogleSignInAccount,
        onNeedsProfileCompletion: (googleId: String, name: String, email: String, photoUrl: String?) -> Unit
    ) -> Unit,
    onCompleteProfile: suspend (
        googleId: String,
        name: String,
        email: String,
        photoUrl: String?,
        phoneNumber: String,
        city: String,
        role: PlayingRole
    ) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val activity = remember(context) {
        var ctx = context
        while (ctx is android.content.ContextWrapper) {
            if (ctx is Activity) return@remember ctx
            ctx = ctx.baseContext
        }
        null
    }

    val hapticManager = com.cricsaga.app.ui.utils.LocalHapticManager.current
    val scrollState = rememberScrollState()

    var isBusy by remember { mutableStateOf(false) }
    var busyTitle by remember { mutableStateOf("Signing In") }
    var busySubtitle by remember { mutableStateOf("Connecting to Google...") }
    var authErrorMessage by remember { mutableStateOf<String?>(null) }

    // First-time Profile Completion Dialog States
    var showProfileCompletionDialog by remember { mutableStateOf(false) }
    var pendingGoogleId by remember { mutableStateOf("") }
    var pendingGoogleName by remember { mutableStateOf("") }
    var pendingGoogleEmail by remember { mutableStateOf("") }
    var pendingGooglePhotoUrl by remember { mutableStateOf<String?>(null) }

    var compPhone by remember { mutableStateOf("") }
    var compCity by remember { mutableStateOf("Mumbai") }
    var compRole by remember { mutableStateOf(PlayingRole.ALL_ROUNDER) }
    var compError by remember { mutableStateOf<String?>(null) }

    // Google Play Services Sign-In Client
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .build()
    }
    val googleSignInClient = remember(context, activity) {
        GoogleSignIn.getClient(activity ?: context, gso)
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (data != null) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    authErrorMessage = null
                    isBusy = true
                    busyTitle = "Authenticating Player"
                    busySubtitle = "Syncing credentials & cloud profile..."
                    coroutineScope.launch {
                        try {
                            onGoogleSignInSuccess(account) { gId, gName, gEmail, gPhoto ->
                                isBusy = false
                                pendingGoogleId = gId
                                pendingGoogleName = gName
                                pendingGoogleEmail = gEmail
                                pendingGooglePhotoUrl = gPhoto
                                showProfileCompletionDialog = true
                            }
                        } catch (e: Exception) {
                            isBusy = false
                            authErrorMessage = "Sign in error: ${e.localizedMessage}"
                        }
                    }
                } else {
                    isBusy = false
                    authErrorMessage = "Could not retrieve Google account details. Please try again."
                }
            } catch (e: ApiException) {
                isBusy = false
                authErrorMessage = when (e.statusCode) {
                    10 -> "Google Setup Required: App SHA-1 is not registered in Google Cloud Console (Error 10: DEVELOPER_ERROR)."
                    12500 -> "Google Sign-In failed (Error 12500). Please check Google Play Services on your device."
                    12501 -> null // User dismissed the picker dialog
                    7 -> "Network error: Unable to reach Google servers. Please check your internet connection."
                    else -> "Google Sign-In failed (${e.statusCode}): ${e.localizedMessage}"
                }
            } catch (e: Exception) {
                isBusy = false
                authErrorMessage = "Sign in error: ${e.localizedMessage}"
            }
        } else {
            // Data was null; user cancelled without selecting
            isBusy = false
            authErrorMessage = null
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 22.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Brand Emblem
                Image(
                    painter = painterResource(id = R.drawable.logo_round),
                    contentDescription = "CricSaga",
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "CRICSAGA",
                    style = ScoreMedium,
                    letterSpacing = 2.5.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "HAR RUN KI KAHANI",
                    style = SectionLabel,
                    color = CrimsonPrimaryDark,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Text(
                    text = "Grassroots Cricket Platform • Single Source of Truth",
                    style = BodyCaption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(26.dp))

                // Modern Auth Container
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = SlateSurfaceDark,
                    border = BorderStroke(1.dp, CardBorderDark),
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Athlete Access",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Sign in directly with your Google account. Your verified Google name and profile picture are automatically synced to match scorecards and club lineups.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 17.sp,
                            modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
                        )

                        // Feature Highlights
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AuthFeatureBullet(
                                icon = Icons.Default.LockReset,
                                title = "Zero Password Hassle",
                                subtitle = "No passwords to remember, create, or reset."
                            )
                            AuthFeatureBullet(
                                icon = Icons.Default.AccountCircle,
                                title = "Google Photo Synced",
                                subtitle = "Your real avatar appears on scorecards and leaderboards."
                            )
                            AuthFeatureBullet(
                                icon = Icons.Default.SportsCricket,
                                title = "Ad-Free Ground Scoring",
                                subtitle = "Unlimited ball-by-ball matches and squad rosters."
                            )
                        }

                        Spacer(modifier = Modifier.height(22.dp))

                        // PRIMARY ACTION: Continue with Google Button
                        Button(
                            onClick = {
                                hapticManager?.tapUiClick()
                                authErrorMessage = null
                                isBusy = true
                                busyTitle = "Connecting to Google"
                                busySubtitle = "Opening Google Sign-In..."
                                // Explicitly sign out first to clear cached account session in Google Play Services.
                                // This ensures the Google Account Chooser bottom sheet is always displayed,
                                // enabling seamless account switching between multiple Gmail accounts.
                                googleSignInClient.signOut().addOnCompleteListener {
                                    try {
                                        googleSignInLauncher.launch(googleSignInClient.signInIntent)
                                    } catch (e: Exception) {
                                        isBusy = false
                                        authErrorMessage = "Could not open Google sign-in: ${e.localizedMessage}"
                                    }
                                }
                            },
                            enabled = !isBusy,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF1F1F1F)
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                GoogleBrandBadge()
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "CONTINUE WITH GOOGLE",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    letterSpacing = 0.5.sp,
                                    color = Color(0xFF1F1F1F)
                                )
                            }
                        }

                        // Error message if Google auth fails
                        if (authErrorMessage != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = authErrorMessage ?: "",
                                color = DismissalRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Footer
            Text(
                text = "Single Source of Truth Verified • CricSaga 2026",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }

        // =========================================================================
        // Full-Screen Authentication Busy Screen
        // =========================================================================
        if (isBusy) {
            Dialog(
                onDismissRequest = { /* Block dismissal while auth is active */ },
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false,
                    usePlatformDefaultWidth = false
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.75f)),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = SlateSurfaceDark,
                        border = BorderStroke(1.dp, CardBorderDark),
                        shadowElevation = 16.dp,
                        modifier = Modifier
                            .padding(horizontal = 32.dp)
                            .fillMaxWidth(0.85f)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 28.dp, horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(62.dp)
                                    .clip(CircleShape)
                                    .background(CrimsonPrimaryDark.copy(alpha = 0.14f))
                                    .border(1.5.dp, CrimsonPrimaryDark.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.logo_round),
                                    contentDescription = "CricSaga",
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            CircularProgressIndicator(
                                modifier = Modifier.size(34.dp),
                                color = CrimsonPrimaryDark,
                                strokeWidth = 3.dp
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            Text(
                                text = busyTitle,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = busySubtitle,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // =========================================================================
        // First-Time Match Profile Setup Dialog
        // =========================================================================
        if (showProfileCompletionDialog) {
            AlertDialog(
                onDismissRequest = { /* Require user to complete */ },
                containerColor = SlateSurfaceDark,
                shape = RoundedCornerShape(16.dp),
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(CrimsonPrimaryDark.copy(alpha = 0.15f))
                                .border(1.dp, BoundaryAmber, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SportsCricket, contentDescription = null, tint = BoundaryAmber, modifier = Modifier.size(26.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "One Final Step!",
                            style = PlayerNamePrimary,
                            color = TextPrimary,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Welcome $pendingGoogleName",
                            style = BodyCaption,
                            color = BoundaryAmber,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "To list your match performances in local club tournaments and turf scorecards, enter your 10-digit mobile number.",
                            style = BodyCaption,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )

                        // Phone Number
                        OutlinedTextField(
                            value = compPhone,
                            onValueChange = {
                                val filtered = it.filter { ch -> ch.isDigit() }.take(10)
                                compPhone = filtered
                                compError = null
                            },
                            label = { Text("Mobile Number", fontSize = 12.sp) },
                            placeholder = { Text("10-digit mobile number", fontSize = 12.sp) },
                            leadingIcon = {
                                Text("+91", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.padding(start = 12.dp, end = 4.dp))
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // City
                        OutlinedTextField(
                            value = compCity,
                            onValueChange = { compCity = it },
                            label = { Text("Home City / Region", fontSize = 12.sp) },
                            placeholder = { Text("e.g. Mumbai, Bengaluru, Delhi", fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Role Selector
                        Text(
                            text = "PRIMARY PLAYING ROLE",
                            style = MicroTag,
                            color = BoundaryAmber
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                PlayingRole.BATSMAN to "Bat",
                                PlayingRole.BOWLER to "Bowl",
                                PlayingRole.ALL_ROUNDER to "All-Rnd",
                                PlayingRole.WICKET_KEEPER to "WK"
                            ).forEach { (role, label) ->
                                val isSelected = compRole == role
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { compRole = role },
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSelected) CrimsonPrimaryDark else ElevatedSurfaceDark,
                                    border = BorderStroke(0.5.dp, if (isSelected) CrimsonPrimaryDark else CardBorderDark)
                                ) {
                                    Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                        Text(label, style = MicroTag, color = if (isSelected) Color.White else TextSecondary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        if (compError != null) {
                            Text(compError ?: "", color = DismissalRed, style = BodyCaption)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val phoneTrimmed = compPhone.trim()
                            if (phoneTrimmed.length < 10) {
                                compError = "Please enter a valid 10-digit mobile number."
                                return@Button
                            }
                            hapticManager?.tapBoundaryFour()
                            showProfileCompletionDialog = false
                            isBusy = true
                            busyTitle = "Setting Up Athlete Profile"
                            busySubtitle = "Saving credentials & cloud roster sync..."
                            coroutineScope.launch {
                                try {
                                    onCompleteProfile(
                                        pendingGoogleId,
                                        pendingGoogleName,
                                        pendingGoogleEmail,
                                        pendingGooglePhotoUrl,
                                        phoneTrimmed,
                                        compCity,
                                        compRole
                                    )
                                } catch (e: Exception) {
                                    isBusy = false
                                    authErrorMessage = "Profile setup error: ${e.localizedMessage}"
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimaryDark),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("ENTER PITCH & START PLAYING", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                    }
                }
            )
        }
    }
}

@Composable
private fun AuthFeatureBullet(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(BoundaryAmber.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = BoundaryAmber, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun GoogleBrandBadge() {
    Surface(
        shape = CircleShape,
        color = Color(0xFFF2F2F2),
        modifier = Modifier.size(24.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "G",
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                color = Color(0xFF4285F4)
            )
        }
    }
}
