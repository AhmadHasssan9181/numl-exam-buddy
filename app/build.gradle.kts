plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.noobdev.numlexambuddy"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.noobdev.numlexambuddy"
        minSdk = 24
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation ("androidx.compose.material:material-icons-extended:1.5.4")
    // Google Drive API
    implementation ("com.google.api-client:google-api-client-android:2.8.0")
    implementation ("com.google.http-client:google-http-client:1.43.3")
    implementation("com.google.apis:google-api-services-drive:v3-rev20250511-2.0.0")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.11.0")
    implementation("com.google.android.gms:play-services-auth:20.4.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    // Google Drive API
    implementation("com.google.api-client:google-api-client-android:2.8.0")
    implementation("com.google.http-client:google-http-client:1.43.3")

// Coroutines (if not already added)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
}