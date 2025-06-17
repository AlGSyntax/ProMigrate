// == Root build.gradle.kts (nur einmal, im Projektverzeichnis) ==
plugins {
    // Versions _einmal_ zentral deklarieren (+ "apply false"), dann
    // in Modul-Skripten einfach nur noch `id("…")` schreiben.
    id("com.android.application")         version "8.10.1" apply false
    id("org.jetbrains.kotlin.android")    version "2.1.21" apply false
    id("com.google.devtools.ksp")         version "2.1.21-2.0.1" apply false
    id("androidx.navigation.safeargs.kotlin") version "2.9.0" apply false
    id("com.google.gms.google-services")  version "4.4.2" apply false
    id("com.google.firebase.crashlytics") version "3.0.3" apply false
    id("com.google.firebase.firebase-perf") version "1.4.2" apply false
}


