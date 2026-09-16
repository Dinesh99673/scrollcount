package com.probuilder.scrollcount.ui.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Saves the rendered card and hands it to whatever app the user picks.
 *
 * The image goes into the app's own cache folder, which FileProvider exposes
 * read-only and only for as long as the share sheet needs it. Nothing is
 * uploaded anywhere by ScrollCount - the receiving app does whatever it does.
 */
object ShareCard {

    private const val FOLDER = "shared"
    private const val FILE_NAME = "scrollcount_week.png"

    /** Renders, saves and opens the share sheet. Safe to call from the UI. */
    suspend fun share(context: Context, data: ShareCardData): Boolean = withContext(Dispatchers.IO) {
        val bitmap = ShareCardRenderer.render(data)
        val file = save(context, bitmap) ?: return@withContext false
        bitmap.recycle()

        val uri = try {
            FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
        } catch (_: IllegalArgumentException) {
            return@withContext false
        }

        val send = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(send, "Share your week").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        withContext(Dispatchers.Main) {
            runCatching { context.startActivity(chooser) }.isSuccess
        }
    }

    /** Renders the card only, for the in-app preview. */
    suspend fun preview(data: ShareCardData): Bitmap = withContext(Dispatchers.Default) {
        ShareCardRenderer.render(data)
    }

    private fun save(context: Context, bitmap: Bitmap): File? = try {
        val folder = File(context.cacheDir, FOLDER).apply { mkdirs() }
        val file = File(folder, FILE_NAME)
        FileOutputStream(file).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }
        file
    } catch (_: Exception) {
        null
    }
}
