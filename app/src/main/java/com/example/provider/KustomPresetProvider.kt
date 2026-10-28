package com.example.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import java.io.FileNotFoundException

class KustomPresetProvider : ContentProvider() {

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        // Expose widgets in this pack to KWGT
        val cursor = MatrixCursor(arrayOf("_id", "title", "author", "preview", "type", "path"))
        cursor.addRow(
            arrayOf(
                1,
                "ChatGPT Clock & Usage Minimal",
                "Codex",
                "chatgpt_preview",
                "widget",
                "widgets/chatgpt_monitor.kwgt"
            )
        )
        cursor.addRow(
            arrayOf(
                2,
                "ChatGPT Multi-Gauge 5h & Weekly",
                "Codex",
                "chatgpt_preview",
                "widget",
                "widgets/chatgpt_gauge.kwgt"
            )
        )
        return cursor
    }

    override fun getType(uri: Uri): String = "vnd.android.cursor.dir/org.kustom.provider.WIDGETS"

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun openAssetFile(uri: Uri, mode: String): AssetFileDescriptor? {
        val path = uri.path?.removePrefix("/") ?: throw FileNotFoundException("Invalid URI: $uri")
        return try {
            context?.assets?.openFd(path)
        } catch (_: Exception) {
            try {
                context?.assets?.openFd("widgets/$path")
            } catch (_: Exception) {
                null
            }
        }
    }
}
