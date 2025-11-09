package com.wannaverse.imageselector

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
 * @property bytes raw bytes read from the image
 */
data class ImageData(
    /** The raw bytes read from the file */
    val bytes: ByteArray?
)