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
import androidx.compose.foundation.Image


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SplashScreen(
    onSignUp: () -> Unit,
    onLogin: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToBanned: () -> Unit = {}
) {
    val auth = remember { com.google.firebase.auth.FirebaseAuth.getInstance() }
    var isCheckingAuth by remember { mutableStateOf(true) }

    LaunchedEffect(auth.currentUser) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            db.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists() && snapshot.getBoolean("isBanned") == true) {
                        onNavigateToBanned()
                    } else {
                        onNavigateToHome()
                    }
                }
                .addOnFailureListener {
                    onNavigateToHome()
                }
        } else {
            isCheckingAuth = false
        }
    }

    if (isCheckingAuth) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF131313)))
        return
    }

    val brandYellow = CabSyncTokens.BrandYellow
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF121212))) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> OnboardingPage(
                    title = "Find a Ride",
                    subtitle = "Anytime, Anywhere",
                    description = "Connect with verified drivers heading your way. Share the journey and split the costs seamlessly.",
                    imageRes = "https://images.unsplash.com/photo-1449965408869-eaa3f722e40d?q=80&w=1000&auto=format&fit=crop",
                    brandYellow = brandYellow
                )
                1 -> OnboardingPage(
                    title = "Post your Route",
                    subtitle = "Earn while driving",
                    description = "Have empty seats? Post your upcoming trip and accept ride requests from passengers on your route.",
                    imageRes = "https://images.unsplash.com/photo-1549317661-bd32c8ce0db2?q=80&w=1000&auto=format&fit=crop",
                    brandYellow = brandYellow
                )
                2 -> OnboardingPage(
                    title = "Travel Safely",
                    subtitle = "Verified Community",
                    description = "Every user is verified. Check ratings, read reviews, and travel with peace of mind.",
                    imageRes = "https://images.unsplash.com/photo-1516733968668-dbdce39c4651?q=80&w=1000&auto=format&fit=crop",
                    brandYellow = brandYellow
                )
                3 -> FinalOnboardingPage(
                    title = "Ready to Sync?",
                    description = "Join thousands of commuters sharing rides every day.",
                    imageRes = "https://images.unsplash.com/photo-1449965408869-eaa3f722e40d?q=80&w=1000&auto=format&fit=crop",
                    onSignUp = onSignUp,
                    onLogin = onLogin,
                    brandYellow = brandYellow
                )
            }
        }

        // Pagination Dots
        if (pagerState.currentPage < 3) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(4) { iteration ->
                    val color = if (pagerState.currentPage == iteration) brandYellow else Color.White.copy(alpha = 0.3f)
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (pagerState.currentPage == iteration) 10.dp else 8.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(color)
                    )
                }
            }

            // Next/Skip Buttons
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Skip",
                    color = Color.White.copy(alpha = 0.6f),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable {
                        coroutineScope.launch { pagerState.animateScrollToPage(3) }
                    }.padding(8.dp)
                )
                
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .background(brandYellow, androidx.compose.foundation.shape.CircleShape)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next",
                        tint = Color.Black
                    )
                }
            }
        }
    }
}



