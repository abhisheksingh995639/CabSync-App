package com.cabsync.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalContext
import com.cabsync.app.data.RideService

@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToRide: (String) -> Unit
) {
    val currentUser = remember { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser }
    val context = LocalContext.current
    val sharedPrefs = remember(context) { context.getSharedPreferences("cabsync_user_prefs", android.content.Context.MODE_PRIVATE) }
    val name = remember(currentUser, sharedPrefs) { sharedPrefs.getString("user_name", currentUser?.displayName?.ifEmpty { null }) ?: currentUser?.email?.substringBefore('@') ?: "User" }

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

    val isDark = com.cabsync.app.ui.theme.LocalTheme.current.isDark
    val scaffoldBg = if (isDark) CabSyncTokens.Background else CabSyncTokens.LightBackground
    val surfaceContainer = if (isDark) CabSyncTokens.SurfaceContainer else CabSyncTokens.LightSurface
    val textPrimary = if (isDark) Color.White else CabSyncTokens.LightTextPrimary
    val textMuted = if (isDark) Color(0xFFD0C6AB) else CabSyncTokens.LightOnSurfaceVariant
    val innerIconBg = if (isDark) Color(0xFF2A2A2A) else CabSyncTokens.LightSurfaceContainer
    val brandYellow = if (isDark) Color(0xFFFFD700) else CabSyncTokens.LightTextGold
    val cardBg = if (isDark) Color(0xFF1C1B1B) else CabSyncTokens.LightSurface
    val borderDark = if (isDark) Color(0xFF4D4732) else CabSyncTokens.LightBorderDark

    Scaffold(
        containerColor = scaffoldBg
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            CabSyncScreenHeader(
                title = "Ride History",
                onNavigateBack = onNavigateBack,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                if (allMyTrips.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = surfaceContainer)
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
                    }
                } else {
                    // Stats Summary
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        val totalSpent = allMyTrips.sumOf { ride ->
                            val people = (ride.passengers.size + 1).coerceAtLeast(1)
                            ride.price / people
                        }
                        val asHost = allMyTrips.count { it.hostId == currentUser?.uid }
                        val asRider = allMyTrips.count { it.passengers.contains(currentUser?.uid ?: "") }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Total Rides
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                border = androidx.compose.foundation.BorderStroke(1.dp, borderDark)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                                    Text("${allMyTrips.size}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = textPrimary)
                                    Text("Total", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = textMuted, letterSpacing = 1.sp)
                                }
                            }
                            // As Host
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                border = androidx.compose.foundation.BorderStroke(1.dp, borderDark)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                                    Text("$asHost", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = textPrimary)
                                    Text("As Host", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = textMuted, letterSpacing = 1.sp)
                                }
                            }
                            // As Rider
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                border = androidx.compose.foundation.BorderStroke(1.dp, borderDark)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                                    Text("$asRider", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = textPrimary)
                                    Text("Rider", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = textMuted, letterSpacing = 1.sp)
                                }
                            }
                            // Total Spent
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF131313))
                            ) {
                                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                                    Text("₹$totalSpent", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = brandYellow)
                                    Text("Spent", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = textMuted, letterSpacing = 1.sp)
                                }
                            }
                        }
                    }
                    items(allMyTrips) { ride ->
                        RideCard(
                            ride = ride,
                            onClick = { onNavigateToRide(ride.id) }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}
