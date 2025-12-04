package com.wannaverse.imageselector.demo

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wannaverse.imageselector.demo.viewmodels.AppViewModel
import com.wannaverse.imageselector.toImageBitmap

@Composable
fun App(viewModel: AppViewModel = viewModel { AppViewModel() }) {
    val image = remember { viewModel.image }
    val bitmap = image.value?.bytes?.toImageBitmap()

    if(viewModel.showLoadingState) {
        Dialog(
            onDismissRequest = { /* Non-Dismissible */ }
        ) {
            CircularProgressIndicator()
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .safeContentPadding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { viewModel.chooseImage() }
        ) {
            Text(
                text = "Select Image"
            )
        }
        bitmap?.let {
            Image(
                bitmap = it,
                contentDescription = null
            )
        }
    }
}