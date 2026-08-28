package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.core.localization.AppLanguage
import com.example.core.localization.AppStrings
import com.example.ui.theme.GoldGradientColors
import com.example.ui.theme.ImmersiveGold
import com.example.ui.theme.ImmersiveGoldLight
import kotlinx.coroutines.delay

fun isNotificationAccessGranted(context: Context): Boolean {
    return try {
        val pkgName = context.packageName
        val flat = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        flat != null && flat.contains(pkgName)
    } catch (_: Exception) {
        false
    }
}

@Composable
fun SplashScreen(
    language: AppLanguage,
    onNavigateToNext: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val strings = remember(language) { AppStrings.get(language) }

    // Live permission states
    var isNotificationGranted by remember { mutableStateOf(isNotificationAccessGranted(context)) }
    var isStorageGranted by remember { mutableStateOf(true) }
    var showPermissionLock by remember { mutableStateOf(false) }

    // Lifecycle observer to re-check permissions upon return from system settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isNotificationGranted = isNotificationAccessGranted(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Animation controllers
    val scale = remember { Animatable(0.85f) }
    val alpha = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "immersive_pulse")
    val loadingProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "loading_progress"
    )

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
        )
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500, easing = LinearEasing)
        )

        // Exact 1.2-second splash/logo hold then immediate fluid transition
        delay(1200L)
        onNavigateToNext()
    }

    val goldBrush = Brush.linearGradient(
        colors = GoldGradientColors,
        start = Offset(0f, 0f),
        end = Offset(300f, 300f)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable { onNavigateToNext() }
            .testTag("splash_screen_container")
    ) {
        // Ambient background glow orbs from Immersive UI spec
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top-right gold glow aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFD4AF37).copy(alpha = 0.08f),
                        Color(0xFFD4AF37).copy(alpha = 0.02f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.95f, size.height * 0.05f),
                    radius = size.width * 0.65f
                ),
                center = Offset(size.width * 0.95f, size.height * 0.05f),
                radius = size.width * 0.65f
            )

            // Bottom-left gold glow aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFD4AF37).copy(alpha = 0.09f),
                        Color(0xFFD4AF37).copy(alpha = 0.03f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.05f, size.height * 0.95f),
                    radius = size.width * 0.75f
                ),
                center = Offset(size.width * 0.05f, size.height * 0.95f),
                radius = size.width * 0.75f
            )
        }

        // Main Splash Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Immersive Logo: 96x96dp (w-24 h-24), -4deg rotated, gold-gradient rounded-[28px], gold-glow
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .scale(scale.value)
                    .rotate(-4f)
                    .size(96.dp)
                    .shadow(
                        elevation = 24.dp,
                        shape = RoundedCornerShape(28.dp),
                        ambientColor = ImmersiveGold,
                        spotColor = ImmersiveGold
                    )
                    .clip(RoundedCornerShape(28.dp))
                    .background(goldBrush)
                    .testTag("dsz_logo_badge")
            ) {
                Text(
                    text = "DSZ",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = (-1.5).sp,
                    color = Color(0xFF121212)
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Headline: "DSZ Save Chat" (Light + Bold Gold)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "DSZ ",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-0.5).sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Save Chat",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                    color = ImmersiveGold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle Badge with Fine Lines: "— OFFLINE ASSISTANT —"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.alpha(0.6f)
            ) {
                Box(
                    modifier = Modifier
                        .width(18.dp)
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.onBackground)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (language == AppLanguage.URDU) "آف لائن اسسٹنٹ" else "OFFLINE ASSISTANT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.5.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .width(18.dp)
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.onBackground)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Brand Slogan: “Building with Purpose. Creating for People.”
            Text(
                text = "“${strings.brandSlogan}”",
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic,
                fontFamily = FontFamily.Serif,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .testTag("brand_slogan_text")
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Minimalist 140dp x 3dp Loading Bar
            Box(
                modifier = Modifier
                    .width(140.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))
            ) {
                val offsetFraction = (loadingProgress * 1.6f) - 0.4f
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.45f)
                        .height(3.dp)
                        .offset(x = (140 * offsetFraction).dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(ImmersiveGold)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Language Indicator: ENGLISH | اردو
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "ENGLISH",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = if (language == AppLanguage.ENGLISH) ImmersiveGold else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "|",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "اردو",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (language == AppLanguage.URDU) ImmersiveGold else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom Section: DEVELOPED BY & M WASIF DSZ with 3-dot indicator
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = "DEVELOPED BY",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "M WASIF ",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "DSZ",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = ImmersiveGold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3 Luxury Status Dots [gold, slate, slate]
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(ImmersiveGold)
                    )
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                    )
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                    )
                }
            }
        }

        // 🛡️ First-Time Launch Lock & Permission Onboarding Overlay
        AnimatedVisibility(
            visible = showPermissionLock,
            enter = fadeIn(animationSpec = tween(500)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.88f))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.5.dp, ImmersiveGold.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(24.dp, RoundedCornerShape(24.dp), spotColor = ImmersiveGold)
                        .testTag("startup_permission_lock_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(ImmersiveGold.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = ImmersiveGold,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = if (language == AppLanguage.URDU) "اینٹی ڈیلیٹ پرمیشنز کی توثیق" else "Anti-Delete Permissions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (language == AppLanguage.URDU)
                                "واٹس ایپ پیغامات اور میڈیا کو فوری بغیر تاخیر محفوظ کرنے کے لیے نوٹیفکیشن رسائی لازمی فعال کریں۔"
                            else
                                "Enable notification access to instantly intercept and preserve WhatsApp & WhatsApp Business messages and media before sender deletion.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Permission Step 1: Notification Access
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isNotificationGranted) Color(0xFF10B981).copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isNotificationGranted) Color(0xFF10B981) else ImmersiveGold.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        try {
                                            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                            context.startActivity(intent)
                                        } catch (_: Exception) {
                                            val intent = Intent(Settings.ACTION_SETTINGS)
                                            context.startActivity(intent)
                                        }
                                    }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isNotificationGranted) Icons.Default.CheckCircle else Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = if (isNotificationGranted) Color(0xFF10B981) else ImmersiveGold,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = strings.permissionNotificationTitle,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = strings.permissionNotificationDesc,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isNotificationGranted) strings.permissionGrantedBadge else strings.permissionMissingBadge,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isNotificationGranted) Color(0xFF10B981) else ImmersiveGold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Permission Step 2: Storage Access
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isStorageGranted) Color(0xFF10B981).copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isStorageGranted) Color(0xFF10B981) else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        try {
                                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                data = Uri.fromParts("package", context.packageName, null)
                                            }
                                            context.startActivity(intent)
                                        } catch (_: Exception) {}
                                    }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isStorageGranted) Icons.Default.CheckCircle else Icons.Default.Storage,
                                    contentDescription = null,
                                    tint = if (isStorageGranted) Color(0xFF10B981) else ImmersiveGold,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = strings.permissionStorageTitle,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = strings.permissionStorageDesc,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isStorageGranted) strings.permissionGrantedBadge else strings.permissionMissingBadge,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isStorageGranted) Color(0xFF10B981) else ImmersiveGold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Continue Button
                        Button(
                            onClick = {
                                onNavigateToNext()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_continue_splash_permissions"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ImmersiveGold,
                                contentColor = Color(0xFF121212)
                            )
                        ) {
                            Text(
                                text = strings.permissionContinueBtn,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
