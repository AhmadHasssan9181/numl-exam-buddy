plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp") version "2.0.0-1.0.21"
    id("androidx.room") version "2.6.1" // Room plugin
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
    
    room {
        schemaDirectory("$projectDir/schemas")
    }

    packaging {
        resources {
            excludes += listOf(
                "META-INF/INDEX.LIST",
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/license.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/notice.txt",
                "META-INF/*.kotlin_module",
                "META-INF/AL2.0",
                "META-INF/LGPL2.1"
            )
        }
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
    implementation("androidx.navigation:navigation-compose:2.7.7")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("androidx.compose.material:material-icons-extended:1.5.4")

    // Google Drive API
    implementation("com.google.api-client:google-api-client-android:2.8.0")
    implementation("com.google.http-client:google-http-client:1.43.3")
    implementation("com.google.apis:google-api-services-drive:v3-rev20250511-2.0.0")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.11.0")
    implementation("com.google.android.gms:play-services-auth:20.4.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")// Room Database
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    implementation("androidx.room:room-common:$roomVersion")  // Added common module
    ksp("androidx.room:room-compiler:$roomVersion")  // Configured Room with KSP

    // Gemini API
    implementation("com.google.ai.client.generativeai:generativeai:0.2.1")

    // PDF parsing
    implementation("com.itextpdf:itext7-core:8.0.2")

    // AndroidX components
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // File utilities
    implementation("androidx.documentfile:documentfile:1.0.1")
}