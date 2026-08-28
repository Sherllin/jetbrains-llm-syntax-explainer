import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.gradle.api.JavaVersion

plugins {
    kotlin("jvm") version "1.9.24"
    id("org.jetbrains.intellij.platform") version "2.1.0"
}

group = "com.hesl.syntaxexplainer"
version = "0.1.1"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        val localIdePath = providers.gradleProperty("localIdePath").orNull
        if (localIdePath == null) {
            intellijIdeaCommunity("2024.2.6")
        } else {
            local(localIdePath)
        }
        instrumentationTools()
        testFramework(TestFrameworkType.Platform)
    }

    testImplementation(kotlin("test"))
    testRuntimeOnly("junit:junit:4.13.2")
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        if (providers.gradleProperty("localIdePath").isPresent) {
            freeCompilerArgs.add("-Xskip-metadata-version-check")
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

intellijPlatform {
    buildSearchableOptions = false
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "242"
        }
    }
}

tasks {
    test {
        useJUnitPlatform()
        if (providers.gradleProperty("localIdePath").isPresent) {
            doFirst {
                jvmArgumentProviders.removeAll {
                    it.javaClass.name.contains("IntelliJPlatformArgumentProvider")
                }
                systemProperties.remove("java.system.class.loader")
            }
        }
    }
}
