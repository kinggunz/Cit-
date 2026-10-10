plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
    namespace = "com.gunzdev.app"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.gunzdev.app"; minSdk = 24; targetSdk = 34
        versionCode = 1; versionName = "1.0"
        buildConfigField("String", "BACKEND_URL", "\"${project.findProperty("BACKEND_URL")}\"")
    }
    signingConfigs { create("gdev") {
        storeFile = file("gdev.jks"); storePassword = "gdev123"; keyAlias = "gdev"; keyPassword = "gdev123" } }
    buildTypes {
        debug { signingConfig = signingConfigs.getByName("gdev") }
        release { signingConfig = signingConfigs.getByName("gdev"); isMinifyEnabled = false }
    }
    buildFeatures { compose = true; buildConfig = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.14" }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
}
