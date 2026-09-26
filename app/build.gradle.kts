plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

val imagensDrawableGeradas = layout.buildDirectory.dir("generated/res/images/main").get().asFile
val sincronizarImagensDrawable by tasks.registering(Sync::class) {
    from("src/images") {
        include("**/*.png", "**/*.jpg", "**/*.jpeg", "**/*.webp")
        exclude("cadastro/**")
        exclude { detalhe -> detalhe.file.length() == 0L }
        eachFile {
            // O calendário do perfil é diferente do usado no cadastro.
            path = if (file.name == "logo.png") "logo_app.png"
                else if (file.name == "agenda.png" && file.parentFile.name == "icones") "agenda_perfil.png"
                else name.lowercase().replace("-", "_")
        }
        includeEmptyDirs = false
    }
    from("src/images/cadastro") {
        include("*.png")
        eachFile {
            path = name.lowercase().replace("-", "_")
        }
        includeEmptyDirs = false
    }
    into(imagensDrawableGeradas.resolve("drawable"))
}

android {
    namespace = "com.ada.chatmedico.demo"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.ada.chatmedico.demo"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }

    sourceSets {
        getByName("main") {
            manifest.srcFile("src/AndroidManifest.xml")
            res.directories.addAll(listOf("src/res", imagensDrawableGeradas.path))
            kotlin.directories.addAll(listOf("src/chat_medico", "src/telas"))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

}

tasks.named("preBuild") {
    dependsOn(sincronizarImagensDrawable)
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    val pacoteCompose = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(pacoteCompose)

    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.health.connect:connect-client:1.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("io.ktor:ktor-client-okhttp:3.0.3")
    implementation("io.ktor:ktor-client-content-negotiation:3.0.3")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.3")
}
