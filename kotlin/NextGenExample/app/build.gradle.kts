plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.plugin.compose") version "2.1.0"
}

android {
  namespace = "com.example.nextgenexample"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.example.nextgenexample"
    minSdk = 24
    multiDexEnabled = true
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  composeOptions { kotlinCompilerExtensionVersion = "1.5.1" }
  buildFeatures {
    viewBinding = true
    compose = true
  }
}

dependencies {
  implementation("androidx.appcompat:appcompat:1.7.0")
  implementation("androidx.core:core-ktx:1.16.0")
  implementation("androidx.constraintlayout:constraintlayout:2.2.0")
  implementation("androidx.lifecycle:lifecycle-process:2.8.7")
  implementation("androidx.navigation:navigation-fragment-ktx:2.8.6")
  implementation("androidx.navigation:navigation-ui-ktx:2.8.9")
  implementation("androidx.preference:preference-ktx:1.2.1")

  implementation("com.google.android.libraries.ads.mobile.sdk:ads-mobile-sdk:1.3.1")
  implementation("com.google.android.material:material:1.12.0")

  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

  // Jetpack Compose dependencies.
  implementation(platform("androidx.compose:compose-bom:2025.04.00"))
  implementation("androidx.compose.runtime:runtime-android")
  implementation("androidx.compose.foundation:foundation-layout-android:1.7.8")
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.ui:ui-graphics:1.7.8")
  implementation("androidx.compose.material3:material3")
  debugImplementation("androidx.compose.ui:ui-tooling")
}

configurations.configureEach {
  exclude(group = "com.google.android.gms", module = "play-services-ads")
  exclude(group = "com.google.android.gms", module = "play-services-ads-lite")
}
