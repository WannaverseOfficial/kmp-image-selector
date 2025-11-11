package com.wannaverse.imageselector

/**
 * Opens the platform's image picker and returns the selected [ImageData], or `null`
 * if the user cancels the selection.
 */
expect suspend fun selectImage(): ImageData?
