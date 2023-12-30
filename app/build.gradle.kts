plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.adrianbl.traductorml"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.adrianbl.traductorml"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // ML Kit Traductor
    implementation ("com.google.mlkit:translate:17.0.2")

    // ML Kit Identifica el idioma de un texto
    implementation ("com.google.mlkit:language-id:17.0.4")

    // ML Kit Reconocimiento de texto en imágenes
    // Reconocer la escritura latina
    implementation ("com.google.mlkit:text-recognition:16.0.0")

    // Reconocer la escritura china
    implementation ("com.google.mlkit:text-recognition-chinese:16.0.0")

    // Reconocer la escritura Devanagari
    implementation ("com.google.mlkit:text-recognition-devanagari:16.0.0")

    // Reconocer la escritura japonesa
    implementation ("com.google.mlkit:text-recognition-japanese:16.0.0")

    // Reconocer la escritura coreana
    implementation ("com.google.mlkit:text-recognition-korean:16.0.0")

    //Animaciones
    implementation ("com.airbnb.android:lottie:6.2.0")

    //Sweet Alert Dialog
    implementation ("com.github.f0ris.sweetalert:library:1.5.6")
}