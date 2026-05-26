package com.cabsync.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cabsync.app.CabSyncTokens

@Composable
fun BannedScreen(
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val isDark = com.cabsync.app.ui.theme.LocalTheme.current.isDark
    val background = if (isDark) Color(0xFF131313) else CabSyncTokens.LightBackground
    val cardBg = if (isDark) Color(0xFF201F1F) else CabSyncTokens.LightSurface
    val textPrimary = if (isDark) Color.White else CabSyncTokens.LightTextPrimary
    val textMuted = if (isDark) Color.Gray else CabSyncTokens.LightTextMuted

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(cardBg)
                .border(1.dp, Color(0xFF93000A).copy(alpha = 0.3f), RoundedCornerShape(32.dp))
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF93000A).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Gavel,
                    contentDescription = "Access Restricted",
                    tint = Color(0xFFFFB4AB),
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Access Restricted",
                color = textPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Your account has been permanently suspended for violating our community guidelines and safety protocols.",
                color = textMuted,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isDark) Color(0xFF1C1B1B) else Color(0xFFF5F2EB))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "REASON FOR BAN",
                    color = textMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Repeated policy violations (5+ warnings)",
                    color = Color(0xFFFFB4AB),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_EMAIL, arrayOf("abhisheksingh995639@gmail.com"))
                        putExtra(Intent.EXTRA_SUBJECT, "CabSync Account Appeal")
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(context, "No email app found", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDark) Color(0xFF353534) else CabSyncTokens.LightSurfaceContainer,
                    contentColor = textPrimary
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Default.Mail, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Contact Support", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = {
                    com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                    context.getSharedPreferences("cabsync_prefs", android.content.Context.MODE_PRIVATE).edit().clear().apply()
                    context.getSharedPreferences("cabsync_user_prefs", android.content.Context.MODE_PRIVATE).edit().clear().apply()
                    onLogout()
                },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, tint = Color(0xFFFFB4AB), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout", color = Color(0xFFFFB4AB), fontWeight = FontWeight.Bold)
            }
        }
    }
}
