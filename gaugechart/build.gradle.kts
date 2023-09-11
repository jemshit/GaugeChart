plugins {
    id("com.android.library")
    id("kotlin-android")
    id("maven-publish")
    id("org.jetbrains.dokka") version "1.9.0"
}

android {
    namespace = "com.jemshit.GaugeChart"
    compileSdk = 33

    defaultConfig {
        minSdk = 21
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    packaging {
        resources {
            excludes.add("META-INF/*.kotlin_module")
            excludes.add("META-INF/services/javax.annotation.processing.Processor")
            excludes.add("META-INF/LICENSE.md")
            excludes.add("META-INF/LICENSE-notice.md")
        }
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

configurations.all {
    resolutionStrategy {
        eachDependency {
            println(requested.module.toString())
            if (requested.module.toString().startsWith("androidx.emoji2:emoji2")) {
                useVersion("1.3.0")
            }
        }
    }
}

afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>("release") {
                groupId = "com.jemshit.GaugeChart"
                artifactId = "GaugeChart"
                version = "1.0.0"

                afterEvaluate {
                    from(components["release"])
                }
            }
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.0")
    dokkaJavadocPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:1.9.0")

    implementation("androidx.core:core-ktx:1.10.0")
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.runtime:runtime")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
}