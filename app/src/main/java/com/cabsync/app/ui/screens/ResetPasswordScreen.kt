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
fun ResetPasswordScreen(onNavigateBack: () -> Unit) {
    var email by remember { mutableStateOf("") }

    val brandYellow = Color(0xFFFFD700)
    val brandDark = Color(0xFF1E1E1E)
    val brandTextMuted = Color(0xFFA0A0A0)
    val brandText = Color(0xFFF5F5F5)

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF121212))) {
        // Hero Section
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
        ) {
            AsyncImage(
                model = "https://lh3.googleusercontent.com/aida/ADBb0ughV1leKDnyFvwa62MGX2PbN6jlDWiAsdQTmcE7R5vU5nnOS1N-fx0qP8ZkKXGHYct83aVY9RuXkLNkJ7QQCqtqddPPn9EbPu1ygJ5yqtNpBH77JtvKIUTNiEQbTBSQxBKpDmvvcpuRmeVma_jcLvPoTgSc8ssk9-tWeg213wWhI5O7n_JIlHVbz9Ku41QA8oxm7mal7gAdEaQBFiXybMg5QobT_giQ8HWSUKn4RtIWiI4bpjaM7K9AyJqi",
                contentDescription = null,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Glass overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f   to Color(0x33121212),
                            0.7f to Color(0xCC121212),
                            1f   to Color(0xFF121212)
                        )
                    )
            )
            
            // Back button
            CabSyncBackButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(start = 16.dp, top = 16.dp)
                    .align(Alignment.TopStart)
            )
            
            // Title
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 48.dp)
            ) {
                Text(
                    "Reset\nPassword",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 44.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "We'll help you get back on track.",
                    color = brandTextMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Form Container
        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (-32).dp)
                .background(brandDark, RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                .padding(horizontal = 24.dp)
                .padding(top = 40.dp, bottom = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFF353534), RoundedCornerShape(32.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.LockReset,
                        contentDescription = null,
                        tint = brandYellow,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            // Email
            LabelledInput(
                label = "Email Address",
                value = email,
                onValueChange = { email = it },
                placeholder = "Enter your email",
                leadingIcon = { Icon(Icons.Outlined.Email, null, tint = brandTextMuted) },
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Reset Button
            CabSyncButton("Send Reset Link", onClick = { }, icon = Icons.AutoMirrored.Filled.ArrowForward)

            Spacer(modifier = Modifier.height(32.dp))

            // Footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateBack() }
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = brandYellow,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Return to Login",
                    color = brandYellow,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

