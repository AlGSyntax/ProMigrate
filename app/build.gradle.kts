import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.devtools.ksp")version "1.9.20-1.0.14"
}

android {
    namespace = "com.example.promigrate"
    compileSdk = 34


    val localProperties = Properties().apply {
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            load(localPropertiesFile.inputStream())
        }
    }

    val deepLApiKey = localProperties.getProperty("DeepLApiKey") ?: ""



    defaultConfig {
        buildConfigField("String", "DEEP_L_API_KEY", "\"$deepLApiKey\"")
        applicationId = "com.example.promigrate"
        minSdk = 28
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    val retrofitVersion = "2.9.0"
    val roomVersion = "2.6.1"

    // Grundlegende AndroidX- und UI-Bibliotheken
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Firebase SDKs für verschiedene Dienste
    implementation("com.google.firebase:firebase-firestore:24.10.3")
    implementation("com.google.firebase:firebase-analytics:21.5.1")
    implementation("com.google.firebase:firebase-auth:22.3.1")
    implementation("com.google.firebase:firebase-database:20.3.1")
    implementation("com.google.firebase:firebase-storage:20.3.0")
    implementation("com.google.firebase:firebase-crashlytics:18.6.2")
    implementation("com.google.firebase:firebase-perf:20.5.2")

    // Testbibliotheken für Unit- und UI-Tests
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Navigation-Component-Bibliothek für vereinfachte Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    // Kotlin Coroutines für asynchrone Programmierung
    implementation( "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Retrofit und zugehörige Konverter für Netzwerkanfragen
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-moshi:$retrofitVersion")
    implementation ("com.squareup.retrofit2:converter-gson:$retrofitVersion")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")

    // Coil für bildbezogene Aufgaben
    implementation("io.coil-kt:coil:2.5.0")

    // Room für die Datenpersistenz auf lokaler Ebene für zukünftige Implementierungen
    implementation("androidx.room:room-runtime:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")

    // OkHttp3 und Logging-Interceptor für erweiterte HTTP-Anfragen und Logging
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")


    // Glide für Bildlade- und Caching-Aufgaben
    implementation ("com.github.bumptech.glide:glide:4.16.0")

    // Google Sign In für die Authentifizierung über Google-Konten
    implementation ("com.google.android.gms:play-services-auth:21.0.0")

    //WorkManager für Hintergrundaufgaben
    implementation ("androidx.work:work-runtime-ktx:2.9.0")

    // DeepL API Client für Übersetzungen
    implementation ("com.deepl.api:deepl-java:1.4.0")

    // Guava für zusätzliche Hilfsklassen und Methoden
    implementation("com.google.guava:guava:33.0.0-android")



}