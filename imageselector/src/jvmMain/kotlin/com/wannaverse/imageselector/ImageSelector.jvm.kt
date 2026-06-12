package com.wannaverse.imageselector

import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO
import javax.imageio.ImageReader
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

actual suspend fun selectImage(
    reqResolution: IntSize,
    loadingState: (Boolean) -> Unit
): ImageData? {

    val file = withContext(Dispatchers.Main) {
        val chooser = JFileChooser().apply {
            dialogTitle = "Select an Image"
            fileFilter = FileNameExtensionFilter("Image files", "png", "jpg", "jpeg", "gif", "bmp")
        }
        val result = chooser.showOpenDialog(null)
        if (result == JFileChooser.APPROVE_OPTION) chooser.selectedFile else null
    } ?: return null

    try {
        loadingState(true)

        return withContext(Dispatchers.IO) {
            decodeSampledBitmapFromFile(file, reqResolution.width, reqResolution.height)
        }
    } catch (e: Exception) {
        throw RuntimeException(e.message, e)
    } finally {
        loadingState(false)
    }
}

private suspend fun decodeSampledBitmapFromFile(file: File, reqWidth: Int, reqHeight: Int): ImageData? {
    if (!file.exists()) return null

    return withContext(Dispatchers.IO) {
        ImageIO.createImageInputStream(file)
    }?.use { input ->
        val readers = ImageIO.getImageReaders(input)
        if (!readers.hasNext()) return null

        val reader = readers.next() as ImageReader
        reader.input = input

        val srcWidth = reader.getWidth(0)
        val srcHeight = reader.getHeight(0)

        val sampleSize = calculateInSampleSize(srcWidth, srcHeight, reqWidth, reqHeight)

        val param = reader.defaultReadParam.apply {
            setSourceSubsampling(sampleSize, sampleSize, 0, 0)
        }

        val downsampledImage = reader.read(0, param)
        reader.dispose()

        ByteArrayOutputStream().use { outputStream ->
            val success = ImageIO.write(downsampledImage, "JPEG", outputStream)

            if (!success) {
                ImageIO.write(downsampledImage, "png", outputStream)
            }

            val bytes = outputStream.toByteArray()
            if (bytes.isEmpty()) null else ImageData(bytes)
        }
    }
}

fun calculateInSampleSize(srcWidth: Int, srcHeight: Int, reqWidth: Int, reqHeight: Int): Int {
    var inSampleSize = 1
    if (srcHeight > reqHeight || srcWidth > reqWidth) {
        val halfHeight = srcHeight / 2
        val halfWidth = srcWidth / 2
        while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}