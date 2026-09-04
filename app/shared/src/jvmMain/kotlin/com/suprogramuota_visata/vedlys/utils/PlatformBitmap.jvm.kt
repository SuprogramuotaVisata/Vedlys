package com.suprogramuota_visata.vedlys.utils

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.loadImageBitmap
import java.io.ByteArrayInputStream

actual fun decodeImageByteArray(bytes: ByteArray): ImageBitmap {
    return loadImageBitmap(ByteArrayInputStream(bytes))
}
