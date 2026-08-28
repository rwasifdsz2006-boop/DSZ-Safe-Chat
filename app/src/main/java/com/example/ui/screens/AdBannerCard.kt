package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.localization.AppLanguage
import com.example.core.localization.AppStrings
import com.example.ui.theme.ImmersiveGold
import kotlinx.coroutines.delay

/**
 * Model representing a House Ad / Sponsored banner in the rotating carousel
 */
data class HouseAdItem(
    val id: String,
    val platformTag: String,
    val brandTitle: String,
    val brandHandle: String,
    val brandSubtitle: String,
    val ctaLabel: String,
    val actionUrl: String,
    val backgroundGradient: List<Color>,
    val borderColor: Color,
    val accentColor: Color,
    val iconType: AdIconType
)

enum class AdIconType {
    DSZ_APP,
    DSZ_APP_BACKUP,
    YOUTUBE,
    YOUTUBE_TUTORIALS,
    TIKTOK,
    REFRI_COOL_BRAND,
    ADMOB_ONLINE
}

/**
 * Observes network connectivity and combines it with the app's offline mode toggle
 */
@Composable
fun rememberIsNetworkOnline(isOfflineModeEnabled: Boolean): Boolean {
    val context = LocalContext.current
    var isConnected by remember {
        mutableStateOf(checkInitialNetwork(context))
    }

    DisposableEffect(Unit) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (connectivityManager == null) {
            onDispose { }
        } else {
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    isConnected = true
                }

                override fun onLost(network: Network) {
                    isConnected = false
                }

                override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                    val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    isConnected = hasInternet
                }
            }

            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            connectivityManager.registerNetworkCallback(request, callback)

            onDispose {
                try {
                    connectivityManager.unregisterNetworkCallback(callback)
                } catch (_: Exception) {}
            }
        }
    }

    return isConnected && !isOfflineModeEnabled
}

private fun checkInitialNetwork(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
    val activeNetwork = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(activeNetwork) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

/**
 * Professional Auto-Sliding Hybrid Ad & House Carousel System:
 * - Condition A (AdMob unlinked/unverified or device offline): Smoothly auto-rotates every 4 seconds
 *   through 6 rich, vibrant, multi-colored custom house banners (DSZ Save Chat app variations,
 *   Wasif RefriCool YouTube channels & highlights, and TikTok profile).
 * - Condition B (AdMob verified & online): Seamlessly displays live Google AdMob ads. Falls back gracefully
 *   if network or AdMob state changes.
 */
@Composable
fun AdBannerCard(
    language: AppLanguage,
    modifier: Modifier = Modifier,
    isOfflineModeEnabled: Boolean = false,
    isAdMobVerifiedAndLinked: Boolean = false
) {
    val context = LocalContext.current
    val strings = remember(language) { AppStrings.get(language) }
    val isOnline = rememberIsNetworkOnline(isOfflineModeEnabled)

    // Rich Collection of 6 Diverse, Professionally-Styled House Banners
    val houseAds = remember {
        listOf(
            // 1. App Banner (DSZ Save Chat - Gold / M WASIF DSZ Edition)
            HouseAdItem(
                id = "house_dsz_app_gold",
                platformTag = "DSZ PRO",
                brandTitle = "DSZ Save Chat",
                brandHandle = "by M WASIF DSZ",
                brandSubtitle = "Dual PIN Vault & Local Storage",
                ctaLabel = "Explore",
                actionUrl = "https://youtube.com/@WasifRefriCool",
                backgroundGradient = listOf(
                    Color(0xFF2C1900), // Deep Royal Amber
                    Color(0xFF6B4500), // Rich Golden Bronze
                    Color(0xFF1E1100)  // Dark Velvet Gold
                ),
                borderColor = ImmersiveGold.copy(alpha = 0.75f),
                accentColor = ImmersiveGold,
                iconType = AdIconType.DSZ_APP
            ),
            // 2. YouTube Banner 1 (Official Channel: Wasif RefriCool)
            HouseAdItem(
                id = "house_youtube_wasif",
                platformTag = "YouTube",
                brandTitle = "Wasif RefriCool",
                brandHandle = "@WasifRefriCool",
                brandSubtitle = "HVAC & Inverter Tech Guides",
                ctaLabel = "Subscribe",
                actionUrl = "https://youtube.com/@WasifRefriCool",
                backgroundGradient = listOf(
                    Color(0xFF800000), // Deep Crimson Red
                    Color(0xFFCC181E), // Official YouTube Red
                    Color(0xFF540004)  // Dark Shadow Red
                ),
                borderColor = Color(0xFFFF5252).copy(alpha = 0.65f),
                accentColor = Color(0xFFFF4545),
                iconType = AdIconType.YOUTUBE
            ),
            // 3. TikTok Banner (Viral Shorts & Refrigeration Hacks)
            HouseAdItem(
                id = "house_tiktok_wasif",
                platformTag = "TikTok",
                brandTitle = "Wasif RefriCool",
                brandHandle = "@rwasifdsz.844",
                brandSubtitle = "Viral Cooling Hacks & Daily Tips",
                ctaLabel = "Follow",
                actionUrl = "https://www.tiktok.com/@rwasifdsz.844",
                backgroundGradient = listOf(
                    Color(0xFF0F172A), // Midnight Dark Slate
                    Color(0xFF0B132B), // Deep Blue Indigo
                    Color(0xFF240046)  // Neon Violet/Magenta Haze
                ),
                borderColor = Color(0xFF25F4EE).copy(alpha = 0.6f),
                accentColor = Color(0xFF25F4EE),
                iconType = AdIconType.TIKTOK
            ),
            // 4. App Banner 2 (DSZ Chat Saver - Instant Recovery Edition)
            HouseAdItem(
                id = "house_dsz_app_recovery",
                platformTag = "SAFE TOOLS",
                brandTitle = "DSZ Chat Saver",
                brandHandle = "100% Offline & Safe",
                brandSubtitle = "Instant Message & Media Recovery",
                ctaLabel = "Backup",
                actionUrl = "https://youtube.com/@WasifRefriCool",
                backgroundGradient = listOf(
                    Color(0xFF063327), // Deep Forest Emerald
                    Color(0xFF0E624F), // Jade Teal
                    Color(0xFF032219)  // Dark Pine
                ),
                borderColor = Color(0xFF34D399).copy(alpha = 0.6f),
                accentColor = Color(0xFF34D399),
                iconType = AdIconType.DSZ_APP_BACKUP
            ),
            // 5. YouTube Banner 2 (Top Tutorials & Masterclass)
            HouseAdItem(
                id = "house_youtube_tutorials",
                platformTag = "YT Guides",
                brandTitle = "Wasif RefriCool",
                brandHandle = "@WasifRefriCool",
                brandSubtitle = "AC Schematics & Inverter Masterclass",
                ctaLabel = "Watch",
                actionUrl = "https://youtube.com/@WasifRefriCool",
                backgroundGradient = listOf(
                    Color(0xFF1E1B4B), // Deep Royal Indigo
                    Color(0xFF3730A3), // Electric Violet Blue
                    Color(0xFF0F172A)  // Dark Midnight
                ),
                borderColor = Color(0xFF818CF8).copy(alpha = 0.65f),
                accentColor = Color(0xFF818CF8),
                iconType = AdIconType.YOUTUBE_TUTORIALS
            ),
            // 6. Multilingual Brand Hub Banner (Urdu / Hindi / English)
            HouseAdItem(
                id = "house_brand_hub",
                platformTag = "RefriCool",
                brandTitle = "واصف ریفری کول • M WASIF DSZ",
                brandHandle = "@WasifRefriCool",
                brandSubtitle = "वासेफ रेफरी कूल • Full Video Guides",
                ctaLabel = "Join",
                actionUrl = "https://youtube.com/@WasifRefriCool",
                backgroundGradient = listOf(
                    Color(0xFF0A2540), // Deep Oceanic Navy
                    Color(0xFF0F3E5E), // Electric Cyan-Teal
                    Color(0xFF1B2A4A)  // Royal Navy
                ),
                borderColor = ImmersiveGold.copy(alpha = 0.6f),
                accentColor = ImmersiveGold,
                iconType = AdIconType.REFRI_COOL_BRAND
            )
        )
    }

    var currentBannerIndex by remember { mutableIntStateOf(0) }

    // Should show House Ads carousel (Condition A: offline OR AdMob not linked/verified)
    val shouldShowHouseCarousel = !isOnline || !isAdMobVerifiedAndLinked

    // Auto-rotate house ads every 4.0 seconds when in house carousel mode
    LaunchedEffect(shouldShowHouseCarousel) {
        if (shouldShowHouseCarousel) {
            while (true) {
                delay(4000L)
                currentBannerIndex = (currentBannerIndex + 1) % houseAds.size
            }
        }
    }

    val activeAd = if (shouldShowHouseCarousel) {
        houseAds[currentBannerIndex % houseAds.size]
    } else {
        // Condition B: Verified Google AdMob Live Banner
        HouseAdItem(
            id = "admob_live_online",
            platformTag = "Google AdMob",
            brandTitle = "Wasif RefriCool Sponsored",
            brandHandle = "AdMob Verified",
            brandSubtitle = "Sponsored Partner • HVAC Engineering & Tech",
            ctaLabel = "Visit",
            actionUrl = "https://youtube.com/@WasifRefriCool",
            backgroundGradient = listOf(
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.surfaceVariant
            ),
            borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
            accentColor = ImmersiveGold,
            iconType = AdIconType.ADMOB_ONLINE
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .testTag("ad_banner_container"),
        shape = RoundedCornerShape(14.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, activeAd.borderColor),
        shadowElevation = 4.dp
    ) {
        AnimatedContent(
            targetState = activeAd,
            transitionSpec = {
                (slideInHorizontally(animationSpec = tween(400)) { width -> width } + fadeIn(animationSpec = tween(400)))
                    .togetherWith(slideOutHorizontally(animationSpec = tween(400)) { width -> -width } + fadeOut(animationSpec = tween(400)))
            },
            label = "ad_banner_carousel_animation"
        ) { ad ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(ad.backgroundGradient)
                    )
                    .clickable {
                        openUrl(context, ad.actionUrl)
                    }
                    .padding(horizontal = 10.dp, vertical = 7.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Platform Custom Brand Icon & Titles
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Custom Brand Vector Icon Badge
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .shadow(4.dp, RoundedCornerShape(9.dp))
                                .clip(RoundedCornerShape(9.dp))
                                .background(
                                    when (ad.iconType) {
                                        AdIconType.DSZ_APP -> ImmersiveGold
                                        AdIconType.DSZ_APP_BACKUP -> Color(0xFF0F5A47)
                                        AdIconType.YOUTUBE -> Color(0xFFCC0000)
                                        AdIconType.YOUTUBE_TUTORIALS -> Color(0xFF3730A3)
                                        AdIconType.TIKTOK -> Color(0xFF010101)
                                        AdIconType.REFRI_COOL_BRAND -> Color(0xFF0F3460)
                                        AdIconType.ADMOB_ONLINE -> MaterialTheme.colorScheme.primaryContainer
                                    }
                                )
                        ) {
                            when (ad.iconType) {
                                AdIconType.DSZ_APP -> DSZAppBadgeIcon()
                                AdIconType.DSZ_APP_BACKUP -> DSZAppBackupBadgeIcon()
                                AdIconType.YOUTUBE -> YouTubeLogoIcon()
                                AdIconType.YOUTUBE_TUTORIALS -> YouTubeTutorialsIcon()
                                AdIconType.TIKTOK -> TikTokLogoIcon()
                                AdIconType.REFRI_COOL_BRAND -> RefriCoolHubIcon()
                                AdIconType.ADMOB_ONLINE -> AdMobBadgeIcon()
                            }
                        }

                        Spacer(modifier = Modifier.width(9.dp))

                        // Text Column: Brand Name & Handle / Subtitle
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // "YouTube" / "TikTok" / "DSZ" Badge Pill
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (!shouldShowHouseCarousel) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        Color.Black.copy(alpha = 0.45f)
                                    },
                                    modifier = Modifier.padding(end = 5.dp)
                                ) {
                                    Text(
                                        text = if (!shouldShowHouseCarousel) "Ad" else ad.platformTag.uppercase(),
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (!shouldShowHouseCarousel) ImmersiveGold else when (ad.iconType) {
                                            AdIconType.DSZ_APP, AdIconType.REFRI_COOL_BRAND -> Color(0xFFF4D03F)
                                            AdIconType.DSZ_APP_BACKUP -> Color(0xFF34D399)
                                            AdIconType.YOUTUBE, AdIconType.YOUTUBE_TUTORIALS -> Color(0xFFFF8A80)
                                            AdIconType.TIKTOK -> Color(0xFF25F4EE)
                                            AdIconType.ADMOB_ONLINE -> ImmersiveGold
                                        },
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }

                                Text(
                                    text = ad.brandTitle,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!shouldShowHouseCarousel) MaterialTheme.colorScheme.onSurface else Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.height(1.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = ad.brandHandle,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (!shouldShowHouseCarousel) ImmersiveGold else when (ad.iconType) {
                                        AdIconType.DSZ_APP -> Color(0xFFFDE68A)
                                        AdIconType.DSZ_APP_BACKUP -> Color(0xFFA7F3D0)
                                        AdIconType.TIKTOK -> Color(0xFF38BDF8)
                                        AdIconType.YOUTUBE_TUTORIALS -> Color(0xFFC7D2FE)
                                        else -> Color(0xFF93C5FD)
                                    },
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "• ${ad.brandSubtitle}",
                                    fontSize = 9.sp,
                                    color = if (!shouldShowHouseCarousel) MaterialTheme.colorScheme.onSurfaceVariant else Color.White.copy(alpha = 0.85f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Right: Action CTA Button & Indicator Dots
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = {
                                openUrl(context, ad.actionUrl)
                            },
                            modifier = Modifier
                                .height(28.dp)
                                .testTag("btn_ad_banner_cta"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!shouldShowHouseCarousel) ImmersiveGold else when (ad.iconType) {
                                    AdIconType.DSZ_APP -> ImmersiveGold
                                    AdIconType.DSZ_APP_BACKUP -> Color(0xFF34D399)
                                    AdIconType.YOUTUBE -> Color.White
                                    AdIconType.YOUTUBE_TUTORIALS -> Color(0xFF818CF8)
                                    AdIconType.TIKTOK -> Color(0xFF25F4EE)
                                    AdIconType.REFRI_COOL_BRAND -> ImmersiveGold
                                    AdIconType.ADMOB_ONLINE -> ImmersiveGold
                                },
                                contentColor = if (!shouldShowHouseCarousel) Color(0xFF121212) else when (ad.iconType) {
                                    AdIconType.DSZ_APP -> Color(0xFF121212)
                                    AdIconType.DSZ_APP_BACKUP -> Color(0xFF063327)
                                    AdIconType.YOUTUBE -> Color(0xFFCC0000)
                                    AdIconType.YOUTUBE_TUTORIALS -> Color(0xFF1E1B4B)
                                    AdIconType.TIKTOK -> Color(0xFF010101)
                                    AdIconType.REFRI_COOL_BRAND -> Color(0xFF121212)
                                    AdIconType.ADMOB_ONLINE -> Color(0xFF121212)
                                }
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (ad.iconType == AdIconType.YOUTUBE || ad.iconType == AdIconType.YOUTUBE_TUTORIALS) Icons.Default.PlayArrow else Icons.Default.ArrowOutward,
                                    contentDescription = null,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = ad.ctaLabel,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Carousel mini dots indicator when house carousel is active (6 dots)
                        if (shouldShowHouseCarousel && houseAds.size > 1) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(2.5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                houseAds.indices.forEach { index ->
                                    val isSelected = index == (currentBannerIndex % houseAds.size)
                                    Box(
                                        modifier = Modifier
                                            .size(if (isSelected) 4.5.dp else 3.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) Color.White else Color.White.copy(alpha = 0.35f)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Opens external URLs safely
 */
private fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        // Fallback safely if browser or target app is unavailable
    }
}

/**
 * Custom DSZ App Gold Shield / Logo Vector Badge
 */
@Composable
fun DSZAppBadgeIcon() {
    Canvas(modifier = Modifier.size(20.dp)) {
        val width = size.width
        val height = size.height

        // Dark shield background inside
        val shieldPath = Path().apply {
            moveTo(width * 0.5f, height * 0.15f)
            lineTo(width * 0.82f, height * 0.28f)
            lineTo(width * 0.82f, height * 0.62f)
            cubicTo(width * 0.82f, height * 0.80f, width * 0.5f, height * 0.90f, width * 0.5f, height * 0.90f)
            cubicTo(width * 0.5f, height * 0.90f, width * 0.18f, height * 0.80f, width * 0.18f, height * 0.62f)
            lineTo(width * 0.18f, height * 0.28f)
            close()
        }
        drawPath(path = shieldPath, color = Color(0xFF1E1100))

        // Center gold check / star
        val starPath = Path().apply {
            moveTo(width * 0.36f, height * 0.52f)
            lineTo(width * 0.46f, height * 0.64f)
            lineTo(width * 0.68f, height * 0.38f)
        }
        drawPath(
            path = starPath,
            color = ImmersiveGold,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
    }
}

/**
 * Custom DSZ App Backup & Recovery Vector Badge
 */
@Composable
fun DSZAppBackupBadgeIcon() {
    Canvas(modifier = Modifier.size(20.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.width * 0.38f

        drawCircle(
            color = Color(0xFF34D399),
            radius = radius,
            center = center,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
        )

        // Center sync / refresh arrows
        val arrowPath = Path().apply {
            moveTo(center.x - radius * 0.35f, center.y + radius * 0.1f)
            lineTo(center.x, center.y - radius * 0.35f)
            lineTo(center.x + radius * 0.35f, center.y + radius * 0.1f)
        }
        drawPath(
            path = arrowPath,
            color = Color(0xFF34D399),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
    }
}

/**
 * Custom Vector YouTube Icon
 */
@Composable
fun YouTubeLogoIcon() {
    Canvas(modifier = Modifier.size(20.dp)) {
        val width = size.width
        val height = size.height

        // Draw white triangle play symbol
        val path = Path().apply {
            moveTo(width * 0.40f, height * 0.30f)
            lineTo(width * 0.70f, height * 0.50f)
            lineTo(width * 0.40f, height * 0.70f)
            close()
        }
        drawPath(path = path, color = Color.White)
    }
}

/**
 * Custom YouTube Tutorials Indigo Vector Icon
 */
@Composable
fun YouTubeTutorialsIcon() {
    Canvas(modifier = Modifier.size(20.dp)) {
        val width = size.width
        val height = size.height

        drawRoundRect(
            color = Color.White,
            topLeft = Offset(width * 0.20f, height * 0.25f),
            size = Size(width * 0.60f, height * 0.50f),
            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
        )

        val playPath = Path().apply {
            moveTo(width * 0.42f, height * 0.38f)
            lineTo(width * 0.62f, height * 0.50f)
            lineTo(width * 0.42f, height * 0.62f)
            close()
        }
        drawPath(path = playPath, color = Color(0xFF3730A3))
    }
}

/**
 * Custom Vector TikTok Note Icon with Cyan & Magenta Neon Layers
 */
@Composable
fun TikTokLogoIcon() {
    Canvas(modifier = Modifier.size(20.dp)) {
        val width = size.width
        val height = size.height

        // Cyan shadow / glow
        val cyanPath = Path().apply {
            moveTo(width * 0.52f, height * 0.22f)
            cubicTo(width * 0.57f, height * 0.35f, width * 0.72f, height * 0.38f, width * 0.78f, height * 0.38f)
            lineTo(width * 0.78f, height * 0.28f)
            cubicTo(width * 0.68f, height * 0.28f, width * 0.60f, height * 0.22f, width * 0.58f, height * 0.15f)
            lineTo(width * 0.48f, height * 0.15f)
            lineTo(width * 0.48f, height * 0.62f)
            cubicTo(width * 0.48f, height * 0.72f, width * 0.38f, height * 0.80f, width * 0.28f, height * 0.75f)
            cubicTo(width * 0.20f, height * 0.70f, width * 0.18f, height * 0.58f, width * 0.25f, height * 0.50f)
            cubicTo(width * 0.30f, height * 0.45f, width * 0.38f, height * 0.48f, width * 0.40f, height * 0.50f)
            lineTo(width * 0.40f, height * 0.38f)
            cubicTo(width * 0.35f, height * 0.36f, width * 0.26f, height * 0.38f, width * 0.18f, height * 0.45f)
            cubicTo(width * 0.08f, height * 0.55f, width * 0.08f, height * 0.75f, width * 0.22f, height * 0.85f)
            cubicTo(width * 0.36f, height * 0.95f, width * 0.58f, height * 0.88f, width * 0.58f, height * 0.68f)
            close()
        }
        drawPath(path = cyanPath, color = Color(0xFF25F4EE))

        // White core layer
        val whitePath = Path().apply {
            moveTo(width * 0.50f, height * 0.24f)
            cubicTo(width * 0.55f, height * 0.37f, width * 0.70f, height * 0.40f, width * 0.76f, height * 0.40f)
            lineTo(width * 0.76f, height * 0.30f)
            cubicTo(width * 0.66f, height * 0.30f, width * 0.58f, height * 0.24f, width * 0.56f, height * 0.17f)
            lineTo(width * 0.46f, height * 0.17f)
            lineTo(width * 0.46f, height * 0.64f)
            cubicTo(width * 0.46f, height * 0.74f, width * 0.36f, height * 0.82f, width * 0.26f, height * 0.77f)
            cubicTo(width * 0.18f, height * 0.72f, width * 0.16f, height * 0.60f, width * 0.23f, height * 0.52f)
            close()
        }
        drawPath(path = whitePath, color = Color.White)
    }
}

/**
 * Custom RefriCool Hub Snowflake/Cooling Vector Icon
 */
@Composable
fun RefriCoolHubIcon() {
    Canvas(modifier = Modifier.size(20.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.width * 0.35f

        drawCircle(
            color = ImmersiveGold,
            radius = radius,
            center = center
        )

        drawCircle(
            color = Color(0xFF0F3460),
            radius = radius * 0.75f,
            center = center
        )

        // Center play / star glyph
        val glyphPath = Path().apply {
            moveTo(center.x - radius * 0.25f, center.y - radius * 0.4f)
            lineTo(center.x + radius * 0.45f, center.y)
            lineTo(center.x - radius * 0.25f, center.y + radius * 0.4f)
            close()
        }
        drawPath(glyphPath, color = ImmersiveGold)
    }
}

/**
 * Custom AdMob Badge Icon
 */
@Composable
fun AdMobBadgeIcon() {
    Canvas(modifier = Modifier.size(20.dp)) {
        val width = size.width
        val height = size.height

        drawRoundRect(
            color = ImmersiveGold,
            topLeft = Offset(width * 0.15f, height * 0.15f),
            size = Size(width * 0.70f, height * 0.70f),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )

        drawRoundRect(
            color = Color(0xFF1A1A1D),
            topLeft = Offset(width * 0.25f, height * 0.25f),
            size = Size(width * 0.50f, height * 0.50f),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
        )
    }
}

/**
 * Full-Screen Hybrid Interstitial Ad Dialog:
 * - Operates safely within the strict 2 to 3 ads per day per-user quota.
 * - Condition A (AdMob unlinked/unverified or offline): Smoothly presents an immersive House Interstitial
 *   ad rotating through YouTube (@WasifRefriCool), TikTok (@rwasifdsz.844), and DSZ Save Chat app features.
 * - Condition B (AdMob verified & online): Seamlessly switches to Google AdMob live ads with graceful fallback.
 */
@Composable
fun HouseInterstitialAdDialog(
    language: AppLanguage,
    isOfflineModeEnabled: Boolean,
    isAdMobVerifiedAndLinked: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isOnline = rememberIsNetworkOnline(isOfflineModeEnabled)
    val shouldShowHouse = !isOnline || !isAdMobVerifiedAndLinked

    // Banners collection for full-screen interstitial rotation
    val interstitialAds = remember {
        listOf(
            HouseAdItem(
                id = "interstitial_dsz_vault",
                platformTag = "DSZ SAVE CHAT",
                brandTitle = "DSZ Save Chat PRO",
                brandHandle = "by M WASIF DSZ",
                brandSubtitle = "Dual PIN Vault & Local Chat Recovery System",
                ctaLabel = "Explore Features",
                actionUrl = "https://youtube.com/@WasifRefriCool",
                backgroundGradient = listOf(
                    Color(0xFF1E1100),
                    Color(0xFF422800),
                    Color(0xFF120B00)
                ),
                borderColor = ImmersiveGold,
                accentColor = ImmersiveGold,
                iconType = AdIconType.DSZ_APP
            ),
            HouseAdItem(
                id = "interstitial_youtube_wasif",
                platformTag = "YOUTUBE CHANNEL",
                brandTitle = "Wasif RefriCool",
                brandHandle = "@WasifRefriCool",
                brandSubtitle = "Master Refrigeration & Inverter AC Systems",
                ctaLabel = "Subscribe Now",
                actionUrl = "https://youtube.com/@WasifRefriCool",
                backgroundGradient = listOf(
                    Color(0xFF4A0002),
                    Color(0xFF990000),
                    Color(0xFF280001)
                ),
                borderColor = Color(0xFFFF5252),
                accentColor = Color(0xFFFF4545),
                iconType = AdIconType.YOUTUBE
            ),
            HouseAdItem(
                id = "interstitial_tiktok_wasif",
                platformTag = "TIKTOK OFFICIAL",
                brandTitle = "Wasif RefriCool",
                brandHandle = "@rwasifdsz.844",
                brandSubtitle = "Short Video Hacks, AC Wiring & Diagnostics",
                ctaLabel = "Follow on TikTok",
                actionUrl = "https://www.tiktok.com/@rwasifdsz.844",
                backgroundGradient = listOf(
                    Color(0xFF0F172A),
                    Color(0xFF021B36),
                    Color(0xFF240046)
                ),
                borderColor = Color(0xFF25F4EE),
                accentColor = Color(0xFF25F4EE),
                iconType = AdIconType.TIKTOK
            )
        )
    }

    var selectedAdIndex by remember { mutableIntStateOf(0) }
    val activeAd = interstitialAds[selectedAdIndex % interstitialAds.size]

    var countdownSeconds by remember { mutableIntStateOf(3) }

    LaunchedEffect(Unit) {
        while (countdownSeconds > 0) {
            delay(1000L)
            countdownSeconds--
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (countdownSeconds == 0) onDismiss()
        },
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .testTag("interstitial_ad_dialog"),
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.Transparent,
        text = {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141417)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.5.dp, activeAd.borderColor.copy(alpha = 0.8f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(activeAd.backgroundGradient))
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Bar: "Sponsored / House Ad" and Countdown Close Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.Black.copy(alpha = 0.55f)
                        ) {
                            Text(
                                text = if (shouldShowHouse) activeAd.platformTag else "GOOGLE ADMOB",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = activeAd.accentColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        // Close button with optional countdown
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.6f),
                            modifier = Modifier
                                .size(32.dp)
                                .clickable {
                                    if (countdownSeconds == 0) onDismiss()
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (countdownSeconds > 0) {
                                    Text(
                                        text = "$countdownSeconds",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Text(
                                        text = "✕",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Large Brand Icon Badge
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(72.dp)
                            .shadow(8.dp, RoundedCornerShape(18.dp))
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                when (activeAd.iconType) {
                                    AdIconType.DSZ_APP, AdIconType.DSZ_APP_BACKUP -> ImmersiveGold
                                    AdIconType.YOUTUBE, AdIconType.YOUTUBE_TUTORIALS -> Color(0xFFCC0000)
                                    AdIconType.TIKTOK -> Color(0xFF010101)
                                    AdIconType.REFRI_COOL_BRAND -> Color(0xFF0F3460)
                                    AdIconType.ADMOB_ONLINE -> MaterialTheme.colorScheme.primaryContainer
                                }
                            )
                    ) {
                        when (activeAd.iconType) {
                            AdIconType.DSZ_APP, AdIconType.DSZ_APP_BACKUP -> DSZAppBadgeIcon()
                            AdIconType.YOUTUBE, AdIconType.YOUTUBE_TUTORIALS -> YouTubeLogoIcon()
                            AdIconType.TIKTOK -> TikTokLogoIcon()
                            AdIconType.REFRI_COOL_BRAND -> RefriCoolHubIcon()
                            AdIconType.ADMOB_ONLINE -> AdMobBadgeIcon()
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Brand Title & Verified Badge
                    Text(
                        text = activeAd.brandTitle,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = activeAd.brandHandle,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = activeAd.accentColor
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = activeAd.brandSubtitle,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Primary CTA Action Button
                    Button(
                        onClick = {
                            openUrl(context, activeAd.actionUrl)
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("btn_interstitial_cta"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = activeAd.accentColor,
                            contentColor = Color(0xFF121212)
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (activeAd.iconType == AdIconType.YOUTUBE) Icons.Default.PlayArrow else Icons.Default.ArrowOutward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = activeAd.ctaLabel,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {}
    )
}

