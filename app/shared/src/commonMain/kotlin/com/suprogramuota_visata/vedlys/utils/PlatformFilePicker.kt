package com.suprogramuota_visata.vedlys.utils

import java.io.File

expect object PlatformFilePicker {
    fun pickFileToOpen(title: String, description: String, vararg extensions: String): File?
    fun pickFileToSave(title: String, defaultFileName: String, description: String, vararg extensions: String): File?
}
