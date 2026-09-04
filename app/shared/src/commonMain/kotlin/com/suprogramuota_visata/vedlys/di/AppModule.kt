package com.suprogramuota_visata.vedlys.di

import com.suprogramuota_visata.api.ApiSvClient
import org.koin.dsl.module
import java.io.File
import java.util.Properties

import com.suprogramuota_visata.vedlys.utils.PlatformStorage

val appModule = module {
    single {
        val appDataDir = PlatformStorage.getAppDataDir()

        val localPropertiesFile = File(appDataDir, "local.properties")
        var apiHost = "127.0.0.1"
        var apiPort = 8081
        var useHttps = true

        if (localPropertiesFile.exists()) {
            val props = Properties()
            props.load(localPropertiesFile.inputStream())
            apiHost = props.getProperty("API_HOST", apiHost)
            apiPort = props.getProperty("API_PORT", apiPort.toString()).toIntOrNull() ?: apiPort
            useHttps = props.getProperty("USE_HTTPS", "true").toBoolean()
        }

        val instanceIdPath = File(appDataDir, ".sv_instance_id").absolutePath
        
        ApiSvClient(
            host = apiHost,
            port = apiPort,
            useHttps = useHttps,
            instanceIdStoragePath = instanceIdPath
        )
    }
}
