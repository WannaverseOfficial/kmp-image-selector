package com.wannaverse.imageselector

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSData
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.darwin.NSObject
import platform.posix.memcpy
import kotlin.coroutines.resume

private var activePickerDelegate: NSObject? = null
private var activePicker: UIImagePickerController? = null

actual suspend fun selectImage(): ImageData? = suspendCancellableCoroutine { continuation ->
    val picker = UIImagePickerController().apply {
        sourceType =
            UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
        allowsEditing = false
    }

    val delegate = object : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
        override fun imagePickerController(
            picker: UIImagePickerController,
            didFinishPickingMediaWithInfo: Map<Any?, *>
        ) {
            val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
            val data = image?.toJpegData(quality = 1.0)
            val bytes = data?.toByteArray()

            picker.dismissViewControllerAnimated(true) {
                clearActivePickerReferences()
                continuation.resume(bytes?.let { ImageData(bytes = it) })
            }
        }

        override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
            picker.dismissViewControllerAnimated(true) {
                clearActivePickerReferences()
                continuation.resume(null)
            }
        }
    }

    picker.delegate = delegate
    activePicker = picker
    activePickerDelegate = delegate

    val rootController = UIApplication.sharedApplication.keyWindow?.rootViewController
    if (rootController == null) {
        clearActivePickerReferences()
        continuation.resume(null)
        return@suspendCancellableCoroutine
    }

    rootController.presentViewController(picker, true, null)

    continuation.invokeOnCancellation {
        picker.dismissViewControllerAnimated(true, null)
        clearActivePickerReferences()
    }
}

private fun clearActivePickerReferences() {
    activePicker?.delegate = null
    activePicker = null
    activePickerDelegate = null
}

private fun UIImage.toJpegData(quality: Double = 1.0): NSData? {
    return UIImageJPEGRepresentation(this, quality)
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val bytes = ByteArray(this.length.toInt())
    memScoped {
        val rawPtr = bytes.refTo(0).getPointer(this)
        memcpy(rawPtr, this@toByteArray.bytes, this@toByteArray.length)
    }
    return bytes
}
