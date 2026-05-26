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


object CabSyncTokens {
    val BrandYellow = Color(0xFFFFD700)
    val BrandYellowDark = Color(0xFFE6C200)
    val BrandAmber = Color(0xFFFF8C00)
    val BrandGlow = Color(0xFFFFD700).copy(alpha = 0.12f)
    val AccentGreen = Color(0xFF4ADE80)
    val AccentBlue = Color(0xFF60A5FA)

    // ── Dark Theme ──
    val Background = Color(0xFF121212)
    val SurfaceDark = Color(0xFF1E1E1E)
    val SurfaceContainer = Color(0xFF201F1F)
    val TextPrimary = Color(0xFFF5F5F5)
    val TextMuted = Color(0xFFA0A0A0)
    val OnSurfaceVariant = Color(0xFFD0C6AB)
    val Outline = Color(0xFF4D4732)
    val BorderDark = Color(0xFF2C2C2C)
    val BorderGlow = Color(0xFFFFD700).copy(alpha = 0.35f)

    // ── Light Theme ──
    val LightBackground = Color(0xFFF5F5F0)
    val LightSurface = Color(0xFFEBE8E0)
    val LightSurfaceContainer = Color(0xFFE2DDD4)
    val LightTextPrimary = Color(0xFF1A1A1A)
    val LightTextMuted = Color(0xFF6B6558)
    val LightOnSurfaceVariant = Color(0xFF5C5545)
    val LightBorderDark = Color(0xFFCCC5B2)
    val LightBorderGlow = Color(0xFFB8860B).copy(alpha = 0.4f)
    val LightTextGold = Color(0xFFB8860B)

    val GradientYellow = listOf(Color(0xFFFFD700), Color(0xFFFF8C00))
    val GradientCard = listOf(Color(0xFF2A2519), Color(0xFF1C1B1B))
    val GradientSurface = listOf(Color(0xFF252423), Color(0xFF1A1A1A))

    // ── Light Gradients ──
    val LightGradientCard = listOf(Color(0xFFEFECE2), Color(0xFFE5E2D8))
    val LightGradientSurface = listOf(Color(0xFFF0EDE3), Color(0xFFEBE8E0))

    val ButtonHeight = 56.dp
    val InputHeight = 56.dp
    val ScreenPadding = 24.dp

    val ShapeButton = RoundedCornerShape(12.dp)
    val ShapeInput = RoundedCornerShape(12.dp)
    val ShapeCard = RoundedCornerShape(16.dp)
    val ShapeBottomSheet = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
}

