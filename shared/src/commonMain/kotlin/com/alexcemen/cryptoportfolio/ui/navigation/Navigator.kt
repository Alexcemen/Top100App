package com.alexcemen.cryptoportfolio.ui.navigation

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateListOf

sealed interface Screen {
    data object Portfolio : Screen
    data object Settings : Screen
}

class Navigator {
    private val _stack = mutableStateListOf<Screen>(Screen.Portfolio)
    val stack: List<Screen> = _stack

    val current: Screen get() = _stack.last()

    fun navigate(screen: Screen) {
        _stack.add(screen)
    }

    fun back(): Boolean {
        if (_stack.size <= 1) return false
        _stack.removeLast()
        return true
    }
}

val LocalNavigator = compositionLocalOf<Navigator> { error("No navigator provided") }
