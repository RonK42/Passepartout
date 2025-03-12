plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.passepartout"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.passepartout"
        minSdk = 26
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
    buildFeatures {
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation (libs.firebase.ui.storage)
    implementation (libs.play.services.maps.v1810)
    implementation (libs.play.services.location)
    implementation (libs.glide.v4151)
    annotationProcessor (libs.compiler.v4151)
   // implementation(libs.gson)
    implementation (libs.circleimageview)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.firebase.storage)
    //annotationProcessor(libs.compiler)
    implementation(libs.glide)
    implementation(libs.firebase.database)
    implementation(platform(libs.firebase.bom))
    implementation(libs.play.services.auth)
    implementation(libs.google.firebase.auth)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.ui.auth)
    // implementation(libs.facebook.android.sdk)
    implementation(libs.firebase.analytics)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.maps)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
