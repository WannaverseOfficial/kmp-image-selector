package com.wannaverse.imageselector

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

actual fun ByteArray.toImageBitmap(): ImageBitmap {
    val inputStream = ByteArrayInputStream(this)
    val bufferedImage: BufferedImage = ImageIO.read(inputStream)
    return bufferedImage.toComposeImageBitmap()
}