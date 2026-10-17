package com.kubaice.celltrackai.data.repository

import android.content.Context
import android.net.Uri
import com.kubaice.celltrackai.domain.repository.ImageStorageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageStorageRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ImageStorageRepository {

    override suspend fun saveImageFromUri(uriString: String): String? = withContext(Dispatchers.IO) {
        if (uriString.isBlank()) return@withContext null

        val sourceUri = Uri.parse(uriString)

        // If already in local app storage directory, keep existing URI
        if (sourceUri.scheme == "file" && sourceUri.path?.startsWith(context.filesDir.absolutePath) == true) {
            return@withContext uriString
        }

        try {
            val imagesDir = File(context.filesDir, "culture_images").apply {
                if (!exists()) mkdirs()
            }
            val fileName = "img_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
            val destFile = File(imagesDir, fileName)

            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                FileOutputStream(destFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: return@withContext null

            Uri.fromFile(destFile).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun deleteImage(uriString: String): Unit = withContext(Dispatchers.IO) {
        if (uriString.isBlank()) return@withContext
        try {
            val uri = Uri.parse(uriString)
            if (uri.scheme == "file" && uri.path != null) {
                val file = File(uri.path!!)
                if (file.exists() && file.absolutePath.startsWith(context.filesDir.absolutePath)) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
