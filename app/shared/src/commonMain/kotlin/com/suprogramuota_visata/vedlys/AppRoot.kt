package com.suprogramuota_visata.vedlys

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.suprogramuota_visata.api.ApiSvClient
import com.suprogramuota_visata.vedlys.navigation.AppNavigator
import com.suprogramuota_visata.vedlys.navigation.Screen
import com.suprogramuota_visata.vedlys.ui.screens.HomeScreen
import com.suprogramuota_visata.vedlys.ui.screens.LoginScreen
import com.suprogramuota_visata.vedlys.ui.screens.NotificationsScreen
import com.suprogramuota_visata.vedlys.ui.screens.RegisterScreen
import com.suprogramuota_visata.vedlys.ui.screens.TaskDetailScreen
import com.suprogramuota_visata.vedlys.ui.screens.TasksScreen
import com.suprogramuota_visata.vedlys.ui.screens.TypesScreen
import com.suprogramuota_visata.vedlys.ui.screens.HelpScreen

@Composable
fun AppRoot(navigator: AppNavigator, apiClient: ApiSvClient) {
    val coroutineScope = rememberCoroutineScope()
    when (val screen = navigator.current) {
        Screen.Login -> LoginScreen(
            apiClient = apiClient,
            onLoginSuccess = { isAdmin ->
                coroutineScope.launch {
                    AppSettings.syncWithServer(apiClient)
                }
                navigator.replaceRoot(Screen.Home(isAdmin))
            },
            onGoToRegister = { navigator.navigateTo(Screen.Register) },
            onGoToSettings = { navigator.navigateTo(Screen.Settings(false)) }
        )
        Screen.Register -> RegisterScreen(
            apiClient = apiClient,
            onRegistered = { navigator.back() },
            onBack = { navigator.back() }
        )
        is Screen.Home -> HomeScreen(
            isAdmin = screen.isAdmin,
            onOpenTasks = { navigator.navigateTo(Screen.Tasks(screen.isAdmin)) },
            onOpenTypes = { navigator.navigateTo(Screen.Types) },
            onOpenNotifications = { navigator.navigateTo(Screen.Notifications) },
            onOpenSettings = { navigator.navigateTo(Screen.Settings(screen.isAdmin)) },
            onOpenAdmin = { navigator.navigateTo(Screen.AdminUsers) },
            onOpenHelp = { navigator.navigateTo(Screen.Help) },
            onOpenSeklys = { navigator.navigateTo(Screen.Seklys) },
            onOpenRastvedys = { navigator.navigateTo(Screen.Rastvedys) },
            onOpenPlanuoklis = { navigator.navigateTo(Screen.Planuoklis) },
            onLogout = {
                apiClient.authRepository.logout()
                navigator.replaceRoot(Screen.Login)
            }
        )
        is Screen.Tasks -> TasksScreen(
            apiClient = apiClient,
            isAdmin = screen.isAdmin,
            onOpenTask = { id -> navigator.navigateTo(Screen.TaskDetail(id, screen.isAdmin)) },
            onBack = { navigator.back() },
            onHome = { navigator.popToHome() }
        )
        is Screen.TaskDetail -> TaskDetailScreen(
            taskId = screen.taskId,
            apiClient = apiClient,
            isAdmin = screen.isAdmin,
            highlightDetailSeq = screen.highlightDetailSeq,
            onOpenTask = { id, seq -> navigator.navigateTo(Screen.TaskDetail(id, screen.isAdmin, seq)) },
            onBack = { navigator.back() },
            onHome = { navigator.popToHome() }
        )
        Screen.Types -> TypesScreen(
            apiClient = apiClient,
            onBack = { navigator.back() },
            onHome = { navigator.popToHome() }
        )
        Screen.Notifications -> NotificationsScreen(
            apiClient = apiClient,
            onBack = { navigator.back() },
            onHome = { navigator.popToHome() }
        )
        Screen.AdminUsers -> com.suprogramuota_visata.vedlys.ui.screens.AdminUsersScreen(
            apiClient = apiClient,
            onBack = { navigator.back() },
            onHome = { navigator.popToHome() }
        )
        is Screen.Settings -> com.suprogramuota_visata.vedlys.ui.screens.SettingsScreen(
            apiClient = apiClient,
            isAdmin = screen.isAdmin,
            onBack = { navigator.back() },
            onHome = { navigator.popToHome() }
        )
        Screen.Help -> HelpScreen(
            apiClient = apiClient,
            onBack = { navigator.back() },
            onHome = { navigator.popToHome() }
        )
        Screen.Seklys -> com.suprogramuota_visata.vedlys.ui.screens.SeklysScreen(
            apiClient = apiClient,
            onBack = { navigator.back() },
            onHome = { navigator.popToHome() }
        )
        Screen.Rastvedys -> com.suprogramuota_visata.vedlys.ui.screens.RastvedysScreen(
            apiClient = apiClient,
            onBack = { navigator.back() },
            onHome = { navigator.popToHome() }
        )
        Screen.Planuoklis -> com.suprogramuota_visata.vedlys.ui.screens.PlanuoklisScreen(
            apiClient = apiClient,
            onBack = { navigator.back() },
            onHome = { navigator.popToHome() }
        )
    }
}
