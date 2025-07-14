package org.example.appbbmges.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.ui.graphics.vector.ImageVector

sealed interface Screen {

    data object Login : Screen

    sealed interface TabScreen : Screen {
        val tabName: String
        val tabIcon: ImageVector
    }

    data class Dashboard(val username: String? = null) : TabScreen {
        override val tabName: String = "DASHBOARD"
        override val tabIcon: ImageVector = Icons.Default.Dashboard
    }

    data class Franquicias(val username: String? = null) : TabScreen {
        override val tabName: String = "FRANQUICIAS"
        override val tabIcon: ImageVector = Icons.Default.Business
    }

    data class Usuarios(val username: String? = null) : TabScreen {
        override val tabName: String = "USUARIOS"
        override val tabIcon: ImageVector = Icons.Default.People
    }

    data class DisciplinasHorarios(val username: String? = null) : TabScreen {
        override val tabName: String = "DISCIPLINASHORARIOS"
        override val tabIcon: ImageVector = Icons.Default.DateRange
    }

    data class Productos(val username: String? = null) : TabScreen {
        override val tabName: String = "PRODUCTOS"
        override val tabIcon: ImageVector = Icons.Default.ShoppingBag
    }

    data class EventosPromociones(val username: String? = null) : TabScreen {
        override val tabName: String = "EVENTOSPROMOCIONES"
        override val tabIcon: ImageVector = Icons.Default.Event
    }
    data class Settings(val username: String? = null) : Screen {
    }
}