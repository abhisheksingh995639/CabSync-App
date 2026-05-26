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
import com.google.firebase.firestore.FirebaseFirestore


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToProfile: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {}
) {
    val currentUser = remember { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sharedPrefs = remember(context) { context.getSharedPreferences("cabsync_user_prefs", android.content.Context.MODE_PRIVATE) }
    val name = remember(currentUser, sharedPrefs) { sharedPrefs.getString("user_name", currentUser?.displayName?.ifEmpty { null }) ?: currentUser?.email?.substringBefore('@') ?: "Abhishek Singh" }
    val phone = remember(currentUser, sharedPrefs) { sharedPrefs.getString("user_phone", currentUser?.phoneNumber) ?: "" }
    var userProfileState by remember { mutableStateOf<com.cabsync.app.models.UserProfile?>(null) }
    val avatarUrl = remember(userProfileState, currentUser, name) {
        userProfileState?.photoUrl?.ifEmpty { null }
            ?: sharedPrefs.getString("user_avatar", null)
            ?: currentUser?.photoUrl?.toString()
            ?: "https://api.dicebear.com/7.x/avataaars/svg?seed=$name"
    }
    var userLanguageState by remember { mutableStateOf("English (US)") }
    var phoneVisibilityState by remember { mutableStateOf("Everyone") }

    var showVisibilityDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    var notificationsEnabled by remember { mutableStateOf(true) }
    var rideHistory by remember { mutableStateOf(true) }

    val isDark = com.cabsync.app.ui.theme.LocalTheme.current.isDark
    val scaffoldBg = if (isDark) Color(0xFF131313) else CabSyncTokens.LightBackground
    val cardBg = if (isDark) Color(0xFF201F1F) else CabSyncTokens.LightSurface
    val cardBorderColor = if (isDark) Color(0xFF353534) else CabSyncTokens.LightBorderDark
    val textHeaderColor = if (isDark) CabSyncTokens.OnSurfaceVariant else CabSyncTokens.LightOnSurfaceVariant
    val dialogBg = if (isDark) Color(0xFF1C1B1B) else CabSyncTokens.LightSurface
    val dialogTextPrimary = if (isDark) Color.White else CabSyncTokens.LightTextPrimary
    val dialogTextMuted = if (isDark) Color.Gray else CabSyncTokens.LightTextMuted
    val logoutBtnBg = if (isDark) Color(0xFF201F1F) else CabSyncTokens.LightSurface

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
            userProfileState = profile
            phoneVisibilityState = profile.phoneVisibility
            notificationsEnabled = profile.notificationsEnabled
            rideHistory = profile.rideHistoryVisible
        }
    }

    fun saveProfileSetting(update: (com.cabsync.app.models.UserProfile) -> com.cabsync.app.models.UserProfile) {
        currentUser?.uid?.let { uid ->
            val cur = userProfileState ?: com.cabsync.app.models.UserProfile(uid = uid)
            val updated = update(cur)
            userProfileState = updated
            
            val updates = mapOf<String, Any>(
                "phoneVisibility" to updated.phoneVisibility,
                "notificationsEnabled" to updated.notificationsEnabled,
                "rideHistoryVisible" to updated.rideHistoryVisible
            )
            com.cabsync.app.data.UserService.updateUserFields(uid, updates)
        }
    }

    if (showVisibilityDialog) {
        AlertDialog(
            onDismissRequest = { showVisibilityDialog = false },
            containerColor = dialogBg,
            shape = RoundedCornerShape(28.dp),
            title = { Text("Phone Number", fontWeight = FontWeight.Bold, color = if (isDark) CabSyncTokens.BrandYellow else CabSyncTokens.LightTextGold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Everyone", "Group Member", "Only Me").forEach { vis ->
                        Surface(
                            onClick = {
                                phoneVisibilityState = vis
                                saveProfileSetting { it.copy(phoneVisibility = vis) }
                                showVisibilityDialog = false
                                android.widget.Toast.makeText(context, "Phone visibility set to $vis", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            color = if (phoneVisibilityState == vis) (if (isDark) Color(0xFF353534) else CabSyncTokens.LightSurfaceContainer) else Color.Transparent,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(vis, color = dialogTextPrimary, modifier = Modifier.padding(16.dp), fontSize = 16.sp, fontWeight = if (phoneVisibilityState == vis) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            containerColor = dialogBg,
            shape = RoundedCornerShape(28.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Commute, null, tint = CabSyncTokens.BrandYellow, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("About CabSync", fontWeight = FontWeight.Bold, color = dialogTextPrimary, fontSize = 20.sp)
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Version 1.0.0", color = if (isDark) CabSyncTokens.BrandYellow else CabSyncTokens.LightTextGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("CabSync is a premium carpooling and ride-sharing platform designed for secure, split-fare commuting.", color = dialogTextMuted, fontSize = 14.sp, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("© 2026 CabSync Technologies", color = if (isDark) Color.DarkGray else CabSyncTokens.LightTextMuted, fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CabSyncTokens.BrandYellow, contentColor = Color.Black),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("OK", fontWeight = FontWeight.Bold) }
            }
        )
    }

    var showDeactivateDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeactivateDialog) {
        AlertDialog(
            onDismissRequest = { showDeactivateDialog = false },
            containerColor = dialogBg,
            shape = RoundedCornerShape(28.dp),
            title = { Text("Deactivate Account", fontWeight = FontWeight.Bold, color = Color(0xFFFFB4AB)) },
            text = { Text("Your profile will be hidden from other users. You can reactivate your account anytime by logging back in. Proceed?", color = dialogTextPrimary) },
            confirmButton = {
                var isDeactivating by remember { mutableStateOf(false) }
                TextButton(
                    enabled = !isDeactivating,
                    onClick = {
                        isDeactivating = true
                        currentUser?.uid?.let { uid ->
                            val db = FirebaseFirestore.getInstance()
                            db.collection("users").document(uid)
                                .update(mapOf(
                                    "isDeactivated" to true,
                                    "deactivatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                                ))
                                .addOnSuccessListener {
                                    android.widget.Toast.makeText(context, "Account deactivated. See you soon!", android.widget.Toast.LENGTH_LONG).show()
                                    showDeactivateDialog = false
                                    isDeactivating = false
                                    onLogout()
                                }
                                .addOnFailureListener {
                                    android.widget.Toast.makeText(context, "Failed to deactivate account", android.widget.Toast.LENGTH_SHORT).show()
                                    isDeactivating = false
                                }
                        }
                    }
                ) { Text(if (isDeactivating) "Deactivating..." else "Deactivate", color = Color(0xFFFFB4AB), fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeactivateDialog = false }) { Text("Cancel", color = dialogTextMuted) }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = dialogBg,
            shape = RoundedCornerShape(28.dp),
            title = { Text("Delete Account", fontWeight = FontWeight.Bold, color = Color(0xFFFFB4AB)) },
            text = { Text("This action is permanent and cannot be undone. All your ride history and profile data will be deleted. Are you absolutely sure?", color = dialogTextPrimary) },
            confirmButton = {
                var isDeleting by remember { mutableStateOf(false) }
                TextButton(
                    enabled = !isDeleting,
                    onClick = {
                        isDeleting = true
                        currentUser?.uid?.let { uid ->
                            val db = FirebaseFirestore.getInstance()
                            db.collection("users").document(uid)
                                .update(mapOf(
                                    "isDeleted" to true,
                                    "deletedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                                ))
                                .addOnSuccessListener {
                                    currentUser.delete()
                                        .addOnCompleteListener { task ->
                                            isDeleting = false
                                            showDeleteDialog = false
                                            if (task.isSuccessful) {
                                                android.widget.Toast.makeText(context, "Your account has been deleted.", android.widget.Toast.LENGTH_LONG).show()
                                                onLogout()
                                            } else {
                                                val exception = task.exception
                                                if (exception is com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException) {
                                                    android.widget.Toast.makeText(context, "Please log out and log back in before deleting your account for security reasons.", android.widget.Toast.LENGTH_LONG).show()
                                                } else {
                                                    android.widget.Toast.makeText(context, "Account deleted from database. Please re-authenticate to clear auth session.", android.widget.Toast.LENGTH_LONG).show()
                                                    onLogout()
                                                }
                                            }
                                        }
                                }
                                .addOnFailureListener {
                                    android.widget.Toast.makeText(context, "Failed to delete account from database", android.widget.Toast.LENGTH_SHORT).show()
                                    isDeleting = false
                                }
                        }
                    }
                ) { Text(if (isDeleting) "Deleting..." else "Delete Permanently", color = Color(0xFFFFB4AB), fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel", color = dialogTextMuted) }
            }
        )
    }

    Scaffold(
        containerColor = scaffoldBg
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            CabSyncScreenHeader(title = "Settings", onNavigateBack = onNavigateBack, modifier = Modifier.padding(horizontal = 16.dp))
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Manage your account and app preferences",
                fontSize = 14.sp,
                color = textHeaderColor,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            SettingsSectionHeader("Account")
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, cardBorderColor)
            ) {
                Column {
                    SettingsItem(
                        icon = Icons.Default.Person,
                        title = "Profile",
                        subtitle = name,
                        endIcon = Icons.Default.ChevronRight,
                        onClick = onNavigateToProfile
                    )
                    HorizontalDivider(color = cardBorderColor)
                    SettingsItem(
                        icon = Icons.Default.Call,
                        title = "Phone Number",
                        subtitle = phone.ifBlank { "Set Phone Number" },
                        endIcon = if (phone.isBlank()) Icons.Default.ChevronRight else null,
                        onClick = {
                            if (phone.isBlank()) {
                                onNavigateToProfile()
                            }
                        }
                    )
                    HorizontalDivider(color = cardBorderColor)
                    val isVerified = userProfileState?.isVerified ?: true
                    SettingsItem(
                        icon = if (isVerified) Icons.Default.VerifiedUser else Icons.Default.ErrorOutline,
                        title = "Account Status",
                        subtitle = if (isVerified) "Verified User" else "Not Verified",
                        endIcon = null,
                        onClick = { }
                    )
                }
            }


            SettingsSectionHeader("Appearance")
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, cardBorderColor)
            ) {
                val themeState = com.cabsync.app.ui.theme.LocalTheme.current
                SettingsSwitchItem(
                    icon = if (themeState.isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                    title = "Dark Mode",
                    subtitle = if (themeState.isDark) "Currently using dark theme" else "Currently using light theme",
                    checked = themeState.isDark,
                    onCheckedChange = { themeState.toggle() }
                )
            }

            SettingsSectionHeader("Preferences")
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, cardBorderColor)
            ) {
                Column {
                    SettingsSwitchItem(
                        icon = Icons.Default.Notifications,
                        title = "Notifications",
                        checked = notificationsEnabled,
                        onCheckedChange = {
                            notificationsEnabled = it
                            saveProfileSetting { p -> p.copy(notificationsEnabled = it) }
                            android.widget.Toast.makeText(context, if (it) "Notifications Enabled" else "Notifications Disabled", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    )
                    HorizontalDivider(color = cardBorderColor)
                    SettingsItem(
                        icon = Icons.Default.Language,
                        title = "Language",
                        subtitle = userLanguageState,
                        endIcon = null,
                        onClick = { }
                    )
                }
            }

            SettingsSectionHeader("Privacy Controls")
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, cardBorderColor)
            ) {
                Column {
                    SettingsItem(
                        icon = Icons.Default.Call,
                        title = "Phone Number",
                        subtitle = "Choose who can see your phone number.",
                        endText = phoneVisibilityState,
                        endIcon = Icons.Default.ExpandMore,
                        onClick = { showVisibilityDialog = true }
                    )
                    HorizontalDivider(color = cardBorderColor)
                    SettingsSwitchItem(
                        icon = Icons.Default.History,
                        title = "Ride History",
                        subtitle = "Make your past rides visible to others.",
                        checked = rideHistory,
                        onCheckedChange = {
                            rideHistory = it
                            saveProfileSetting { p -> p.copy(rideHistoryVisible = it) }
                            android.widget.Toast.makeText(context, if (it) "Ride History Visible" else "Ride History Hidden", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            SettingsSectionHeader("About")
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, cardBorderColor)
            ) {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "About CabSync",
                    subtitle = "Version 1.0.0",
                    endIcon = Icons.Default.ChevronRight,
                    onClick = { showAboutDialog = true }
                )
            }

            SettingsSectionHeader("Danger Zone")
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, Color(0xFF93000A).copy(alpha = 0.3f))
            ) {
                Column {
                    SettingsItem(
                        icon = Icons.Default.Block,
                        title = "Deactivate Account",
                        subtitle = "Temporarily hide your profile from CabSync.",
                        endIcon = Icons.Default.ChevronRight,
                        onClick = { showDeactivateDialog = true }
                    )
                    HorizontalDivider(color = cardBorderColor)
                    SettingsItem(
                        icon = Icons.Default.DeleteForever,
                        title = "Delete Account",
                        subtitle = "Permanently delete your profile and account data.",
                        endIcon = Icons.Default.ChevronRight,
                        onClick = { showDeleteDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 8.dp),
                color = logoutBtnBg,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFFFB4AB).copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Logout,
                        contentDescription = "Logout",
                        tint = Color(0xFFFFB4AB),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Logout",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFB4AB)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
}

