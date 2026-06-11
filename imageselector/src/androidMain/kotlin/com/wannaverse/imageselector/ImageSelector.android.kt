package com.wannaverse.imageselector

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

private var imageSelectorLauncher: ActivityResultLauncher<String>? = null
private var pendingContinuation: ((ImageData?) -> Unit)? = null
private var currentActivity: ComponentActivity? = null
lateinit  var imageLoadingState: ((Boolean) -> Unit)
private var reqImageResolution: IntSize? = null
fun setImageSelectorActivity(activity: ComponentActivity) {
    currentActivity = activity
}

fun ComponentActivity.registerImageSelectorLauncher() {
    imageSelectorLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            runCatching {
                uri?.let {
                    reqImageResolution?.let {
                        decodeSampledBitmapFromUri(this, uri, it)
                    }
                }
            }.onFailure {
                throw RuntimeException(it.message)
            }
        }
    pendingContinuation = null
}

actual suspend fun selectImage(reqResolution: IntSize, loadingState: (Boolean) -> Unit): ImageData? = suspendCancellableCoroutine { continuation ->

    reqImageResolution = reqResolution
    val launcher = imageSelectorLauncher
    if (launcher == null) {
        continuation.resume(null)
        return@suspendCancellableCoroutine
    }

    pendingContinuation = { imageData ->
        continuation.resume(imageData)
    }

    imageLoadingState = { state ->
        loadingState(state)
    }
    launcher.launch("image/*")
}

@OptIn(DelicateCoroutinesApi::class)
fun decodeSampledBitmapFromUri(context: Context, uri: Uri, reqResolution: IntSize) = GlobalScope.launch(
    Dispatchers.Default) {
    imageLoadingState.invoke(true)
    // Open a ParcelFileDescriptor from the ContentResolver
    // "r" means read-only mode
    context.contentResolver.openFileDescriptor(uri, "r")?.use { parcelFileDescriptor ->
        val fileDescriptor = parcelFileDescriptor.fileDescriptor

        // Step 1: Decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options)

        // Step 2: Calculate the downsampling scale factor
        options.inSampleSize = calculateInSampleSize(options, reqResolution.width, reqResolution.height)

        // Step 3: Decode the actual bitmap using the calculated sample size
        options.inJustDecodeBounds = false

        // This decodes straight into memory at the exact lower size requested
        ImageData(BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options).asImageBitmap().toByteArray()).let {
            pendingContinuation?.invoke(it)
            pendingContinuation = null
        }
    }
    imageLoadingState.invoke(false)
}