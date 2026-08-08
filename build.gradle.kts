import godot.gradle.GodotLanguage
import godot.annotation.processor.classgraph.AnnotationProcessingMode

plugins {
    id("com.utopia-rise.godot-kotlin-jvm") version "0.17.0-4.7.2"
}

repositories {
    mavenLocal()
    mavenCentral()
    google()
}

godot {
    languages.set(setOf(GodotLanguage.KOTLIN))
    annotationProcessingMode.set(AnnotationProcessingMode.Inferred)
    isGodotCoroutinesEnabled.set(true)
}

kotlin.sourceSets.main {
    kotlin.srcDirs("demo")
}
