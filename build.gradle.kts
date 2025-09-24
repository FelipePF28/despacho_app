plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.despachoapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.despachoapp"
        minSdk = 21
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
    // --- UI base
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // --- Firebase BOM (administra versiones de artefactos Firebase)
    //implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation(platform("com.google.firebase:firebase-bom:32.8.1"))

    // Autenticación (email/contraseña)
    implementation("com.google.firebase:firebase-auth")

    // Realtime Database
    implementation("com.google.firebase:firebase-database")

    // Ubicación (FusedLocationProviderClient)
    implementation("com.google.android.gms:play-services-location:21.3.0")
}
