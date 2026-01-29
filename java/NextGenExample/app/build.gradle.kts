plugins { alias(libs.plugins.androidApplication) }

android {
  namespace = "com.example.nextgenexample"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.example.nextgenexample"
    minSdk = 24
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
  buildFeatures { viewBinding = true }
}

dependencies {
  implementation(libs.ads.mobile.sdk)
  implementation(libs.appcompat)
  implementation(libs.constraintlayout)
  implementation(libs.lifecycle.process)
  implementation(libs.material)
  implementation(libs.navigation.fragment)
  implementation(libs.navigation.ui)
  implementation(libs.preference)
  implementation(libs.recyclerview)
}

configurations.configureEach {
  exclude(group = "com.google.android.gms", module = "play-services-ads")
  exclude(group = "com.google.android.gms", module = "play-services-ads-lite")
}
