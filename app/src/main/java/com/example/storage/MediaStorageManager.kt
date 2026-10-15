package com.example.storage

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.model.MediaResourceItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object MediaStorageManager {

    private val EXPECTED_FILES = listOf(
        "promo.mp4" to "Video promocional para clientes",
        "catalogo.pdf" to "Catálogo de precios oficial",
        "bienvenida.jpg" to "Imagen de bienvenida de WhatsApp",
        "app.apk" to "Instalador de respaldo"
    )

    fun getMediaDirectory(context: Context): File {
        val dir = File(context.filesDir, "media")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    suspend fun getResourceItems(context: Context): List<MediaResourceItem> = withContext(Dispatchers.IO) {
        val dir = getMediaDirectory(context)
        val existingFiles = dir.list()?.toSet() ?: emptySet()

        EXPECTED_FILES.map { (name, desc) ->
            MediaResourceItem(
                filename = name,
                isPresent = existingFiles.contains(name),
                description = desc
            )
        }
    }

    suspend fun saveUriToMedia(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            var fileName = "recurso_${System.currentTimeMillis()}"
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    val name = cursor.getString(nameIndex)
                    if (!name.isNullOrBlank()) {
                        fileName = name
                    }
                }
            }

            val targetFile = File(getMediaDirectory(context), fileName)
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            fileName
        } catch (e: Exception) {
            null
        }
    }
}
