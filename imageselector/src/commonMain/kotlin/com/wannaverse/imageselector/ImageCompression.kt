package com.wannaverse.imageselector

/**
 * Output format used during compression.
 */
enum class ImageCompressionFormat {
    JPEG,
    PNG
}

/**
 * Options that control image compression.
 *
 * JPEG is the default because it gives predictable file-size savings for photos.
 * PNG is supported as a lossless option, but size targeting becomes best-effort.
 */
data class ImageCompressionOptions(
    val format: ImageCompressionFormat = ImageCompressionFormat.JPEG,
    val quality: Int = 90,
    val maxBytes: Long? = null,
    val minQuality: Int = 55,
    val qualityStep: Int = 5
) {
    init {
        require(quality in 0..100) { "quality must be between 0 and 100." }
        require(minQuality in 0..100) { "minQuality must be between 0 and 100." }
        require(minQuality <= quality) { "minQuality must be less than or equal to quality." }
        require(qualityStep > 0) { "qualityStep must be greater than 0." }
        require(maxBytes == null || maxBytes > 0) { "maxBytes must be greater than 0 when provided." }
    }
}

/**
 * Returns a compressed copy of this [ImageData].
 */
fun ImageData.compress(
    options: ImageCompressionOptions = ImageCompressionOptions()
): ImageData = copy(
    bytes = bytes?.compressImage(options)
)

/**
 * Compresses this [ByteArray] toward the provided maximum size.
 */
fun ByteArray.compressToMaxBytes(
    maxBytes: Long,
    format: ImageCompressionFormat = ImageCompressionFormat.JPEG,
    quality: Int = 90,
    minQuality: Int = 55,
    qualityStep: Int = 5
): ByteArray = compressImage(
    ImageCompressionOptions(
        format = format,
        quality = quality,
        maxBytes = maxBytes,
        minQuality = minQuality,
        qualityStep = qualityStep
    )
)

/**
 * Compresses this [ImageData] toward the provided maximum size.
 */
fun ImageData.compressToMaxBytes(
    maxBytes: Long,
    format: ImageCompressionFormat = ImageCompressionFormat.JPEG,
    quality: Int = 90,
    minQuality: Int = 55,
    qualityStep: Int = 5
): ImageData = compress(
    ImageCompressionOptions(
        format = format,
        quality = quality,
        maxBytes = maxBytes,
        minQuality = minQuality,
        qualityStep = qualityStep
    )
)
