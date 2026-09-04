package com.suprogramuota_visata.vedlys.utils

import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

actual object PlatformFilePicker {
    actual fun pickFileToOpen(title: String, description: String, vararg extensions: String): File? {
        val chooser = JFileChooser().apply {
            dialogTitle = title
            if (extensions.isNotEmpty()) {
                fileFilter = FileNameExtensionFilter(description, *extensions)
            }
        }
        return if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            chooser.selectedFile
        } else {
            null
        }
    }

    actual fun pickFileToSave(title: String, defaultFileName: String, description: String, vararg extensions: String): File? {
        val chooser = JFileChooser().apply {
            dialogTitle = title
            selectedFile = File(defaultFileName)
            if (extensions.isNotEmpty()) {
                fileFilter = FileNameExtensionFilter(description, *extensions)
            }
        }
        return if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            chooser.selectedFile
        } else {
            null
        }
    }
}
