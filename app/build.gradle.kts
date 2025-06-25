plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.replica.replicaisland"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.replica.replicaisland"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    implementation(libs.junit)
}
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
