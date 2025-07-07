// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.4.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.23" apply false
    // This line tells the entire project what KSP is and where to find it.
    id("com.google.devtools.ksp") version "1.9.23-1.0.20" apply false
}