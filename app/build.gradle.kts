plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    //id("com.chaquo.python")
}

android {
    namespace = "com.example.scaffoldsmart"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.scaffoldsmart"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        /*ndk {
            // On Apple silicon, you can omit x86_64.
            abiFilters += listOf("arm64-v8a", "x86_64")
        }*/

       /* chaquopy {
            defaultConfig {
                pip {
                    // A requirement specifier, with or without a version number:
                    install("firebase_admin")
                }
            }
        }*/
        // default Python source directory is src/main/python
        // else want to set of yours own
        /*sourceSets.getByName("main") {
            setRoot("some/other/main")
        }*/

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    // for email feature
    packaging {
        resources {
            excludes.add("META-INF/NOTICE.md")
            excludes.add("META-INF/LICENSE.md")
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Firebase
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.storage)

    // Fragment Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // UI
    implementation (libs.ssp.android)
    implementation (libs.sdp.android)
    implementation(libs.androidx.swiperefreshlayout)
    implementation (libs.circleimageview)
    implementation (libs.imagepicker)
    implementation (libs.glide)

    // Push Notification
    implementation (libs.onesignal)

    // define a BOM and its version
    implementation(platform(libs.okhttp.bom))
    // define any required OkHttp artifacts without version
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    // ViewModel
    implementation (libs.androidx.lifecycle.viewmodel.ktx)
    // LiveData
    implementation (libs.androidx.lifecycle.livedata.ktx)

    // Kotlin Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation (libs.androidx.lifecycle.runtime.ktx)

    // Android Mail
    implementation(libs.android.mail)
    implementation(libs.android.activation)

    // TensorFlow Lite
    /*implementation(libs.tensorflow.lite)
    implementation(libs.tensorflow.lite.task.vision)
    implementation(libs.tensorflow.lite.gpu)*/

}