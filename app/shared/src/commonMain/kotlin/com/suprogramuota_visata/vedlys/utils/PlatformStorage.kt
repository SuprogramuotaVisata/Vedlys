package com.suprogramuota_visata.vedlys.utils

import java.io.File

expect object PlatformStorage {
    fun getAppDataDir(): File
}
