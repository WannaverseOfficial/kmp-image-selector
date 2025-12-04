package com.wannaverse.imageselector.demo

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.wannaverse.imageselector.registerImageSelectorLauncher
import com.wannaverse.imageselector.setImageSelectorActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        registerImageSelectorLauncher()
        setImageSelectorActivity(this)
        enableEdgeToEdge(SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT))
        setContent {
            App()
        }
    }
}