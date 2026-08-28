package com.example.model

import com.example.ui.screens.MediaTypeFilter

data class SavedMediaItem(
    val id: String,
    val title: String,
    val senderName: String,
    val type: MediaTypeFilter,
    val timestamp: String,
    val fileSize: String,
    val filePath: String? = null,
    val source: String = "WhatsApp"
)
