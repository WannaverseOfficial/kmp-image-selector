package com.wannaverse.imageselector

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

actual suspend fun ByteArray.downSamplingToImageBitmap(
    coroutineScope: CoroutineScope,
    reqHeight: Int,
    reqWidth: Int
): ImageBitmap? {
    var imageBitmap: ImageBitmap? = null
    val byteArray = this
    coroutineScope.launch(Dispatchers.Default) {
        BitmapFactory.Options().runCatching {
            inJustDecodeBounds = true
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, this)
            inSampleSize = calculateInSampleSize(this, reqHeight, reqWidth)
            inJustDecodeBounds = false
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, this)
                .asImageBitmap()
        }
        .onSuccess {
            imageBitmap = it
        }
        .onFailure {
            throw RuntimeException(it.message)
        }
    }.join()
    return imageBitmap
}

private fun calculateInSampleSize(options: BitmapFactory.Options, reqHeight: Int, reqWidth: Int): Int {
    val (height: Int, width: Int) = options.run { outHeight to outWidth }
    var inSampleSize = 1
    if(height > reqHeight || width > reqWidth) {
        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2

        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}
actual fun ByteArray.toImageBitmap(): ImageBitmap {
    return BitmapFactory.decodeByteArray(this, 0, size).asImageBitmap()
}

actual fun ImageBitmap.toByteArray(): ByteArray {
    val bitmap = this.asAndroidBitmap()
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    return stream.toByteArray()
}

actual fun ByteArray.compressImage(options: ImageCompressionOptions): ByteArray {
    val bitmap = BitmapFactory.decodeByteArray(this, 0, size) ?: return this

    val format = when (options.format) {
        ImageCompressionFormat.JPEG -> Bitmap.CompressFormat.JPEG
        ImageCompressionFormat.PNG -> Bitmap.CompressFormat.PNG
    }

    var quality = options.quality
    var compressed = bitmap.encode(format, quality)

    if (format == Bitmap.CompressFormat.JPEG) {
        val maxBytes = options.maxBytes
        while (maxBytes != null && compressed.size > maxBytes && quality > options.minQuality) {
            quality = maxOf(options.minQuality, quality - options.qualityStep)
            compressed = bitmap.encode(format, quality)
            if (quality == options.minQuality) {
                break
            }
        }
    }

    return compressed
}

private fun Bitmap.encode(format: Bitmap.CompressFormat, quality: Int): ByteArray {
    val stream = ByteArrayOutputStream()
    compress(format, quality, stream)
    return stream.toByteArray()
}
