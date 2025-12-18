plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.studkompas"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.studkompas"
        minSdk = 24
        targetSdk = 36
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("com.github.chrisbanes:PhotoView:2.0.0")
    implementation("com.google.code.gson:gson:2.10.1")

    testImplementation(libs.junit)
    testImplementation("org.mockito:mockito-core:4.11.0")

    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("io.appmetrica.analytics:analytics:7.14.0")
    // Для Android 12+ нужно добавить экспорт intent-filter
    implementation("androidx.work:work-runtime:2.7.0")
}