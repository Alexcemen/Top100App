package com.alexcemen.cryptoportfolio.platform

import platform.Foundation.NSUserDefaults

actual class SecureStorage {
    private val defaults = NSUserDefaults.standardUserDefaults

    actual fun getString(key: String, default: String): String {
        return defaults.stringForKey(key) ?: default
    }

    actual fun getInt(key: String, default: Int): Int {
        return if (defaults.objectForKey(key) != null) {
            defaults.integerForKey(key).toInt()
        } else {
            default
        }
    }

    actual fun putString(key: String, value: String) {
        defaults.setObject(value, forKey = key)
    }

    actual fun putInt(key: String, value: Int) {
        defaults.setInteger(value.toLong(), forKey = key)
    }
}
