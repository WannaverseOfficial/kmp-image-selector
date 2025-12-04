package com.wannaverse.imageselector

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import java.io.InputStream
import kotlin.coroutines.resume

private var imageSelectorLauncher: ActivityResultLauncher<String>? = null
private var onSelectCallback: ((ByteArray?) -> Unit)? = null

fun ComponentActivity.registerImageSelectorLauncher() {
    val activity = this
    imageSelectorLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            val result = uri?.let {
                try {
                    activity.contentResolver.openInputStream(it)?.use(InputStream::readBytes)
                } catch (e: IOException) {
                    e.printStackTrace()
                    null
                }
            }
            onSelectCallback?.invoke(result)
            onSelectCallback = null
        }
}

@OptIn(ExperimentalCoroutinesApi::class)
actual suspend fun launchImagePicker(): ByteArray? = suspendCancellableCoroutine { continuation ->
    imageSelectorLauncher ?: throw IllegalStateException(
        "Image selector launcher is not registered. Make sure to call registerImageSelectorLauncher() in your Activity before using selectImage()."
    )

    onSelectCallback = continuation::resume
    imageSelectorLauncher!!.launch("image/*")
}