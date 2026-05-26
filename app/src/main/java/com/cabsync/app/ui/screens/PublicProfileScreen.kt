package com.cabsync.app.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.cabsync.app.data.RideService
import com.cabsync.app.models.Ride
import com.cabsync.app.models.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.cabsync.app.CabSyncTokens
import com.cabsync.app.CabSyncScreenHeader
import com.cabsync.app.CabSyncOutlinedButton
import com.cabsync.app.CabSyncButton
import com.cabsync.app.RideCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicProfileScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    onNavigateToRide: (String) -> Unit = {}
) {
    val isDark = com.cabsync.app.ui.theme.LocalTheme.current.isDark
    val scaffoldBg = if (isDark) CabSyncTokens.Background else CabSyncTokens.LightBackground
    val cardBg = if (isDark) CabSyncTokens.SurfaceContainer else CabSyncTokens.LightSurface
    val textPrimary = if (isDark) Color.White else CabSyncTokens.LightTextPrimary
    val textMuted = if (isDark) Color.Gray else CabSyncTokens.LightTextMuted
    val textAccent = if (isDark) Color(0xFFD0C6AB) else CabSyncTokens.LightTextMuted
    val borderDark = if (isDark) CabSyncTokens.BorderDark else CabSyncTokens.LightBorderDark

    val context = LocalContext.current
    var userProfileState by remember { mutableStateOf<UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showRateDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var reportReasonInput by remember { mutableStateOf("") }
    var ratingInput by remember { mutableStateOf(5) }

    val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var currentUserName by remember { mutableStateOf("Anonymous") }

    LaunchedEffect(userId, currentUserId) {
        try {
            val db = FirebaseFirestore.getInstance()
            if (currentUserId.isNotEmpty()) {
                val reporterSnap = db.collection("users").document(currentUserId).get().await()
                if (reporterSnap.exists()) {
                    currentUserName = reporterSnap.getString("name") ?: "Anonymous"
                }
            }
            val snapshot = db.collection("users").document(userId).get().await()
            if (snapshot.exists()) {
                userProfileState = snapshot.toObject(UserProfile::class.java)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    val name = userProfileState?.name ?: "Unknown User"
    val avatarUrl = userProfileState?.photoUrl?.takeIf { it.isNotBlank() } ?: "https://api.dicebear.com/7.x/avataaars/svg?seed=${name}"
    val phone = userProfileState?.phone ?: ""
    

    // Visibility checks based on user settings
    var hasMutualRide by remember { mutableStateOf(false) }
    val isOwnProfile = userId == currentUserId

    LaunchedEffect(userId, currentUserId) {
        if (userId.isNotEmpty() && currentUserId.isNotEmpty() && !isOwnProfile) {
            val db = FirebaseFirestore.getInstance()
            db.collection("rides")
                .whereEqualTo("hostId", userId)
                .whereArrayContains("passengers", currentUserId)
                .get()
                .addOnSuccessListener { q1 ->
                    if (!q1.isEmpty) {
                        hasMutualRide = true
                    } else {
                        db.collection("rides")
                            .whereEqualTo("hostId", currentUserId)
                            .whereArrayContains("passengers", userId)
                            .get()
                            .addOnSuccessListener { q2 ->
                                hasMutualRide = !q2.isEmpty
                            }
                    }
                }
        }
    }

    val isPhoneVisible = isOwnProfile || when (userProfileState?.phoneVisibility) {
        "Everyone" -> true
        "Group members" -> hasMutualRide
        else -> false
    }
    val isRideHistoryVisible = isOwnProfile || (userProfileState?.rideHistoryVisible ?: true)

    val myHostRides = remember(RideService.liveRides, userProfileState) {
        RideService.liveRides.filter { it.hostId == userId || it.hostName.equals(name, ignoreCase = true) }
    }
    val myRiderRides = remember(RideService.liveRides, userProfileState) {
        RideService.liveRides.filter { ride ->
            ride.passengers.contains(userId) ||
            ride.passengers.contains(name) ||
            ride.passengerDetails.any { it.userId == userId || it.userName.equals(name, ignoreCase = true) }
        }
    }
    val allMyTrips = remember(myHostRides, myRiderRides) {
        (myHostRides + myRiderRides).distinctBy { it.id }.sortedByDescending { it.date }
    }
    val asHostCount = myHostRides.size
    val asRiderCount = myRiderRides.size
    val totalRidesCount = asHostCount + asRiderCount

    Scaffold(
        containerColor = scaffoldBg
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CabSyncTokens.BrandYellow)
            }
        } else {
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                CabSyncScreenHeader(
                    title = "User Profile",
                    onNavigateBack = onNavigateBack,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                // Avatar
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "User Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(112.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardBg)
                        .border(4.dp, cardBg, RoundedCornerShape(16.dp))
                )

                Spacer(modifier = Modifier.height(16.dp))

                // User Info
                Text(
                    text = name,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )

                if (phone.isNotBlank() && isPhoneVisible) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = phone,
                        fontSize = 14.sp,
                        color = textAccent
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Rating & Verification Pills Side-by-Side
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rating Pill
                    Surface(
                        color = cardBg,
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
                        color = cardBg,
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

                // Action Buttons: Rate & Report
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        CabSyncOutlinedButton(
                            text = "Rate User",
                            onClick = {
                                showRateDialog = true
                            },
                            icon = { Icon(Icons.Default.ThumbUp, contentDescription = null, tint = textPrimary, modifier = Modifier.size(18.dp)) },
                            borderColor = borderDark,
                            containerColor = cardBg,
                            textColor = textPrimary
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        CabSyncOutlinedButton(
                            text = "Report",
                            onClick = {
                                showReportDialog = true
                            },
                            icon = { Icon(Icons.Default.ReportProblem, contentDescription = null, tint = Color(0xFFFFB4AB), modifier = Modifier.size(18.dp)) },
                            borderColor = Color(0xFF93000A).copy(alpha = 0.5f),
                            containerColor = Color(0xFF93000A).copy(alpha = 0.2f),
                            textColor = Color(0xFFFFB4AB)
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
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            border = BorderStroke(1.dp, borderDark)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(42.dp).background(if (isDark) Color(0xFF2A2A2A) else CabSyncTokens.LightSurfaceContainer, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = textAccent, modifier = Modifier.size(22.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("$totalRidesCount", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                    Text("Total Rides", fontSize = 12.sp, color = textAccent)
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
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            border = BorderStroke(1.dp, borderDark)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(42.dp).background(if (isDark) Color(0xFF2A2A2A) else CabSyncTokens.LightSurfaceContainer, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Key, contentDescription = null, tint = textAccent, modifier = Modifier.size(22.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("$asHostCount", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                    Text("As Host", fontSize = 12.sp, color = textAccent)
                                }
                            }
                        }

                        // As Rider
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            border = BorderStroke(1.dp, borderDark)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(42.dp).background(if (isDark) Color(0xFF2A2A2A) else CabSyncTokens.LightSurfaceContainer, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = textAccent, modifier = Modifier.size(22.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("$asRiderCount", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                    Text("As Rider", fontSize = 12.sp, color = textAccent)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Recent History Section
                if (isRideHistoryVisible) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Recent History",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        if (allMyTrips.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                border = BorderStroke(1.dp, borderDark)
                            ) {
                                Column(
                                    modifier = Modifier.padding(32.dp).fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier.size(64.dp).background(if (isDark) Color(0xFF2A2A2A) else CabSyncTokens.LightSurfaceContainer, RoundedCornerShape(32.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.History, contentDescription = null, tint = textAccent, modifier = Modifier.size(32.dp))
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("No public ride history yet", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                allMyTrips.forEach { ride ->
                                    RideCard(
                                        ride = ride,
                                        onClick = { onNavigateToRide(ride.id) }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = BorderStroke(1.dp, borderDark)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = textMuted, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Ride history is private", fontSize = 16.sp, color = textMuted)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }

    if (showRateDialog) {
        val targetName = userProfileState?.name ?: "User"
        AlertDialog(
            onDismissRequest = { showRateDialog = false },
            containerColor = cardBg,
            shape = RoundedCornerShape(28.dp),
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "How was your experience sharing a ride with $targetName?",
                        color = textPrimary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    ) {
                        for (i in 1..5) {
                            val isSelected = i <= ratingInput
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (isSelected) (if (isDark) CabSyncTokens.BrandYellow else CabSyncTokens.LightTextGold) else textMuted.copy(alpha = 0.3f),
                                modifier = Modifier
                                    .size(44.dp)
                                    .clickable { ratingInput = i }
                                    .padding(4.dp)
                            )
                        }
                    }
                    val ratingLabel = when (ratingInput) {
                        1 -> "Poor"
                        2 -> "Below Average"
                        3 -> "Average"
                        4 -> "Good"
                        5 -> "Excellent"
                        else -> ""
                    }
                    Text(
                        text = ratingLabel,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) CabSyncTokens.BrandYellow else CabSyncTokens.LightTextGold,
                        fontSize = 16.sp
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val currentRating = userProfileState?.rating ?: 0.0
                        val currentCount = userProfileState?.ratingCount ?: 0
                        com.cabsync.app.data.UserService.submitUserRating(
                            targetUid = userId,
                            newRatingVal = ratingInput.toDouble(),
                            currentRating = currentRating,
                            currentCount = currentCount,
                            onComplete = { roundedRating, newCount ->
                                userProfileState = userProfileState?.copy(
                                    rating = roundedRating,
                                    ratingCount = newCount
                                )
                                android.widget.Toast.makeText(context, "Rating updated successfully!", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        )
                        showRateDialog = false
                    }
                ) {
                    Text("Submit", color = if (isDark) CabSyncTokens.BrandYellow else CabSyncTokens.LightTextGold, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRateDialog = false }) {
                    Text("Cancel", color = textMuted)
                }
            }
        )
    }

    if (showReportDialog) {
        val targetName = userProfileState?.name ?: "User"
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            containerColor = cardBg,
            shape = RoundedCornerShape(28.dp),
            title = {
                Text("Report User", color = Color(0xFFFFB4AB), fontWeight = FontWeight.Bold, fontSize = 20.sp)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Please specify why you are reporting $targetName. Our team will review this report.",
                        color = textPrimary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    OutlinedTextField(
                        value = reportReasonInput,
                        onValueChange = { reportReasonInput = it },
                        placeholder = { Text("Ex: No-show, inappropriate behavior, etc.", color = textMuted) },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFFB4AB),
                            unfocusedBorderColor = borderDark,
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary
                        )
                    )
                }
            },
            confirmButton = {
                var isSubmitting by remember { mutableStateOf(false) }
                TextButton(
                    enabled = !isSubmitting,
                    onClick = {
                        if (reportReasonInput.trim().isEmpty()) {
                            android.widget.Toast.makeText(context, "Please provide a reason", android.widget.Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }
                        isSubmitting = true
                        val db = FirebaseFirestore.getInstance()
                        val reportData = hashMapOf(
                            "targetUserId" to userId,
                            "targetName" to targetName,
                            "reporterId" to currentUserId,
                            "reporterName" to currentUserName,
                            "reason" to reportReasonInput,
                            "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                            "status" to "pending"
                        )
                        db.collection("reports").add(reportData)
                            .addOnSuccessListener {
                                android.widget.Toast.makeText(context, "Report submitted successfully!", android.widget.Toast.LENGTH_SHORT).show()
                                showReportDialog = false
                                reportReasonInput = ""
                                isSubmitting = false
                            }
                            .addOnFailureListener {
                                android.widget.Toast.makeText(context, "Failed to submit report", android.widget.Toast.LENGTH_SHORT).show()
                                isSubmitting = false
                            }
                    }
                ) {
                    Text(if (isSubmitting) "Submitting..." else "Submit", color = Color(0xFFFFB4AB), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("Cancel", color = textMuted)
                }
            }
        )
    }
}
}
