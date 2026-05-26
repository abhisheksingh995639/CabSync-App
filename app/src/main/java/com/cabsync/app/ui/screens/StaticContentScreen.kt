package com.cabsync.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cabsync.app.CabSyncTokens
import com.cabsync.app.CabSyncScreenHeader
import com.cabsync.app.CabSyncButton

@Composable
fun StaticContentScreen(
    title: String,
    contentPairs: List<Pair<String, String>>,
    onNavigateBack: () -> Unit
) {
    val isDark = com.cabsync.app.ui.theme.LocalTheme.current.isDark
    val scaffoldBg = if (isDark) Color(0xFF131313) else CabSyncTokens.LightBackground
    val cardBg = if (isDark) Color(0xFF201F1F) else CabSyncTokens.LightSurface
    val textColor = if (isDark) Color.White else CabSyncTokens.LightTextPrimary
    val textMuted = if (isDark) Color.Gray else CabSyncTokens.LightTextMuted
    val borderColor = if (isDark) Color(0xFF333333) else CabSyncTokens.LightBorderDark

    val headerIcon = when (title) {
        "Safety Guidelines" -> Icons.Default.Security
        "Privacy Policy" -> Icons.Default.PrivacyTip
        "Terms of Service" -> Icons.Default.Description
        else -> Icons.Default.Help
    }

    Scaffold(
        containerColor = scaffoldBg
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            CabSyncScreenHeader(
                title = title, 
                onNavigateBack = onNavigateBack, 
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Hero Icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(CabSyncTokens.BrandYellow.copy(alpha = 0.15f))
                        .border(2.dp, CabSyncTokens.BrandYellow, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = headerIcon,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = CabSyncTokens.BrandYellow
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = textColor,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                contentPairs.forEachIndexed { index, (heading, body) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) Color(0xFF333333) else CabSyncTokens.LightSurfaceContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = textColor
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = heading,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = textColor,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Text(
                                    text = body,
                                    fontSize = 14.sp,
                                    color = textMuted,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                CabSyncButton(
                    text = "Got it",
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
