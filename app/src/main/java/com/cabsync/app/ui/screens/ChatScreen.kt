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
fun ChatScreen(rideId: String, onNavigateBack: () -> Unit) {
    val isDark = com.cabsync.app.ui.theme.LocalTheme.current.isDark
    val scaffoldBg = if (isDark) Color(0xFF131313) else CabSyncTokens.LightBackground
    val cardBg = if (isDark) Color(0xFF201F1F) else CabSyncTokens.LightSurface
    val textPrimary = if (isDark) Color.White else CabSyncTokens.LightTextPrimary
    val textMuted = if (isDark) Color.Gray else CabSyncTokens.LightTextMuted
    val borderDark = if (isDark) Color(0xFF4D4732) else CabSyncTokens.LightBorderDark

    val ride = RideService.liveRides.find { it.id == rideId }
    var message by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf<List<com.cabsync.app.models.ChatMessage>>(emptyList()) }
    val currentUserId = remember { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    DisposableEffect(rideId) {
        val reg = RideService.observeMessages(rideId) { newMsgs ->
            messages = newMsgs
        }
        onDispose { reg?.remove() }
    }

    Scaffold(
        containerColor = scaffoldBg,
        bottomBar = {
            BottomAppBar(
                containerColor = cardBg,
                modifier = Modifier.navigationBarsPadding().imePadding()
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = message,
                        onValueChange = { message = it },
                        placeholder = { Text("Type a message...", color = textMuted.copy(alpha = 0.6f)) },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = scaffoldBg,
                            unfocusedContainerColor = scaffoldBg,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (message.isNotBlank()) {
                                RideService.sendMessage(rideId, message.trim())
                                message = ""
                            }
                        },
                        modifier = Modifier.background(Color(0xFFFFD700), RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color.Black)
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                CabSyncBackButton(onClick = onNavigateBack)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(ride?.carModel ?: "Ride Chat", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                    Text("${ride?.pickup} → ${ride?.destination}", fontSize = 12.sp, color = textMuted)
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
            ) {
                item {
                    Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, borderDark.copy(alpha = 0.3f))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, null, tint = Color(0xFFFFD700), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Safety First", fontWeight = FontWeight.Bold, color = textPrimary)
                            Text("This is a shared ride chat. Coordinate your pickup details safely.", color = textMuted, fontSize = 12.sp)
                        }
                    }
                }
            }
            
            items(messages) { msg ->
                val isMine = msg.senderId == currentUserId
                val sdf = remember { java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()) }
                val timeStr = remember(msg.timestamp) { sdf.format(java.util.Date(msg.timestamp)) }
                
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    contentAlignment = if (isMine) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Column(horizontalAlignment = if (isMine) Alignment.End else Alignment.Start) {
                        if (!isMine) {
                            Text(msg.senderName, fontSize = 10.sp, color = Color(0xFFFFD700), modifier = Modifier.padding(bottom = 2.dp, start = 4.dp))
                        }
                        Surface(
                            color = if (isMine) Color(0xFFFFD700) else (if (isDark) Color(0xFF2A2A2A) else CabSyncTokens.LightSurfaceContainer),
                            border = if (isMine) null else BorderStroke(1.dp, if (isDark) Color(0xFF353534) else CabSyncTokens.LightBorderDark),
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isMine) 16.dp else 4.dp,
                                bottomEnd = if (isMine) 4.dp else 16.dp
                            )
                        ) {
                            Text(
                                text = msg.text,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                color = if (isMine) Color.Black else textPrimary
                            )
                        }
                        Text(timeStr, fontSize = 10.sp, color = textMuted, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }
    }
}
}

