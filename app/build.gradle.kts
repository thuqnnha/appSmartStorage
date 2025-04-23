plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.appsmartstorage"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.appsmartstorage"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.inappmessaging)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.mysql)
    implementation ("androidx.drawerlayout:drawerlayout:1.1.1")
    implementation ("com.google.android.material:material:1.5.0")
    //qr
    implementation ("com.journeyapps:zxing-android-embedded:4.3.0")
}