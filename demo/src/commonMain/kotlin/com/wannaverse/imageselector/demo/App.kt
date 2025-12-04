package com.wannaverse.imageselector.demo

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.wannaverse.imageselector.launchImagePicker
import com.wannaverse.imageselector.toImageBitmap
import kotlinx.coroutines.launch

@Composable
fun App() {
    val scope = rememberCoroutineScope()
    var imageData: ByteArray? by remember { mutableStateOf(null) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        imageData?.let {
            Image(
                bitmap = it.toImageBitmap(),
                contentDescription = "Selected Image"
            )
        }

        Button(
            onClick = {
                scope.launch { imageData = launchImagePicker() }
            }
        ) {
            Text("Select Image")
        }
    }
}