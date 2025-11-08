package com.wannaverse.imageselector

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import kotlin.coroutines.resume

private var imageSelectorLauncher: ActivityResultLauncher<String>? = null
private var pendingContinuation: ((ImageData?) -> Unit)? = null
private var currentActivity: ComponentActivity? = null

fun setImageSelectorActivity(activity: ComponentActivity) {
    currentActivity = activity
}

fun ComponentActivity.registerImageSelectorLauncher() {
    imageSelectorLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            val activity = currentActivity ?: return@registerForActivityResult
            val result = uri?.let {
                try {
                    val bytes = activity.contentResolver.openInputStream(it)?.use { s -> s.readBytes() }
                    ImageData(
                        imageBitmap = bytes?.toImageBitmap()
                    )
                } catch (e: IOException) {
                    e.printStackTrace()
                    null
                }
            }
            pendingContinuation?.invoke(result)
            pendingContinuation = null
        }
}

actual class ImageSelector actual constructor() {
    actual suspend fun selectImage(): ImageData? = suspendCancellableCoroutine { continuation ->
        val launcher = imageSelectorLauncher
        if (launcher == null) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        pendingContinuation = { imageData ->
            continuation.resume(imageData)
        }

        launcher.launch("image/*")
    }
}