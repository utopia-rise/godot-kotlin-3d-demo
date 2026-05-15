import godot.gradle.GodotLanguage
import godot.entrygenerator.settings.RegistrationFileLayoutMode

plugins {
    id("com.utopia-rise.godot-kotlin-jvm") version "0.16.0-4.6.2"
}

repositories {
    mavenLocal()
    mavenCentral()
    google()
}

godot {
    languages.set(setOf(GodotLanguage.KOTLIN))
    registrationFilesDirectory.set(projectDir.resolve("scripts"))
    registrationFilesLayoutMode.set(RegistrationFileLayoutMode.HIERARCHICAL)
    isGodotCoroutinesEnabled.set(true)
}

kotlin.sourceSets.main {
    kotlin.srcDirs("demo")
}
