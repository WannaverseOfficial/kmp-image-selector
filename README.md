<img alt="Wannaverse Logo" src="./assets/logo.png" width="288"/>

[![Build iOS](https://github.com/WannaverseOfficial/kmp-image-selector/actions/workflows/build-mac.yaml/badge.svg)](https://github.com/WannaverseOfficial/kmp-image-selector/actions)
[![Build Linux](https://github.com/WannaverseOfficial/kmp-image-selector/actions/workflows/build-linux.yaml/badge.svg)](https://github.com/WannaverseOfficial/kmp-image-selector/actions)

# Image Selector

A Kotlin Multiplatform image selection library for Android, iOS and Desktop.

## Features
* Selecting images on Android, iOS and Desktop
* Setting aspect ratios
* Compressing images by quality or toward a target file size

## Supported Platform

| Platform | Supported |
|:---------|:----------|
| Android  | ✔️        |
| iOS      | ✔️        |
| Desktop  | ✔️        |
| Web      | ❌️        |

## Installation
See the releases section of this repository for the latest version.

To your `build.gradle` under `commonMain.dependencies` add:

```kotlin
implementation "com.wannaverse:imageselector:<version>"
```

Important this library uses native code. You will need the additional dependencies depending on the targets you are building for:

**Android** under `androidMain.dependencies`: `implementation("com.wannaverse:imageselector-android:")`

**iOS (ARM)** under `iosMain.dependencies`: `implementation("com.wannaverse:imageselector-iosarm64:")`

**iOS x64** under `iosMain.dependencies`: `implementation("com.wannaverse:imageselector-iosx64:")`

**jvm**: `implementation("com.wannaverse:imageselector-jvm:")`

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

    fun chooseImage() = viewModelScope.launch {
        image.value = selectImage()
    }
}
```

You can then compress the selected image when needed:

```kotlin
val compressed = image.value?.compress(
    ImageCompressionOptions(
        quality = 88
    )
)
```

Or target a maximum size, for example 5 MB:

```kotlin
val underFiveMb = image.value?.compressToMaxBytes(
    maxBytes = 5L * 1024L * 1024L
)
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
            viewModel.chooseImage()
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

## License
MIT LICENSE. See [LICENSE](./LICENSE) for details.

## Contributing
Pull requests and feature requests are welcome!
If you encounter any issues, feel free to open an issue.
