package org.example.appbbmges

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.example.appbbmges.data.DatabaseDriverFactory

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "AppBBMGes",
    ) {
        App(DatabaseDriverFactory())
    }
}