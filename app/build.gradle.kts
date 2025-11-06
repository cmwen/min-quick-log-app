plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.minandroidapp"
    compileSdk = 34

    defaultConfig {
        val fallbackVersionCode = providers.gradleProperty("APP_VERSION_CODE")
            .map(String::toInt)
            .getOrElse(1)
        val fallbackVersionName = providers.gradleProperty("APP_VERSION_NAME")
            .getOrElse("1.0.0")
        val suppliedVersionCode = project.findProperty("versionCode") as String?
        val suppliedVersionName = project.findProperty("versionName") as String?

        applicationId = "com.example.minandroidapp"
        minSdk = 24
        targetSdk = 34
        versionCode = suppliedVersionCode?.toIntOrNull() ?: fallbackVersionCode
        versionName = suppliedVersionName ?: fallbackVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
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
    }
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)

    testImplementation(libs.junit4)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
}
