package com.wannaverse.imageselector

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

actual class ImageSelector {
    actual suspend fun selectImage(): ImageData? {
        return withContext(Dispatchers.IO) {
            try {
                val chooser = JFileChooser().apply {
                    dialogTitle = "Select an Image"
                    fileFilter = FileNameExtensionFilter("Image files", "png", "jpg", "jpeg", "gif", "bmp")
                }

                val result = chooser.showOpenDialog(null)
                if (result == JFileChooser.APPROVE_OPTION) {
                    val file = chooser.selectedFile
                    val bytes = file.readBytes()
                    ImageData(
                        imageBitmap = bytes.toImageBitmap()
                    )
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
