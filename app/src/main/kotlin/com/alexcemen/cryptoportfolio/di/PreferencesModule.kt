package com.alexcemen.cryptoportfolio.di

import android.content.Context
import com.alexcemen.cryptoportfolio.data.prefs.SecurePreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
object PreferencesModule {

    @Provides
    @ActivityRetainedScoped
    fun provideSecurePreferences(@ApplicationContext context: Context): SecurePreferences =
        SecurePreferences(context)
}
