package io.github.kevinah95.delete_l06

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "DeleteL06",
    ) {
        App()
    }
}