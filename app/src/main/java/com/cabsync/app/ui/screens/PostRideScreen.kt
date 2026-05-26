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
fun PostRideScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToJoined: () -> Unit,
    onNavigateToMessages: () -> Unit
) {
    val currentUser = remember { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser }
    val context = LocalContext.current
    val sharedPrefs = remember(context) { context.getSharedPreferences("cabsync_user_prefs", android.content.Context.MODE_PRIVATE) }
    val name = remember(currentUser, sharedPrefs) { sharedPrefs.getString("user_name", currentUser?.displayName?.ifEmpty { null }) ?: currentUser?.email?.substringBefore('@') ?: "Abhishek Singh" }
    var avatarUrl by remember {
        mutableStateOf(
            sharedPrefs.getString("user_avatar", null)
                ?: currentUser?.photoUrl?.toString()
                ?: "https://api.dicebear.com/7.x/avataaars/svg?seed=$name"
        )
    }
    var ratingState by remember { mutableStateOf(0.0f) }

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
                avatarUrl = profile.photoUrl
            }
            ratingState = profile.rating.toFloat()
        }
    }

    val myHostRides = remember(RideService.liveRides, currentUser, name) {
        RideService.liveRides.filter { it.hostId == currentUser?.uid || it.hostName.equals(name, ignoreCase = true) }
    }
    val totalRidesCount = remember(myHostRides) { myHostRides.size.coerceAtLeast(1) }
    var pickup by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("Select Date") }
    var time by remember { mutableStateOf("Select Time") }
    var carModel by remember { mutableStateOf("") }
    var carModelExpanded by remember { mutableStateOf(false) }
    val carOptions = listOf("Sedan", "SUV", "Hatchback", "MPV", "Not Confirmed")
    var seats by remember { mutableStateOf("2") }
    var price by remember { mutableStateOf("") }
    var rideType by remember { mutableStateOf("AC") }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val calendar = java.util.Calendar.getInstance()
        android.app.DatePickerDialog(
            context,
            R.style.CabSyncPickerDialog,
            { _, year, month, day ->
                date = "%04d-%02d-%02d".format(year, month + 1, day)
                showDatePicker = false
            },
            calendar.get(java.util.Calendar.YEAR),
            calendar.get(java.util.Calendar.MONTH),
            calendar.get(java.util.Calendar.DAY_OF_MONTH)
        ).also { dialog ->
            dialog.setOnDismissListener { showDatePicker = false }
            dialog.show()
        }
    }

    if (showTimePicker) {
        val calendar = java.util.Calendar.getInstance()
        android.app.TimePickerDialog(
            context,
            R.style.CabSyncPickerDialog,
            { _, hour, minute ->
                val amPm = if (hour < 12) "AM" else "PM"
                val hour12 = if (hour % 12 == 0) 12 else hour % 12
                time = "%02d:%02d %s".format(hour12, minute, amPm)
                showTimePicker = false
            },
            calendar.get(java.util.Calendar.HOUR_OF_DAY),
            calendar.get(java.util.Calendar.MINUTE),
            false
        ).also { dialog ->
            dialog.setOnDismissListener { showTimePicker = false }
            dialog.show()
        }
    }

    val isDark = com.cabsync.app.ui.theme.LocalTheme.current.isDark
    val brandYellow = Color(0xFFFFD700)
    val brandDark = if (isDark) Color(0xFF131313) else CabSyncTokens.LightBackground
    val surfaceContainer = if (isDark) Color(0xFF201F1F) else CabSyncTokens.LightSurfaceContainer
    val onSurfaceVariant = if (isDark) Color(0xFFD0C6AB) else CabSyncTokens.LightOnSurfaceVariant
    val cardBgGradient = if (isDark) listOf(Color(0xFF262215), Color(0xFF1E1D1D)) else listOf(CabSyncTokens.LightSurface, CabSyncTokens.LightSurfaceContainer)
    val textPrimary = if (isDark) Color.White else CabSyncTokens.LightTextPrimary
    val textMuted = if (isDark) Color(0xFF888880) else CabSyncTokens.LightTextMuted
    val inputBg = if (isDark) Color(0xFF333230) else CabSyncTokens.LightSurface
    val inputBorder = if (isDark) Color(0xFF3E3C38) else CabSyncTokens.LightBorderDark

    Scaffold(
        containerColor = brandDark,
        bottomBar = {
            CabSyncNavBar(
                selectedTab = NavTab.POST,
                onHomeClick = onNavigateToHome,
                onFindClick = onNavigateToSearch,
                onPostClick = { },
                onJoinedClick = onNavigateToJoined,
                onChatClick = onNavigateToMessages
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            CabSyncScreenHeader("Post a Ride", onNavigateBack = onNavigateBack, modifier = Modifier.padding(horizontal = 16.dp))
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Text("Fill in the details to find your carpool partners.", color = onSurfaceVariant, fontSize = 14.sp)
                
                Spacer(modifier = Modifier.height(32.dp))

            // Location Block
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(colors = cardBgGradient),
                        RoundedCornerShape(24.dp)
                    )
                    .border(1.dp, if (isDark) CabSyncTokens.BorderGlow else CabSyncTokens.LightBorderGlow, RoundedCornerShape(24.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Text("PICKUP LOCATION", fontSize = 10.sp, color = brandYellow.copy(alpha = 0.8f), fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(Modifier.height(8.dp))
                    TextField(
                        value = pickup,
                        onValueChange = { pickup = it },
                        placeholder = { Text("Where from?", color = textMuted) },
                        modifier = Modifier.fillMaxWidth().border(1.dp, inputBorder, RoundedCornerShape(12.dp)),
                        leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = brandYellow.copy(alpha = 0.8f)) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = inputBg,
                            unfocusedContainerColor = inputBg,
                            focusedIndicatorColor = brandYellow,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
                Column {
                    Text("DESTINATION", fontSize = 10.sp, color = brandYellow.copy(alpha = 0.8f), fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(Modifier.height(8.dp))
                    TextField(
                        value = destination,
                        onValueChange = { destination = it },
                        placeholder = { Text("Where to?", color = textMuted) },
                        modifier = Modifier.fillMaxWidth().border(1.dp, inputBorder, RoundedCornerShape(12.dp)),
                        leadingIcon = { Icon(Icons.Default.SportsScore, null, tint = brandYellow.copy(alpha = 0.8f)) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = inputBg,
                            unfocusedContainerColor = inputBg,
                            focusedIndicatorColor = brandYellow,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Time Block
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("DATE", fontSize = 10.sp, color = onSurfaceVariant, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        color = inputBg,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, inputBorder)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarToday, null, tint = brandYellow.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(date, color = if (date == "Select Date") textMuted else textPrimary, fontSize = 14.sp)
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("TIME", fontSize = 10.sp, color = onSurfaceVariant, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        color = inputBg,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, inputBorder)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, null, tint = brandYellow.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(time, color = if (time == "Select Time") textMuted else textPrimary, fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Vehicle Block
            Column {
                Text("VEHICLE MODEL", fontSize = 10.sp, color = onSurfaceVariant, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = carModelExpanded,
                    onExpandedChange = { carModelExpanded = !carModelExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = carModel,
                        onValueChange = { },
                        readOnly = true,
                        placeholder = { Text("Select Vehicle Type", color = textMuted, fontSize = 14.sp) },
                        modifier = Modifier.menuAnchor().fillMaxWidth().border(1.dp, inputBorder, RoundedCornerShape(12.dp)),
                        leadingIcon = { Icon(Icons.Default.DirectionsCar, null, tint = brandYellow.copy(alpha = 0.8f)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = carModelExpanded) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = inputBg,
                            unfocusedContainerColor = inputBg,
                            focusedIndicatorColor = brandYellow,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary,
                            focusedTrailingIconColor = onSurfaceVariant,
                            unfocusedTrailingIconColor = onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = carModelExpanded,
                        onDismissRequest = { carModelExpanded = false },
                        modifier = Modifier.background(if (isDark) Color(0xFF2A2A2A) else CabSyncTokens.LightSurfaceContainer)
                    ) {
                        carOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, color = if (isDark) Color.White else CabSyncTokens.LightTextPrimary) },
                                onClick = {
                                    carModel = option
                                    carModelExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Seats & Price Block
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("SEATS AVAILABLE", fontSize = 10.sp, color = onSurfaceVariant, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(Modifier.height(8.dp))
                    TextField(
                        value = seats,
                        onValueChange = { seats = it },
                        modifier = Modifier.fillMaxWidth().border(1.dp, inputBorder, RoundedCornerShape(12.dp)),
                        leadingIcon = { Icon(Icons.Default.AirlineSeatReclineNormal, null, tint = brandYellow.copy(alpha = 0.8f)) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = inputBg,
                            unfocusedContainerColor = inputBg,
                            focusedIndicatorColor = brandYellow,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("TOTAL RIDE FARE (₹)", fontSize = 10.sp, color = onSurfaceVariant, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(Modifier.height(8.dp))
                    TextField(
                        value = price,
                        onValueChange = { price = it },
                        placeholder = { Text("0.00", color = textMuted) },
                        modifier = Modifier.fillMaxWidth().border(1.dp, inputBorder, RoundedCornerShape(12.dp)),
                        leadingIcon = { Icon(Icons.Default.Payments, null, tint = brandYellow.copy(alpha = 0.8f)) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = inputBg,
                            unfocusedContainerColor = inputBg,
                            focusedIndicatorColor = brandYellow,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Comfort Type (AC/Non-AC)
            Text("COMFORT TYPE", fontSize = 10.sp, color = onSurfaceVariant, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(surfaceContainer, RoundedCornerShape(16.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Any", "AC", "Non-AC").forEach { type ->
                    val isSelected = rideType == type
                    Surface(
                        onClick = { rideType = type },
                        modifier = Modifier.weight(1f),
                        color = if (isSelected) (if (isDark) Color(0xFF353534) else Color(0xFFF5F5F0)) else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = type,
                            modifier = Modifier.padding(vertical = 12.dp),
                            textAlign = TextAlign.Center,
                            color = if (isSelected) brandYellow else onSurfaceVariant,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(48.dp))

            var isPosting by remember { mutableStateOf(false) }
            CabSyncButton(
                text = if (isPosting) "Posting..." else "Post Your Ride",
                onClick = {
                    if (isPosting) return@CabSyncButton
                    isPosting = true
                    val newRide = Ride(
                        hostId = currentUser?.uid ?: "",
                        hostName = name,
                        hostAvatar = avatarUrl,
                        pickup = pickup.ifEmpty { "Gorakhpur" },
                        destination = destination.ifEmpty { "Lucknow" },
                        price = price.toIntOrNull() ?: 500,
                        time = if (time == "Select Time") "09:30 AM" else time,
                        date = if (date == "Select Date") "2026-05-18" else date,
                        seatsLeft = seats.toIntOrNull() ?: 2,
                        seats = seats.toIntOrNull() ?: 2,
                        isVerified = true,
                        rating = ratingState,
                        totalRides = totalRidesCount.toString(),
                        carModel = carModel.ifEmpty { "Not Confirmed" },
                        tags = listOf(rideType, "Verified"),
                        reviews = emptyList(),
                        createdAt = java.util.Date()
                    )
                    RideService.postRide(
                        ride = newRide,
                        onSuccess = {
                            isPosting = false
                            onNavigateToHome()
                        },
                        onFailure = {
                            isPosting = false
                            onNavigateToHome()
                        }
                    )
                },
                icon = Icons.AutoMirrored.Filled.Send
            )
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
}

