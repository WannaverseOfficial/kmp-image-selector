package com.wannaverse.imageselector

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.ByteArrayOutputStream

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
