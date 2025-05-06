plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.5")
    implementation("com.google.firebase:firebase-database:21.0.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    //glide
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    //gson
    implementation ("com.google.code.gson:gson:2.8.9")
    //firebase autg
    implementation("com.google.firebase:firebase-auth:23.0.0")
    //razorpay
    implementation ("com.razorpay:checkout:1.6.40")
    implementation ("androidx.recyclerview:recyclerview:1.2.1")
    implementation ("com.google.android.gms:play-services-auth:20.3.0")
    // Google Sign-In
    implementation ("com.google.android.gms:play-services-auth:20.0.1")
    // Firebase Bom (Bill of Materials) to keep dependencies up to date
    implementation ("com.google.firebase:firebase-bom:30.0.0")
}
