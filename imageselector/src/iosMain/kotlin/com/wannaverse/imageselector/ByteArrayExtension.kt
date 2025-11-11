package com.wannaverse.imageselector

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Data
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

actual fun ByteArray.toImageBitmap(): ImageBitmap {
    return Image
        .makeFromEncoded(Data.makeFromBytes(this).bytes)
        .toComposeImageBitmap()
}

actual fun ImageBitmap.toByteArray(): ByteArray {
    val skiaImage = Image.makeFromBitmap(this.asSkiaBitmap())
    return skiaImage.encodeToData(EncodedImageFormat.PNG, 100)?.bytes ?: ByteArray(0)
}