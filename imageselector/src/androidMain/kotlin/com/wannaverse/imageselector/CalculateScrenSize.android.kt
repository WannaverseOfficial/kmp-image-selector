package com.wannaverse.imageselector

import android.content.Context
import android.os.Build
import android.view.WindowManager

actual fun getCurrentWindowSize(): WindowSize {
    val windowManager = getAppContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val metrics = windowManager.currentWindowMetrics
        val bounds = metrics.bounds
        WindowSize(width = bounds.width(), height = bounds.height())
    } else {
        val display = windowManager.defaultDisplay
        val point = android.graphics.Point()
        display.getSize(point)
        WindowSize(width = point.x, height = point.y)
    }
}