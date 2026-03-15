package com.alexcemen.cryptoportfolio.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.alexcemen.cryptoportfolio.data.db.AppDatabase
import com.alexcemen.cryptoportfolio.data.db.PortfolioDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE portfolio_table ADD COLUMN cmcId INTEGER")
    }
}

@Module
@InstallIn(ActivityRetainedComponent::class)
object DatabaseModule {

    @Provides
    @ActivityRetainedScoped
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "crypto_portfolio.db")
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    fun providePortfolioDao(db: AppDatabase): PortfolioDao = db.portfolioDao()
}
