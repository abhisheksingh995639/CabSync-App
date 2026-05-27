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


@Composable
fun CabSyncButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    containerColor: Color = CabSyncTokens.BrandYellow,
    textColor: Color = Color(0xFF121212)
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(CabSyncTokens.ButtonHeight),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        shape = CabSyncTokens.ShapeButton,
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
                .background(
                    if (containerColor == CabSyncTokens.BrandYellow) {
                        Brush.linearGradient(colors = listOf(CabSyncTokens.BrandYellow, CabSyncTokens.BrandAmber))
                    } else androidx.compose.ui.graphics.SolidColor(containerColor)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                if (icon != null && text == "Save Changes") {
                    Icon(icon, null, tint = textColor, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(text, color = textColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                if (icon != null && text != "Save Changes") {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(icon, null, tint = textColor, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun CabSyncOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
    borderColor: Color = CabSyncTokens.BorderDark,
    containerColor: Color = Color(0xFF1E1E1E),
    textColor: Color = CabSyncTokens.TextPrimary
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(CabSyncTokens.ButtonHeight),
        border = BorderStroke(1.dp, borderColor),
        shape = CabSyncTokens.ShapeButton,
        colors = ButtonDefaults.outlinedButtonColors(containerColor = containerColor)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(text, color = textColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun CabSyncBackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val isDark = com.cabsync.app.ui.theme.LocalTheme.current.isDark
    val bg = if (isDark) CabSyncTokens.SurfaceContainer else CabSyncTokens.LightSurfaceContainer
    val borderCol = if (isDark) CabSyncTokens.Outline.copy(alpha = 0.3f) else CabSyncTokens.LightBorderDark
    val iconTint = if (isDark) Color.White else CabSyncTokens.LightTextPrimary

    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(40.dp)
            .background(bg, RoundedCornerShape(20.dp))
            .border(1.dp, borderCol, RoundedCornerShape(20.dp))
    ) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = iconTint, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun CabSyncScreenHeader(title: String, onNavigateBack: () -> Unit, modifier: Modifier = Modifier) {
    val isDark = com.cabsync.app.ui.theme.LocalTheme.current.isDark
    val textColor = if (isDark) Color.White else CabSyncTokens.LightTextPrimary
    Row(modifier = modifier.fillMaxWidth().padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        CabSyncBackButton(onClick = onNavigateBack)
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = textColor)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelledInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: @Composable () -> Unit,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordVisibilityChange: () -> Unit = {},
    keyboardType: androidx.compose.ui.text.input.KeyboardType = androidx.compose.ui.text.input.KeyboardType.Text
) {
    val brandTextMuted = Color(0xFFA0A0A0)
    val brandText = Color(0xFFF5F5F5)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            label.uppercase(), 
            fontSize = 12.sp, 
            color = brandTextMuted, 
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color(0xFF555555)) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = leadingIcon,
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = onPasswordVisibilityChange) {
                        Icon(
                            if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            null,
                            tint = brandTextMuted
                        )
                    }
                }
            } else null,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFF2C2C2C),
                focusedBorderColor = Color(0xFFFFD700),
                unfocusedContainerColor = Color(0xFF1A1A1A),
                focusedContainerColor = Color(0xFF1A1A1A),
                cursorColor = Color(0xFFFFD700),
                focusedTextColor = brandText,
                unfocusedTextColor = brandText,
                unfocusedLeadingIconColor = brandTextMuted,
                focusedLeadingIconColor = brandTextMuted,
                unfocusedTrailingIconColor = brandTextMuted,
                focusedTrailingIconColor = brandTextMuted,
                unfocusedPlaceholderColor = Color(0xFF555555),
                focusedPlaceholderColor = Color(0xFF555555)
            ),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (isPassword && !passwordVisible)
                androidx.compose.ui.text.input.PasswordVisualTransformation()
            else androidx.compose.ui.text.input.VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true
        )
    }
}

enum class NavTab(val icon: @Composable () -> Unit) {
    HOME({ Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(25.dp)) }),
    FIND({ Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(25.dp)) }),
    POST({ Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(25.dp)) }),
    JOINED({ Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(25.dp)) }),
    CHAT({ Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(25.dp)) })
}

@Composable
fun CabSyncNavBar(
    selectedTab: NavTab,
    onHomeClick: () -> Unit,
    onFindClick: () -> Unit,
    onPostClick: () -> Unit,
    onJoinedClick: () -> Unit,
    onChatClick: () -> Unit
) {
    val isDark = com.cabsync.app.ui.theme.LocalTheme.current.isDark
    val brandYellow = Color(0xFFFFD700)
    val brandDark = Color(0xFF131313)
    val surfaceContainer = if (isDark) Color(0xFF201F1F) else CabSyncTokens.LightSurfaceContainer
    val onSurfaceVariant = if (isDark) Color(0xFFD0C6AB) else CabSyncTokens.LightOnSurfaceVariant
    val navBgColors = if (isDark) listOf(Color(0xFF282726), Color(0xFF1E1D1D)) else listOf(CabSyncTokens.LightSurface, CabSyncTokens.LightSurfaceContainer)

    val tabs = listOf(
        NavTab.HOME to onHomeClick,
        NavTab.FIND to onFindClick,
        NavTab.POST to onPostClick,
        NavTab.JOINED to onJoinedClick,
        NavTab.CHAT to onChatClick
    )

    val selectedIndex = tabs.indexOfFirst { it.first == selectedTab }
    val tabCount = tabs.size

    // Animate the horizontal center position of the cut-out
    val density = LocalDensity.current
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val tabWidthPx = with(density) { (screenWidth / tabCount).toPx() }
    
    val animatedCenterX by animateFloatAsState(
        targetValue = (selectedIndex + 0.5f) * tabWidthPx,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessVeryLow // Even more fluid motion
        ),
        label = "cutoutCenterX"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .layout { measurable, constraints ->
                val height = 80.dp.roundToPx()
                val drawingHeight = 120.dp.roundToPx()
                val placeable = measurable.measure(constraints.copy(minHeight = drawingHeight, maxHeight = drawingHeight))
                layout(placeable.width, height) {
                    placeable.place(0, height - drawingHeight)
                }
            }
            .graphicsLayer(clip = false),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Custom Path Bar Background
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) {
            // Increased radius and height by ~10% for the "detached" groove look
            val dipRadius = 46.dp.toPx() 
            val dipHeight = 40.dp.toPx()
            
            val path = Path().apply {
                moveTo(0f, 0f)
                
                val dipWidth = dipRadius * 2.4f // Slightly wider
                val startDip = animatedCenterX - dipWidth / 2
                val endDip = animatedCenterX + dipWidth / 2
                
                lineTo(startDip, 0f)
                
                cubicTo(
                    x1 = animatedCenterX - dipRadius * 0.75f, y1 = 0f,
                    x2 = animatedCenterX - dipRadius * 0.85f, y2 = dipHeight,
                    x3 = animatedCenterX, y3 = dipHeight
                )
                
                cubicTo(
                    x1 = animatedCenterX + dipRadius * 0.85f, y1 = dipHeight,
                    x2 = animatedCenterX + dipRadius * 0.75f, y2 = 0f,
                    x3 = endDip, y3 = 0f
                )
                
                lineTo(size.width, 0f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            
            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = navBgColors
                )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            tabs.forEach { (tab, onClick) ->
                val isSelected = tab == selectedTab

                val iconTint by animateColorAsState(
                    targetValue = if (isSelected) brandDark else onSurfaceVariant,
                    animationSpec = tween(durationMillis = 300),
                    label = "iconTint_${tab.name}"
                )

                val iconScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.1f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "iconScale_${tab.name}"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            onClick = onClick,
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    val bottomPadding by animateDpAsState(
                        targetValue = if (isSelected) 42.dp else 22.dp,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "bottomPadding_${tab.name}"
                    )

                    Box(
                        modifier = Modifier
                            .padding(bottom = bottomPadding)
                            .size(if (isSelected) 60.dp else 48.dp)
                            .background(
                                color = if (isSelected) brandYellow else Color.Transparent,
                                shape = RoundedCornerShape(30.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        CompositionLocalProvider(
                            LocalContentColor provides iconTint
                        ) {
                            Box(modifier = Modifier.graphicsLayer(scaleX = iconScale, scaleY = iconScale)) {
                                tab.icon()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    val isDark = com.cabsync.app.ui.theme.LocalTheme.current.isDark
    val containerColor = if (isDark) Color(0xFF2E2D2B) else CabSyncTokens.LightSurface
    val borderColor = if (isDark) Color(0xFF3E3C38) else CabSyncTokens.LightBorderDark
    val textColor = if (isDark) Color.White else CabSyncTokens.LightTextPrimary
    val placeholderColor = if (isDark) Color(0xFF888880) else CabSyncTokens.LightTextMuted
    val iconColor = if (isDark) Color(0xFFFFD700).copy(alpha = 0.7f) else Color(0xFFB8860B)

    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, fontSize = 14.sp, color = placeholderColor) },
        modifier = modifier
            .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
        leadingIcon = { Icon(icon, null, tint = iconColor, modifier = Modifier.size(16.dp)) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = containerColor,
            unfocusedContainerColor = containerColor,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
            cursorColor = Color(0xFFFFD700)
        ),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )
}

@Composable
fun DateTimePickerField(
    value: String,
    placeholder: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = com.cabsync.app.ui.theme.LocalTheme.current.isDark
    val containerColor = if (isDark) Color(0xFF2E2D2B) else CabSyncTokens.LightSurface
    val borderColor = if (isDark) Color(0xFF3E3C38) else CabSyncTokens.LightBorderDark
    val textColor = if (value.isEmpty()) {
        if (isDark) Color(0xFF888880) else CabSyncTokens.LightTextMuted
    } else {
        if (isDark) Color.White else CabSyncTokens.LightTextPrimary
    }
    val iconColor = if (isDark) Color(0xFFFFD700).copy(alpha = 0.7f) else Color(0xFFB8860B)

    Box(
        modifier = modifier
            .background(containerColor, RoundedCornerShape(12.dp))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (value.isEmpty()) placeholder else value,
                fontSize = 14.sp,
                color = textColor
            )
        }
    }
}

@Composable
fun DashboardSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onViewAll: (() -> Unit)? = null,
    emptyText: String,
    emptySubtext: String,
    emptyIcon: androidx.compose.ui.graphics.vector.ImageVector,
    actionText: String,
    modifier: Modifier = Modifier,
    onActionClick: () -> Unit = {},
    content: (@Composable () -> Unit)? = null
) {
    val isDark = com.cabsync.app.ui.theme.LocalTheme.current.isDark
    val textPrimary = if (isDark) Color.White else CabSyncTokens.LightTextPrimary
    val textMuted = if (isDark) Color(0xFFD0C6AB) else CabSyncTokens.LightTextMuted
    val cardBg = if (isDark) Color(0xFF131313) else CabSyncTokens.LightSurfaceContainer
    val cardBorder = if (isDark) Color(0xFF4D4732).copy(alpha = 0.3f) else CabSyncTokens.LightBorderDark.copy(alpha = 0.5f)
    val emptyIconTint = if (isDark) Color(0xFFD0C6AB) else CabSyncTokens.LightOnSurfaceVariant
    val btnBg = if (isDark) Color(0xFFF5F5F0) else Color.Black
    val btnText = if (isDark) Color.Black else Color.White

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = Color(0xFFFFD700), modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, color = textPrimary, fontSize = 16.sp)
            }
            if (onViewAll != null) {
                TextButton(onClick = onViewAll, contentPadding = PaddingValues(0.dp)) {
                    Text("View all", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Icon(Icons.Default.ChevronRight, null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                }
            }
        }
        
        Spacer(Modifier.height(8.dp))
        
        if (content != null) {
            content()
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, cardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(48.dp)
                            .background(
                                Brush.radialGradient(colors = listOf(CabSyncTokens.BrandYellow.copy(alpha = 0.2f), Color.Transparent)),
                                RoundedCornerShape(12.dp)
                            )
                            .border(1.dp, CabSyncTokens.BrandYellow.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(emptyIcon, null, tint = emptyIconTint, modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(emptyText, fontWeight = FontWeight.Bold, color = textPrimary, fontSize = 16.sp)
                    Text(
                        emptySubtext,
                        color = textMuted,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, bottom = 24.dp).padding(horizontal = 16.dp)
                    )
                    Button(
                        onClick = onActionClick,
                        colors = ButtonDefaults.buttonColors(containerColor = btnBg),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Text(actionText, color = btnText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun RideCard(ride: Ride, onClick: () -> Unit) {
    val brandYellow = Color(0xFFFFD700)
    val isDark = com.cabsync.app.ui.theme.LocalTheme.current.isDark
    val context = LocalContext.current
    val imageLoader = remember(context) {
        ImageLoader.Builder(context)
            .components { add(SvgDecoder.Factory()) }
            .build()
    }
    val onSurfaceVariant = if (isDark) Color(0xFFD0C6AB) else CabSyncTokens.LightOnSurfaceVariant
    val gradientColors = if (isDark) CabSyncTokens.GradientCard else CabSyncTokens.LightGradientCard
    val borderColor = if (isDark) CabSyncTokens.BorderGlow else CabSyncTokens.LightBorderGlow
    val textPrimary = if (isDark) Color.White else CabSyncTokens.LightTextPrimary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(colors = gradientColors),
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header: Host info and Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = ride.hostAvatar,
                        imageLoader = imageLoader,
                        contentDescription = null,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(22.dp))
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(ride.hostName, fontWeight = FontWeight.Bold, color = textPrimary, fontSize = 16.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, null, tint = brandYellow, modifier = Modifier.size(14.dp))
                            Text(" ${ride.rating}  •  ${if (ride.tags.contains("AC")) "AC" else "Non-AC"}", fontSize = 12.sp, color = onSurfaceVariant)
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("₹${ride.price}", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = brandYellow)
                    Text("per person", fontSize = 10.sp, color = onSurfaceVariant.copy(alpha = 0.7f))
                }
            }

            Spacer(Modifier.height(24.dp))

            // Middle: Route
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .border(2.dp, onSurfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(5.dp))
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("PICKUP", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = onSurfaceVariant.copy(alpha = 0.5f), letterSpacing = 0.5.sp)
                    }
                    Text(ride.pickup, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                
                Box(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .width(30.dp)
                        .height(1.dp)
                        .background(onSurfaceVariant.copy(alpha = 0.2f))
                )

                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("DESTINATION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = onSurfaceVariant.copy(alpha = 0.5f), letterSpacing = 0.5.sp)
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(brandYellow, RoundedCornerShape(5.dp))
                        )
                    }
                    Text(ride.destination, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            Spacer(Modifier.height(24.dp))

            // Footer: Two rows of details
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Row 1: Date and Time
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.CalendarToday, null, tint = onSurfaceVariant, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("DATE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = onSurfaceVariant.copy(alpha = 0.5f))
                            Text(ride.date, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = textPrimary)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text("TIME", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = onSurfaceVariant.copy(alpha = 0.5f))
                            Text(ride.time, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = textPrimary)
                        }
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.Schedule, null, tint = onSurfaceVariant, modifier = Modifier.size(14.dp))
                    }
                }

                // Row 2: Vehicle and Seats
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.DirectionsCar, null, tint = onSurfaceVariant, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("VEHICLE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = onSurfaceVariant.copy(alpha = 0.5f))
                            Text(ride.carModel, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = textPrimary)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text("SEATS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = onSurfaceVariant.copy(alpha = 0.5f))
                            Text("${ride.seatsLeft} left", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = brandYellow)
                        }
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.Group, null, tint = brandYellow, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
        }
    }
}
@Composable
fun CabSyncDrawerContent(
    onClose: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToSafety: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {},
    onNavigateToTerms: () -> Unit = {},
    onNavigateToHelp: () -> Unit = {}
) {
    val context = LocalContext.current
    val currentUser = remember { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser }
    val imageLoader = remember(context) {
        ImageLoader.Builder(context)
            .components { add(SvgDecoder.Factory()) }
            .build()
    }
    val sharedPrefs = remember(context) { context.getSharedPreferences("cabsync_user_prefs", android.content.Context.MODE_PRIVATE) }
    var name by remember(currentUser, sharedPrefs) { mutableStateOf(sharedPrefs.getString("user_name", currentUser?.displayName?.ifEmpty { null }) ?: currentUser?.email?.substringBefore('@') ?: "Abhishek Singh") }
    var avatarUrl by remember {
        mutableStateOf(
            sharedPrefs.getString("user_avatar", null)
                ?: currentUser?.photoUrl?.toString()
                ?: "https://api.dicebear.com/7.x/avataaars/svg?seed=${sharedPrefs.getString("user_name", currentUser?.displayName?.ifEmpty { null }) ?: currentUser?.email?.substringBefore('@') ?: "Abhishek Singh"}"
        )
    }
    var isVerified by remember { mutableStateOf(true) }
    var rating by remember { mutableStateOf(4.9) }

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
            name = profile.name
            isVerified = profile.isVerified
            rating = profile.rating
            if (profile.photoUrl.isNotBlank()) {
                avatarUrl = profile.photoUrl
            }
        }
    }

    val isDark = com.cabsync.app.ui.theme.LocalTheme.current.isDark
    val drawerBg = if (isDark) Color(0xFF1A1A1A) else CabSyncTokens.LightSurface
    val textPrimary = if (isDark) Color.White else CabSyncTokens.LightTextPrimary
    val dividerColor = if (isDark) Color(0xFF353534) else CabSyncTokens.LightBorderDark

    ModalDrawerSheet(
        drawerContainerColor = drawerBg,
        drawerShape = RoundedCornerShape(topEnd = 32.dp, bottomEnd = 32.dp),
        modifier = Modifier.width(320.dp).fillMaxHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(CabSyncTokens.BrandYellow.copy(alpha = 0.15f), Color.Transparent)
                        )
                    )
                    .padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 24.dp)
            ) {
                Column {
                    Box(modifier = Modifier.clickable { onNavigateToProfile() }) {
                        AsyncImage(
                            model = avatarUrl,
                            imageLoader = imageLoader,
                            contentDescription = "User Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .border(2.dp, CabSyncTokens.BrandYellow, RoundedCornerShape(20.dp))
                        )
                        if (isVerified) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(x = 6.dp, y = 6.dp)
                                    .size(24.dp)
                                    .background(CabSyncTokens.BrandYellow, RoundedCornerShape(12.dp))
                                    .border(2.dp, drawerBg, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Verified,
                                    contentDescription = "Verified",
                                    tint = Color.Black,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = CabSyncTokens.BrandYellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$rating",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) CabSyncTokens.BrandYellow else CabSyncTokens.LightTextGold
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                DrawerMenuItem(
                    icon = Icons.Default.Person,
                    text = "Profile",
                    isSelected = false,
                    onClick = {
                        onClose()
                        onNavigateToProfile()
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                DrawerMenuItem(
                    icon = Icons.Default.Settings,
                    text = "Settings",
                    isSelected = false,
                    onClick = {
                        onClose()
                        onNavigateToSettings()
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = dividerColor
                )

                DrawerMenuItem(
                    icon = Icons.Default.Security,
                    text = "Safety Guidelines",
                    isSelected = false,
                    onClick = {
                        onClose()
                        onNavigateToSafety()
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                DrawerMenuItem(
                    icon = Icons.Default.PrivacyTip,
                    text = "Privacy Policy",
                    isSelected = false,
                    onClick = {
                        onClose()
                        onNavigateToPrivacy()
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                DrawerMenuItem(
                    icon = Icons.Default.Description,
                    text = "Terms of Service",
                    isSelected = false,
                    onClick = {
                        onClose()
                        onNavigateToTerms()
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                DrawerMenuItem(
                    icon = Icons.Default.Help,
                    text = "Help Center",
                    isSelected = false,
                    onClick = {
                        onClose()
                        onNavigateToHelp()
                    }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                HorizontalDivider(
                    modifier = Modifier.padding(bottom = 20.dp),
                    color = dividerColor
                )

                val logoutBg = if (isDark) Color(0xFF93000A).copy(alpha = 0.15f) else Color(0xFFBA1A1A).copy(alpha = 0.1f)
                val logoutBorderColor = if (isDark) Color(0xFF93000A).copy(alpha = 0.3f) else Color(0xFFBA1A1A).copy(alpha = 0.25f)
                val logoutContentColor = if (isDark) Color(0xFFFFB4AB) else Color(0xFFBA1A1A)

                Surface(
                    onClick = {
                        onClose()
                        onLogout()
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    color = logoutBg,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, logoutBorderColor)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = logoutContentColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Logout",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = logoutContentColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                    Text(
                        text = "Version 1.0.0",
                        fontSize = 12.sp,
                        color = if (isDark) Color.Gray else CabSyncTokens.LightTextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "© 2026 CabSync Technologies",
                        fontSize = 12.sp,
                        color = if (isDark) Color.Gray else CabSyncTokens.LightTextMuted
                    )
                }
            }
        }
    }
}

@Composable
fun DrawerMenuItem(
    icon: ImageVector,
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isDark = com.cabsync.app.ui.theme.LocalTheme.current.isDark
    val containerColor = if (isSelected) CabSyncTokens.BrandYellow else Color.Transparent
    val contentColor = if (isSelected) Color(0xFF131313) else (if (isDark) Color.White else CabSyncTokens.LightTextPrimary)

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        color = containerColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = text,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    val isDark = com.cabsync.app.ui.theme.LocalTheme.current.isDark
    val headerColor = if (isDark) CabSyncTokens.BrandYellow else Color(0xFFB8860B)
    Text(
        text = title.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = headerColor,
        modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    endText: String? = null,
    endIcon: ImageVector? = null,
    onClick: () -> Unit = {}
) {
    val isDark = com.cabsync.app.ui.theme.LocalTheme.current.isDark
    val iconContainerBg = if (isDark) Color(0xFF353534) else CabSyncTokens.LightSurfaceContainer
    val titleColor = if (isDark) Color.White else CabSyncTokens.LightTextPrimary
    val subtitleColor = if (isDark) CabSyncTokens.OnSurfaceVariant else CabSyncTokens.LightOnSurfaceVariant
    val endTextContainerBg = if (isDark) Color(0xFF353534) else CabSyncTokens.LightSurfaceContainer
    val endTextColor = if (isDark) Color.White else CabSyncTokens.LightTextPrimary
    val endIconTint = if (isDark) CabSyncTokens.OnSurfaceVariant else CabSyncTokens.LightOnSurfaceVariant

    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconContainerBg, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = CabSyncTokens.BrandYellow, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = titleColor)
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(subtitle, fontSize = 12.sp, color = subtitleColor)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            if (endText != null) {
                Surface(
                    color = endTextContainerBg,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(endText, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = endTextColor)
                        if (endIcon != null) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(endIcon, contentDescription = null, tint = endIconTint, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            } else if (endIcon != null) {
                Icon(endIcon, contentDescription = null, tint = endIconTint, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val isDark = com.cabsync.app.ui.theme.LocalTheme.current.isDark
    val iconContainerBg = if (isDark) Color(0xFF353534) else CabSyncTokens.LightSurfaceContainer
    val titleColor = if (isDark) Color.White else CabSyncTokens.LightTextPrimary
    val subtitleColor = if (isDark) CabSyncTokens.OnSurfaceVariant else CabSyncTokens.LightOnSurfaceVariant

    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(iconContainerBg, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = CabSyncTokens.BrandYellow, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = titleColor)
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle, fontSize = 12.sp, color = subtitleColor)
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = if (isDark) Color.Black else Color(0xFFF5F5F0),
                checkedTrackColor = CabSyncTokens.BrandYellow,
                uncheckedThumbColor = if (isDark) Color(0xFFD0C6AB) else CabSyncTokens.LightOnSurfaceVariant,
                uncheckedTrackColor = if (isDark) Color(0xFF353534) else CabSyncTokens.LightSurfaceContainer
            )
        )
    }
}

