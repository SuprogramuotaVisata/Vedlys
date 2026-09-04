package com.suprogramuota_visata.vedlys

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.suprogramuota_visata.api.ApiSvClient
import com.suprogramuota_visata.vedlys.navigation.AppNavigator
import com.suprogramuota_visata.vedlys.navigation.Screen
import com.suprogramuota_visata.vedlys.ui.theme.VedlysTheme
import com.suprogramuota_visata.vedlys.di.appModule
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.inject

fun main() = application {
    startKoin {
        modules(appModule)
    }

    val apiClient by inject<ApiSvClient>(ApiSvClient::class.java)
    val windowState = rememberWindowState(size = DpSize(1280.dp, 850.dp))

    Window(
        onCloseRequest = ::exitApplication,
        state = windowState,
        title = "Vedlys"
    ) {
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
