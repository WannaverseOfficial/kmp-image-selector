package com.wannaverse.imageselector

/**
 * Opens the platform's image picker and returns the selected image as [ByteArray], or `null`
 * if the user cancels the selection.
 */
expect suspend fun launchImagePicker(): ByteArray?
