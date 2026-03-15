package com.alexcemen.cryptoportfolio.platform

expect class SecureStorage {
    fun getString(key: String, default: String): String
    fun getInt(key: String, default: Int): Int
    fun putString(key: String, value: String)
    fun putInt(key: String, value: Int)
}
