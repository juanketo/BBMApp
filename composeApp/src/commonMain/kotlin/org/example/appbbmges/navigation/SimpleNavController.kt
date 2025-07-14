package org.example.appbbmges.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SimpleNavController(initialScreen: Screen) {
    private val _currentScreen = MutableStateFlow(initialScreen)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val backStack: MutableList<Screen> = mutableListOf()

    fun navigateTo(screen: Screen, clearBackStack: Boolean = false) {
        if (clearBackStack) {
            backStack.clear()
        } else {
            backStack.add(_currentScreen.value)
        }
        _currentScreen.update { screen }
    }

    fun navigateBack(): Boolean {
        return if (backStack.isNotEmpty()) {
            _currentScreen.update { backStack.last() }
            backStack.removeLast()
            true
        } else {
            println("No hay pantallas en el backStack")
            false
        }
    }

    fun navigateToRoute(route: String, clearBackStack: Boolean = false) {
        val screen = when (route) {
            "login" -> Screen.Login
            "dashboard" -> Screen.Dashboard()
            "franquicias" -> Screen.Franquicias()
            "usuarios" -> Screen.Usuarios()
            "disciplinas_horarios" -> Screen.DisciplinasHorarios()
            "productos" -> Screen.Productos()
            "eventos_promociones" -> Screen.EventosPromociones()
            "settings" -> Screen.Settings()
            else -> return
        }
        navigateTo(screen, clearBackStack)
    }

    fun canNavigateBack(): Boolean {
        return backStack.isNotEmpty()
    }


     //limpia todo el backstack y vuelve a la pantalla inicial

    fun navigateAndClearBackStack(screen: Screen) {
        backStack.clear()
        _currentScreen.update { screen }
    }
}