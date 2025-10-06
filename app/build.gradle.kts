import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.kapt")
    id("jacoco")
}

android {
    namespace = "com.example.mixmate"
    compileSdk = 36

    val localProperties = Properties().apply {
        val file = rootProject.file("local.properties")
        if (file.exists()) file.inputStream().use { load(it) }
    }

    val apiKey: String = localProperties.getProperty("API_KEY") ?: ""

    defaultConfig {
        applicationId = "com.example.mixmate"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "API_KEY", "\"$apiKey\"")

        manifestPlaceholders["API_KEY"] = apiKey
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

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

jacoco {
    toolVersion = "0.8.11"
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    val fileFilter = listOf(
        "**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/Manifest*.*", "**/*Test*.*",
        "**/databinding/**", "**/androidx/**"
    )
    val debugTree = fileTree(layout.buildDirectory.dir("intermediates/javac/debug/classes")) { exclude(fileFilter) }
    val kotlinDebugTree = fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug")) { exclude(fileFilter) }
    classDirectories.setFrom(files(debugTree, kotlinDebugTree))
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(files(layout.buildDirectory.file("jacoco/testDebugUnitTest.exec")))
}

// Ensure coverage runs with check (optional)
tasks.named("check") { dependsOn("jacocoTestReport") }

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Retrofit + JSON
    implementation(libs.retrofit)
    implementation(libs.converterGson)

    // Room (Kotlin + coroutines)
    implementation(libs.roomRuntime)
    implementation(libs.roomKtx)
    kapt(libs.roomCompiler)

    // Lifecycle
    implementation(libs.lifecycleViewModel)
    implementation(libs.lifecycleRuntime)

    // Coroutines
    implementation(libs.coroutinesAndroid)

    // Glide for images
    implementation(libs.glide)
    kapt(libs.glideCompiler)

    testImplementation(libs.mockwebserver)
    testImplementation(libs.coroutinesTest)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.rules)
}
