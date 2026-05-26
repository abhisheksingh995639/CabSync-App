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
import androidx.compose.foundation.horizontalScroll
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


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    initialPickup: String,
    initialDest: String,
    initialDate: String = "",
    initialTime: String = "",
    onNavigateBack: () -> Unit,
    onNavigateToRide: (String) -> Unit,
    onNavigateToJoined: () -> Unit,
    onNavigateToMessages: () -> Unit,
    onNavigateToPost: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val isDark = com.cabsync.app.ui.theme.LocalTheme.current.isDark
    val brandYellow = Color(0xFFFFD700)
    val scaffoldBg = if (isDark) CabSyncTokens.Background else CabSyncTokens.LightBackground
    val surfaceContainerVal = if (isDark) Color(0xFF201F1F) else CabSyncTokens.LightSurface
    val cardBgVal = if (isDark) Color(0xFF1C1B1B) else CabSyncTokens.LightSurfaceContainer
    val onSurfaceVariantVal = if (isDark) Color(0xFFD0C6AB) else CabSyncTokens.LightOnSurfaceVariant
    val textPrimaryVal = if (isDark) Color.White else CabSyncTokens.LightTextPrimary
    val textMutedVal = if (isDark) Color.Gray else CabSyncTokens.LightTextMuted
    val borderDarkVal = if (isDark) Color(0xFF4D4732) else CabSyncTokens.LightBorderDark
    val borderGlowVal = if (isDark) CabSyncTokens.BorderGlow else CabSyncTokens.LightBorderGlow

    var pickup by remember { mutableStateOf(initialPickup) }
    var destination by remember { mutableStateOf(initialDest) }
    var searchedDate by remember(initialDate) { mutableStateOf(initialDate) }
    var searchedTime by remember(initialTime) { mutableStateOf(initialTime) }

    val filters = listOf("Any", "AC", "Non-AC", "Max ₹2500")
    var selectedFilter by remember { mutableStateOf("Any") }
    
    val rides = remember(pickup, destination, selectedFilter, searchedDate, searchedTime) {
        RideService.matchRides(pickup, destination, searchedDate, searchedTime).filter { ride ->
            when (selectedFilter) {
                "Any" -> true
                "AC" -> ride.tags.contains("AC")
                "Non-AC" -> ride.tags.contains("Non-AC")
                "Max ₹2500" -> ride.price <= 2500
                else -> true
            }
        }
    }

    val isExactSearch = searchedDate.isNotBlank() && searchedTime.isNotBlank()
    val normSearchedDate = remember(searchedDate) { RideService.normalizeDate(searchedDate) }
    
    val exactMatches = remember(rides, isExactSearch, normSearchedDate, searchedTime) {
        if (!isExactSearch) emptyList()
        else rides.filter { ride ->
            RideService.normalizeDate(ride.date) == normSearchedDate &&
            ride.time.trim().equals(searchedTime.trim(), ignoreCase = true)
        }
    }
    
    val similarRides = remember(rides, isExactSearch, exactMatches) {
        if (!isExactSearch) rides
        else rides.filterNot { exactMatches.contains(it) }
    }

    var showFilterSheet by remember { mutableStateOf(false) }
    var tempRideType by remember { mutableStateOf(selectedFilter.let { if (it == "AC" || it == "Non-AC") it else "Any" }) }
    var timeRange by remember { mutableStateOf(0f..24f) }
    var maxPricePrice by remember { mutableStateOf(2500f) }
    var tempDate by remember(searchedDate) { mutableStateOf(searchedDate) }
    var tempVehicle by remember { mutableStateOf("Any") }
    var minSeats by remember { mutableStateOf(1f) }
    
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val selectedDate = datePickerState.selectedDateMillis
                    if (selectedDate != null) {
                        val sdf = java.text.SimpleDateFormat("dd/MM/yy", java.util.Locale.getDefault())
                        tempDate = sdf.format(java.util.Date(selectedDate))
                    }
                    showDatePicker = false
                }) {
                    Text("OK", color = brandYellow, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = textMutedVal)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = cardBgVal,
                titleContentColor = textPrimaryVal,
                headlineContentColor = textPrimaryVal,
                selectedDayContainerColor = brandYellow,
                selectedDayContentColor = Color.Black,
                todayContentColor = brandYellow,
                todayDateBorderColor = brandYellow
            )
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = sheetState,
            containerColor = scaffoldBg,
            dragHandle = { BottomSheetDefaults.DragHandle(color = borderDarkVal) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with Title and Apply button
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, null, tint = brandYellow, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Filter Results",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimaryVal
                        )
                    }
                    Text(
                        "Apply",
                        color = brandYellow,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .clickable {
                                selectedFilter = if (tempRideType != "Any") tempRideType else if (maxPricePrice < 5000) "Max ₹${maxPricePrice.toInt()}" else "Any"
                                searchedDate = tempDate
                                showFilterSheet = false
                            }
                            .padding(8.dp)
                    )
                }
                
                Spacer(Modifier.height(32.dp))
                
                // 1. TIME RANGE
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("TIME RANGE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textMutedVal, letterSpacing = 1.sp)
                    val startHour = timeRange.start.toInt()
                    val endHour = timeRange.endInclusive.toInt()
                    Text(
                        "${if (startHour % 12 == 0) 12 else startHour % 12}:00 ${if (startHour < 12) "AM" else "PM"} - ${if (endHour % 12 == 0) 12 else endHour % 12}:59 ${if (endHour < 12) "AM" else "PM"}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimaryVal
                    )
                }
                Spacer(Modifier.height(12.dp))
                RangeSlider(
                    value = timeRange,
                    onValueChange = { timeRange = it },
                    valueRange = 0f..24f,
                    colors = SliderDefaults.colors(
                        thumbColor = textPrimaryVal,
                        activeTrackColor = brandYellow,
                        inactiveTrackColor = borderDarkVal.copy(alpha = 0.5f)
                    )
                )
                
                Spacer(Modifier.height(32.dp))

                // 2. DATE
                Text("DATE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textMutedVal, letterSpacing = 1.sp)
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(cardBgVal, RoundedCornerShape(12.dp))
                        .border(1.dp, borderDarkVal.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .clickable { showDatePicker = true }
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, null, tint = onSurfaceVariantVal, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(if (tempDate.isEmpty()) "Select Date" else tempDate, color = if (tempDate.isEmpty()) textMutedVal else textPrimaryVal)
                    }
                }

                Spacer(Modifier.height(32.dp))
                
                // 3. MAX PRICE
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("MAX PRICE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textMutedVal, letterSpacing = 1.sp)
                    Text("₹${maxPricePrice.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = textPrimaryVal)
                }
                Spacer(Modifier.height(12.dp))
                Slider(
                    value = maxPricePrice,
                    onValueChange = { maxPricePrice = it },
                    valueRange = 0f..5000f,
                    colors = SliderDefaults.colors(
                        thumbColor = textPrimaryVal,
                        activeTrackColor = brandYellow,
                        inactiveTrackColor = borderDarkVal.copy(alpha = 0.5f)
                    )
                )

                Spacer(Modifier.height(32.dp))
                HorizontalDivider(color = borderDarkVal.copy(alpha = 0.5f))
                Spacer(Modifier.height(32.dp))

                // 4. RIDE TYPE
                Text("RIDE TYPE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textMutedVal, letterSpacing = 1.sp)
                Spacer(Modifier.height(16.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf("Any", "AC", "Non-AC").forEach { type ->
                        val isSelected = tempRideType == type
                        Surface(
                            onClick = { tempRideType = type },
                            color = if (isSelected) brandYellow else cardBgVal,
                            shape = RoundedCornerShape(24.dp),
                            border = if (isSelected) null else BorderStroke(1.dp, borderDarkVal.copy(alpha = 0.5f))
                        ) {
                            Text(type, modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp), color = if (isSelected) Color.Black else textPrimaryVal, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                // 5. VEHICLE
                Text("VEHICLE TYPE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textMutedVal, letterSpacing = 1.sp)
                Spacer(Modifier.height(16.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf("Any", "Sedan", "SUV", "Hatchback", "MPV").forEach { v ->
                        val isSelected = tempVehicle == v
                        Surface(
                            onClick = { tempVehicle = v },
                            color = if (isSelected) brandYellow else cardBgVal,
                            shape = RoundedCornerShape(24.dp),
                            border = if (isSelected) null else BorderStroke(1.dp, borderDarkVal.copy(alpha = 0.5f))
                        ) {
                            Text(v, modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp), color = if (isSelected) Color.Black else textPrimaryVal, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                // 6. SEATS LEFT
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("MIN SEATS AVAILABLE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textMutedVal, letterSpacing = 1.sp)
                    Text("${minSeats.toInt()}+", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = textPrimaryVal)
                }
                Spacer(Modifier.height(12.dp))
                Slider(
                    value = minSeats,
                    onValueChange = { minSeats = it },
                    valueRange = 1f..4f,
                    steps = 2,
                    colors = SliderDefaults.colors(
                        thumbColor = textPrimaryVal,
                        activeTrackColor = brandYellow,
                        inactiveTrackColor = borderDarkVal.copy(alpha = 0.5f)
                    )
                )

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }

    Scaffold(
        containerColor = scaffoldBg,
        bottomBar = {
            CabSyncNavBar(
                selectedTab = NavTab.FIND,
                onHomeClick = onNavigateToHome,
                onFindClick = { },
                onPostClick = onNavigateToPost,
                onJoinedClick = onNavigateToJoined,
                onChatClick = onNavigateToMessages
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            CabSyncScreenHeader("Available Rides", onNavigateBack = onNavigateBack, modifier = Modifier.padding(horizontal = 16.dp))
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            TextField(
                                value = destination,
                                onValueChange = { 
                                    destination = it
                                    if (pickup.isNotEmpty()) pickup = ""
                                    if (searchedDate.isNotEmpty()) searchedDate = ""
                                    if (searchedTime.isNotEmpty()) searchedTime = ""
                                },
                                placeholder = { Text("Where are you going?", fontSize = 14.sp) },
                                modifier = Modifier.weight(1f),
                                leadingIcon = { Icon(Icons.Default.Search, null, tint = onSurfaceVariantVal, modifier = Modifier.size(20.dp)) },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = cardBgVal,
                                    unfocusedContainerColor = cardBgVal,
                                    focusedIndicatorColor = brandYellow,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = brandYellow,
                                    focusedTextColor = textPrimaryVal,
                                    unfocusedTextColor = textPrimaryVal,
                                    focusedPlaceholderColor = onSurfaceVariantVal,
                                    unfocusedPlaceholderColor = onSurfaceVariantVal
                                ),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(cardBgVal, RoundedCornerShape(12.dp))
                                    .border(1.dp, borderDarkVal, RoundedCornerShape(12.dp))
                                    .clickable { showFilterSheet = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Tune, null, tint = textPrimaryVal, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 24.dp).horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filters.forEach { filter ->
                            val isSelected = selectedFilter == filter
                            Surface(
                                modifier = Modifier.clickable { selectedFilter = filter },
                                color = if (isSelected) brandYellow else cardBgVal,
                                shape = RoundedCornerShape(20.dp),
                                border = if (isSelected) null else BorderStroke(1.dp, borderDarkVal)
                            ) {
                                Text(
                                    text = filter,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    color = if (isSelected) Color.Black else textPrimaryVal,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }

                if (rides.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 64.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(surfaceContainerVal, RoundedCornerShape(24.dp))
                                    .border(1.dp, borderDarkVal.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    tint = brandYellow,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                "No Rides Available",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimaryVal,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "We couldn't find any rides matching your search criteria. Be the first to post a ride on this route!",
                                fontSize = 14.sp,
                                color = onSurfaceVariantVal,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                            Button(
                                onClick = onNavigateToPost,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = brandYellow)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Post a Ride",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                } else if (isExactSearch) {
                    if (exactMatches.isNotEmpty()) {
                        item {
                            Text("Exact Matches", fontWeight = FontWeight.Bold, color = brandYellow, fontSize = 18.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                        }
                        items(exactMatches) { ride ->
                            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                RideCard(ride = ride) { onNavigateToRide(ride.id) }
                            }
                        }
                        if (similarRides.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Other Available Rides", fontWeight = FontWeight.Bold, color = textMutedVal, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                            }
                            items(similarRides) { ride ->
                                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                    RideCard(ride = ride) { onNavigateToRide(ride.id) }
                                }
                            }
                        }
                    } else {
                        item {
                            val warningCardBg = if (isDark) Color(0xFF2A1919) else Color(0xFFFFDAD6)
                            val warningBorderColor = if (isDark) Color(0xFF93000A).copy(alpha = 0.5f) else Color(0xFFBA1A1A).copy(alpha = 0.5f)
                            val warningTextColor = if (isDark) Color(0xFFFFB4AB) else Color(0xFF410002)
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = warningCardBg),
                                border = BorderStroke(1.dp, warningBorderColor)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = warningTextColor, modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text("Exact Match Not Found", fontWeight = FontWeight.Bold, color = warningTextColor, fontSize = 16.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("No rides found exactly at $searchedDate, $searchedTime. Showing similar available rides below:", color = warningTextColor.copy(alpha = 0.8f), fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Similar Available Rides", fontWeight = FontWeight.Bold, color = brandYellow, fontSize = 18.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                        }
                        items(similarRides) { ride ->
                            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                RideCard(ride = ride) { onNavigateToRide(ride.id) }
                            }
                        }
                    }
                } else {
                    items(rides) { ride ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            RideCard(ride = ride) { onNavigateToRide(ride.id) }
                        }
                    }
                }
            }
        }
    }
}

