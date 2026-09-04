package com.suprogramuota_visata.vedlys

import android.app.Application
import com.suprogramuota_visata.vedlys.utils.PlatformStorage

class VedlysApp : Application() {
    override fun onCreate() {
        super.onCreate()
        PlatformStorage.init(this)
    }
}
