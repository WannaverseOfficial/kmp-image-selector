<img alt="Wannaverse Logo" src="./assets/logo.png" width="288"/>

[![Build iOS](https://github.com/WannaverseOfficial/kmp-image-selector/actions/workflows/build-mac.yaml/badge.svg)](https://github.com/WannaverseOfficial/kmp-image-selector/actions)
[![Build Linux](https://github.com/WannaverseOfficial/kmp-image-selector/actions/workflows/build-linux.yaml/badge.svg)](https://github.com/WannaverseOfficial/kmp-image-selector/actions)

# Image Selector

A Kotlin Multiplatform image selection library for Android, iOS and Desktop.

## Features
* Selecting images on Android, iOS and Desktop
* Setting aspect ratios
* Image rotation

## ✅ Supported Platform

| Platform | Supported |
|:---------|:----------|
| Android  | ✔️        |
| iOS      | ✔️        |
| Desktop  | ✔️        |
| Web      | ❌️        |

## 🚀 Installation
See the releases section of this repository for the latest version.

```kotlin
implementation "com.wannaverse:imageselector:<version>"
```

## Usage

[KDocs](https://wannaverseofficial.github.io/kmp-image-selector/)

Below is a sample code that you may use.

When setting up the image selector on Android, you will need to add the following to your `MainActivity`'s `onCreate` function:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Add these to register the image selector
    setImageSelectorActivity(this)
    registerImageSelectorLauncher()
}
```

Example `ViewModel`:
```kotlin
class AppViewModel : ViewModel() {
    val image = mutableStateOf<ImageData?>(null)

    fun selectImage() = viewModelScope.launch {
        image.value = ImageSelector().selectImage()
    }
}
```

Example Composable:
```kotlin
@Composable
fun App(viewModel: AppViewModel = viewModel { AppViewModel() }) {
    val image = remember { viewModel.image }
    val bitmap = image.value?.bytes?.toImageBitmap()
    
    Column(modifier = Modifier
        .fillMaxSize()
        .safeContentPadding()
    ) {
        Button(onClick = {
            viewModel.selectImage()
        }) {
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
```

## 📄 License
MIT LICENSE. See [LICENSE](./LICENSE) for details.

## 🙌 Contributing
Pull requests and feature requests are welcome!
If you encounter any issues, feel free to open an issue.