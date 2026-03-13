package com.alexcemen.cryptoportfolio.ui.mvi

import androidx.navigation3.runtime.NavKey

abstract class AppNavKey : NavKey {
    open val type: String = ""
}
