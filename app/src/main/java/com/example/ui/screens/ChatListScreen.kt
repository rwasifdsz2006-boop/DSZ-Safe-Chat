package com.example.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.localization.AppLanguage
import com.example.core.localization.AppStrings
import com.example.core.localization.LocalizedContent
import com.example.model.ChatSource
import com.example.model.SavedMessage
import com.example.ui.theme.GoldGradientColors
import com.example.ui.theme.ImmersiveGold
import com.example.ui.viewmodel.MessageFilter
import kotlinx.coroutines.launch
import java.io.File

enum class ChatSubTab(val titleRes: String, val icon: ImageVector) {
    ALL("All", Icons.Default.DoneAll),
    MESSENGER("Messenger", Icons.Default.Chat),
    BUSINESS("Business", Icons.Default.Business)
}

@Composable
fun ChatListScreen(
    language: AppLanguage,
    messages: List<SavedMessage>,
    searchQuery: String,
    activeFilter: MessageFilter,
    selectedMessage: SavedMessage?,
    isDecoyVaultActive: Boolean = false,
    onSearchChange: (String) -> Unit,
    onFilterChange: (MessageFilter) -> Unit,
    onSelectMessage: (SavedMessage?) -> Unit,
    onDeleteMessage: (String) -> Unit
) {
    val strings = remember(language) { AppStrings.get(language) }
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // 3 Sub-tabs: 0 -> ALL, 1 -> MESSENGER (WhatsApp), 2 -> BUSINESS (WhatsApp Business)
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })

    // Decoy vault mode displays empty chats
    val displayedMessages = if (isDecoyVaultActive) emptyList() else messages

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("chat_list_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Top Sub-Tabs Navigation (All, Messenger, Business)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("chat_sub_tabs_container"),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ChatSubTabItem(
                            title = strings.subTabAll,
                            icon = Icons.Default.DoneAll,
                            isSelected = pagerState.currentPage == 0,
                            modifier = Modifier.weight(1f),
                            testTag = "sub_tab_all",
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(0)
                                }
                            }
                        )

                        ChatSubTabItem(
                            title = strings.subTabMessenger,
                            icon = Icons.Default.Chat,
                            isSelected = pagerState.currentPage == 1,
                            modifier = Modifier.weight(1f),
                            testTag = "sub_tab_messenger",
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(1)
                                }
                            }
                        )

                        ChatSubTabItem(
                            title = strings.subTabBusiness,
                            icon = Icons.Default.Business,
                            isSelected = pagerState.currentPage == 2,
                            modifier = Modifier.weight(1f),
                            testTag = "sub_tab_business",
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(2)
                                }
                            }
                        )
                    }
                }
            }

            // 2. Search Bar Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 10.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("chat_search_bar"),
                    placeholder = {
                        Text(
                            text = strings.searchChatsPlaceholder,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = ImmersiveGold,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = ImmersiveGold,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                )
            }

            // 3. Swipeable Sub-Tabs View (HorizontalPager)
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .testTag("chats_horizontal_pager")
            ) { page ->
                // Filter messages strictly according to selected sub-tab (All, Messenger, Business) and search query
                val subTabFilteredMessages = remember(displayedMessages, searchQuery, page) {
                    displayedMessages.filter { msg ->
                        val matchesSearch = searchQuery.isBlank() ||
                                msg.senderName.contains(searchQuery, ignoreCase = true) ||
                                msg.messageText.contains(searchQuery, ignoreCase = true) ||
                                (msg.originalDeletedContent?.contains(searchQuery, ignoreCase = true) == true)

                        val matchesSubTab = when (page) {
                            0 -> true // ALL: Shows all chats
                            1 -> msg.source == ChatSource.WHATSAPP // MESSENGER: Standard WhatsApp
                            2 -> msg.source == ChatSource.WHATSAPP_BUSINESS // BUSINESS: WhatsApp Business
                            else -> true
                        }

                        matchesSearch && matchesSubTab
                    }
                }

                if (subTabFilteredMessages.isEmpty()) {
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
                                    imageVector = Icons.Default.ChatBubbleOutline,
                                    contentDescription = null,
                                    tint = ImmersiveGold,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = when (page) {
                                    1 -> "No WhatsApp Messages"
                                    2 -> "No WhatsApp Business Messages"
                                    else -> strings.emptyChatsTitle
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = strings.emptyChatsSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = ImmersiveGold,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = strings.secureOfflineBadge,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp,
                                        color = ImmersiveGold
                                    )
                                }
                                Text(
                                    text = "${subTabFilteredMessages.size} Saved",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        items(subTabFilteredMessages, key = { it.id }) { message ->
                            ChatMessageCard(
                                message = message,
                                strings = strings,
                                onClick = { onSelectMessage(message) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Detail / Recovered Message Inspection Dialog
    if (selectedMessage != null) {
        MessageDetailDialog(
            message = selectedMessage,
            strings = strings,
            onDismiss = { onSelectMessage(null) },
            onDelete = {
                onDeleteMessage(selectedMessage.id)
                onSelectMessage(null)
            }
        )
    }
}

@Composable
fun ChatSubTabItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    testTag: String,
    onClick: () -> Unit
) {
    val goldBrush = Brush.linearGradient(
        colors = GoldGradientColors,
        start = Offset(0f, 0f),
        end = Offset(100f, 100f)
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isSelected) ImmersiveGold else Color.Transparent
            )
            .clickable(onClick = onClick)
            .testTag(testTag)
            .padding(horizontal = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color(0xFF121212) else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color(0xFF121212) else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ChatMessageCard(
    message: SavedMessage,
    strings: LocalizedContent,
    onClick: () -> Unit
) {
    val goldBrush = Brush.linearGradient(
        colors = GoldGradientColors,
        start = Offset(0f, 0f),
        end = Offset(100f, 100f)
    )

    val avatarBitmap = remember(message.avatarUri) {
        message.avatarUri?.let { path ->
            try {
                val file = File(path)
                if (file.exists()) BitmapFactory.decodeFile(path)?.asImageBitmap() else null
            } catch (_: Exception) {
                null
            }
        }
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            if (message.isDeletedBySender) ImmersiveGold.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("chat_card_${message.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar Photo / Letter
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (message.isDeletedBySender) {
                                    goldBrush
                                } else {
                                    Brush.linearGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.surfaceVariant,
                                            MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    )
                                }
                            )
                    ) {
                        if (avatarBitmap != null) {
                            Image(
                                bitmap = avatarBitmap,
                                contentDescription = message.senderName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = message.senderAvatarLetter,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (message.isDeletedBySender) Color(0xFF121212) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = message.senderName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            // Source Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(message.source.badgeColorHex).copy(alpha = 0.15f))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = message.source.displayName,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(message.source.badgeColorHex)
                                )
                            }

                            if (message.isDeletedBySender) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                    ) {
                                    Text(
                                        text = strings.deletedMessageBadge,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFEF4444)
                                    )
                                }
                            }
                        }
                    }
                }

                Text(
                    text = message.timestamp,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Message Preview Content
            if (message.isDeletedBySender && message.originalDeletedContent != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                        .border(1.dp, ImmersiveGold.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = message.messageText,
                                fontSize = 11.sp,
                                fontStyle = FontStyle.Italic,
                                color = Color(0xFFEF4444)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "“${message.originalDeletedContent}”",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = ImmersiveGold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            } else {
                Text(
                    text = message.messageText,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun MessageDetailDialog(
    message: SavedMessage,
    strings: LocalizedContent,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    val avatarBitmap = remember(message.avatarUri) {
        message.avatarUri?.let { path ->
            try {
                val file = File(path)
                if (file.exists()) BitmapFactory.decodeFile(path)?.asImageBitmap() else null
            } catch (_: Exception) {
                null
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ImmersiveGold)
                ) {
                    if (avatarBitmap != null) {
                        Image(
                            bitmap = avatarBitmap,
                            contentDescription = message.senderName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = message.senderAvatarLetter,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF121212)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = message.senderName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${message.source.displayName} • ${message.timestamp}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        },
        text = {
            Column {
                if (message.isDeletedBySender) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFEF4444).copy(alpha = 0.12f))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "⚠️ Sender deleted this message in ${message.source.displayName}. Recovered safely offline by DSZ Save Chat.",
                            fontSize = 11.sp,
                            color = Color(0xFFEF4444),
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Original Recovered Content:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ImmersiveGold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = message.originalDeletedContent ?: message.messageText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )
                } else {
                    Text(
                        text = message.messageText,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Stored exclusively in local database.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = onDelete) {
                    Text(text = "Delete", color = Color(0xFFEF4444))
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onDismiss) {
                    Text(text = "Done", fontWeight = FontWeight.Bold, color = ImmersiveGold)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}
