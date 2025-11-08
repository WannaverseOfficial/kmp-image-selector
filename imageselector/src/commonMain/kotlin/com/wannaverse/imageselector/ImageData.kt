package com.wannaverse.imageselector

import androidx.compose.ui.graphics.ImageBitmap

/**
 * A container class representing a loaded image.
 *
 * Example usage:
 *
 * ```kotlin
 * val bitmap = image.value?.imageBitmap
 * bitmap?.let {
 *     Image(
 *         bitmap = it,
 *         contentDescription = null
 *     )
 * }
 * ```
 *
 * On iOS, this will always be `null` because iOS does not have the same concept of a URI as Android/JVM.
 * @property imageBitmap which can be used to draw the image
 */
data class ImageData(
    /** An ImageBitmap used for drawing */
    val imageBitmap: ImageBitmap?
)