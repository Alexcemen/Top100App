package com.alexcemen.cryptoportfolio.di

import com.alexcemen.cryptoportfolio.data.network.CmcApiService
import com.alexcemen.cryptoportfolio.data.network.MexcApiService
import com.alexcemen.cryptoportfolio.data.network.MexcSigningInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named

@Module
@InstallIn(ActivityRetainedComponent::class)
object NetworkModule {

    @Provides
    @ActivityRetainedScoped
    @Named("mexc")
    fun provideMexcOkHttpClient(signingInterceptor: MexcSigningInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(signingInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

    @Provides
    @ActivityRetainedScoped
    @Named("cmc")
    fun provideCmcOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

    @Provides
    @ActivityRetainedScoped
    fun provideMexcApiService(@Named("mexc") client: OkHttpClient): MexcApiService =
        Retrofit.Builder()
            .baseUrl("https://api.mexc.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MexcApiService::class.java)

    @Provides
    @ActivityRetainedScoped
    fun provideCmcApiService(@Named("cmc") client: OkHttpClient): CmcApiService =
        Retrofit.Builder()
            .baseUrl("https://pro-api.coinmarketcap.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CmcApiService::class.java)
}
