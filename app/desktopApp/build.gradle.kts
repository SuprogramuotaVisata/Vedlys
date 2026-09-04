import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(project(":app:shared"))

    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.apisv)
    implementation(libs.koin.core)

    implementation(libs.compose.uiToolingPreview)
}

compose.desktop {
    application {
        mainClass = "com.suprogramuota_visata.vedlys.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Msi, TargetFormat.Dmg, TargetFormat.Deb)
            packageName = "Vedlys"
            packageVersion = "1.0.4"
            description = "Vedlys – ApiSv klientinė aplikacija"
            vendor = "Suprogramuota Visata"

            windows {
                menuGroup = "Vedlys"
                upgradeUuid = "9F8B3A2C-1C7E-4D2A-8E55-2AB4C1E3F8D9"
            }
        }
    }
}