plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.jogo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.jogo"
        minSdk = 29
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(project(":MathLibrary"))
    implementation(libs.core)
    implementation(libs.firebase.database)

    // Dependências de teste
    testImplementation("junit:junit:4.13.2")
    testImplementation(libs.junit.jupiter)
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.espresso:espresso-core:3.4.0")
    testImplementation("org.mockito:mockito-core:4.8.0")
    testImplementation(("org.mockito:mockito-inline:4.8.0"))
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testImplementation ("org.junit.jupiter:junit-jupiter-engine:5.9.0")
    testRuntimeOnly ("org.junit.platform:junit-platform-launcher:1.9.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.9.0")



    // Dependências do Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-firestore:24.8.1")
    implementation("com.google.firebase:firebase-common-ktx:20.2.0")
    implementation("com.google.firebase:firebase-firestore-ktx:24.1.1")
    implementation("androidx.core:core-ktx:1.6.0")
}