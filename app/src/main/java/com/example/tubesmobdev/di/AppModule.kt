package com.example.tubesmobdev.di

import android.content.Context
import androidx.annotation.OptIn
import androidx.room.Room
import com.example.tubesmobdev.data.local.dao.ListeningRecordDao
import com.example.tubesmobdev.data.local.dao.SongDao
import com.example.tubesmobdev.data.local.database.SongDatabase
import com.example.tubesmobdev.data.local.preferences.AuthPreferences
import com.example.tubesmobdev.data.local.preferences.IAuthPreferences
import com.example.tubesmobdev.data.local.preferences.IPlayerPreferences
import com.example.tubesmobdev.data.local.preferences.IServicePreferences
import com.example.tubesmobdev.data.local.preferences.PlayerPreferences
import com.example.tubesmobdev.data.local.preferences.ServicePreferences
import com.example.tubesmobdev.data.remote.api.AuthApi
import com.example.tubesmobdev.data.remote.api.ProfileApi
import com.example.tubesmobdev.data.remote.api.SongApi
import com.example.tubesmobdev.data.remote.interceptor.AuthInterceptor
import com.example.tubesmobdev.data.repository.AuthRepository
import com.example.tubesmobdev.data.repository.ProfileRepository
import com.example.tubesmobdev.data.repository.IAuthRepository
import com.example.tubesmobdev.data.repository.ListeningRecordRepository
import com.example.tubesmobdev.data.repository.OnlineSongRepository
import com.example.tubesmobdev.data.repository.SongRepository
import com.example.tubesmobdev.manager.PlaybackConnection
import com.example.tubesmobdev.manager.PlayerManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.UnstableApi
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        logging: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(authInterceptor)
            .cache(null)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://34.101.226.132:3000/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun provideAuthPreferences(@ApplicationContext context: Context): IAuthPreferences {
        return AuthPreferences(context)
    }

    @Singleton
    @Provides
    fun providePlayerPreferences(@ApplicationContext context: Context): IPlayerPreferences {
        return PlayerPreferences(context)
    }

    @Singleton
    @Provides
    fun provideServicePreferences(@ApplicationContext context: Context): IServicePreferences {
        return ServicePreferences(context)
    }


    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }
    @Provides
    @Singleton
    fun provideProfileApi(retrofit: Retrofit): ProfileApi {
        return retrofit.create(ProfileApi::class.java)
    }
    @Singleton
    @Provides
    fun provideAuthRepository(
        authApi: AuthApi,
        authPreferences: IAuthPreferences,
        playerPreferences: IPlayerPreferences,
        @ApplicationContext context: Context
    ): IAuthRepository {
        return AuthRepository(authApi, context, authPreferences, playerPreferences)
    }
    @Provides
    @Singleton
    fun provideProfileRepository(profileApi: ProfileApi): ProfileRepository {
        return ProfileRepository(profileApi)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SongDatabase {
        return Room.databaseBuilder(context, SongDatabase::class.java, "songs.db").build()
    }

    @Provides
    @Singleton
    fun provideSongRepository(songDao: SongDao, authPreferences: IAuthPreferences): SongRepository {
        return SongRepository(songDao, authPreferences)
    }

    @Provides
    @Singleton
    fun provideSongDao(db: SongDatabase): SongDao {
        return db.songDao()
    }

    @Provides
    @Singleton
    fun provideListeningRecordRepository(
        dao: ListeningRecordDao,
        authPreferences: IAuthPreferences
    ): ListeningRecordRepository {
        return ListeningRecordRepository(dao, authPreferences)
    }

    @Provides
    @Singleton
    fun provideListeningRecordDao(db: SongDatabase): ListeningRecordDao {
        return db.listeningRecordDao()
    }

    @Provides
    @Singleton
    fun providePlayerManager(@ApplicationContext context: Context): PlayerManager {
        return PlayerManager(context)
    }

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun providePlaybackConnection(@ApplicationContext context: Context): PlaybackConnection {
        return PlaybackConnection(context)
    }


    @Provides
    @Singleton
    fun provideSongApi(retrofit: Retrofit): SongApi {
        return retrofit.create(SongApi::class.java)
    }

    @Provides
    @Singleton
    fun provideOnlineSongRepository(apiService: SongApi): OnlineSongRepository {
        return OnlineSongRepository(apiService)
    }
}