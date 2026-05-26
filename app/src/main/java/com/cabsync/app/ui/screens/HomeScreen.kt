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
fun HomeScreen(
    onNavigateToSearch: (String, String, String, String) -> Unit,
    onNavigateToRide: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToJoined: () -> Unit,
    onNavigateToMessages: () -> Unit,
    onNavigateToPost: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSafety: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {},
    onNavigateToTerms: () -> Unit = {},
    onNavigateToHelp: () -> Unit = {}
) {
    val currentUser = remember { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser }
    val context = LocalContext.current
    val sharedPrefs = remember(context) { context.getSharedPreferences("cabsync_user_prefs", android.content.Context.MODE_PRIVATE) }
    var avatarUrl by remember {
        mutableStateOf(
            sharedPrefs.getString("user_avatar", null)
                ?: currentUser?.photoUrl?.toString()
                ?: "https://api.dicebear.com/7.x/avataaars/svg?seed=${currentUser?.displayName ?: "User"}"
        )
    }
    var firstName by remember {
        mutableStateOf(
            (sharedPrefs.getString("user_name", currentUser?.displayName?.ifEmpty { null }) ?: currentUser?.email?.substringBefore('@') ?: "User").split(" ").first().uppercase()
        )
    }

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    currentUser?.uid?.let { uid ->
                        com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("users").document(uid)
                            .update("fcmToken", token)
                    }
                }
            }
        }
    }

    LaunchedEffect(currentUser) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val permission = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
            if (permission != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            } else {
                com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
                        currentUser?.uid?.let { uid ->
                            com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("users").document(uid)
                                .update("fcmToken", token)
                        }
                    }
                }
            }
        } else {
            com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    currentUser?.uid?.let { uid ->
                        com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("users").document(uid)
                            .update("fcmToken", token)
                    }
                }
            }
        }

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
            if (profile.name.isNotEmpty()) {
                firstName = profile.name.split(" ").first().uppercase()
            }
        }
    }

    val myPostedRides = remember(RideService.liveRides, currentUser) {
        RideService.liveRides.filter { it.hostId == currentUser?.uid && it.status.equals("open", ignoreCase = true) }
    }
    val myJoinedRides = remember(RideService.liveRides, currentUser) {
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

    val pendingRequestsPreview = remember(myRequests, RideService.liveRides) {
        myRequests.filter { it.status == "pending" }.mapNotNull { req ->
            val ride = RideService.liveRides.find { it.id == req.rideId }
            if (ride != null) Pair(req, ride) else null
        }
    }

    var pickup by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var globalAnnouncement by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("system").document("announcement")
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null && snapshot.exists()) {
                    val isActive = snapshot.getBoolean("active") ?: false
                    if (isActive) {
                        globalAnnouncement = snapshot.getString("message")
                    } else {
                        globalAnnouncement = null
                    }
                } else {
                    globalAnnouncement = null
                }
            }
    }



    if (showDatePicker) {
        val calendar = java.util.Calendar.getInstance()
        android.app.DatePickerDialog(
            context,
            R.style.CabSyncPickerDialog,
            { _, year, month, day ->
                date = "%02d/%02d/%04d".format(day, month + 1, year)
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
    val textPrimary = if (isDark) Color.White else CabSyncTokens.LightTextPrimary

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            CabSyncDrawerContent(
                onClose = { coroutineScope.launch { drawerState.close() } },
                onNavigateToProfile = onNavigateToProfile,
                onLogout = {
                    coroutineScope.launch { drawerState.close() }
                    com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                    context.getSharedPreferences("cabsync_prefs", android.content.Context.MODE_PRIVATE).edit().clear().apply()
                    context.getSharedPreferences("cabsync_user_prefs", android.content.Context.MODE_PRIVATE).edit().clear().apply()
                    onLogout()
                },
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToSafety = onNavigateToSafety,
                onNavigateToPrivacy = onNavigateToPrivacy,
                onNavigateToTerms = onNavigateToTerms,
                onNavigateToHelp = onNavigateToHelp
            )
        }
    ) {
        Scaffold(
            containerColor = brandDark,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { 
                        Text("CabSync", fontWeight = FontWeight.ExtraBold, color = brandYellow, fontSize = 28.sp) 
                    },
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, null, tint = brandYellow, modifier = Modifier.size(32.dp))
                        }
                    },
                    actions = {
                        Box(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(40.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .border(2.dp, brandYellow, RoundedCornerShape(20.dp))
                                .clickable { onNavigateToProfile() }
                        ) {
                            AsyncImage(
                                model = avatarUrl,
                                contentDescription = "User Profile",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().background(surfaceContainer)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = brandDark)
                )
            },
            bottomBar = {
                CabSyncNavBar(
                    selectedTab = NavTab.HOME,
                    onHomeClick = { },
                    onFindClick = { onNavigateToSearch("", "", "", "") },
                    onPostClick = onNavigateToPost,
                    onJoinedClick = onNavigateToJoined,
                    onChatClick = onNavigateToMessages
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                if (!globalAnnouncement.isNullOrEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(brandYellow, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Campaign, contentDescription = "Announcement", tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = globalAnnouncement!!,
                                color = Color.Black,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                item {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        Text(
                            text = buildAnnotatedString {
                                append("WELCOME BACK, ")
                                withStyle(SpanStyle(color = brandYellow)) {
                                    append(firstName)
                                }
                            },
                            color = onSurfaceVariant,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = buildAnnotatedString {
                                append("Go anywhere with\n")
                                withStyle(SpanStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, fontFamily = androidx.compose.ui.text.font.FontFamily.Serif, color = brandYellow)) {
                                    append("CabSync.")
                                }
                            },
                            color = textPrimary,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 38.sp
                        )
                        Text(
                            text = "Reliable rides, verified community, smarter splitting.",
                            color = onSurfaceVariant,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                item {
                    val searchBg = if (isDark) listOf(Color(0xFF252218), Color(0xFF131313)) else listOf(Color(0xFFFAF7EE), Color(0xFFEBE8E0))
                    val searchBorder = if (isDark) CabSyncTokens.BorderGlow else CabSyncTokens.LightBorderGlow
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        border = BorderStroke(1.dp, searchBorder)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().background(
                                Brush.verticalGradient(colors = searchBg),
                                shape = RoundedCornerShape(24.dp)
                            )
                        ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            DashboardInput(
                                value = pickup,
                                onValueChange = { pickup = it },
                                placeholder = "Pickup",
                                icon = Icons.Default.LocationOn,
                                modifier = Modifier.fillMaxWidth()
                            )
                            DashboardInput(
                                value = destination,
                                onValueChange = { destination = it },
                                placeholder = "Destination",
                                icon = Icons.Default.Flag,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                DateTimePickerField(
                                    value = date,
                                    placeholder = "Date",
                                    icon = Icons.Default.CalendarToday,
                                    onClick = { showDatePicker = true },
                                    modifier = Modifier.weight(1f)
                                )
                                DateTimePickerField(
                                    value = time,
                                    placeholder = "Time",
                                    icon = Icons.Default.Schedule,
                                    onClick = { showTimePicker = true },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (errorMessage.isNotEmpty()) {
                                Text(
                                    text = errorMessage,
                                    color = Color(0xFFFFB4AB),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                            Button(
                                onClick = {
                                    if (pickup.isBlank() || destination.isBlank() || date.isBlank() || time.isBlank()) {
                                        errorMessage = "Please fill in all fields (Pickup, Destination, Date, and Time)."
                                        android.widget.Toast.makeText(context, "All fields are required", android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        errorMessage = ""
                                        onNavigateToSearch(pickup, destination, date, time)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = brandYellow)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Search, null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Search Rides", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }
                        }
                    }
                }

                item {
                    DashboardSection(
                        title = "My Posted Rides",
                        icon = Icons.Default.PersonPinCircle,
                        emptyText = "No rides posted yet",
                        emptySubtext = "Share your journey and split the costs with the community.",
                        emptyIcon = Icons.Default.DirectionsCar,
                        actionText = "Post Your First Ride",
                        onActionClick = onNavigateToPost,
                        content = if (myPostedRides.isNotEmpty()) {
                            {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    myPostedRides.take(2).forEach { ride ->
                                        val postedBorder = if (isDark) CabSyncTokens.BorderGlow else CabSyncTokens.LightBorderGlow
                                        val postedBgColors = if (isDark) listOf(Color(0xFF242115), Color(0xFF1C1B1B)) else CabSyncTokens.LightGradientCard
                                        Card(
                                            modifier = Modifier.fillMaxWidth().clickable { onNavigateToRide(ride.id) },
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                            border = BorderStroke(1.dp, postedBorder)
                                        ) {
                                            Box(
                                                modifier = Modifier.fillMaxWidth().background(
                                                    Brush.horizontalGradient(colors = postedBgColors),
                                                    shape = RoundedCornerShape(16.dp)
                                                )
                                            ) {
                                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Commute, null, tint = brandYellow, modifier = Modifier.size(28.dp))
                                                Spacer(modifier = Modifier.width(16.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text("${ride.pickup} → ${ride.destination}", fontWeight = FontWeight.Bold, color = textPrimary)
                                                    Text("${ride.date} • ${ride.time}", fontSize = 12.sp, color = if (isDark) Color.Gray else CabSyncTokens.LightTextMuted)
                                                }
                                                Text("₹${ride.price}", fontWeight = FontWeight.Bold, color = brandYellow)
                                            }
                                             }
                                        }
                                    }
                                }
                            }
                        } else null
                    )
                }

                item {
                    DashboardSection(
                        title = "Joined Rides",
                        icon = Icons.Default.Group,
                        onViewAll = onNavigateToJoined,
                        emptyText = "No joined rides yet",
                        emptySubtext = "Start joining carpools to track your journey statuses here.",
                        emptyIcon = Icons.Default.Group,
                        actionText = "Find a Ride",
                        onActionClick = { onNavigateToSearch("", "", "", "") },
                        modifier = Modifier.padding(bottom = 32.dp),
                        content = if (myJoinedRides.isNotEmpty() || pendingRequestsPreview.isNotEmpty()) {
                            {
                                val joinedItems = myJoinedRides.map { it to "CONFIRMED" }
                                val pendingItems = pendingRequestsPreview.map { it.second to "PENDING" }
                                val combinedItems = (joinedItems + pendingItems).take(2)
                                
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    combinedItems.forEach { (ride, status) ->
                                        val joinedBorder = if (status == "CONFIRMED") {
                                            if (isDark) Color(0xFF4ADE80).copy(alpha = 0.3f) else Color(0xFF2E7D32).copy(alpha = 0.4f)
                                        } else {
                                            if (isDark) Color(0xFFFFD700).copy(alpha = 0.3f) else Color(0xFFFBC02D).copy(alpha = 0.4f)
                                        }
                                        val joinedBgColors = if (status == "CONFIRMED") {
                                            if (isDark) listOf(Color(0xFF162016), Color(0xFF1C1B1B)) else listOf(Color(0xFFE8F5E9), Color(0xFFE5E2D8))
                                        } else {
                                            if (isDark) listOf(Color(0xFF242115), Color(0xFF1C1B1B)) else listOf(Color(0xFFFFFDE7), Color(0xFFE5E2D8))
                                        }
                                        val statusColor = if (status == "CONFIRMED") {
                                            if (isDark) Color(0xFF8CD69E) else Color(0xFF2E7D32)
                                        } else {
                                            if (isDark) Color(0xFFFFD700) else Color(0xFFF57F17)
                                        }
                                        Card(
                                            modifier = Modifier.fillMaxWidth().clickable { onNavigateToRide(ride.id) },
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                            border = BorderStroke(1.dp, joinedBorder)
                                        ) {
                                            Box(
                                                modifier = Modifier.fillMaxWidth().background(
                                                    Brush.horizontalGradient(colors = joinedBgColors),
                                                    shape = RoundedCornerShape(16.dp)
                                                )
                                            ) {
                                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                                AsyncImage(
                                                    model = ride.hostAvatar,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(Color.DarkGray)
                                                )
                                                Spacer(modifier = Modifier.width(16.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text("${ride.pickup} → ${ride.destination}", fontWeight = FontWeight.Bold, color = textPrimary)
                                                    Text("Host: ${ride.hostName}", fontSize = 12.sp, color = if (isDark) Color.Gray else CabSyncTokens.LightTextMuted)
                                                }
                                                Text(status, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = statusColor)
                                            }
                                        }
                                             }
                                    }
                                }
                            }
                        } else null
                    )
                }
            }
        }
    }
}

