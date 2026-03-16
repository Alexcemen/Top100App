package com.alexcemen.cryptoportfolio.platform

import androidx.room.Room
import androidx.room.RoomDatabase
import com.alexcemen.cryptoportfolio.data.db.AppDatabase
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

actual fun getDatabaseBuilder(context: PlatformContext): RoomDatabase.Builder<AppDatabase> {
    val dbFilePath = documentDirectory() + "/crypto_portfolio.db"
    return Room.databaseBuilder<AppDatabase>(name = dbFilePath)
}

private fun documentDirectory(): String {
    val paths = NSFileManager.defaultManager.URLsForDirectory(
        NSDocumentDirectory, NSUserDomainMask
    )
    return (paths.first() as NSURL).path!!
}
