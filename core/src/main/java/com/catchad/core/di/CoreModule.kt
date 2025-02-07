package com.catchad.core.di

import com.catchad.core.BuildConfig
import com.catchad.core.data.local.datastore.DataStoreDataSource
import com.catchad.core.data.local.datastore.PreferencesDataStore
import com.catchad.core.data.local.room.AppDatabase
import com.catchad.core.data.local.room.RoomDataSource
import com.catchad.core.data.mapper.ContentMapper
import com.catchad.core.data.remote.ApiDataSource
import com.catchad.core.data.remote.ApiResponseAdapter
import com.catchad.core.data.remote.ApiService
import com.catchad.core.data.repository.ContentRepositoryImpl
import com.catchad.core.data.repository.DeviceRepositoryImpl
import com.catchad.core.domain.helpers.DeviceInfoHelper
import com.catchad.core.domain.repository.ContentRepository
import com.catchad.core.domain.repository.DeviceRepository
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

val dataModule = module {
    single { ContentMapper() }
    single { RoomDataSource(get()) }
    single { PreferencesDataStore(androidContext()) }
    single { DataStoreDataSource(get()) }
    single { ApiDataSource(get()) }
}

val helperModule = module {
    single { DeviceInfoHelper() }
}

val repositoryModule = module {
    single<ContentRepository> { ContentRepositoryImpl(get(), get()) }
    single<DeviceRepository> { DeviceRepositoryImpl(get(), get(), get()) }
}

val databaseModule = module {
    factory { get<AppDatabase>().contentDao() }
    single { AppDatabase.getInstance(androidContext()) }
}

val firebaseModule = module {
    single { Firebase.firestore }
}

val retrofitModule = module {
    single {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()
    }
    single {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(
                MoshiConverterFactory.create(
                    Moshi.Builder()
                        .add(ApiResponseAdapter())
                        .addLast(KotlinJsonAdapterFactory())
                        .build()
                )
            )
            .client(get())
            .build()
    }
    single { get<Retrofit>().create(ApiService::class.java) }
}