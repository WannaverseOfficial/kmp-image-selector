package com.wannaverse.imageselector

/**
 * A container class representing a loaded image.
 *
 * Example usage:
 *
 * ```kotlin
 * val bitmap = image.value?.bytes?.toImageBitMap()
 * bitmap?.let {
 *     Image(
 *         bitmap = it,
 *         contentDescription = null
 *     )
 * }
 * ```
 *
 * @property bytes raw bytes read from the image
 */
data class ImageData(
    /** The raw bytes read from the file */
    val bytes: ByteArray?
)