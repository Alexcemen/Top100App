package com.alexcemen.cryptoportfolio.platform

actual object Logger {
    actual fun d(tag: String, message: String) {
        println("D/$tag: $message")
    }

    actual fun e(tag: String, message: String, throwable: Throwable?) {
        println("E/$tag: $message ${throwable?.stackTraceToString().orEmpty()}")
    }

    actual fun i(tag: String, message: String) {
        println("I/$tag: $message")
    }
}
