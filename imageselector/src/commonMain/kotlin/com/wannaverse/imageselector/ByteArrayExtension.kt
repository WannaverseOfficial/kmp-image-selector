package com.wannaverse.imageselector

import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.CoroutineScope

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

/**
 * Returns a bitmap with down sampling for the current screen size.
 *
 * This is an `expect` function; it's actual implementation is platform-specific.
 *
 * A coroutineScope is needed as this is a CPU intensive task and user may forget to use `Dispatchers.Default` leading to errors or app crash.
 * This method uses the coroutineScope with `Dispatchers.Default`.
 *
 * @param reqHeight = height of the screen on which the image will be displayed.
 * @param reqWidth = width of the screen on which the image will be displayed.
 * @throws RuntimeException it throws any exception caught while down sampling the image.
 */
expect suspend fun ByteArray.downSamplingToImageBitmap(coroutineScope: CoroutineScope, reqHeight: Int, reqWidth: Int): ImageBitmap?