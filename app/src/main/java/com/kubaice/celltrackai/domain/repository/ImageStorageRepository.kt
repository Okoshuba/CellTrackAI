package com.kubaice.celltrackai.domain.repository

interface ImageStorageRepository {
    suspend fun saveImageFromUri(uriString: String): String?
    suspend fun deleteImage(uriString: String)
}
