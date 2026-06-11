package com.wannaverse.imageselector

import androidx.compose.ui.unit.IntSize

/**
 * Opens the platform's image picker and returns the selected [ImageData], or `null`
 * if the user cancels the selection.
 */
expect suspend fun selectImage(reqResolution: IntSize, loadingState: (Boolean) -> Unit): ImageData?