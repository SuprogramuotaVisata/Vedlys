package com.suprogramuota_visata.vedlys.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Programos ekranai. Naudojama sealed class, kad ateityje
 * būtų lengva pridėti parametrų (pvz. taskId).
 */
sealed class Screen {
    data object Login : Screen()
    data object Register : Screen()
    data class Home(val isAdmin: Boolean = false) : Screen()
    data class Tasks(val isAdmin: Boolean) : Screen()
    data class TaskDetail(val taskId: String, val isAdmin: Boolean, val highlightDetailSeq: Int? = null) : Screen()
    data object Types : Screen()
    data object Notifications : Screen()
    data object AdminUsers : Screen()
    data class Settings(val isAdmin: Boolean = false) : Screen()
    data object Help : Screen()
    data object Seklys : Screen()
    data object Rastvedys : Screen()
    data object Planuoklis : Screen()
}

/**
 * Paprastas in-memory navigatorius su atminties stack'u.
 * Tinka šio dydžio aplikacijai – be papildomų priklausomybių.
 */
class AppNavigator(initial: Screen) {
    private val backStack = mutableListOf(initial)
    var current by mutableStateOf<Screen>(initial)
        private set

    fun navigateTo(screen: Screen) {
        backStack.add(screen)
        current = screen
    }

    /** Pakeičia pradinį ekraną – naudojama po sėkmingo prisijungimo. */
    fun replaceRoot(screen: Screen) {
        backStack.clear()
        backStack.add(screen)
        current = screen
    }

    fun back(): Boolean {
        if (backStack.size <= 1) return false
        backStack.removeAt(backStack.lastIndex)
        current = backStack.last()
        return true
    }

    /** Grįžta tiesiai į pagrindinį Home ekraną, išvalant dukterinius ekranus iš dėklo */
    fun popToHome(): Boolean {
        val homeIndex = backStack.indexOfLast { it is Screen.Home }
        if (homeIndex >= 0) {
            while (backStack.lastIndex > homeIndex) {
                backStack.removeAt(backStack.lastIndex)
            }
            current = backStack.last()
            return true
        }
        return false
    }
}
