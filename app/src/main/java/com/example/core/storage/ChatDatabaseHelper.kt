package com.example.core.storage

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.model.ChatSource
import com.example.model.SavedMediaItem
import com.example.model.SavedMessage
import com.example.ui.screens.MediaTypeFilter

/**
 * Production-Grade Local SQLite Database for Instant Chat & Media Interception
 * - Saves incoming notifications the exact millisecond they arrive with zero latency.
 * - Permanent Anti-Delete storage: Messages and media never get erased if deleted on WhatsApp.
 * - Complete user control: Allows local storage cleaning whenever device storage is full.
 */
class ChatDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_MESSAGES (
                $COL_ID TEXT PRIMARY KEY,
                $COL_SENDER_NAME TEXT NOT NULL,
                $COL_AVATAR_LETTER TEXT NOT NULL,
                $COL_AVATAR_URI TEXT,
                $COL_MESSAGE_TEXT TEXT NOT NULL,
                $COL_TIMESTAMP TEXT NOT NULL,
                $COL_TIMESTAMP_MILLIS INTEGER NOT NULL,
                $COL_SOURCE TEXT NOT NULL,
                $COL_IS_DELETED INTEGER DEFAULT 0,
                $COL_ORIGINAL_DELETED TEXT,
                $COL_MEDIA_TYPE TEXT,
                $COL_MEDIA_URI TEXT,
                $COL_UNREAD INTEGER DEFAULT 1
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_MEDIA (
                $COL_MEDIA_ID TEXT PRIMARY KEY,
                $COL_MEDIA_TITLE TEXT NOT NULL,
                $COL_MEDIA_SENDER TEXT NOT NULL,
                $COL_MEDIA_TYPE_FILTER TEXT NOT NULL,
                $COL_MEDIA_TIMESTAMP TEXT NOT NULL,
                $COL_MEDIA_FILE_SIZE TEXT NOT NULL,
                $COL_MEDIA_PATH TEXT,
                $COL_MEDIA_SOURCE TEXT NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MESSAGES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MEDIA")
        onCreate(db)
    }

    // Insert or update incoming intercepted message instantly
    fun insertMessage(message: SavedMessage) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_ID, message.id)
            put(COL_SENDER_NAME, message.senderName)
            put(COL_AVATAR_LETTER, message.senderAvatarLetter)
            put(COL_AVATAR_URI, message.avatarUri)
            put(COL_MESSAGE_TEXT, message.messageText)
            put(COL_TIMESTAMP, message.timestamp)
            put(COL_TIMESTAMP_MILLIS, message.timestampMillis)
            put(COL_SOURCE, message.source.name)
            put(COL_IS_DELETED, if (message.isDeletedBySender) 1 else 0)
            put(COL_ORIGINAL_DELETED, message.originalDeletedContent)
            put(COL_MEDIA_TYPE, message.mediaType)
            put(COL_MEDIA_URI, message.mediaUri)
            put(COL_UNREAD, if (message.unread) 1 else 0)
        }
        db.insertWithOnConflict(TABLE_MESSAGES, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    // Retrieve all saved messages ordered by latest timestamp
    fun getAllMessages(): List<SavedMessage> {
        val list = mutableListOf<SavedMessage>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_MESSAGES,
            null,
            null,
            null,
            null,
            null,
            "$COL_TIMESTAMP_MILLIS DESC"
        )
        cursor.use {
            val idIdx = cursor.getColumnIndexOrThrow(COL_ID)
            val nameIdx = cursor.getColumnIndexOrThrow(COL_SENDER_NAME)
            val avatarIdx = cursor.getColumnIndexOrThrow(COL_AVATAR_LETTER)
            val avatarUriIdx = cursor.getColumnIndexOrThrow(COL_AVATAR_URI)
            val textIdx = cursor.getColumnIndexOrThrow(COL_MESSAGE_TEXT)
            val timeIdx = cursor.getColumnIndexOrThrow(COL_TIMESTAMP)
            val timeMillisIdx = cursor.getColumnIndexOrThrow(COL_TIMESTAMP_MILLIS)
            val sourceIdx = cursor.getColumnIndexOrThrow(COL_SOURCE)
            val delIdx = cursor.getColumnIndexOrThrow(COL_IS_DELETED)
            val origDelIdx = cursor.getColumnIndexOrThrow(COL_ORIGINAL_DELETED)
            val mediaTypeIdx = cursor.getColumnIndexOrThrow(COL_MEDIA_TYPE)
            val mediaUriIdx = cursor.getColumnIndexOrThrow(COL_MEDIA_URI)
            val unreadIdx = cursor.getColumnIndexOrThrow(COL_UNREAD)

            while (cursor.moveToNext()) {
                val sourceEnum = try {
                    ChatSource.valueOf(cursor.getString(sourceIdx))
                } catch (_: Exception) {
                    ChatSource.WHATSAPP
                }
                list.add(
                    SavedMessage(
                        id = cursor.getString(idIdx),
                        senderName = cursor.getString(nameIdx),
                        senderAvatarLetter = cursor.getString(avatarIdx),
                        avatarUri = cursor.getString(avatarUriIdx),
                        messageText = cursor.getString(textIdx),
                        timestamp = cursor.getString(timeIdx),
                        timestampMillis = cursor.getLong(timeMillisIdx),
                        source = sourceEnum,
                        isDeletedBySender = cursor.getInt(delIdx) == 1,
                        originalDeletedContent = cursor.getString(origDelIdx),
                        mediaType = cursor.getString(mediaTypeIdx),
                        mediaUri = cursor.getString(mediaUriIdx),
                        unread = cursor.getInt(unreadIdx) == 1
                    )
                )
            }
        }
        return list
    }

    // Insert media item into dedicated Media Hub
    fun insertMediaItem(item: SavedMediaItem) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_MEDIA_ID, item.id)
            put(COL_MEDIA_TITLE, item.title)
            put(COL_MEDIA_SENDER, item.senderName)
            put(COL_MEDIA_TYPE_FILTER, item.type.name)
            put(COL_MEDIA_TIMESTAMP, item.timestamp)
            put(COL_MEDIA_FILE_SIZE, item.fileSize)
            put(COL_MEDIA_PATH, item.filePath)
            put(COL_MEDIA_SOURCE, item.source)
        }
        db.insertWithOnConflict(TABLE_MEDIA, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    // Retrieve all media items for Media Hub
    fun getAllMediaItems(): List<SavedMediaItem> {
        val list = mutableListOf<SavedMediaItem>()
        val db = readableDatabase
        val cursor = db.query(TABLE_MEDIA, null, null, null, null, null, "$COL_MEDIA_ID DESC")
        cursor.use {
            val idIdx = cursor.getColumnIndexOrThrow(COL_MEDIA_ID)
            val titleIdx = cursor.getColumnIndexOrThrow(COL_MEDIA_TITLE)
            val senderIdx = cursor.getColumnIndexOrThrow(COL_MEDIA_SENDER)
            val typeIdx = cursor.getColumnIndexOrThrow(COL_MEDIA_TYPE_FILTER)
            val timeIdx = cursor.getColumnIndexOrThrow(COL_MEDIA_TIMESTAMP)
            val sizeIdx = cursor.getColumnIndexOrThrow(COL_MEDIA_FILE_SIZE)
            val pathIdx = cursor.getColumnIndexOrThrow(COL_MEDIA_PATH)
            val sourceIdx = cursor.getColumnIndexOrThrow(COL_MEDIA_SOURCE)

            while (cursor.moveToNext()) {
                val typeEnum = try {
                    MediaTypeFilter.valueOf(cursor.getString(typeIdx))
                } catch (_: Exception) {
                    MediaTypeFilter.ALL
                }
                list.add(
                    SavedMediaItem(
                        id = cursor.getString(idIdx),
                        title = cursor.getString(titleIdx),
                        senderName = cursor.getString(senderIdx),
                        type = typeEnum,
                        timestamp = cursor.getString(timeIdx),
                        fileSize = cursor.getString(sizeIdx),
                        filePath = cursor.getString(pathIdx),
                        source = cursor.getString(sourceIdx)
                    )
                )
            }
        }
        return list
    }

    fun deleteMessage(id: String) {
        writableDatabase.delete(TABLE_MESSAGES, "$COL_ID = ?", arrayOf(id))
    }

    fun deleteMedia(id: String) {
        writableDatabase.delete(TABLE_MEDIA, "$COL_MEDIA_ID = ?", arrayOf(id))
    }

    fun clearAllMessages() {
        writableDatabase.delete(TABLE_MESSAGES, null, null)
    }

    fun clearAllMedia() {
        writableDatabase.delete(TABLE_MEDIA, null, null)
    }

    companion object {
        private const val DATABASE_NAME = "dsz_save_chat_vault.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_MESSAGES = "saved_messages"
        private const val COL_ID = "id"
        private const val COL_SENDER_NAME = "sender_name"
        private const val COL_AVATAR_LETTER = "avatar_letter"
        private const val COL_AVATAR_URI = "avatar_uri"
        private const val COL_MESSAGE_TEXT = "message_text"
        private const val COL_TIMESTAMP = "timestamp"
        private const val COL_TIMESTAMP_MILLIS = "timestamp_millis"
        private const val COL_SOURCE = "source"
        private const val COL_IS_DELETED = "is_deleted"
        private const val COL_ORIGINAL_DELETED = "original_deleted"
        private const val COL_MEDIA_TYPE = "media_type"
        private const val COL_MEDIA_URI = "media_uri"
        private const val COL_UNREAD = "unread"

        private const val TABLE_MEDIA = "saved_media"
        private const val COL_MEDIA_ID = "media_id"
        private const val COL_MEDIA_TITLE = "title"
        private const val COL_MEDIA_SENDER = "sender_name"
        private const val COL_MEDIA_TYPE_FILTER = "type_filter"
        private const val COL_MEDIA_TIMESTAMP = "timestamp"
        private const val COL_MEDIA_FILE_SIZE = "file_size"
        private const val COL_MEDIA_PATH = "file_path"
        private const val COL_MEDIA_SOURCE = "source"
    }
}
