package com.wannaverse.imageselector

data class WindowSize(val width: Int, val height: Int)

expect fun getCurrentWindowSize(): WindowSize