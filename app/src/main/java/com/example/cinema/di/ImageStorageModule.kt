package com.example.cinema.di

import android.content.Context
import com.example.cinema.data.local.image.FavouriteImageStore
import com.example.cinema.data.local.image.InternalFavouriteImageStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton
import okhttp3.OkHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ImageDownloadClient

@Module
@InstallIn(SingletonComponent::class)
object ImageStorageModule {

    @Provides
    @Singleton
    @ImageDownloadClient
    fun provideImageDownloadClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideFavouriteImageStore(
        @ApplicationContext context: Context,
        @ImageDownloadClient client: OkHttpClient
    ): FavouriteImageStore {
        val directory = File(
            context.filesDir,
            "favourite_images"
        )

        return InternalFavouriteImageStore(
            directory = directory,
            client = client
        )
    }
}