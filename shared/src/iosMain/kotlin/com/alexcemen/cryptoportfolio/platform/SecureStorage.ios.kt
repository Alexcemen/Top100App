package com.alexcemen.cryptoportfolio.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.CFTypeRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanTrue
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.SecItemUpdate
import platform.Security.errSecItemNotFound
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

private const val SERVICE_NAME = "com.alexcemen.cryptoportfolio"

@OptIn(ExperimentalForeignApi::class)
actual class SecureStorage {

    actual fun getString(key: String, default: String): String {
        return keychainGet(key) ?: default
    }

    actual fun getInt(key: String, default: Int): Int {
        return keychainGet(key)?.toIntOrNull() ?: default
    }

    actual fun putString(key: String, value: String) {
        keychainSet(key, value)
    }

    actual fun putInt(key: String, value: Int) {
        keychainSet(key, value.toString())
    }

    private fun keychainGet(key: String): String? = memScoped {
        val query = CFDictionaryCreateMutable(null, 5, null, null)
        CFDictionaryAddValue(query, kSecClass, kSecClassGenericPassword)
        CFDictionaryAddValue(query, kSecAttrService, CFBridgingRetain(SERVICE_NAME))
        CFDictionaryAddValue(query, kSecAttrAccount, CFBridgingRetain(key))
        CFDictionaryAddValue(query, kSecReturnData, kCFBooleanTrue)
        CFDictionaryAddValue(query, kSecMatchLimit, kSecMatchLimitOne)

        val result = alloc<CFTypeRefVar>()
        val status = SecItemCopyMatching(query, result.ptr)

        if (status == errSecSuccess) {
            val data = CFBridgingRelease(result.value) as? NSData ?: return null
            NSString.create(data = data, encoding = NSUTF8StringEncoding) as? String
        } else {
            null
        }
    }

    private fun keychainSet(key: String, value: String) = memScoped {
        val data = (value as NSString).dataUsingEncoding(NSUTF8StringEncoding) ?: return@memScoped

        // Try to update existing item first
        val query = CFDictionaryCreateMutable(null, 3, null, null)
        CFDictionaryAddValue(query, kSecClass, kSecClassGenericPassword)
        CFDictionaryAddValue(query, kSecAttrService, CFBridgingRetain(SERVICE_NAME))
        CFDictionaryAddValue(query, kSecAttrAccount, CFBridgingRetain(key))

        val update = CFDictionaryCreateMutable(null, 1, null, null)
        CFDictionaryAddValue(update, kSecValueData, CFBridgingRetain(data))

        val updateStatus = SecItemUpdate(query, update)
        if (updateStatus == errSecItemNotFound) {
            // Item doesn't exist, add it
            val addQuery = CFDictionaryCreateMutable(null, 4, null, null)
            CFDictionaryAddValue(addQuery, kSecClass, kSecClassGenericPassword)
            CFDictionaryAddValue(addQuery, kSecAttrService, CFBridgingRetain(SERVICE_NAME))
            CFDictionaryAddValue(addQuery, kSecAttrAccount, CFBridgingRetain(key))
            CFDictionaryAddValue(addQuery, kSecValueData, CFBridgingRetain(data))
            SecItemAdd(addQuery, null)
        }
    }
}
