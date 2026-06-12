package com.wannaverse.imageselector

import androidx.compose.ui.unit.IntSize
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.autoreleasepool
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.darwin.NSObject
import kotlin.coroutines.resume

private var activePickerDelegate: NSObject? = null
private var activePicker: UIImagePickerController? = null

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual suspend fun selectImage(
    reqResolution: IntSize,
    loadingState: (Boolean) -> Unit
): ImageData? = suspendCancellableCoroutine { continuation ->
    val picker = UIImagePickerController().apply {
        sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
        allowsEditing = false
    }

    val delegate = object : NSObject(), UIImagePickerControllerDelegateProtocol,
        UINavigationControllerDelegateProtocol {
        override fun imagePickerController(
            picker: UIImagePickerController,
            didFinishPickingMediaWithInfo: Map<Any?, *>
        ) {
            loadingState(true)

            val originalImage = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage

            if (originalImage == null) {
                picker.dismissViewControllerAnimated(true) {
                    clearActivePickerReferences()
                    loadingState(false)
                    continuation.resume(null)
                }
                return
            }

            CoroutineScope(Dispatchers.Default).launch {
                var resultBytes: ByteArray? = null

                autoreleasepool {
                    val (srcWidth, srcHeight) = originalImage.size.useContents { width to height }

                    val scaleFactor = minOf(reqResolution.width.toDouble() / srcWidth, reqResolution.height.toDouble() / srcHeight)
                    val finalScale = if (scaleFactor < 1.0) scaleFactor else 1.0
                    val targetWidth = srcWidth * finalScale
                    val targetHeight = srcHeight * finalScale

                    val targetSize = platform.CoreGraphics.CGSizeMake(targetWidth, targetHeight)

                    platform.UIKit.UIGraphicsBeginImageContextWithOptions(targetSize, false, 1.0)
                    originalImage.drawInRect(platform.CoreGraphics.CGRectMake(0.0, 0.0, targetWidth, targetHeight))
                    val downsampledImage = platform.UIKit.UIGraphicsGetImageFromCurrentImageContext()
                    platform.UIKit.UIGraphicsEndImageContext()

                    if (downsampledImage != null) {
                        val nsData = platform.UIKit.UIImageJPEGRepresentation(downsampledImage, 0.85)
                        if (nsData != null) {
                            val byteArray = ByteArray(nsData.length.toInt())
                            if (byteArray.isNotEmpty()) {
                                byteArray.usePinned { pinned ->
                                    platform.posix.memcpy(pinned.addressOf(0), nsData.bytes, nsData.length)
                                }
                            }
                            resultBytes = byteArray
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    picker.dismissViewControllerAnimated(true) {
                        clearActivePickerReferences()
                        loadingState(false)

                        continuation.resume(resultBytes?.let { ImageData(bytes = it) })
                    }
                }
            }
        }
        override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
            picker.dismissViewControllerAnimated(true) {
                clearActivePickerReferences()
                loadingState(false)
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