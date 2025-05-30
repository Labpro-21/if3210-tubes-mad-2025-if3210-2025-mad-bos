plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    kotlin("kapt")
}

android {
    namespace = "com.example.tubesmobdev"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.tubesmobdev"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isDebuggable = false
            isMinifyEnabled = true
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

    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.ui)

    implementation(libs.androidx.media)

    implementation(libs.androidx.work.runtime.ktx)
    implementation (libs.androidx.hilt.work)

    implementation(libs.androidx.material3.window.size)
    implementation(libs.androidx.palette)

    implementation(libs.glide)
    implementation(libs.androidx.material3.window.size.class1.android)
    implementation(libs.firebase.components)
    implementation(libs.core)
    implementation(libs.androidx.mediarouter)
    implementation(libs.play.services.location)
    implementation(libs.zxing.android.embedded)
    kapt(libs.compiler)
    implementation(libs.accompanist.permissions)
    implementation(libs.androidx.recyclerview)
    implementation("androidx.camera:camera-core:1.2.3")
    implementation("androidx.camera:camera-camera2:1.2.3")
    implementation("androidx.camera:camera-lifecycle:1.2.3")
    implementation("androidx.camera:camera-view:1.3.0-alpha07")
    implementation("com.google.mlkit:barcode-scanning:17.2.0")

    implementation(libs.coil.compose)
    implementation("com.google.accompanist:accompanist-permissions:0.31.1-alpha")
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.room.runtime)
    kapt (libs.androidx.room.compiler)
    annotationProcessor (libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)


    implementation(libs.androidx.core.splashscreen)

    implementation(libs.androidx.material.icons.extended)
    implementation(libs.material3)
    implementation(libs.androidx.navigation.navigation.compose)
    implementation(libs.androidx.hilt.hilt.navigation.compose)

    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)
    implementation(libs.retrofit)

    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)


    implementation(libs.androidx.datastore.preferences)
    implementation(libs.tink.android)

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
}

kapt {
    correctErrorTypes = true
}

hilt {
    enableAggregatingTask = true
}
