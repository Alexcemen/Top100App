package com.alexcemen.cryptoportfolio.di

import android.content.Context
import androidx.room.Room
import com.alexcemen.cryptoportfolio.data.db.AppDatabase
import com.alexcemen.cryptoportfolio.data.db.PortfolioDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
object DatabaseModule {

    @Provides
    @ActivityRetainedScoped
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "crypto_portfolio.db").build()

    @Provides
    fun providePortfolioDao(db: AppDatabase): PortfolioDao = db.portfolioDao()
}
