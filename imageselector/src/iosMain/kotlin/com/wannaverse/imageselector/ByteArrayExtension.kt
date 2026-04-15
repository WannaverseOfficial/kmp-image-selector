package com.wannaverse.imageselector

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.Data
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import platform.Foundation.NSData
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePNGRepresentation
import platform.posix.memcpy

actual fun ByteArray.toImageBitmap(): ImageBitmap {
    return Image
        .makeFromEncoded(Data.makeFromBytes(this).bytes)
        .toComposeImageBitmap()
}

actual fun ImageBitmap.toByteArray(): ByteArray {
    val skiaImage = Image.makeFromBitmap(this.asSkiaBitmap())
    return skiaImage.encodeToData(EncodedImageFormat.PNG, 100)?.bytes ?: ByteArray(0)
}

actual fun ByteArray.compressImage(options: ImageCompressionOptions): ByteArray {
    val image = UIImage(data = toNSData()) ?: return this

    var quality = options.quality
    var compressed = image.encode(options.format, quality) ?: return this

    if (options.format == ImageCompressionFormat.JPEG) {
        val maxBytes = options.maxBytes
        while (maxBytes != null && compressed.length.toLong() > maxBytes && quality > options.minQuality) {
            quality = maxOf(options.minQuality, quality - options.qualityStep)
            compressed = image.encode(options.format, quality) ?: break
            if (quality == options.minQuality) {
                break
            }
        }
    }

    return compressed.toByteArray()
}

private fun UIImage.encode(format: ImageCompressionFormat, quality: Int): NSData? {
    return when (format) {
        ImageCompressionFormat.JPEG -> UIImageJPEGRepresentation(this, quality.toDouble() / 100.0)
        ImageCompressionFormat.PNG -> UIImagePNGRepresentation(this)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData = usePinned {
    NSData.dataWithBytes(bytes = it.addressOf(0), length = size.toULong())
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val bytes = ByteArray(length.toInt())
    bytes.usePinned {
        memcpy(it.addressOf(0), this.bytes, length)
    }
    return bytes
}
