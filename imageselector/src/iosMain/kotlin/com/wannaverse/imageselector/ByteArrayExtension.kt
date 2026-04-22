package com.wannaverse.imageselector

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.refTo
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.skia.Data
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import platform.CoreFoundation.CFDataCreate
import platform.CoreFoundation.CFDataGetBytes
import platform.CoreFoundation.CFDataGetLength
import platform.CoreFoundation.CFDataRef
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFRangeMake
import platform.CoreFoundation.CFRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSMutableData
import platform.Foundation.NSNumber
import platform.Foundation.appendBytes
import platform.Foundation.numberWithBool
import platform.Foundation.numberWithInt
import platform.ImageIO.CGImageSourceCreateThumbnailAtIndex
import platform.ImageIO.CGImageSourceCreateWithData
import platform.ImageIO.kCGImageSourceCreateThumbnailFromImageAlways
import platform.ImageIO.kCGImageSourceCreateThumbnailWithTransform
import platform.ImageIO.kCGImageSourceThumbnailMaxPixelSize
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePNGRepresentation
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
actual suspend fun ByteArray.downSamplingToImageBitmap(
    coroutineScope: CoroutineScope,
    reqHeight: Int,
    reqWidth: Int
): ImageBitmap? {
    val byteArray = this
    var imageBitmap: ImageBitmap? = null
    coroutineScope.launch(Dispatchers.Default) {
        runCatching {

            val cfData = byteArray.toCFData()
            val imageSource = CGImageSourceCreateWithData(cfData, null) ?: return@launch
            val maxDimension = maxOf(reqWidth, reqHeight)
            val options = CFDictionaryCreateMutable(null, 3, null, null)
            CFDictionaryAddValue(options, kCGImageSourceCreateThumbnailFromImageAlways, CFBridgingRetain(NSNumber.numberWithBool(true)))
            CFDictionaryAddValue(options, kCGImageSourceCreateThumbnailWithTransform, CFBridgingRetain(NSNumber.numberWithBool(true)))
            CFDictionaryAddValue(options, kCGImageSourceThumbnailMaxPixelSize, CFBridgingRetain(NSNumber.numberWithInt(maxDimension)))
            val cgImage = CGImageSourceCreateThumbnailAtIndex(imageSource, 0u, options) ?: return@launch
            val uiIMage = UIImage.imageWithCGImage(cgImage)

            CFRelease(options)
            CFRelease(imageSource)
            CFRelease(cfData)
            CFRelease(cgImage)

            val nsData = UIImagePNGRepresentation(uiIMage)?: return@launch
            val bytes = nsData.toByteArray()
            val skiaImage = Image.makeFromEncoded(bytes)
            skiaImage.toComposeImageBitmap()
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

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toCFData(): CFDataRef =
    CFDataCreate(null,
        toUByteArray().refTo(0),
        size.toLong())!!

@OptIn(ExperimentalForeignApi::class)
private fun CFDataRef.toByteArray(): ByteArray {
    val length = CFDataGetLength(this)
    return UByteArray(length.toInt()).apply {
        val range = CFRangeMake(0, length)
        CFDataGetBytes(this@toByteArray, range, refTo(0))
    }.toByteArray()
}
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
private fun ByteArray.toNSData(): NSData {
    val data = NSMutableData()
    if (isEmpty()) return data
    usePinned { data.appendBytes(it.addressOf(0), size.toULong()) }
    return data
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val bytes = ByteArray(length.toInt())
    bytes.usePinned {
        memcpy(it.addressOf(0), this.bytes, length)
    }
    return bytes
}
