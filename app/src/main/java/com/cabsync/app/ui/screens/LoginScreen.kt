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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
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
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateToReset: () -> Unit,
    onNavigateToBanned: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val context = LocalContext.current
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                isLoading = true
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnSuccessListener { authResult ->
                        val user = authResult.user
                        if (user != null) {
                            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            db.collection("users").document(user.uid).get()
                                .addOnSuccessListener { snapshot ->
                                    isLoading = false
                                    RideService.startListening()
                                    if (snapshot.exists() && snapshot.getBoolean("isBanned") == true) {
                                        onNavigateToBanned()
                                    } else {
                                        onLoginSuccess()
                                    }
                                }
                                .addOnFailureListener {
                                    isLoading = false
                                    RideService.startListening()
                                    onLoginSuccess()
                                }
                        } else {
                            isLoading = false
                            RideService.startListening()
                            onLoginSuccess()
                        }
                    }
                    .addOnFailureListener { e ->
                        isLoading = false
                        errorMessage = e.localizedMessage ?: "Google Sign-In failed"
                    }
            }
        } catch (e: ApiException) {
            errorMessage = "Google Sign-In failed (${e.statusCode}: ${com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes.getStatusCodeString(e.statusCode)})"
        }
    }

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
            
            // Title
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 48.dp)
            ) {
                Text(
                    "Welcome\nBack",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 44.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Moving people together, safely.",
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
            // College Email Prompt
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF202010)),
                border = BorderStroke(1.dp, brandYellow.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = "Verified",
                        tint = brandYellow,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Sign in with your college email (.edu, .ac.in) for automatic account verification!",
                        color = brandYellow,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Medium
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

            Spacer(modifier = Modifier.height(20.dp))

            // Password
            Column(modifier = Modifier.fillMaxWidth()) {
                LabelledInput(
                    label = "Password",
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "Enter your password",
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = brandTextMuted, modifier = Modifier.size(18.dp)) },
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onPasswordVisibilityChange = { passwordVisible = !passwordVisible }
                )
                
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    Text(
                        "Forgot Password?",
                        color = brandYellow,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable { onNavigateToReset() }
                            .padding(top = 8.dp, start = 8.dp, bottom = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (errorMessage.isNotEmpty()) {
                Text(errorMessage, color = Color.Red, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))
            }

            CabSyncButton(
                text = if (isLoading) "Logging in..." else "Login",
                onClick = {
                    if (email.isEmpty() || password.isEmpty()) {
                        errorMessage = "Please enter email and password"
                        return@CabSyncButton
                    }
                    if (isLoading) return@CabSyncButton
                    isLoading = true
                    errorMessage = ""

                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email.trim(), password)
                        .addOnSuccessListener { authResult ->
                            val user = authResult.user
                            if (user != null) {
                                if (!user.isEmailVerified) {
                                    FirebaseAuth.getInstance().signOut()
                                    isLoading = false
                                    errorMessage = "Please verify your email to login. Check your inbox and spam folder."
                                    return@addOnSuccessListener
                                }
                                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                db.collection("users").document(user.uid).get()
                                    .addOnSuccessListener { snapshot ->
                                        isLoading = false
                                        RideService.startListening()
                                        if (snapshot.exists() && snapshot.getBoolean("isBanned") == true) {
                                            onNavigateToBanned()
                                        } else {
                                            onLoginSuccess()
                                        }
                                    }
                                    .addOnFailureListener {
                                        isLoading = false
                                        RideService.startListening()
                                        onLoginSuccess()
                                    }
                            } else {
                                isLoading = false
                                RideService.startListening()
                                onLoginSuccess()
                            }
                        }
                        .addOnFailureListener { e ->
                            isLoading = false
                            errorMessage = e.localizedMessage ?: "Login failed"
                        }
                },
                icon = Icons.AutoMirrored.Filled.ArrowForward
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Or Separator
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFF2C2C2C))
                Text(
                    " OR ",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = brandTextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFF2C2C2C))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Google Button
            CabSyncOutlinedButton(
                "Continue with Google",
                onClick = {
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(context.getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build()
                    val googleSignInClient = GoogleSignIn.getClient(context, gso)
                    googleSignInClient.signOut().addOnCompleteListener {
                        googleSignInClient.revokeAccess().addOnCompleteListener {
                            googleSignInLauncher.launch(googleSignInClient.signInIntent)
                        }
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_google_logo),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color.Unspecified
                    )
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Footer
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp), horizontalArrangement = Arrangement.Center) {
                Text("Don't have an account?", color = brandTextMuted, fontSize = 14.sp)
                Text(
                    " Sign Up",
                    color = brandYellow,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onNavigateToSignUp() }
                )
            }
        }
    }
}

