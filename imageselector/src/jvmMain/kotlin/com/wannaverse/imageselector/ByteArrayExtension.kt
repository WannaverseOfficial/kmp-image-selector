package com.wannaverse.imageselector

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.graphics.toComposeImageBitmap
import java.awt.Color
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import javax.imageio.ImageWriter
import javax.imageio.stream.MemoryCacheImageOutputStream

actual fun ByteArray.toImageBitmap(): ImageBitmap {
    val inputStream = ByteArrayInputStream(this)
    val bufferedImage: BufferedImage = ImageIO.read(inputStream)
    return bufferedImage.toComposeImageBitmap()
}

actual fun ImageBitmap.toByteArray(): ByteArray {
    val bufferedImage = this.toAwtImage()
    val outputStream = ByteArrayOutputStream()
    ImageIO.write(bufferedImage, "png", outputStream)
    return outputStream.toByteArray()
}

actual fun ByteArray.compressImage(options: ImageCompressionOptions): ByteArray {
    val sourceImage = ImageIO.read(ByteArrayInputStream(this)) ?: return this

    var quality = options.quality
    var compressed = sourceImage.encode(options.format, quality) ?: return this

    if (options.format == ImageCompressionFormat.JPEG) {
        val maxBytes = options.maxBytes
        while (maxBytes != null && compressed.size > maxBytes && quality > options.minQuality) {
            quality = maxOf(options.minQuality, quality - options.qualityStep)
            compressed = sourceImage.encode(options.format, quality) ?: break
            if (quality == options.minQuality) {
                break
            }
        }
    }

    return compressed
}

private fun BufferedImage.encode(format: ImageCompressionFormat, quality: Int): ByteArray? {
    return when (format) {
        ImageCompressionFormat.PNG -> ByteArrayOutputStream().use { output ->
            ImageIO.write(this, "png", output)
            output.toByteArray()
        }
        ImageCompressionFormat.JPEG -> encodeJpeg(quality)
    }
}

private fun BufferedImage.encodeJpeg(quality: Int): ByteArray? {
    val writer = ImageIO.getImageWritersByFormatName("jpeg").asSequence().firstOrNull() ?: return null
    return ByteArrayOutputStream().use { output ->
        MemoryCacheImageOutputStream(output).use { imageOutput ->
            writer.output = imageOutput
            writer.write(null, javax.imageio.IIOImage(asJpegCompatibleImage(), null, null), writer.jpegWriteParam(quality))
            writer.dispose()
        }
        output.toByteArray()
    }
}

private fun BufferedImage.asJpegCompatibleImage(): BufferedImage {
    if (type == BufferedImage.TYPE_INT_RGB) return this

    val converted = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    val graphics = converted.createGraphics()
    graphics.color = Color.WHITE
    graphics.fillRect(0, 0, width, height)
    graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
    graphics.drawImage(this, 0, 0, null)
    graphics.dispose()
    return converted
}

private fun ImageWriter.jpegWriteParam(quality: Int): ImageWriteParam =
    defaultWriteParam.apply {
        compressionMode = ImageWriteParam.MODE_EXPLICIT
        compressionQuality = quality.coerceIn(0, 100) / 100f
    }
