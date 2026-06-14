package com.wannaverse.imageselector

/**
 * Opens the platform's image picker and returns the selected [ImageData], or `null`
 * if the user cancels the selection.
 * The image isn't loaded in the memory in its actual size, it's downsampled according to the
 * current window size of the app and then loaded into memory as [ImageData] which is lower than the
 * actual size eliminating the possibility of `OutOfMemoryError`
 * @param loadingState a callback to pass true if image is processing and false if it has finished processing the image.
 */
expect suspend fun selectImage(loadingState: (Boolean) -> Unit = {}): ImageData?