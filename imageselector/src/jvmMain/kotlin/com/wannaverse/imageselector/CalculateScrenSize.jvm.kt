package com.wannaverse.imageselector

import java.awt.Window

actual fun getCurrentWindowSize(): WindowSize {
    val activeWindow = Window.getWindows().firstOrNull { it.isFocused }
        ?: Window.getWindows().firstOrNull() // Fallback to first available window

    return if (activeWindow != null) {
        WindowSize(
            width = activeWindow.width,
            height = activeWindow.height
        )
    } else {
        // Complete fallback to full screen if no window is rendered yet
        val screenSize = java.awt.Toolkit.getDefaultToolkit().screenSize
        WindowSize(width = screenSize.width, height = screenSize.height)
    }
}