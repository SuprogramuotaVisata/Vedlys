package com.suprogramuota_visata.vedlys.utils

import java.io.File

actual object PlatformFilePicker {
    actual fun pickFileToOpen(title: String, description: String, vararg extensions: String): File? {
        return null
    }

    actual fun pickFileToSave(title: String, defaultFileName: String, description: String, vararg extensions: String): File? {
        return null
    }
}
