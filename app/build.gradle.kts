plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // Temporarily disabled kapt due to Kotlin 2.0 compatibility issues
    // id("kotlin-kapt") // For annotation processing (Room, Moshi)
}

android {
    namespace = "com.mgb.mrfcmanager"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.mgb.mrfcmanager"
        minSdk = 25
        targetSdk = 36
        versionCode = 28
        versionName = "2.0.28"

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
    
    // Custom APK naming
    applicationVariants.all {
        outputs.all {
            val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            val versionName = defaultConfig.versionName
            val buildType = buildType.name
            output.outputFileName = "MGB-MRFC-Manager-v${versionName}-${buildType}.apk"
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
        buildConfig = true
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.6")

    // ViewModel (for future backend integration)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // SwipeRefreshLayout
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // Image loading
    implementation("io.coil-kt:coil:2.5.0")

    // CameraX for attendance photos
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")

    // Charts for compliance dashboard
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // ========== BACKEND INTEGRATION DEPENDENCIES ==========

    // Networking - Retrofit & OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // JSON Parsing - Moshi (using reflection instead of codegen to avoid kapt)
    implementation("com.squareup.moshi:moshi:1.15.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    // Temporarily disabled kapt codegen
    // kapt("com.squareup.moshi:moshi-kotlin-codegen:1.15.0")

    // Coroutines (for async operations)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // ViewModel & LiveData runtime
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // Room (local database for offline support)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    // Temporarily disabled kapt compiler
    // kapt("androidx.room:room-compiler:2.6.1")

    // DataStore (for preferences & token storage)
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Security (for encrypted token storage)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // WorkManager (for background sync)
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}