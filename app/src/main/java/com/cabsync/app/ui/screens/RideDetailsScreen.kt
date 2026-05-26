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
fun RideDetailsScreen(rideId: String, onNavigateBack: () -> Unit, onNavigateToChat: (String) -> Unit, onNavigateToPublicProfile: (String) -> Unit = {}) {
    val ride = RideService.liveRides.find { it.id == rideId }
    
    if (ride == null) {
        val isDark = com.cabsync.app.ui.theme.LocalTheme.current.isDark
        Box(Modifier.fillMaxSize().background(if (isDark) Color(0xFF131313) else CabSyncTokens.LightBackground), contentAlignment = Alignment.Center) {
            Text("Ride not found", color = if (isDark) Color.White else CabSyncTokens.LightTextPrimary)
        }
        return
    }

    val isDark = com.cabsync.app.ui.theme.LocalTheme.current.isDark
    val scaffoldBg = if (isDark) Color(0xFF131313) else CabSyncTokens.LightBackground
    val cardBg = if (isDark) Color(0xFF1C1B1B) else CabSyncTokens.LightSurface
    val cardBg2 = if (isDark) Color(0xFF201F1F) else CabSyncTokens.LightSurfaceContainer
    val textPrimary = if (isDark) Color.White else CabSyncTokens.LightTextPrimary
    val textMuted = if (isDark) Color.Gray else CabSyncTokens.LightTextMuted
    val borderDark = if (isDark) Color(0xFF4D4732) else CabSyncTokens.LightBorderDark
    val brandYellow = if (isDark) Color(0xFFFFD700) else CabSyncTokens.LightTextGold

    val context = LocalContext.current
    val sharedPrefs = remember(context) { context.getSharedPreferences("cabsync_user_prefs", android.content.Context.MODE_PRIVATE) }
    val currentUser = remember { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser }
    val currentUserId = currentUser?.uid ?: ""
    val currentUserName = remember(currentUser, sharedPrefs) { sharedPrefs.getString("user_name", currentUser?.displayName?.ifEmpty { null }) ?: currentUser?.email?.substringBefore('@') ?: "Passenger" }
    var currentUserAvatar by remember {
        mutableStateOf(
            sharedPrefs.getString("user_avatar", null)
                ?: currentUser?.photoUrl?.toString()
                ?: "https://api.dicebear.com/7.x/avataaars/svg?seed=$currentUserName"
        )
    }

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
            if (profile.photoUrl.isNotEmpty()) {
                currentUserAvatar = profile.photoUrl
            }
        }
    }

    val totalFare = ride.price
    val splitFare = remember(ride) { if ((ride.passengers.size + 1) > 0) kotlin.math.round(totalFare.toFloat() / (ride.passengers.size + 1)).toInt() else totalFare }
    val tripId = remember(ride) { "#" + ride.id.takeLast(6).uppercase() }
    val isHost = ride.hostId == currentUserId
    val hasJoined = ride.passengers.contains(currentUserId)

    var pendingRequests by remember { mutableStateOf<List<com.cabsync.app.models.RideRequest>>(emptyList()) }
    DisposableEffect(rideId, isHost) {
        var reg: com.google.firebase.firestore.ListenerRegistration? = null
        if (isHost) {
            reg = RideService.observeRequests(rideId) { list ->
                pendingRequests = list
            }
        }
        onDispose {
            reg?.remove()
        }
    }

    var myRequest by remember { mutableStateOf<com.cabsync.app.models.RideRequest?>(null) }
    DisposableEffect(rideId, currentUserId, isHost) {
        var reg: com.google.firebase.firestore.ListenerRegistration? = null
        if (!isHost && currentUserId.isNotEmpty()) {
            reg = RideService.observeMyRequest(rideId, currentUserId) { req ->
                myRequest = req
            }
        }
        onDispose {
            reg?.remove()
        }
    }

    val hasRequested = myRequest != null && myRequest?.status == "pending"

    val passengerDetailsMap = remember { mutableStateMapOf<String, com.cabsync.app.models.UserProfile>() }
    LaunchedEffect(ride.passengers) {
        ride.passengers.forEach { pId ->
            if (!passengerDetailsMap.containsKey(pId)) {
                kotlin.runCatching {
                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    db.collection("users").document(pId).get().addOnSuccessListener { snapshot ->
                        if (snapshot.exists()) {
                            val profile = snapshot.toObject(com.cabsync.app.models.UserProfile::class.java)
                            if (profile != null) {
                                passengerDetailsMap[pId] = profile
                            }
                        }
                    }
                }
            }
        }
    }

    val confirmedPassengers = remember(ride.passengers, currentUserId, currentUserName, currentUserAvatar, passengerDetailsMap.toMap()) {
        ride.passengers.map { passengerId ->
            val profile = passengerDetailsMap[passengerId]
            com.cabsync.app.models.JoinRequest(
                userId = passengerId,
                userName = profile?.name ?: (if (passengerId == currentUserId) currentUserName else "Passenger (${passengerId.take(4)})"),
                userAvatar = profile?.photoUrl ?: (if (passengerId == currentUserId) currentUserAvatar else "https://api.dicebear.com/7.x/avataaars/svg?seed=$passengerId")
            )
        }
    }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showLeaveDialog by remember { mutableStateOf(false) }

    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            containerColor = cardBg,
            shape = RoundedCornerShape(28.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ExitToApp, null, tint = brandYellow, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Leave Ride?", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = brandYellow)
                }
            },
            text = {
                Text("Are you sure you want to leave this carpool group? Your seat will be made available to other passengers.", color = textPrimary.copy(alpha = 0.8f), fontSize = 15.sp)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLeaveDialog = false
                        RideService.leaveRide(ride, currentUserId) {
                            onNavigateBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF93000A), contentColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Leave", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLeaveDialog = false },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = brandYellow),
                    border = BorderStroke(1.dp, borderDark),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Cancel", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = cardBg,
            shape = RoundedCornerShape(28.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, null, tint = brandYellow, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Delete Ride?", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = brandYellow)
                }
            },
            text = {
                Text("Are you sure you want to permanently remove this ride? This action cannot be undone.", color = textPrimary.copy(alpha = 0.8f), fontSize = 15.sp)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        RideService.deleteRide(rideId) {
                            onNavigateBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF93000A), contentColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteDialog = false },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = brandYellow),
                    border = BorderStroke(1.dp, borderDark),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Cancel", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Scaffold(containerColor = scaffoldBg) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            CabSyncScreenHeader("Ride Details", onNavigateBack = onNavigateBack, modifier = Modifier.padding(horizontal = 16.dp))
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, borderDark.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(48.dp).background(brandYellow, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Commute, null, tint = Color.Black)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Ride Details", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = textPrimary)
                                Text("Trip ID: $tripId", fontSize = 12.sp, color = textMuted)
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Box(modifier = Modifier.background(if (isDark) Color(0xFF353534) else CabSyncTokens.LightSurfaceContainer, RoundedCornerShape(16.dp)).border(1.dp, borderDark, RoundedCornerShape(16.dp)).padding(horizontal = 12.dp, vertical = 4.dp)) {
                                Text(ride.status, color = if (ride.status.equals("completed", ignoreCase = true)) Color(0xFF8CD69E) else brandYellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Row {
                                Icon(Icons.Default.LocationOn, null, tint = brandYellow, modifier = Modifier.size(24.dp).border(2.dp, brandYellow, RoundedCornerShape(12.dp)).padding(4.dp))
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text("PICKUP POINT", fontSize = 12.sp, color = textMuted)
                                    Text(ride.pickup, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textPrimary)
                                    Text("${ride.date} • ${ride.time}", fontSize = 12.sp, color = textMuted)
                                }
                            }
                            Box(modifier = Modifier.padding(start = 11.dp).width(2.dp).height(40.dp).background(if (isDark) Color(0xFF353534) else CabSyncTokens.LightBorderDark))
                            Row {
                                Icon(Icons.Default.Map, null, tint = Color.Black, modifier = Modifier.size(24.dp).background(brandYellow, RoundedCornerShape(12.dp)).padding(4.dp))
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text("FINAL DESTINATION", fontSize = 12.sp, color = textMuted)
                                    Text(ride.destination, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textPrimary)
                                }
                            }
                        }
                    }
                }
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg2),
                    border = BorderStroke(1.dp, borderDark.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ESTIMATED YOUR SPLIT", fontSize = 12.sp, color = textMuted)
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("₹", fontSize = 28.sp, color = brandYellow, modifier = Modifier.padding(bottom = 6.dp))
                            Text("$splitFare", fontSize = 57.sp, fontWeight = FontWeight.Bold, color = brandYellow)
                        }
                        Text("PAID DIRECTLY TO HOST", fontSize = 12.sp, color = textMuted.copy(alpha = 0.8f))
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp), color = borderDark.copy(alpha = 0.3f))
                        
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("TOTAL FARE", fontSize = 12.sp, color = textMuted)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("₹$totalFare", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                            }
                            Box(modifier = Modifier.width(1.dp).height(48.dp).background(borderDark.copy(alpha = 0.3f)))
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("AVAILABLE SEATS", fontSize = 12.sp, color = textMuted)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${ride.seatsLeft} left", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = brandYellow)
                            }
                        }
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp), color = borderDark.copy(alpha = 0.3f))
                        
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("VEHICLE TYPE", fontSize = 12.sp, color = textMuted)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(ride.carModel, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Box(modifier = Modifier.width(1.dp).height(48.dp).background(borderDark.copy(alpha = 0.3f)))
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("COMFORT", fontSize = 12.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(if (ride.tags.contains("AC")) "AC" else "Non-AC", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = brandYellow)
                            }
                        }
                    }
                }
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
                    Icon(Icons.Default.Group, null, tint = brandYellow)
                    Text(" Confirmed Passengers (${1 + ride.passengers.size})", fontWeight = FontWeight.Bold, color = textPrimary)
                }
                val hostModifier = if (ride.hostId != currentUserId) {
                    Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable { onNavigateToPublicProfile(ride.hostId) }
                } else {
                    Modifier.fillMaxWidth().padding(bottom = 8.dp)
                }
                Card(
                    modifier = hostModifier,
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, borderDark.copy(alpha = 0.2f))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = if (ride.hostId == currentUserId) currentUserAvatar else ride.hostAvatar,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(Color.DarkGray),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(if (ride.hostId == currentUserId) "${ride.hostName} (You)" else ride.hostName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textPrimary)
                            Text("HOST", fontSize = 12.sp, color = brandYellow)
                        }
                        if (ride.hostId != currentUserId) {
                            Text("View Profile ↗", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = brandYellow, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }

            // Display confirmed passengers
            items(confirmedPassengers) { passenger ->
                val passModifier = if (passenger.userId != currentUserId) {
                    Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable { onNavigateToPublicProfile(passenger.userId) }
                } else {
                    Modifier.fillMaxWidth().padding(bottom = 8.dp)
                }
                Card(
                    modifier = passModifier,
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, borderDark.copy(alpha = 0.1f))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = if (passenger.userId == currentUserId) currentUserAvatar else passenger.userAvatar,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(Color.DarkGray),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(if (passenger.userId == currentUserId) "${passenger.userName} (You)" else passenger.userName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("CONFIRMED", fontSize = 12.sp, color = Color(0xFF8CD69E))
                        }
                        if (passenger.userId != currentUserId) {
                            Text("View Profile ↗", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = brandYellow, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }

            // Host-Only Pending Join Requests Section
            if (isHost && pendingRequests.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                        Icon(Icons.Default.PersonAdd, null, tint = brandYellow)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Join Requests (${pendingRequests.size})", fontWeight = FontWeight.Bold, color = textPrimary)
                    }
                }
                items(pendingRequests) { req ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg2),
                        border = BorderStroke(1.dp, borderDark)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = req.passengerPhoto,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(Color.DarkGray)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(req.passengerName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textPrimary)
                                Text("Wants to join your ride", fontSize = 12.sp, color = textMuted)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(
                                    onClick = { RideService.declineRequest(req.id) {} },
                                    modifier = Modifier.background(Color(0xFF93000A).copy(alpha = 0.2f), RoundedCornerShape(12.dp)).size(40.dp)
                                ) {
                                    Icon(Icons.Default.Close, "Decline", tint = Color(0xFFFFB4AB))
                                }
                                IconButton(
                                    onClick = { RideService.acceptRequest(ride, req) {} },
                                    modifier = Modifier.background(Color(0xFF1C2C1F), RoundedCornerShape(12.dp)).size(40.dp)
                                ) {
                                    Icon(Icons.Default.Check, "Accept", tint = Color(0xFF8CD69E))
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Action Button for User
            if (hasJoined) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2C1F)),
                        border = BorderStroke(1.dp, Color(0xFF2D4D35))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF8CD69E))
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("BOOKING CONFIRMED", fontWeight = FontWeight.Bold, color = Color(0xFF8CD69E), fontSize = 12.sp)
                                Text("You are ready to go!", color = Color(0xFF8CD69E).copy(alpha = 0.8f), fontSize = 12.sp)
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { showLeaveDialog = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF93000A).copy(alpha = 0.15f), contentColor = Color(0xFFFFB4AB)),
                        border = BorderStroke(1.dp, Color(0xFF93000A).copy(alpha = 0.4f))
                    ) {
                        Icon(Icons.Default.ExitToApp, null, tint = Color(0xFFFFB4AB))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Leave Ride Group")
                    }
                }
            }

            if (isHost || hasJoined) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    CabSyncButton("Start Chatting", onClick = { onNavigateToChat(rideId) })
                }
            } else if (ride.status.equals("completed", ignoreCase = true)) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF2A2A2A) else CabSyncTokens.LightSurfaceContainer)) {
                        Text("This ride has been completed", modifier = Modifier.padding(16.dp).fillMaxWidth(), textAlign = TextAlign.Center, color = textMuted)
                    }
                }
            } else if (hasRequested) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF2E2D2B) else CabSyncTokens.LightSurfaceContainer), border = BorderStroke(1.dp, if (isDark) Color(0xFF8C7D55) else brandYellow)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Icon(Icons.Default.PendingActions, null, tint = brandYellow)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Request Sent", color = brandYellow, fontWeight = FontWeight.Bold)
                                Text("Waiting for Host Approval", color = brandYellow.copy(alpha = 0.8f), fontSize = 12.sp)
                            }
                        }
                    }
                }
            } else if (ride.seatsLeft <= 0) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF2A2A2A) else CabSyncTokens.LightSurfaceContainer)) {
                        Text("No seats available", modifier = Modifier.padding(16.dp).fillMaxWidth(), textAlign = TextAlign.Center, color = textMuted)
                    }
                }
            } else {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    var isRequesting by remember { mutableStateOf(false) }
                    CabSyncButton(if (isRequesting) "Sending Request..." else "Request to Join (₹$splitFare)", onClick = {
                        if (isRequesting) return@CabSyncButton
                        isRequesting = true
                        RideService.requestToJoin(
                            rideId = rideId,
                            ridePickup = ride.pickup,
                            rideDestination = ride.destination,
                            passengerId = currentUserId,
                            passengerName = currentUserName,
                            passengerPhoto = currentUserAvatar,
                            hostId = ride.hostId
                        ) {
                            isRequesting = false
                        }
                    })
                }
            }

            // Host Management Options Card (Share, Complete, Delete)
            if (isHost) {
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text("Host Management", fontSize = 12.sp, color = textMuted, modifier = Modifier.padding(bottom = 8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = BorderStroke(1.dp, borderDark.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Share Button
                            OutlinedButton(
                                onClick = {
                                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(android.content.Intent.EXTRA_SUBJECT, "Join my CabSync ride!")
                                        putExtra(android.content.Intent.EXTRA_TEXT, "Carpool from ${ride.pickup} to ${ride.destination} on ${ride.date} at ${ride.time}. Fare: ₹${ride.price} per seat. Join here: https://cabsync.netlify.app/ride/${ride.id}")
                                    }
                                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Ride via"))
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = textPrimary),
                                border = BorderStroke(1.dp, borderDark)
                            ) {
                                Icon(Icons.Default.Share, null, tint = brandYellow)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Share Ride Details")
                            }

                            // Mark Completed Button
                            if (!ride.status.equals("completed", ignoreCase = true)) {
                                OutlinedButton(
                                    onClick = {
                                        RideService.updateRideStatus(rideId, "completed") {}
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF1C2C1F).copy(alpha = 0.5f), contentColor = Color(0xFF8CD69E)),
                                    border = BorderStroke(1.dp, Color(0xFF2D4D35))
                                ) {
                                    Icon(Icons.Default.DoneAll, null, tint = Color(0xFF8CD69E))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Mark as Completed")
                                }
                            }

                            // Delete Button
                            OutlinedButton(
                                onClick = {
                                    showDeleteDialog = true
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF93000A).copy(alpha = 0.2f), contentColor = Color(0xFFFFB4AB)),
                                border = BorderStroke(1.dp, Color(0xFF93000A).copy(alpha = 0.5f))
                            ) {
                                Icon(Icons.Default.Delete, null, tint = Color(0xFFFFB4AB))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Delete Ride")
                            }
                        }
                    }
                }
            }
        }
    }
}
}

