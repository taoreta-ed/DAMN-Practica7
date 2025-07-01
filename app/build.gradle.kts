// build.gradle (app)

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services") // Plugin de Google Services
}

android {
    namespace = "com.example.damn_practica7"
    compileSdk = 36 // Actualizado a 36 según tu input

    defaultConfig {
        applicationId = "com.example.damn_practica7"
        minSdk = 24 // API 24 como solicitaste
        targetSdk = 36 // Actualizado a 36 según tu input
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
        sourceCompatibility = JavaVersion.VERSION_11 // Actualizado a 11 según tu input
        targetCompatibility = JavaVersion.VERSION_11 // Actualizado a 11 según tu input
    }
    kotlinOptions {
        jvmTarget = "11" // Actualizado a 11 según tu input
    }
    buildFeatures {
        viewBinding = true // Habilitar View Binding para facilitar la interacción con vistas
    }
}

dependencies {
    // Dependencias estándar de Android
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity-ktx:1.9.0") // Para ActivityResultLauncher
    implementation("androidx.fragment:fragment-ktx:1.8.0") // Para ActivityResultLauncher

    // Firebase BOM (Platform) - Gestiona las versiones de Firebase para consistencia
    implementation(platform("com.google.firebase:firebase-bom:33.1.0")) // Mantenemos la versión 33.1.0 para compatibilidad, aunque tu input tenía 33.16.0. Si tienes problemas, podemos ajustarla.

    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth-ktx")

    // Cloud Firestore
    implementation("com.google.firebase:firebase-firestore-ktx")

    // Cloud Storage
    implementation("com.google.firebase:firebase-storage-ktx")

    // Realtime Database
    implementation("com.google.firebase:firebase-database-ktx")

    // Firebase Cloud Messaging (FCM)
    implementation("com.google.firebase:firebase-messaging-ktx")

    // Firebase Analytics (añadido según tu input)
    implementation("com.google.firebase:firebase-analytics-ktx")

    // Glide para cargar imágenes (opcional pero muy útil para fotos de perfil)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // Pruebas
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
