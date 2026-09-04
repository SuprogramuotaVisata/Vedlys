package com.suprogramuota_visata.vedlys.utils

import android.content.Context
import java.io.File

actual object PlatformStorage {
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
        val filesDir = appContext?.filesDir?.absolutePath
        if (filesDir != null) {
            try {
                System.setProperty("user.home", filesDir)
            } catch (_: Throwable) {}
        }
    }

    actual fun getAppDataDir(): File {
        val baseDir = appContext?.filesDir
            ?: File(System.getProperty("user.home")?.takeIf { it.isNotBlank() && it != "/" } ?: "/data/data/com.suprogramuota_visata.vedlys/files")
        val dir = File(baseDir, ".vedlys")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }
}
