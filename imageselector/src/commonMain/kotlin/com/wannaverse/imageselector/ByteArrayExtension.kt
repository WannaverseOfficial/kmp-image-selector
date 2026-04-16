package com.wannaverse.imageselector

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Converts this [ByteArray] into an [ImageBitmap].
 *
 * This is an `expect` function; its actual implementation is platform-specific.
 */
expect fun ByteArray.toImageBitmap(): ImageBitmap

/**
 * Converts this [ImageBitmap] into a [ByteArray]
 *
 * This is an `expect` function; its actual implementation is platform-specific.
 */
expect fun ImageBitmap.toByteArray(): ByteArray

/**
 * Compresses this [ByteArray] if it contains a supported image.
 *
 * If compression cannot be applied, the original bytes are returned unchanged.
 */
expect fun ByteArray.compressImage(
    options: ImageCompressionOptions = ImageCompressionOptions()
): ByteArray
