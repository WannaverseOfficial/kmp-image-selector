package com.wannaverse.imageselector

import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

/**
 * Adjusts this [ImageBitmap] to a target aspect ratio.
 *
 * Example usage:
 *
 * ```kotlin
 * val image: ImageBitmap = // load image
 *
 * // Crop to square
 * val square = image.withAspectRatio(1f)
 *
 * // Crop to 16:9
 * val croppedWide = image.withAspectRatio(16f / 9f)
 *
 * // Scale to 16:9 with height = 1080px
 * val scaledWide = image.withAspectRatio(16f / 9f, scale = true, targetHeight = 1080)
 * ```
 *
 * @param aspectRatio The desired width/height ratio (e.g. 16f / 9f, 1f for square).
 * @param scale Whether to scale instead of crop. Default = false (crop).
 * @param targetHeight Optional target height if scaling (only used when [scale] = true).
 */
fun ImageBitmap.withAspectRatio(
    aspectRatio: Float,
    scale: Boolean = false,
    targetHeight: Int? = null
): ImageBitmap {
    val width = this.width
    val height = this.height
    val currentAspect = width.toFloat() / height.toFloat()

    // --- Scale Mode ---
    if (scale) {
        val outHeight = targetHeight ?: height
        val outWidth = (outHeight * aspectRatio).toInt()

        val scaled = ImageBitmap(outWidth, outHeight)
        val canvas = Canvas(scaled)
        canvas.drawImageRect(
            image = this,
            srcOffset = IntOffset.Zero,
            srcSize = IntSize(width, height),
            dstOffset = IntOffset.Zero,
            dstSize = IntSize(outWidth, outHeight),
            paint = Paint()
        )
        return scaled
    }

    // --- Crop Mode (default) ---
    val (cropWidth, cropHeight) = if (currentAspect > aspectRatio) {
        val newWidth = (height * aspectRatio).toInt()
        newWidth to height
    } else {
        val newHeight = (width / aspectRatio).toInt()
        width to newHeight
    }

    val xOffset = (width - cropWidth) / 2
    val yOffset = (height - cropHeight) / 2

    val cropped = ImageBitmap(cropWidth, cropHeight)
    val canvas = Canvas(cropped)

    canvas.drawImageRect(
        image = this,
        srcOffset = IntOffset(xOffset, yOffset),
        srcSize = IntSize(cropWidth, cropHeight),
        dstOffset = IntOffset.Zero,
        dstSize = IntSize(cropWidth, cropHeight),
        paint = Paint()
    )
    return cropped
}

