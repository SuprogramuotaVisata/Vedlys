package com.suprogramuota_visata.vedlys

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.suprogramuota_visata.api.ApiSvClient
import com.suprogramuota_visata.vedlys.navigation.AppNavigator
import com.suprogramuota_visata.vedlys.navigation.Screen
import com.suprogramuota_visata.vedlys.ui.theme.VedlysTheme
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import com.suprogramuota_visata.vedlys.di.appModule

import com.suprogramuota_visata.vedlys.utils.PlatformStorage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        PlatformStorage.init(this)

        if (GlobalContext.getOrNull() == null) {
            startKoin {
                modules(appModule)
            }
        }

        setContent {
            val apiClient = remember {
                GlobalContext.get().get<ApiSvClient>()
            }
            val isDarkMode by AppSettings.isDarkMode.collectAsState()
            val fontSize by AppSettings.selectedFontSize.collectAsState()

            VedlysTheme(darkTheme = isDarkMode, fontSize = fontSize) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navigator = remember { AppNavigator(initial = Screen.Login) }
                    AppRoot(navigator = navigator, apiClient = apiClient)
                }
            }
        }
    }
}
