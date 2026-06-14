package com.wannaverse.imageselector

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

private var imageSelectorLauncher: ActivityResultLauncher<String>? = null
private var pendingContinuation: ((ImageData?) -> Unit)? = null
private var currentActivity: ComponentActivity? = null
private lateinit  var imageLoadingState: ((Boolean) -> Unit)
private var reqImageResolution: WindowSize? = null

fun setImageSelectorActivity(activity: ComponentActivity) {
    currentActivity = activity
}

fun getAppContext(): Context {
    if(currentActivity == null) throw RuntimeException("add `setImageSelectorActivity(this)` in `MainActivity.kt` of your android module!")
    return currentActivity!!.applicationContext
}
fun ComponentActivity.registerImageSelectorLauncher() {
    imageSelectorLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null && reqImageResolution != null) {
                decodeSampledBitmapFromUri(this, uri, reqImageResolution!!)
            } else {
                pendingContinuation?.invoke(null)
                pendingContinuation = null
            }
        }
}

actual suspend fun selectImage(reqResolution: WindowSize, loadingState: (Boolean) -> Unit): ImageData? = suspendCancellableCoroutine { continuation ->

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
fun decodeSampledBitmapFromUri(context: Context, uri: Uri, reqResolution: WindowSize) = GlobalScope.launch(
    Dispatchers.Default) {
    imageLoadingState.invoke(true)
    var resultData: ImageData? = null

    try {
        context.contentResolver.openFileDescriptor(uri, "r")?.use { parcelFileDescriptor ->
            val fileDescriptor = parcelFileDescriptor.fileDescriptor

            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options)

            options.inSampleSize = calculateInSampleSize(options, reqResolution.width, reqResolution.height)
            options.inJustDecodeBounds = false

            val bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options)
            if (bitmap != null) {
                resultData = ImageData(bitmap.asImageBitmap().toByteArray())
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        pendingContinuation?.invoke(resultData)
        pendingContinuation = null
        imageLoadingState.invoke(false)
    }
}