// App-level build configuration for Splitwise Mobile
plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.splitwise"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.splitwise"
        minSdk = 24  // Android 7.0 Nougat
        targetSdk = 34  // Android 14
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Enable ViewBinding for type-safe view access
    buildFeatures {
        viewBinding = true
    }

}

dependencies {
    // AndroidX Core Libraries
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    
    // Testing Libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Networking Libraries
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
}