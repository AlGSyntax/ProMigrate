buildscript {
    repositories {
        google()
        mavenCentral()

    }
    dependencies {
        classpath("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:1.9.22-1.0.17")
        classpath("com.google.gms:google-services:4.4.1")
        classpath ("com.google.firebase:firebase-crashlytics-gradle:2.9.9")
        classpath ("com.google.firebase:perf-plugin:1.4.2")
        classpath ("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.7")
    }
}

plugins {
    id("com.android.application") version "8.3.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.devtools.ksp") version "1.9.20-1.0.14" apply false
}
