package com.cabsync.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.draw.shadow
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import coil.ImageLoader
import coil.decode.SvgDecoder
import com.cabsync.app.data.RideService
import com.cabsync.app.models.Ride
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.layout
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import kotlin.math.PI
import kotlin.math.cos
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    startEditing: Boolean = false,
    onNavigateBack: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onLogout: () -> Unit = {},
    onNavigateToRide: (String) -> Unit = {},
    onNavigateToHistory: () -> Unit = {}
) {
    val currentUser = remember { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser }
    val context = LocalContext.current
    val imageLoader = remember(context) {
        ImageLoader.Builder(context)
            .components {
                add(SvgDecoder.Factory())
            }
            .build()
    }
    val coroutineScope = rememberCoroutineScope()
    val sharedPrefs = remember(context) { context.getSharedPreferences("cabsync_user_prefs", android.content.Context.MODE_PRIVATE) }
    var isEditing by remember { mutableStateOf(startEditing) }
    var name by remember(currentUser, sharedPrefs) { mutableStateOf(sharedPrefs.getString("user_name", currentUser?.displayName?.ifEmpty { null }) ?: currentUser?.email?.substringBefore('@') ?: "Abhishek Singh") }
    var email by remember(currentUser) { mutableStateOf(currentUser?.email ?: "abhisheksingh@gmail.com") }
    var phone by remember(currentUser, sharedPrefs) { mutableStateOf(sharedPrefs.getString("user_phone", currentUser?.phoneNumber) ?: "") }
    val avatarPresets = remember(currentUser) {
        val list = mutableListOf<String>()
        currentUser?.photoUrl?.toString()?.takeIf { it.isNotBlank() }?.let { googlePhoto ->
            list.add(googlePhoto)
        }
        list.addAll(
            listOf(
                "https://api.dicebear.com/7.x/avataaars/svg?seed=Felix",
                "https://api.dicebear.com/7.x/avataaars/svg?seed=Aneka",
                "https://api.dicebear.com/7.x/avataaars/svg?seed=Jack",
                "https://api.dicebear.com/7.x/avataaars/svg?seed=Milo",
                "https://api.dicebear.com/7.x/avataaars/svg?seed=Zoe",
                "https://api.dicebear.com/7.x/avataaars/svg?seed=Leo",
                "https://api.dicebear.com/7.x/avataaars/svg?seed=Mia",
                "https://api.dicebear.com/7.x/avataaars/svg?seed=Max",
                "https://api.dicebear.com/7.x/bottts/svg?seed=Spooky",
                "https://api.dicebear.com/7.x/bottts/svg?seed=Gizmo",
                "https://api.dicebear.com/7.x/lorelei/svg?seed=Precious",
                "https://api.dicebear.com/7.x/lorelei/svg?seed=Cookie"
            )
        )
        list
    }
    var avatarUrl by remember {
        mutableStateOf(
            sharedPrefs.getString("user_avatar", null)
                ?: currentUser?.photoUrl?.toString()
                ?: "https://api.dicebear.com/7.x/avataaars/svg?seed=${sharedPrefs.getString("user_name", currentUser?.displayName?.ifEmpty { null }) ?: currentUser?.email?.substringBefore('@') ?: "Abhishek Singh"}"
        )
    }

    var userProfileState by remember { mutableStateOf<com.cabsync.app.models.UserProfile?>(null) }

    val isDark = com.cabsync.app.ui.theme.LocalTheme.current.isDark
    val scaffoldBg = if (isDark) CabSyncTokens.Background else CabSyncTokens.LightBackground
    val surfaceContainer = if (isDark) CabSyncTokens.SurfaceContainer else CabSyncTokens.LightSurface
    val borderDark = if (isDark) CabSyncTokens.BorderDark else CabSyncTokens.LightBorderDark
    val textPrimary = if (isDark) Color.White else CabSyncTokens.LightTextPrimary
    val textMuted = if (isDark) Color(0xFFD0C6AB) else CabSyncTokens.LightOnSurfaceVariant
    val textSubtitle = if (isDark) Color.Gray else CabSyncTokens.LightTextMuted
    val innerIconBg = if (isDark) Color(0xFF2A2A2A) else CabSyncTokens.LightSurfaceContainer

    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { uid ->
            val profile = com.cabsync.app.data.UserService.getUserProfile(
                uid = uid,
                sharedPrefs = sharedPrefs,
                fallbackEmail = currentUser.email,
                fallbackName = currentUser.displayName,
                fallbackPhone = currentUser.phoneNumber,
                fallbackPhotoUrl = currentUser.photoUrl?.toString()
            )
            userProfileState = profile
            name = profile.name
            email = profile.email
            phone = profile.phone
            if (profile.photoUrl.isNotEmpty()) {
                avatarUrl = profile.photoUrl
            }
            sharedPrefs.edit()
                .putString("user_name", profile.name)
                .putString("user_phone", profile.phone)
                .putString("user_avatar", profile.photoUrl.ifEmpty { avatarUrl })
                .apply()
        }
    }

    val myHostRides = remember(RideService.liveRides, currentUser, name) {
        RideService.liveRides.filter { it.hostId == currentUser?.uid || it.hostName.equals(name, ignoreCase = true) }
    }
    val myRiderRides = remember(RideService.liveRides, currentUser, name) {
        RideService.liveRides.filter { ride ->
            ride.passengers.contains(currentUser?.uid ?: "") ||
            ride.passengers.contains(name) ||
            ride.passengerDetails.any { it.userId == currentUser?.uid || it.userName.equals(name, ignoreCase = true) }
        }
    }
    val allMyTrips = remember(myHostRides, myRiderRides) {
        (myHostRides + myRiderRides).distinctBy { it.id }
            .filter { it.status.equals("completed", ignoreCase = true) }
            .sortedByDescending { it.date }
    }
    val completedHostRides = remember(myHostRides) { myHostRides.filter { it.status.equals("completed", ignoreCase = true) } }
    val completedRiderRides = remember(myRiderRides) { myRiderRides.filter { it.status.equals("completed", ignoreCase = true) } }
    val uniqueCompletedRides = remember(completedHostRides, completedRiderRides) { (completedHostRides + completedRiderRides).distinctBy { it.id } }

    val asHostCount = completedHostRides.size
    val asRiderCount = completedRiderRides.size
    val totalRidesCount = uniqueCompletedRides.size
    val totalSpent = remember(uniqueCompletedRides) {
        uniqueCompletedRides.sumOf { ride ->
            val totalPeople = ride.passengers.size + 1
            kotlin.math.round(ride.price.toDouble() / totalPeople.toDouble()).toLong()
        }
    }

    Scaffold(
        containerColor = scaffoldBg
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            CabSyncScreenHeader(
                title = if (isEditing) "Edit Profile" else "Profile",
                onNavigateBack = if (isEditing) ({ isEditing = false }) else onNavigateBack,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!isEditing) {
                    // Profile Overview (Matching Reference HTML)
                    Spacer(modifier = Modifier.height(24.dp))

                // Avatar
                AsyncImage(
                    model = avatarUrl,
                    imageLoader = imageLoader,
                    contentDescription = "User Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(112.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(surfaceContainer)
                        .border(4.dp, surfaceContainer, RoundedCornerShape(16.dp))
                )

                Spacer(modifier = Modifier.height(16.dp))

                // User Info
                Text(
                    text = name,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                val isPhoneVisible = userProfileState?.phoneVisibility ?: "Everyone"
                if (phone.isNotBlank() && isPhoneVisible != "Nobody") {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = phone,
                        fontSize = 14.sp,
                        color = textMuted
                    )
                } else if (phone.isBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { isEditing = true }
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = null, tint = CabSyncTokens.BrandYellow, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add Phone Number",
                            fontSize = 14.sp,
                            color = CabSyncTokens.BrandYellow,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Rating & Verification Pills Side-by-Side
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rating Pill
                    Surface(
                        color = surfaceContainer,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = CabSyncTokens.BrandYellow, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(String.format("%.1f Rating", userProfileState?.rating ?: 0.0), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = textPrimary)
                        }
                    }

                    // Verification Pill
                    val verifiedState = userProfileState?.isVerified ?: false
                    Surface(
                        color = surfaceContainer,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (verifiedState) Icons.Default.Verified else Icons.Default.Cancel,
                                contentDescription = null,
                                tint = if (verifiedState) Color(0xFF81C784) else Color(0xFFE57373),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (verifiedState) "Verified" else "Not Verified",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = textPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons: Edit & Logout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        CabSyncOutlinedButton(
                            text = "Edit Profile",
                            onClick = { isEditing = true },
                            icon = { Icon(Icons.Default.Edit, contentDescription = null, tint = textPrimary, modifier = Modifier.size(18.dp)) },
                            borderColor = borderDark,
                            containerColor = surfaceContainer,
                            textColor = textPrimary
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        val logoutBg = if (isDark) Color(0xFF93000A).copy(alpha = 0.2f) else Color(0xFFBA1A1A).copy(alpha = 0.1f)
                        val logoutBorder = if (isDark) Color(0xFF93000A).copy(alpha = 0.5f) else Color(0xFFBA1A1A).copy(alpha = 0.25f)
                        val logoutContent = if (isDark) Color(0xFFFFB4AB) else Color(0xFFBA1A1A)

                        CabSyncOutlinedButton(
                            text = "Log Out",
                            onClick = {
                                com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(
                                    context,
                                    com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
                                ).signOut()
                                com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                                onLogout()
                            },
                            icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = logoutContent, modifier = Modifier.size(18.dp)) },
                            borderColor = logoutBorder,
                            containerColor = logoutBg,
                            textColor = logoutContent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Ride Stats Bento Grid
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Ride Stats",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Total Rides
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = surfaceContainer),
                            border = BorderStroke(1.dp, borderDark)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(42.dp).background(innerIconBg, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = textMuted, modifier = Modifier.size(22.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("$totalRidesCount", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                    Text("Total Rides", fontSize = 12.sp, color = textMuted)
                                }
                            }
                        }

                        // Total Spent
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = surfaceContainer),
                            border = BorderStroke(1.dp, CabSyncTokens.BrandYellow.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(42.dp).background(CabSyncTokens.BrandYellow.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = CabSyncTokens.BrandYellow, modifier = Modifier.size(22.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("₹$totalSpent", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = if (isDark) CabSyncTokens.BrandYellow else CabSyncTokens.LightTextGold)
                                    Text("Total Value", fontSize = 12.sp, color = textMuted)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // As Host
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = surfaceContainer),
                            border = BorderStroke(1.dp, borderDark)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(42.dp).background(innerIconBg, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Key, contentDescription = null, tint = textMuted, modifier = Modifier.size(22.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("$asHostCount", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                    Text("As Host", fontSize = 12.sp, color = textMuted)
                                }
                            }
                        }

                        // As Rider
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = surfaceContainer),
                            border = BorderStroke(1.dp, borderDark)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(42.dp).background(innerIconBg, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = textMuted, modifier = Modifier.size(22.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("$asRiderCount", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                    Text("As Rider", fontSize = 12.sp, color = textMuted)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Recent History Section
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent History",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Text(
                            text = "View All",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDark) CabSyncTokens.BrandYellow else CabSyncTokens.LightTextGold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onNavigateToHistory() }
                                .padding(4.dp)
                        )
                    }

                    if (allMyTrips.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = surfaceContainer),
                            border = BorderStroke(1.dp, borderDark)
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier.size(64.dp).background(innerIconBg, RoundedCornerShape(32.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.History, contentDescription = null, tint = textMuted, modifier = Modifier.size(32.dp))
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("No ride history yet", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Your completed trips will appear here.", fontSize = 14.sp, color = textMuted, textAlign = TextAlign.Center)
                                Spacer(modifier = Modifier.height(24.dp))
                                CabSyncButton(
                                    text = "Find a Ride",
                                    onClick = onNavigateToSearch,
                                    icon = Icons.Default.Search,
                                    modifier = Modifier.width(200.dp)
                                )
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            allMyTrips.take(3).forEach { ride ->
                                RideCard(
                                    ride = ride,
                                    onClick = { onNavigateToRide(ride.id) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))
            } else {
                // Edit Profile Form
                Spacer(modifier = Modifier.height(16.dp))
                Box(contentAlignment = Alignment.BottomEnd) {
                    AsyncImage(
                        model = avatarUrl,
                        imageLoader = imageLoader,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(128.dp)
                            .clip(RoundedCornerShape(64.dp))
                            .border(4.dp, borderDark, RoundedCornerShape(64.dp))
                    )
                    IconButton(
                        onClick = { },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFFFD700), RoundedCornerShape(20.dp))
                            .border(2.dp, scaffoldBg, RoundedCornerShape(20.dp))
                    ) {
                        Icon(Icons.Default.PhotoCamera, null, tint = Color.Black, modifier = Modifier.size(20.dp))
                    }
                }

                Text("Choose Profile Avatar", modifier = Modifier.padding(top = 16.dp, bottom = 8.dp), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = textPrimary)

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    items(avatarPresets) { presetUrl ->
                        val isSelected = avatarUrl == presetUrl
                        val isGooglePhoto = currentUser?.photoUrl?.toString()?.takeIf { it.isNotBlank() } == presetUrl
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(32.dp))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) CabSyncTokens.BrandYellow else borderDark,
                                    shape = RoundedCornerShape(32.dp)
                                )
                                .clickable {
                                    avatarUrl = presetUrl
                                }
                        ) {
                            AsyncImage(
                                model = presetUrl,
                                imageLoader = imageLoader,
                                contentDescription = "Avatar Option",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            if (isGooglePhoto) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .padding(vertical = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Google",
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(modifier = Modifier.fillMaxWidth().background(surfaceContainer, RoundedCornerShape(16.dp)).padding(16.dp)) {
                    Text("Full Name", fontSize = 14.sp, color = textMuted)
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = innerIconBg,
                            unfocusedContainerColor = innerIconBg,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Phone Number", fontSize = 14.sp, color = textMuted)
                    TextField(
                        value = phone.replace("+91", "").filter { it.isDigit() },
                        onValueChange = { newValue ->
                            val digitsOnly = newValue.filter { it.isDigit() }
                            if (digitsOnly.length <= 10) {
                                phone = "+91 $digitsOnly"
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = { Text("0000000000") },
                        prefix = { Text("+91 ", color = textPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Phone, null) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = innerIconBg,
                            unfocusedContainerColor = innerIconBg,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                }

                Spacer(modifier = Modifier.height(32.dp))

                CabSyncButton(
                    text = "Save Changes",
                    onClick = {
                        currentUser?.updateProfile(
                            com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build()
                        )
                        sharedPrefs.edit()
                            .putString("user_name", name)
                            .putString("user_phone", phone)
                            .putString("user_avatar", avatarUrl)
                            .apply()

                        currentUser?.uid?.let { uid ->
                            val updates = mapOf<String, Any>(
                                "name" to name,
                                "phone" to phone,
                                "photoUrl" to avatarUrl
                            )
                            com.cabsync.app.data.UserService.updateUserFields(uid, updates)

                            val updatedProfile = (userProfileState ?: com.cabsync.app.models.UserProfile(uid = uid)).copy(
                                name = name,
                                email = email,
                                phone = phone,
                                photoUrl = avatarUrl
                            )
                            userProfileState = updatedProfile
                        }

                        android.widget.Toast.makeText(context, "Profile saved to database successfully!", android.widget.Toast.LENGTH_SHORT).show()
                        isEditing = false
                    },
                    icon = Icons.Default.Save
                )

                Spacer(modifier = Modifier.height(12.dp))

                CabSyncOutlinedButton("Cancel", onClick = { isEditing = false })
                
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}
}

