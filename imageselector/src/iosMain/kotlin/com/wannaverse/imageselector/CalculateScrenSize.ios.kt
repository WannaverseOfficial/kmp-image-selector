package com.wannaverse.imageselector

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.UIKit.UIApplication
import platform.UIKit.UIScreen
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene

@OptIn(ExperimentalForeignApi::class)
actual fun getCurrentWindowSize(): WindowSize {
    val activeScene = UIApplication.sharedApplication.connectedScenes
        .filterIsInstance<UIWindowScene>()
        .firstOrNull()

    // 2. Extract the key window bounds from that active scene
    val windowBounds = activeScene?.windows?.filterIsInstance<UIWindow>()
        ?.firstOrNull { it.isKeyWindow() }?.bounds
        ?: UIScreen.mainScreen.bounds // Fallback to main screen if window isn't loaded yet

    val scale = UIScreen.mainScreen.scale

    val widthPx = (windowBounds.useContents { size.width } * scale).toInt()
    val heightPx = (windowBounds.useContents { size.height } * scale).toInt()

    return WindowSize(width = widthPx, height = heightPx)
}