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
fun MessagesScreen(
    onNavigateToChat: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToJoined: () -> Unit,
    onNavigateToPost: () -> Unit
) {
    val isDark = com.cabsync.app.ui.theme.LocalTheme.current.isDark
    val brandYellow = if (isDark) Color(0xFFFFD700) else CabSyncTokens.LightTextGold
    val brandDark = if (isDark) Color(0xFF131313) else CabSyncTokens.LightBackground
    val surfaceContainer = if (isDark) Color(0xFF201F1F) else CabSyncTokens.LightSurfaceContainer
    val onSurfaceVariant = if (isDark) Color(0xFFD0C6AB) else CabSyncTokens.LightTextMuted
    val textPrimary = if (isDark) Color.White else CabSyncTokens.LightTextPrimary
    val textMuted = if (isDark) Color.Gray else CabSyncTokens.LightTextMuted
    val dividerColor = if (isDark) Color(0xFF353534) else CabSyncTokens.LightBorderDark

    var searchQuery by remember { mutableStateOf("") }
    val filteredMessages = RideService.liveRides.filter { ride ->
        ride.hostName.contains(searchQuery, ignoreCase = true) || 
        ride.destination.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        containerColor = brandDark,
        bottomBar = {
            CabSyncNavBar(
                selectedTab = NavTab.CHAT,
                onHomeClick = onNavigateToHome,
                onFindClick = onNavigateToSearch,
                onPostClick = onNavigateToPost,
                onJoinedClick = onNavigateToJoined,
                onChatClick = { }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            CabSyncScreenHeader("Messages", onNavigateBack = onNavigateBack, modifier = Modifier.padding(horizontal = 16.dp))
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                item {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search messages...", color = onSurfaceVariant.copy(alpha = 0.5f)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = onSurfaceVariant) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, null, tint = onSurfaceVariant)
                                }
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = surfaceContainer,
                            unfocusedContainerColor = surfaceContainer,
                            focusedIndicatorColor = brandYellow,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary
                        ),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )
                }

                items(filteredMessages) { ride ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onNavigateToChat(ride.id) }.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box {
                            AsyncImage(
                                model = ride.hostAvatar,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp))
                            )
                            if (ride.id == "1") {
                                Box(modifier = Modifier.size(12.dp).background(Color(0xFFFFD700), RoundedCornerShape(6.dp)).border(2.dp, brandDark, RoundedCornerShape(6.dp)).align(Alignment.TopEnd).offset(x = 4.dp, y = (-4).dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(ride.hostName, fontWeight = FontWeight.Bold, color = textPrimary)
                                Text(if (ride.id == "1") "Just now" else "2h ago", fontSize = 12.sp, color = if (ride.id == "1") brandYellow else textMuted)
                            }
                            Text("Ride to ${ride.destination}", fontSize = 12.sp, color = if (ride.id == "1") brandYellow else textMuted)
                            Text("Tap to open chat", fontSize = 14.sp, color = textMuted, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        }
                    }
                    HorizontalDivider(color = dividerColor.copy(alpha = 0.5f))
                }
            }
        }
    }
}

