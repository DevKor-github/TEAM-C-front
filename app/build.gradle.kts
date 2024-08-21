plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
}

android {
    namespace = "com.devkor.kodaero"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.devkor.kodaero"
        minSdk = 24
        targetSdk = 34
        versionCode = 2
        versionName = "0.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            applicationIdSuffix = ".release"
            resValue("string", "app_name", "고대로")
        }
        debug {
            applicationIdSuffix = ".debug"
            resValue("string", "app_name", "고대로 Debug")
        }
        create("nodemask") {
            applicationIdSuffix = ".nodemask"
            resValue("string", "app_name", "고대로 NodeMask")
        }
    }

    buildFeatures {
        dataBinding = true
        viewBinding = true
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
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("com.google.android.gms:play-services-location:21.2.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("com.google.ar.sceneform:filament-android:1.17.1")
    implementation ("com.google.code.gson:gson:2.8.6")
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation("androidx.compose.ui:ui-graphics-android:1.6.8")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("com.naver.maps:map-sdk:3.18.0")
    implementation("com.github.chrisbanes:PhotoView:2.3.0")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.google.android.flexbox:flexbox:3.0.0")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.3.1")
    implementation("com.caverock:androidsvg:1.4")
    implementation("com.github.bumptech.glide:glide:4.15.1")
    implementation("com.otaliastudios:zoomlayout:1.9.0")


}
