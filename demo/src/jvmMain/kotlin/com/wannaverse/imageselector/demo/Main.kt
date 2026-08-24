package com.wannaverse.imageselector.demo

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "country-selector",
    ) {
        App()
    }
}