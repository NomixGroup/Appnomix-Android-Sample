import groovy.json.JsonOutput
import groovy.json.JsonSlurper

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.appnomixsample"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.appnomixsample"
        minSdk = 21
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21

        // needed to enable compatibility with Java language features and APIs that are not natively supported on older versions of the Android platform
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "21"
    }
    buildFeatures {
        compose = true
    }

    sourceSets {
        getByName("main").res.srcDirs(
            "src/main/res",
            "${layout.buildDirectory.asFile.get()}/generated/res/appConfiguration"
        )
    }
}

tasks.register("generateConfigValues") {
    val jsonFile = File("$projectDir/app-configuration.json")
    val outputDir = File("${layout.buildDirectory.asFile.get()}/generated/res/appConfiguration/values")
    val outputXml = File(outputDir, "values.xml")

    doLast {
        if (!jsonFile.exists()) {
            throw GradleException("Config file not found: $jsonFile")
        }

        val jsonText = jsonFile.readText()
        val json = JsonSlurper().parseText(jsonText) as Map<String, Any>

        val credentials = json["credentials"] as Map<String, Any>

        outputDir.mkdirs()
        outputXml.writeText(
            """
            |<?xml version="1.0" encoding="utf-8"?>
            |<resources>
            |    <string name="client_id">${credentials["client_id"]}</string>
            |    <string name="auth_token">${credentials["auth_token"]}</string>
            |    <string name="onboarding_customization">${escapeJsonForXml(JsonOutput.toJson(json["onboarding_customization"]))}</string>
            |</resources>
            """.trimMargin()
        )
    }
}

fun escapeJsonForXml(json: String): String {
    return json
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "\\\"")
        .replace("'", "&apos;")
}

tasks.named("preBuild").configure {
    dependsOn("generateConfigValues")
}

dependencies {
    // Appnomix SDK dependency
    implementation(libs.appnomix.sdk)

    // needed to enable compatibility with Java language features and APIs that are not natively supported on older versions of the Android platform
    coreLibraryDesugaring(libs.android.desugaring)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}