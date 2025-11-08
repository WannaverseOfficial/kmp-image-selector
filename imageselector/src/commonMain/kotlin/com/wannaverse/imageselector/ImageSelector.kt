package com.wannaverse.imageselector

/**
 * A platform-agnostic image selector.
 *
 * This `expect` class defines a contract for selecting an image from the user's device.
 * The actual implementation is handled by platform-specific image picking logic
 */
expect class ImageSelector() {
    /**
     * Opens the platform's image picker and returns the selected [ImageData], or `null`
     * if the user cancels the selection.
     */
    suspend fun selectImage(): ImageData?
}