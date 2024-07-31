package com.catchad.core.di

import com.catchad.core.data.local.AppDatabase
import com.catchad.core.data.local.RoomDataSource
import com.catchad.core.data.mapper.ContentMapper
import com.catchad.core.data.repository.ContentRepositoryImpl
import com.catchad.core.domain.repository.ContentRepository
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    single { ContentMapper() }
    single { RoomDataSource(get()) }
}

val repositoryModule = module {
    single<ContentRepository> { ContentRepositoryImpl(get(), get()) }
}

val databaseModule = module {
    factory { get<AppDatabase>().contentDao() }
    single { AppDatabase.getInstance(androidContext()) }
}

val firebaseModule = module {
    single { Firebase.firestore }
}