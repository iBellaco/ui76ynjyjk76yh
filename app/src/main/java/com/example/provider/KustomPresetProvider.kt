package com.example.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipFile

class KustomPresetProvider : ContentProvider() {

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        val segments = uri.pathSegments
        val action = segments.firstOrNull() ?: ""

        Log.d(TAG, "KWGT Query URI: $uri, action: $action")

        return when {
            // KWGT standard preset listing: content://authority/list/widgets
            action.equals(ACTION_LIST, ignoreCase = true) -> {
                val cursor = MatrixCursor(arrayOf("filename"))
                val widgetFiles = listWidgetAssets()
                for (file in widgetFiles) {
                    cursor.addRow(arrayOf(file))
                }
                cursor
            }

            // KWGT file info query: content://authority/info/...
            action.equals(ACTION_INFO, ignoreCase = true) -> {
                val cursor = MatrixCursor(arrayOf("valid", "size", "modified"))
                val targetPath = extractCleanPath(uri)
                val cacheFile = getCachedAssetFile(targetPath)
                val isValid = cacheFile != null && cacheFile.exists() && cacheFile.length() > 0
                cursor.addRow(
                    arrayOf(
                        if (isValid) "true" else "false",
                        (cacheFile?.length() ?: 0L).toString(),
                        (cacheFile?.lastModified() ?: System.currentTimeMillis()).toString()
                    )
                )
                cursor
            }

            // Fallback general preset cursor
            else -> {
                val cursor = MatrixCursor(arrayOf("_id", "title", "author", "preview", "type", "path", "filename"))
                cursor.addRow(
                    arrayOf(
                        1,
                        "ChatGPT Clock & 5h Usage",
                        "Codex KWGT",
                        "chatgpt_preview",
                        "widget",
                        "widgets/chatgpt_monitor.kwgt",
                        "chatgpt_monitor.kwgt"
                    )
                )
                cursor.addRow(
                    arrayOf(
                        2,
                        "ChatGPT Multi-Gauge 5h & Weekly",
                        "Codex KWGT",
                        "chatgpt_preview",
                        "widget",
                        "widgets/chatgpt_gauge.kwgt",
                        "chatgpt_gauge.kwgt"
                    )
                )
                cursor
            }
        }
    }

    override fun getType(uri: Uri): String = "vnd.android.cursor.dir/org.kustom.provider.WIDGETS"

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        Log.d(TAG, "KWGT openFile: $uri")
        val cleanPath = extractCleanPath(uri)
        val file = getCachedAssetFile(cleanPath)
            ?: throw FileNotFoundException("Kustom file not found: $uri (path: $cleanPath)")
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    }

    override fun openAssetFile(uri: Uri, mode: String): AssetFileDescriptor {
        Log.d(TAG, "KWGT openAssetFile: $uri")
        val pfd = openFile(uri, mode)
        return AssetFileDescriptor(pfd, 0, pfd.statSize)
    }

    private fun extractCleanPath(uri: Uri): String {
        var raw = uri.path ?: uri.encodedSchemeSpecificPart ?: ""
        raw = raw.replace("//", "/")
        if (raw.startsWith("/")) raw = raw.removePrefix("/")
        if (raw.startsWith("list/")) raw = raw.removePrefix("list/")
        if (raw.startsWith("info/")) raw = raw.removePrefix("info/")
        if (raw.contains("archive:")) {
            raw = raw.substringAfter("archive:")
        }
        return raw.trim()
    }

    private fun listWidgetAssets(): List<String> {
        val list = mutableListOf<String>()
        try {
            val assets = context?.assets?.list("widgets") ?: emptyArray()
            for (f in assets) {
                if (f.endsWith(".kwgt") || f.endsWith(".kwgt.zip") || f.endsWith(".komp")) {
                    list.add(f)
                }
            }
        } catch (_: Exception) {
            list.add("chatgpt_monitor.kwgt")
            list.add("chatgpt_gauge.kwgt")
        }
        if (list.isEmpty()) {
            list.add("chatgpt_monitor.kwgt")
            list.add("chatgpt_gauge.kwgt")
        }
        return list
    }

    private fun getCachedAssetFile(targetPath: String): File? {
        val ctx = context ?: return null
        val cacheDir = File(ctx.cacheDir, "kustom_provider_cache").apply { mkdirs() }

        // Case 1: direct preset file request (e.g., "widgets/chatgpt_monitor.kwgt" or "chatgpt_monitor.kwgt")
        val fileName = File(targetPath).name
        val assetPath = if (targetPath.startsWith("widgets/")) targetPath else "widgets/$fileName"

        // Check if path refers to an internal entry inside the zip (e.g. "widgets/chatgpt_monitor.kwgt/preset.json")
        if (targetPath.contains(".kwgt/") || targetPath.contains(".kwgt.zip/")) {
            val archiveSubPath = if (targetPath.contains(".kwgt/")) {
                targetPath.substringBefore(".kwgt/") + ".kwgt"
            } else {
                targetPath.substringBefore(".kwgt.zip/") + ".kwgt.zip"
            }
            val entryName = targetPath.substringAfter(archiveSubPath).removePrefix("/")
            val baseArchive = getCachedAssetFile(archiveSubPath) ?: return null

            val entryFile = File(cacheDir, "${baseArchive.nameWithoutExtension}_${entryName.replace('/', '_')}")
            try {
                ZipFile(baseArchive).use { zip ->
                    val entry = zip.getEntry(entryName) ?: return null
                    zip.getInputStream(entry).use { input ->
                        FileOutputStream(entryFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                }
                return entryFile
            } catch (e: Exception) {
                Log.e(TAG, "Error extracting zip entry $entryName from $archiveSubPath", e)
                return null
            }
        }

        // Standard copy of the asset file to cacheDir so ParcelFileDescriptor can read it
        val cachedPreset = File(cacheDir, fileName)
        return try {
            val isModified = !cachedPreset.exists() || cachedPreset.length() == 0L
            if (isModified) {
                val input: InputStream = try {
                    ctx.assets.open(assetPath)
                } catch (_: Exception) {
                    ctx.assets.open(fileName)
                }
                input.use { inStream ->
                    FileOutputStream(cachedPreset).use { outStream ->
                        inStream.copyTo(outStream)
                    }
                }
            }
            cachedPreset
        } catch (e: Exception) {
            Log.e(TAG, "Failed to copy asset $assetPath to cache", e)
            null
        }
    }

    companion object {
        private const val TAG = "KustomPresetProvider"
        private const val ACTION_LIST = "list"
        private const val ACTION_INFO = "info"
    }
}
