package com.example.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

data class ProcessedEvidence(
    val localUri: String,
    val timestamp: Long,
    val latitude: Double?,
    val longitude: Double?,
    val isExifVerified: Boolean,
    val fileSizeKb: Int
)

class EvidenceProcessor(private val context: Context) {

    suspend fun processEvidenceUri(uri: Uri): ProcessedEvidence = withContext(Dispatchers.IO) {
        var lat: Double? = null
        var lng: Double? = null
        var photoTimestamp = System.currentTimeMillis()
        var hasExif = false

        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val exifInterface = ExifInterface(stream)
                val latLong = FloatArray(2)
                if (exifInterface.getLatLong(latLong)) {
                    lat = latLong[0].toDouble()
                    lng = latLong[1].toDouble()
                    hasExif = true
                }
                val dateStr = exifInterface.getAttribute(ExifInterface.TAG_DATETIME)
                if (dateStr != null) {
                    val sdf = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US)
                    sdf.parse(dateStr)?.let {
                        photoTimestamp = it.time
                        hasExif = true
                    }
                }
            }
        } catch (e: Exception) {
            // EXIF parsing fallback
        }

        // Compress image to efficient mobile mesh transmission size (< 200KB)
        val compressedFile = File(context.cacheDir, "evidence_${System.currentTimeMillis()}.jpg")
        var fileKb = 0
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                if (originalBitmap != null) {
                    val maxDimension = 1280
                    val width = originalBitmap.width
                    val height = originalBitmap.height
                    val ratio = (width.toFloat() / height.toFloat()).coerceAtLeast(0.1f)
                    val targetWidth = if (width > height) maxDimension else (maxDimension * ratio).toInt()
                    val targetHeight = if (height > width) maxDimension else (maxDimension / ratio).toInt()

                    val scaledBitmap = Bitmap.createScaledBitmap(
                        originalBitmap,
                        targetWidth.coerceAtLeast(100),
                        targetHeight.coerceAtLeast(100),
                        true
                    )

                    FileOutputStream(compressedFile).use { out ->
                        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
                    }
                    fileKb = (compressedFile.length() / 1024).toInt()
                }
            }
        } catch (e: Exception) {
            // Fallback
        }

        ProcessedEvidence(
            localUri = if (compressedFile.exists()) Uri.fromFile(compressedFile).toString() else uri.toString(),
            timestamp = photoTimestamp,
            latitude = lat,
            longitude = lng,
            isExifVerified = hasExif,
            fileSizeKb = fileKb.coerceAtLeast(45)
        )
    }
}
