import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.multiplatform.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.dokka)
    alias(libs.plugins.publishing)
}

group = "com.wannaverse"
version = "1.4.1"

kotlin {
    jvmToolchain(21)
    android {
        namespace = "com.wannaverse.imageselector"
        compileSdk = libs.versions.android.compileSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        androidResources {
            enable = true
        }
    }

    iosArm64()
    iosSimulatorArm64()
    jvm()
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(libs.compose.ui)
        }
        jvmMain.dependencies {
            implementation(libs.kotlinx.coroutines.swing)
        }
        iosMain.dependencies {
            implementation(libs.skiko)
        }
    }
}

compose.desktop {
    application {
        nativeDistributions {
            packageName = "com.wannaverse.imageselector"
            packageVersion = version.toString()
        }
    }
}

mavenPublishing {
    publishToMavenCentral()

    if (!project.hasProperty("skipSigning")) {
        signAllPublications()
    }

    coordinates(group.toString(), "imageselector", version.toString())

    pom {
        name = "Image Selector"
        description = "An image selection library for Kotlin Multiplatform"
        inceptionYear = "2025"
        url = "https://github.com/WannaverseOfficial/kmp-image-selector"
        licenses {
            license {
                name = "MIT License"
                url = "https://opensource.org/licenses/MIT"
                distribution = "https://opensource.org/licenses/MIT"
            }
        }
        developers {
            developer {
                id = "Wannaverse"
                name = "wannaverse"
                url = "https://github.com/WannaverseOfficial"
            }
        }
        scm {
            url = "https://github.com/WannaverseOfficial/kmp-image-selector"
            connection = "scm:git:git://github.com/WannaverseOfficial/kmp-image-selector.git"
            developerConnection = "scm:git:ssh://git@github.com/WannaverseOfficial/kmp-image-selector.git"
        }
    }
}

dokka {
    dokkaPublications.html {
        outputDirectory.set(file("${rootDir}/docs"))
    }
}
