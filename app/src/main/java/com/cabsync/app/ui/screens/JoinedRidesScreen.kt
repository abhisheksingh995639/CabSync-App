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
fun JoinedRidesScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToJoined: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToMessages: () -> Unit,
    onNavigateToPost: () -> Unit,
    onNavigateToRide: (String) -> Unit
) {
    val currentUser = remember { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser }
    val joinedRides = remember(RideService.liveRides, currentUser) {
        RideService.liveRides.filter { it.passengers.contains(currentUser?.uid ?: "") }
    }
    
    var myRequests by remember { mutableStateOf<List<com.cabsync.app.models.RideRequest>>(emptyList()) }
    
    DisposableEffect(currentUser?.uid) {
        val reg = currentUser?.uid?.let { uid ->
            RideService.observeMyAllRequests(uid) { reqs ->
                myRequests = reqs
            }
        }
        onDispose { reg?.remove() }
    }

    val pendingRequests = remember(myRequests, RideService.liveRides) {
        myRequests.filter { it.status == "pending" }.mapNotNull { req ->
            val ride = RideService.liveRides.find { it.id == req.rideId }
            if (ride != null) Pair(req, ride) else null
        }
    }

    val isDark = com.cabsync.app.ui.theme.LocalTheme.current.isDark
    val scaffoldBg = if (isDark) CabSyncTokens.Background else CabSyncTokens.LightBackground
    val cardBg = if (isDark) Color(0xFF201F1F) else CabSyncTokens.LightSurface
    val textPrimary = if (isDark) Color.White else CabSyncTokens.LightTextPrimary
    val textMuted = if (isDark) Color.Gray else CabSyncTokens.LightTextMuted
    val borderDark = if (isDark) Color(0xFF353534) else CabSyncTokens.LightBorderDark
    val brandYellow = if (isDark) Color(0xFFFFD700) else CabSyncTokens.LightTextGold

    Scaffold(
        containerColor = scaffoldBg,
        bottomBar = {
            CabSyncNavBar(
                selectedTab = NavTab.JOINED,
                onHomeClick = onNavigateToHome,
                onFindClick = onNavigateToSearch,
                onPostClick = onNavigateToPost,
                onJoinedClick = { },
                onChatClick = onNavigateToMessages
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            CabSyncScreenHeader("Joined Rides", onNavigateBack = onNavigateBack, modifier = Modifier.padding(horizontal = 16.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Text("Track your current journeys and manage pending requests.", color = textMuted, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(32.dp))
                }
            
            item {
                Text("ACTIVE JOURNEYS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = brandYellow, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(16.dp))
                if (joinedRides.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier.size(64.dp).background(borderDark, RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.NoTransfer, null, tint = textMuted, modifier = Modifier.size(36.dp))
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Text("No active journeys", fontWeight = FontWeight.Bold, color = textPrimary)
                            Text("Join a ride to see your confirmed carpools here.", color = textMuted, fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            Spacer(modifier = Modifier.height(24.dp))
                            CabSyncButton("Find a Ride", onClick = onNavigateToSearch, modifier = Modifier.width(200.dp))
                        }
                    }
                }
            }

            items(joinedRides) { ride ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).clickable { onNavigateToRide(ride.id) },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, borderDark)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = ride.hostAvatar,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(Color.DarkGray)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                textPrimary?.let { Text(ride.hostName, fontWeight = FontWeight.Bold, color = it) }
                                Text("${ride.pickup} → ${ride.destination}", fontSize = 14.sp, color = brandYellow)
                            }
                            Surface(color = brandYellow.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                                Text("CONFIRMED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = brandYellow, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Date: ${ride.date}", fontSize = 12.sp, color = textMuted)
                            textPrimary?.let { Text("Fare: ₹${ride.price}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = it) }
                        }
                    }
                }
            }

            item {
                if (pendingRequests.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text("PENDING REQUESTS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textMuted, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            items(pendingRequests) { (req, ride) ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).clickable { onNavigateToRide(ride.id) },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, borderDark)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = ride.hostAvatar,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(Color.DarkGray)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                textPrimary?.let { Text(ride.hostName, fontWeight = FontWeight.Bold, color = it) }
                                Text("${ride.pickup} → ${ride.destination}", fontSize = 14.sp, color = textMuted)
                            }
                            Surface(color = borderDark, shape = RoundedCornerShape(8.dp)) {
                                Text("PENDING", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textMuted, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Date: ${ride.date}", fontSize = 12.sp, color = textMuted)
                            textPrimary?.let { Text("Fare: ₹${ride.price}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = it) }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            var isCancelling by remember { mutableStateOf(false) }
                            Button(
                                onClick = {
                                    isCancelling = true
                                    RideService.cancelRequest(req.id) {
                                        isCancelling = false
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252).copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(if (isCancelling) "Cancelling..." else "Cancel Request", color = Color(0xFFFF5252), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { onNavigateToRide(ride.id) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = cardBg),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, borderDark)
                            ) {
                                Text("View Ride", color = textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
}


