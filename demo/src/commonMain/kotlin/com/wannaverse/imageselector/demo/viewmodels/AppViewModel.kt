package com.wannaverse.imageselector.demo.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wannaverse.imageselector.ImageData
import com.wannaverse.imageselector.selectImage
import kotlinx.coroutines.launch

class AppViewModel : ViewModel() {
    val image = mutableStateOf<ImageData?>(null)
    var showLoadingState by mutableStateOf(false)
    fun chooseImage() = viewModelScope.launch {
        image.value = selectImage( loadingState = { showLoadingState = it } )
    }
}