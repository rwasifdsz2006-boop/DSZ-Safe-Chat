package com.example.model

enum class ChatSource(val displayName: String, val badgeColorHex: Long) {
    WHATSAPP("WhatsApp", 0xFF25D366),
    WHATSAPP_BUSINESS("WhatsApp Business", 0xFF128C7E),
    TELEGRAM("Telegram", 0xFF0088CC),
    DIRECT("Direct SMS", 0xFFD4AF37)
}

data class SavedMessage(
    val id: String,
    val senderName: String,
    val senderAvatarLetter: String,
    val messageText: String,
    val timestamp: String,
    val source: ChatSource,
    val isDeletedBySender: Boolean = false,
    val originalDeletedContent: String? = null,
    val unread: Boolean = false,
    val avatarUri: String? = null,
    val timestampMillis: Long = System.currentTimeMillis(),
    val mediaType: String? = null,
    val mediaUri: String? = null
)

object SampleChatData {
    fun getInitialMessages(): List<SavedMessage> = emptyList()
}
