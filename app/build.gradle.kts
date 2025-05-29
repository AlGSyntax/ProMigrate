import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("androidx.navigation.safeargs.kotlin")// Kotlin Gradle Plugin [oai_citation:3‡kotlinlang.org](https://kotlinlang.org/docs/releases.html#:~:text=To%20upgrade%20your%20project%20to,file)
    id("com.google.gms.google-services")             // Google Services Gradle Plugin [oai_citation:4‡firebase.google.com](https://firebase.google.com/support/release-notes/android#:~:text=Performance%20Monitoring%20plugin%20%20com.google.firebase%3Aperf,appcheck)
    id("com.google.firebase.crashlytics")         // Firebase Crashlytics Gradle Plugin [oai_citation:5‡firebase.google.com](https://firebase.google.com/support/release-notes/android#:~:text=Crashlytics%20%20com.google.firebase%3Afirebase,dataconnect%2016.0.2)
    id("com.google.firebase.firebase-perf")        // Firebase Performance Gradle Plugin [oai_citation:6‡firebase.google.com](https://firebase.google.com/support/release-notes/android#:~:text=modeldownloader%2025,database%2021.0.0)
    id("com.google.devtools.ksp")           // Kotlin Symbol Processing (KSP) Plugin [oai_citation:7‡github.com](https://github.com/google/ksp#:~:text=google%2Fksp%3A%20Kotlin%20Symbol%20Processing%20API,No) [oai_citation:8‡mvnrepository.com](https://mvnrepository.com/artifact/com.google.devtools.ksp/com.google.devtools.ksp.gradle.plugin/2.1.21-2.0.1#:~:text=com.google.devtools.ksp.gradle.plugin%20%C2%BB%202.1.21,gle%2Fksp%20%C2%B7%20May%2014%2C%202025)
}

android {
    namespace = "com.example.promigrate"
    compileSdk = 35


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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}


dependencies {
    // --- AndroidX Core UI Libraries ---
    implementation("androidx.core:core-ktx:1.16.0")                     // AndroidX Core KTX [oai_citation:12‡developer.android.com](https://developer.android.com/jetpack/androidx/releases/core#:~:text=Core%20and%20Core)
    implementation("androidx.appcompat:appcompat:1.7.0")                // AndroidX AppCompat [oai_citation:13‡developer.android.com](https://developer.android.com/jetpack/androidx/releases/appcompat#:~:text=Version%201)
    implementation("com.google.android.material:material:1.12.0")       // Material Components für Android [oai_citation:14‡mvnrepository.com](https://mvnrepository.com/artifact/com.google.android.material/material#:~:text=1)
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")  // ConstraintLayout (Views) [oai_citation:15‡developer.android.com](https://developer.android.com/jetpack/androidx/releases/constraintlayout#:~:text=Version%202) [oai_citation:16‡developer.android.com](https://developer.android.com/jetpack/androidx/releases/constraintlayout#:~:text=February%2026%2C%202025)

    // --- Firebase und Google Play Services ---
    implementation("com.google.firebase:firebase-analytics:22.4.0")     // Firebase Analytics [oai_citation:17‡firebase.google.com](https://firebase.google.com/support/release-notes/android#:~:text=AdMob%20%20com.google.android.gms%3Aplay,playintegrity%2018.0.0) [oai_citation:18‡firebase.google.com](https://firebase.google.com/support/release-notes/android#:~:text=Analytics%20%20com.google.firebase%3Afirebase,api)
    implementation("com.google.firebase:firebase-auth:23.2.1")          // Firebase Authentication [oai_citation:19‡firebase.google.com](https://firebase.google.com/support/release-notes/android#:~:text=App%20Distribution%20plugin%20%20com.google.firebase%3Afirebase,firestore%2025.1.4%20%20108%20com.google.firebase%3Afirebase) [oai_citation:20‡firebase.google.com](https://firebase.google.com/support/release-notes/android#:~:text=Authentication%20%20com.google.firebase%3Afirebase,gradle%203.0.3)
    implementation("com.google.firebase:firebase-firestore:25.1.4")     // Firebase Firestore (Cloud Firestore) [oai_citation:21‡firebase.google.com](https://firebase.google.com/support/release-notes/android#:~:text=App%20Distribution%20plugin%20%20com.google.firebase%3Afirebase,firestore%2025.1.4%20%20108%20com.google.firebase%3Afirebase) [oai_citation:22‡firebase.google.com](https://firebase.google.com/support/release-notes/android#:~:text=Authentication%20%20com.google.firebase%3Afirebase,gradle%203.0.3)
    implementation("com.google.firebase:firebase-crashlytics:19.4.3")   // Firebase Crashlytics SDK [oai_citation:23‡firebase.google.com](https://firebase.google.com/support/release-notes/android#:~:text=Cloud%20Messaging%20%20com.google.firebase%3Afirebase,beta03) [oai_citation:24‡firebase.google.com](https://firebase.google.com/support/release-notes/android#:~:text=Crashlytics%20%20com.google.firebase%3Afirebase,dataconnect%2016.0.2)
    implementation("com.google.firebase:firebase-perf:21.0.5")          // Firebase Performance Monitoring SDK [oai_citation:25‡firebase.google.com](https://firebase.google.com/support/release-notes/android#:~:text=Firebase%20installations%20%20com.google.firebase%3Afirebase,config%2022.1.2) [oai_citation:26‡firebase.google.com](https://firebase.google.com/support/release-notes/android#:~:text=modeldownloader%2025,database%2021.0.0)
    implementation("com.google.firebase:firebase-storage:21.0.2")       // Firebase Cloud Storage [oai_citation:27‡firebase.google.com](https://firebase.google.com/support/release-notes/android#:~:text=Cloud%20Functions%20for%20Firebase%20Client,crashlytics%2019.4.3) [oai_citation:28‡firebase.google.com](https://firebase.google.com/support/release-notes/android#:~:text=Cloud%20Messaging%20%20com.google.firebase%3Afirebase,beta03)
    implementation("com.google.firebase:firebase-database:21.0.0")      // Firebase Realtime Database [oai_citation:29‡firebase.google.com](https://firebase.google.com/support/release-notes/android#:~:text=modeldownloader%2025,services%204.4.2) [oai_citation:30‡firebase.google.com](https://firebase.google.com/support/release-notes/android#:~:text=Performance%20Monitoring%20plugin%20%20com.google.firebase%3Aperf,appcheck)
    implementation("com.google.android.gms:play-services-auth:21.3.0")  // Google Sign-In (Play Services Auth) [oai_citation:31‡mvnrepository.com](https://mvnrepository.com/artifact/com.google.android.gms/play-services-auth#:~:text=21) [oai_citation:32‡mvnrepository.com](https://mvnrepository.com/artifact/com.google.android.gms/play-services-auth#:~:text=21)

    // --- Networking / HTTP Clients ---
    implementation("com.squareup.retrofit2:retrofit:3.0.0")                     // Retrofit HTTP-Client (Typ-sicher) [oai_citation:33‡mvnrepository.com](https://mvnrepository.com/artifact/com.squareup.retrofit2/retrofit#:~:text=3) [oai_citation:34‡github.com](https://github.com/square/retrofit/releases#:~:text=,14)
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")               // Retrofit Converter für JSON (Gson) [oai_citation:35‡mvnrepository.com](https://mvnrepository.com/artifact/com.squareup.retrofit2/converter-gson#:~:text=3) [oai_citation:36‡mvnrepository.com](https://mvnrepository.com/artifact/com.squareup.retrofit2/converter-gson#:~:text=2)
    implementation("com.squareup.retrofit2:converter-moshi:3.0.0")              // Retrofit Converter für JSON (Moshi)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")                        // OkHttp3 HTTP-Client [oai_citation:37‡github.com](https://github.com/square/retrofit/releases#:~:text=,14) [oai_citation:38‡mvnrepository.com](https://mvnrepository.com/artifact/com.squareup.okhttp3/okhttp#:~:text=4)
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")           // OkHttp3 Logging-Interceptor (HTTP Logging) [oai_citation:39‡github.com](https://github.com/square/okhttp/blob/master/okhttp-logging-interceptor/README.md#:~:text=okhttp%2Fokhttp,interceptor%3A4.12.0%22%29) [oai_citation:40‡repo1.maven.org](https://repo1.maven.org/maven2/com/squareup/okhttp3/logging-interceptor/4.12.0/#:~:text=com%2Fsquareup%2Fokhttp3%2Flogging,javadoc.jar.asc)

    // --- JSON Parser ---
    implementation("com.squareup.moshi:moshi:1.15.2")                           // Moshi JSON-Bibliothek (Kotlin-freundlich) [oai_citation:41‡mvnrepository.com](https://mvnrepository.com/artifact/com.squareup.moshi/moshi-kotlin#:~:text=1) [oai_citation:42‡mvnrepository.com](https://mvnrepository.com/artifact/com.squareup.moshi/moshi-kotlin#:~:text=May%2012%2C%202023)
    implementation("com.squareup.moshi:moshi-kotlin:1.15.2")
    // Moshi Kotlin-Adapter (für Dataclasses)
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.2")                       // Moshi Codegen (Annotation Processing über KSP) [oai_citation:43‡github.com](https://github.com/square/moshi/blob/master/CHANGELOG.md#:~:text=moshi%2FCHANGELOG.md%20at%20master%20,faster%20and%20better%20supports) [oai_citation:44‡github.com](https://github.com/google/ksp#:~:text=google%2Fksp%3A%20Kotlin%20Symbol%20Processing%20API,No)

    // --- Image Loading Libraries ---
    implementation("io.coil-kt:coil:2.7.0")                                     // Coil Bildladebibliothek (Coroutine-basiert) [oai_citation:45‡mvnrepository.com](https://mvnrepository.com/artifact/io.coil-kt/coil#:~:text=2) [oai_citation:46‡mvnrepository.com](https://mvnrepository.com/artifact/io.coil-kt/coil#:~:text=2)
    implementation("com.github.bumptech.glide:glide:4.16.0")                    // Glide Bildladebibliothek (bewährt) [oai_citation:47‡stackoverflow.com](https://stackoverflow.com/questions/78531818/how-to-implement-glide-library-in-build-gradle-kts#:~:text=how%20to%20implement%20glide%20library,com.github.bumptech.glide%3Aglide%3A4.16.0) [oai_citation:48‡mvnrepository.com](https://mvnrepository.com/artifact/com.github.bumptech.glide/glide#:~:text=4)
    // (Optional) Wenn Glide mit Annotationen verwendet wird: Kapt-Compiler hinzufügen:
    // kapt("com.github.bumptech.glide:compiler:4.16.0")                        // Glide Annotation Processor (nur falls @GlideModule genutzt)

    // --- Jetpack Room (Database ORM) ---
    implementation("androidx.room:room-runtime:2.7.1")                          // Room Runtime [oai_citation:49‡developer.android.com](https://developer.android.com/jetpack/androidx/releases/room#:~:text=Kotlin) [oai_citation:50‡developer.android.com](https://developer.android.com/jetpack/androidx/releases/room#:~:text=dependencies%20,1)
    implementation("androidx.room:room-ktx:2.7.1")                               // Room Kotlin Extensions & Coroutines Support [oai_citation:51‡developer.android.com](https://developer.android.com/jetpack/androidx/releases/room#:~:text=%2F%2F%20optional%20,ktx%3A%24room_version) [oai_citation:52‡developer.android.com](https://developer.android.com/jetpack/androidx/releases/room#:~:text=,compiler%3A%24room_version)
    ksp("androidx.room:room-compiler:2.7.1")                                    // Room Annotation Processor via KSP [oai_citation:53‡developer.android.com](https://developer.android.com/jetpack/androidx/releases/room#:~:text=dependencies%20,1) [oai_citation:54‡developer.android.com](https://developer.android.com/jetpack/androidx/releases/room#:~:text=ksp%28%22androidx.room%3Aroom)

    // --- Jetpack Navigation (Fragment Navigation) ---
    implementation("androidx.navigation:navigation-fragment-ktx:2.9.0")         // Navigation Component (Fragment) [oai_citation:55‡developer.android.com](https://developer.android.com/jetpack/androidx/releases/navigation#:~:text=def%20nav_version%20%3D%20) [oai_citation:56‡developer.android.com](https://developer.android.com/jetpack/androidx/releases/navigation#:~:text=%2F%2F%20Jetpack%20Compose%20Integration%20implementation,compose%3A%24nav_version)
    implementation("androidx.navigation:navigation-ui-ktx:2.9.0")               // Navigation UI (ActionBar/NavUI) [oai_citation:57‡developer.android.com](https://developer.android.com/jetpack/androidx/releases/navigation#:~:text=%2F%2F%20Jetpack%20Compose%20Integration%20implementation,compose%3A%24nav_version)

    // --- Jetpack WorkManager (Background Tasks) ---
    implementation("androidx.work:work-runtime-ktx:2.10.1")                     // WorkManager (mit Kotlin Coroutines Unterstützung) [oai_citation:58‡mvnrepository.com](https://mvnrepository.com/artifact/androidx.work/work-runtime-ktx#:~:text=2) [oai_citation:59‡mvnrepository.com](https://mvnrepository.com/artifact/androidx.work/work-runtime-ktx#:~:text=2)

    // --- Kotlin Coroutines ---
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")      // Kotlin Coroutines Core (für Hintergrundarbeiten) [oai_citation:60‡github.com](https://github.com/Kotlin/kotlinx.coroutines/releases#:~:text=1) [oai_citation:61‡github.com](https://github.com/Kotlin/kotlinx.coroutines/releases#:~:text=%2A%20Fixed%20the%20%60kotlinx,4399)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")   // Kotlin Coroutines Android (Dispatchers.Main etc.)

    // --- Guava (Google Core Libraries für Java) ---
    implementation("com.google.guava:guava:33.4.8-android")                     // Guava Android-Version (Core Libraries) [oai_citation:62‡github.com](https://github.com/google/guava#:~:text=google%2Fguava%3A%20Google%20core%20libraries%20for,com.google.guava%3Aguava) [oai_citation:63‡mvnrepository.com](https://mvnrepository.com/artifact/com.google.guava/guava#:~:text=match%20at%20L124%2033.4.8)

    implementation("androidx.security:security-crypto:1.0.0")






}