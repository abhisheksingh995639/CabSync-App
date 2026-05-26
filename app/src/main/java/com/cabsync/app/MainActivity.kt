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
import androidx.navigation.navDeepLink
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


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RideService.startListening()
        setContent {
            CabSyncApp()
        }
    }
}

// ──────────────────────────────────────────
// Design System Standards & Tokens
// ──────────────────────────────────────────

@Composable
fun CabSyncApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(SvgDecoder.Factory())
            }
            .build()
    }
    val navController = rememberNavController()

    val prefs = remember(context) { context.getSharedPreferences("cabsync_user_prefs", android.content.Context.MODE_PRIVATE) }
    var isDark by remember { mutableStateOf(prefs.getBoolean("theme_is_dark", true)) }
    val themeState = com.cabsync.app.ui.theme.ThemeState(
        isDark = isDark,
        toggle = {
            isDark = !isDark
            prefs.edit().putBoolean("theme_is_dark", isDark).apply()
        }
    )

    val lightColors = lightColorScheme(
        primary = Color(0xFFB8860B),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFFFD700),
        onPrimaryContainer = Color(0xFF3A3000),
        surface = Color(0xFFF5F5F0),
        onSurface = Color(0xFF1A1A1A),
        surfaceContainer = Color(0xFFEBE8E0),
        background = Color(0xFFF5F5F0),
        onBackground = Color(0xFF1A1A1A),
        outline = Color(0xFF9E9A8E),
        outlineVariant = Color(0xFFCCBF80),
        surfaceTint = Color(0xFFF5F5F0)
    )
    val darkColors = darkColorScheme(
        primary = Color(0xFFFFD700),
        onPrimary = Color(0xFF121212),
        primaryContainer = Color(0xFFFFD700),
        onPrimaryContainer = Color(0xFF3A3000),
        surface = Color(0xFF131313),
        onSurface = Color(0xFFE5E2E1),
        surfaceContainer = Color(0xFF201F1F),
        background = Color(0xFF131313),
        onBackground = Color(0xFFE5E2E1),
        outline = Color(0xFF999077),
        outlineVariant = Color(0xFF4D4732),
        surfaceTint = Color(0xFF131313)
    )

    com.cabsync.app.ui.theme.LocalTheme provides themeState
    MaterialTheme(colorScheme = if (isDark) darkColors else lightColors) {
        CompositionLocalProvider(
            coil.compose.LocalImageLoader provides imageLoader,
            com.cabsync.app.ui.theme.LocalTheme provides themeState
        ) {
            Surface(modifier = Modifier.fillMaxSize()) {
                val navigateToLogin = remember { { navController.navigate("login") } }
                val navigateToSignUp = remember { { navController.navigate("signup") } }
                val navigateToHome = remember { { navController.navigate("home") { popUpTo(0) } } }
                val navigateToSearch = remember { { navController.navigate("search") } }
                val navigateToJoined = remember { { navController.navigate("joined") } }
                val navigateToMessages = remember { { navController.navigate("messages") } }
                val navigateToPost = remember { { navController.navigate("post") } }
                val navigateToProfile = remember { { navController.navigate("profile") } }
                val navigateToProfileEdit = remember { { navController.navigate("profile?edit=true") } }
                val navigateToSettings = remember { { navController.navigate("settings") } }
                val navigateToHistory = remember { { navController.navigate("history") } }
                val navigateToReset = remember { { navController.navigate("reset_password") } }
                val popBackStack = remember { { navController.popBackStack(); Unit } }
                
                val onLogout = remember { {
                    context.getSharedPreferences("cabsync_prefs", android.content.Context.MODE_PRIVATE).edit().clear().apply()
                    context.getSharedPreferences("cabsync_user_prefs", android.content.Context.MODE_PRIVATE).edit().clear().apply()
                    navController.navigate("login") { popUpTo(0) }
                } }
                
                val navigateToBanned = remember { {
                    navController.navigate("banned") { popUpTo(0) }
                } }
                
                // Removed unused onSplashFinish                
                val navigateToSearchWithArgs = remember { { pickup: String, dest: String, date: String, time: String -> 
                    val encP = android.net.Uri.encode(pickup.trim())
                    val encD = android.net.Uri.encode(dest.trim())
                    val encDate = android.net.Uri.encode(date.trim())
                    val encTime = android.net.Uri.encode(time.trim())
                    navController.navigate("search?pickup=$encP&dest=$encD&date=$encDate&time=$encTime") 
                } }
                
                val navigateToRide = remember { { id: String -> 
                    navController.navigate("ride/$id") 
                } }
                
                val navigateToChat = remember { { id: String -> 
                    navController.navigate("chat/$id") 
                } }
                val navigateToPublicProfile = remember { { userId: String ->
                    navController.navigate("user_profile/$userId")
                } }

                val navigateToHelp = remember { { navController.navigate("help") } }
                val navigateToSafety = remember { { navController.navigate("safety") } }
                val navigateToPrivacy = remember { { navController.navigate("privacy") } }
                val navigateToTerms = remember { { navController.navigate("terms") } }

                val safetyContent = listOf(
                    "1. Identity Verification" to "Always verify the identity of your co-commuters. Check their profile ratings and verification badges before starting a journey.",
                    "2. Meet in Public" to "For your first meeting, always choose a well-lit, public location like a campus gate or a major transit hub.",
                    "3. Share Your Trip" to "Use the 'Share Trip' feature to send your live location and ride details to a trusted friend or family member.",
                    "4. Mutual Respect" to "Maintain a professional and friendly environment. Any form of harassment or misconduct will lead to an immediate permanent ban."
                )

                val privacyContent = listOf(
                    "Data Collection" to "We collect minimal data required for ride coordination: your name, email, phone number, and location coordinates when using the app.",
                    "Usage" to "Your data is only used to connect you with relevant ride partners and to improve the matching algorithm. We never sell your data to third parties.",
                    "Security" to "All sensitive information is encrypted. We use industry-standard security protocols to ensure your data stays protected."
                )

                val termsContent = listOf(
                    "User Responsibility" to "Users are responsible for their own safety and conduct. CabSync provides the platform but does not employ hosts or provide transport services.",
                    "Fare Splitting" to "Fare amounts are estimates. Actual splits must be agreed upon by all parties before the trip begins. CabSync does not handle financial transactions between users.",
                    "Cancellations" to "Hosts and passengers should provide at least 1 hour notice for cancellations to maintain a high trust score."
                )

                val helpContent = listOf(
                    "How to Post a Ride?" to "Go to the 'Post' tab, enter your origin, destination, time, and available seats. Your ride will be visible to others instantly.",
                    "How to Join a Ride?" to "Browse available rides, click 'Details', and hit 'Request to Join'. The host will notify you once approved.",
                    "Reporting Issues" to "If you face any issues during a ride, use the 'Report' feature in the Ride Details page or email abhisheksingh995639@gmail.com ."
                )

                NavHost(navController = navController, startDestination = "splash") {
                composable("splash") {
                    SplashScreen(
                        onSignUp = navigateToSignUp,
                        onLogin = navigateToLogin,
                        onNavigateToHome = navigateToHome,
                        onNavigateToBanned = navigateToBanned
                    )
                }
                composable("login") {
                    LoginScreen(
                        onLoginSuccess = navigateToHome,
                        onNavigateToSignUp = navigateToSignUp,
                        onNavigateToReset = navigateToReset,
                        onNavigateToBanned = navigateToBanned
                    )
                }
                composable("banned") {
                    com.cabsync.app.ui.screens.BannedScreen(
                        onLogout = onLogout
                    )
                }
                composable("signup") {
                    SignUpScreen(
                        onSignUpSuccess = navigateToHome,
                        onNavigateToLogin = popBackStack
                    )
                }
                composable("reset_password") {
                    ResetPasswordScreen(onNavigateBack = popBackStack)
                }
                composable("home") {
                    HomeScreen(
                        onNavigateToSearch = navigateToSearchWithArgs,
                        onNavigateToRide = navigateToRide,
                        onNavigateToMessages = navigateToMessages,
                        onNavigateToPost = navigateToPost,
                        onNavigateToProfile = navigateToProfile,
                        onNavigateToJoined = navigateToJoined,
                        onLogout = onLogout,
                        onNavigateToSettings = navigateToSettings,
                        onNavigateToSafety = navigateToSafety,
                        onNavigateToPrivacy = navigateToPrivacy,
                        onNavigateToTerms = navigateToTerms,
                        onNavigateToHelp = navigateToHelp
                    )
                }
                composable("settings") {
                    SettingsScreen(
                        onNavigateBack = popBackStack,
                        onLogout = onLogout,
                        onNavigateToProfile = navigateToProfile,
                        onNavigateToEditProfile = navigateToProfileEdit
                    )
                }
                composable("post") {
                    PostRideScreen(
                        onNavigateBack = popBackStack,
                        onNavigateToHome = navigateToHome,
                        onNavigateToSearch = navigateToSearch,
                        onNavigateToJoined = navigateToJoined,
                        onNavigateToMessages = navigateToMessages
                    )
                }
                composable(
                    "search?pickup={pickup}&dest={dest}&date={date}&time={time}",
                    arguments = listOf(
                        navArgument("pickup") { defaultValue = "" },
                        navArgument("dest") { defaultValue = "" },
                        navArgument("date") { defaultValue = "" },
                        navArgument("time") { defaultValue = "" }
                    )
                ) { backStackEntry ->
                    val rawPickup = backStackEntry.arguments?.getString("pickup") ?: ""
                    val rawDest = backStackEntry.arguments?.getString("dest") ?: ""
                    val rawDate = backStackEntry.arguments?.getString("date") ?: ""
                    val rawTime = backStackEntry.arguments?.getString("time") ?: ""

                    val pickup = android.net.Uri.decode(rawPickup)
                    val dest = android.net.Uri.decode(rawDest)
                    val date = android.net.Uri.decode(rawDate)
                    val time = android.net.Uri.decode(rawTime)

                    SearchScreen(
                        initialPickup = pickup,
                        initialDest = dest,
                        initialDate = date,
                        initialTime = time,
                        onNavigateBack = popBackStack,
                        onNavigateToRide = navigateToRide,
                        onNavigateToJoined = navigateToJoined,
                        onNavigateToMessages = navigateToMessages,
                        onNavigateToPost = navigateToPost,
                        onNavigateToHome = navigateToHome
                    )
                }
                composable("ride/{rideId}",
                    arguments = listOf(navArgument("rideId") { type = NavType.StringType }),
                    deepLinks = listOf(
                        navDeepLink { uriPattern = "https://cabsync.netlify.app/ride/{rideId}" },
                        navDeepLink { uriPattern = "http://cabsync.netlify.app/ride/{rideId}" }
                    )
                ) { backStackEntry ->
                    val rideId = backStackEntry.arguments?.getString("rideId") ?: ""
                    RideDetailsScreen(
                        rideId = rideId,
                        onNavigateBack = popBackStack,
                        onNavigateToChat = navigateToChat,
                        onNavigateToPublicProfile = navigateToPublicProfile
                    )
                }
                composable(
                    "profile?edit={edit}",
                    arguments = listOf(androidx.navigation.navArgument("edit") { defaultValue = false; type = androidx.navigation.NavType.BoolType })
                ) { backStackEntry ->
                    val edit = backStackEntry.arguments?.getBoolean("edit") ?: false
                    ProfileScreen(
                        startEditing = edit,
                        onNavigateBack = popBackStack,
                        onNavigateToSearch = navigateToSearch,
                        onLogout = onLogout,
                        onNavigateToRide = navigateToRide,
                        onNavigateToHistory = navigateToHistory
                    )
                }
                composable("user_profile/{userId}",
                    arguments = listOf(navArgument("userId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val userId = backStackEntry.arguments?.getString("userId") ?: ""
                    com.cabsync.app.ui.screens.PublicProfileScreen(
                        userId = userId,
                        onNavigateBack = popBackStack,
                        onNavigateToRide = navigateToRide
                    )
                }
                composable("joined") {
                    JoinedRidesScreen(
                        onNavigateToHome = navigateToHome,
                        onNavigateToSearch = navigateToSearch,
                        onNavigateToJoined = { },
                        onNavigateToProfile = navigateToProfile,
                        onNavigateBack = popBackStack,
                        onNavigateToPost = navigateToPost,
                        onNavigateToMessages = navigateToMessages,
                        onNavigateToRide = navigateToRide
                    )
                }
                composable("messages") {
                    MessagesScreen(
                        onNavigateToChat = navigateToChat,
                        onNavigateBack = popBackStack,
                        onNavigateToHome = navigateToHome,
                        onNavigateToSearch = navigateToSearch,
                        onNavigateToJoined = navigateToJoined,
                        onNavigateToPost = navigateToPost
                    )
                }
                composable("chat/{rideId}") { backStackEntry ->
                    val rId = backStackEntry.arguments?.getString("rideId") ?: ""
                    ChatScreen(
                        rideId = rId,
                        onNavigateBack = popBackStack
                    )
                }
                composable("safety") {
                    com.cabsync.app.ui.screens.StaticContentScreen(
                        title = "Safety Guidelines",
                        contentPairs = safetyContent,
                        onNavigateBack = popBackStack
                    )
                }
                composable("privacy") {
                    com.cabsync.app.ui.screens.StaticContentScreen(
                        title = "Privacy Policy",
                        contentPairs = privacyContent,
                        onNavigateBack = popBackStack
                    )
                }
                composable("terms") {
                    com.cabsync.app.ui.screens.StaticContentScreen(
                        title = "Terms of Service",
                        contentPairs = termsContent,
                        onNavigateBack = popBackStack
                    )
                }
                composable("help") {
                    com.cabsync.app.ui.screens.StaticContentScreen(
                        title = "Help Center",
                        contentPairs = helpContent,
                        onNavigateBack = popBackStack
                    )
                }
                composable("history") {
                    HistoryScreen(
                        onNavigateBack = popBackStack,
                        onNavigateToSearch = navigateToSearch,
                        onNavigateToRide = navigateToRide
                    )
                }
            }
        }
    }
    }
}



