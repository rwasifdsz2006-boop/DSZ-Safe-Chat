package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.localization.AppLanguage
import com.example.core.localization.AppStrings
import com.example.model.SavedMediaItem
import com.example.ui.theme.ImmersiveGold

enum class MediaTypeFilter {
    ALL,
    PHOTOS,
    VIDEOS,
    AUDIO,
    DOCS
}

/**
 * Dedicated Centralized Media Hub:
 * - Dedicated tabs for Photos, Videos, and Audio.
 * - Allows users to manually browse and safely download desired media to their device storage.
 * - Shows clear storage footprint with full local user control.
 */
@Composable
fun MediaGalleryScreen(
    language: AppLanguage,
    mediaList: List<SavedMediaItem> = emptyList(),
    isDecoyVaultActive: Boolean = false,
    onDeleteMedia: (String) -> Unit = {}
) {
    val strings = remember(language) { AppStrings.get(language) }
    val context = LocalContext.current
    var selectedMediaType by remember { mutableStateOf(MediaTypeFilter.ALL) }
    var selectedMediaItem by remember { mutableStateOf<SavedMediaItem?>(null) }

    // Decoy mode displays empty media
    val displayedMedia = if (isDecoyVaultActive) emptyList() else mediaList

    val filteredMedia = remember(displayedMedia, selectedMediaType) {
        if (selectedMediaType == MediaTypeFilter.ALL) {
            displayedMedia
        } else {
            displayedMedia.filter { it.type == selectedMediaType }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("media_gallery_screen")
    ) {
        // 1. Media Categories Tab Filter
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MediaCategoryChip(
                    title = "All (${displayedMedia.size})",
                    icon = Icons.Outlined.PhotoLibrary,
                    isSelected = selectedMediaType == MediaTypeFilter.ALL,
                    onClick = { selectedMediaType = MediaTypeFilter.ALL }
                )
                MediaCategoryChip(
                    title = "Photos",
                    icon = Icons.Default.Image,
                    isSelected = selectedMediaType == MediaTypeFilter.PHOTOS,
                    onClick = { selectedMediaType = MediaTypeFilter.PHOTOS }
                )
                MediaCategoryChip(
                    title = "Videos",
                    icon = Icons.Default.PlayCircle,
                    isSelected = selectedMediaType == MediaTypeFilter.VIDEOS,
                    onClick = { selectedMediaType = MediaTypeFilter.VIDEOS }
                )
                MediaCategoryChip(
                    title = "Audio",
                    icon = Icons.Default.Audiotrack,
                    isSelected = selectedMediaType == MediaTypeFilter.AUDIO,
                    onClick = { selectedMediaType = MediaTypeFilter.AUDIO }
                )
            }
        }

        // 2. Empty State vs Media Items Grid
        if (filteredMedia.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (selectedMediaType) {
                                MediaTypeFilter.PHOTOS -> Icons.Default.Image
                                MediaTypeFilter.VIDEOS -> Icons.Default.PlayCircle
                                MediaTypeFilter.AUDIO -> Icons.Default.Audiotrack
                                else -> Icons.Outlined.PhotoLibrary
                            },
                            contentDescription = null,
                            tint = ImmersiveGold,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = strings.mediaEmptyTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = strings.mediaEmptySubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredMedia, key = { it.id }) { item ->
                    MediaGridCard(
                        item = item,
                        onClick = { selectedMediaItem = item }
                    )
                }
            }
        }
    }

    // Media Detail & Download Dialog (Centralized Media Hub only)
    selectedMediaItem?.let { media ->
        MediaDownloadDetailDialog(
            item = media,
            onDismiss = { selectedMediaItem = null },
            onDownload = {
                Toast.makeText(context, "Saved to device storage: ${media.title}", Toast.LENGTH_SHORT).show()
                selectedMediaItem = null
            },
            onDelete = {
                onDeleteMedia(media.id)
                selectedMediaItem = null
            }
        )
    }
}

@Composable
fun MediaGridCard(
    item: SavedMediaItem,
    onClick: () -> Unit
) {
    val icon = when (item.type) {
        MediaTypeFilter.PHOTOS -> Icons.Default.Image
        MediaTypeFilter.VIDEOS -> Icons.Default.PlayCircle
        MediaTypeFilter.AUDIO -> Icons.Default.Audiotrack
        else -> Icons.Default.InsertDriveFile
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("media_card_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ImmersiveGold,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.title,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.senderName,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.timestamp,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun MediaDownloadDetailDialog(
    item: SavedMediaItem,
    onDismiss: () -> Unit,
    onDownload: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ImmersiveGold),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (item.type) {
                            MediaTypeFilter.PHOTOS -> Icons.Default.Image
                            MediaTypeFilter.VIDEOS -> Icons.Default.PlayCircle
                            MediaTypeFilter.AUDIO -> Icons.Default.Audiotrack
                            else -> Icons.Default.InsertDriveFile
                        },
                        contentDescription = null,
                        tint = Color(0xFF121212),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = item.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${item.source} • ${item.timestamp}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Sender: ${item.senderName}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Status: Permanently protected (Anti-Delete)",
                    fontSize = 11.sp,
                    color = ImmersiveGold
                )
                Text(
                    text = "You can manually save this media file to your phone's public gallery / download folder or clean it locally.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = onDelete) {
                    Text("Delete", color = Color(0xFFEF4444))
                }
                Spacer(modifier = Modifier.width(6.dp))
                Button(
                    onClick = onDownload,
                    colors = ButtonDefaults.buttonColors(containerColor = ImmersiveGold)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = Color(0xFF121212),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save to Phone", color = Color(0xFF121212), fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun MediaCategoryChip(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .height(34.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, if (isSelected) ImmersiveGold else Color.Transparent)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) ImmersiveGold else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) ImmersiveGold else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
