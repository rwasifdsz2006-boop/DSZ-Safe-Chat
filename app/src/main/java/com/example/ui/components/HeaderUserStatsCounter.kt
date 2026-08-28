package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.stats.TikTokFormatUtils
import com.example.core.stats.UserStats
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.GoldGradientColors
import com.example.ui.theme.ImmersiveGold
import kotlinx.coroutines.delay

enum class StatStep {
    TOTAL_JOINED,
    LIVE_ONLINE,
    DAILY_ACTIVE
}

/**
 * 3-Step Live Header User Stats Counter:
 * - Step 1: Total Installed / Joined (TikTok style formatting like 0, 1.2K, 50K, 1.5M, never decreases)
 * - Step 2: Live Online Now with pulsing green dot (🟢)
 * - Step 3: Daily Active Users (DAU) resetting strictly at 12:00 AM midnight
 */
@Composable
fun HeaderUserStatsCounter(
    stats: UserStats,
    themeMode: AppThemeMode,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableStateOf(StatStep.LIVE_ONLINE) }
    var showStatsDialog by remember { mutableStateOf(false) }

    // Auto-cycle through the 3 steps smoothly every 3.5 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(3500L)
            currentStep = when (currentStep) {
                StatStep.LIVE_ONLINE -> StatStep.TOTAL_JOINED
                StatStep.TOTAL_JOINED -> StatStep.DAILY_ACTIVE
                StatStep.DAILY_ACTIVE -> StatStep.LIVE_ONLINE
            }
        }
    }

    // Pulsing animation for the Live Online indicator (🟢)
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val isDark = themeMode != AppThemeMode.LIGHT

    val cardBackground = if (isDark) {
        Color(0xFF1E1912) // Warm obsidian gold
    } else {
        Color(0xFFFFF9E6) // Warm golden cream
    }

    val cardBorderColor = if (isDark) {
        ImmersiveGold.copy(alpha = 0.65f)
    } else {
        ImmersiveGold.copy(alpha = 0.85f)
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { showStatsDialog = true }
            .testTag("header_user_stats_counter"),
        shape = RoundedCornerShape(12.dp),
        color = cardBackground,
        border = BorderStroke(1.dp, cardBorderColor),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(350)) + slideInVertically(animationSpec = tween(350)) { height -> height / 2 })
                        .togetherWith(fadeOut(animationSpec = tween(350)) + slideOutVertically(animationSpec = tween(350)) { height -> -height / 2 })
                },
                label = "header_stat_step_animation"
            ) { step ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    when (step) {
                        StatStep.LIVE_ONLINE -> {
                            // Step 2: Live Online with pulsing green dot
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(12.dp)
                            ) {
                                if (stats.isOnline) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .scale(pulseScale)
                                            .clip(CircleShape)
                                            .background(Color(0xFF22C55E).copy(alpha = pulseAlpha * 0.45f))
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF22C55E))
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF94A3B8))
                                    )
                                }
                            }
                            Text(
                                text = "${stats.liveOnlineCount} Live",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (stats.isOnline) Color(0xFF22C55E) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        StatStep.TOTAL_JOINED -> {
                            // Step 1: TikTok Style Total Joined
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = ImmersiveGold,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "${TikTokFormatUtils.formatTikTokCount(stats.totalJoined)} Joined",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) ImmersiveGold else Color(0xFF8A6200)
                            )
                        }

                        StatStep.DAILY_ACTIVE -> {
                            // Step 3: DAU (Resets at midnight)
                            Icon(
                                imageVector = Icons.Default.Today,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "${TikTokFormatUtils.formatTikTokCount(stats.dailyActiveUsers)} Today",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFFFDE68A) else Color(0xFFB45309)
                            )
                        }
                    }
                }
            }
        }
    }

    // Detailed Stats Modal Dialog showing all 3 steps side by side
    if (showStatsDialog) {
        UserStatsDetailDialog(
            stats = stats,
            themeMode = themeMode,
            onDismiss = { showStatsDialog = false }
        )
    }
}

/**
 * Detailed 3-Step Stats Dialog
 */
@Composable
fun UserStatsDetailDialog(
    stats: UserStats,
    themeMode: AppThemeMode,
    onDismiss: () -> Unit
) {
    val isDark = themeMode != AppThemeMode.LIGHT
    val goldBrush = Brush.linearGradient(GoldGradientColors)

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .testTag("user_stats_detail_dialog"),
        shape = RoundedCornerShape(20.dp),
        containerColor = if (isDark) Color(0xFF181512) else Color(0xFFFFFDF5),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(goldBrush),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SignalCellularAlt,
                        contentDescription = null,
                        tint = Color(0xFF121212),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Column {
                    Text(
                        text = "Live User Statistics",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) ImmersiveGold else Color(0xFF8A6200)
                    )
                    Text(
                        text = "DSZ Save Chat • Real-time Metrics",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Card 1: TikTok Style Total Joined
                StatMetricCard(
                    icon = Icons.Default.Group,
                    iconColor = ImmersiveGold,
                    title = "Total Joined / Installs",
                    formattedValue = TikTokFormatUtils.formatTikTokCount(stats.totalJoined),
                    exactValue = "${stats.totalJoined} registered installations",
                    subtitle = "TikTok-style permanent lifetime counter. Never resets.",
                    isDark = isDark
                )

                // Card 2: Live Online Now
                StatMetricCard(
                    icon = Icons.Default.SignalCellularAlt,
                    iconColor = Color(0xFF22C55E),
                    title = "Live Online Now",
                    formattedValue = "${stats.liveOnlineCount} Active",
                    exactValue = if (stats.isOnline) "🟢 Network Connected" else "⚪ Offline Mode",
                    subtitle = "Real-time active users connected right now.",
                    isDark = isDark
                )

                // Card 3: Daily Active Users (DAU)
                StatMetricCard(
                    icon = Icons.Default.Today,
                    iconColor = Color(0xFFFBBF24),
                    title = "Daily Active Users (DAU)",
                    formattedValue = TikTokFormatUtils.formatTikTokCount(stats.dailyActiveUsers),
                    exactValue = "${stats.dailyActiveUsers} active today",
                    subtitle = "Resets strictly every 24 hours at 12:00 AM midnight.",
                    isDark = isDark
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = ImmersiveGold)
            ) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun StatMetricCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    formattedValue: String,
    exactValue: String,
    subtitle: String,
    isDark: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF231E18) else Color(0xFFFFF8E7)
        ),
        border = BorderStroke(1.dp, ImmersiveGold.copy(alpha = if (isDark) 0.35f else 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formattedValue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isDark) ImmersiveGold else Color(0xFF8A6200)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = exactValue,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    lineHeight = 12.sp
                )
            }
        }
    }
}
