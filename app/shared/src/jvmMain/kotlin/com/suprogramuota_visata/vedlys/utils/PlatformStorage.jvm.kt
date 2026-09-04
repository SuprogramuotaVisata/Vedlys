package com.suprogramuota_visata.vedlys.utils

import java.io.File

actual object PlatformStorage {
    actual fun getAppDataDir(): File {
        val userHome = System.getProperty("user.home") ?: "."
        val dir = File(userHome, ".vedlys")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }
}
