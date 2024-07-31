package com.catchad.app

import android.app.Application
import com.catchad.app.di.viewModelModule
import com.catchad.core.di.dataModule
import com.catchad.core.di.databaseModule
import com.catchad.core.di.firebaseModule
import com.catchad.core.di.repositoryModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.logger.Level

class BLEExploreApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.NONE)
            androidContext(this@BLEExploreApplication)
            modules(
                dataModule,
                repositoryModule,
                viewModelModule,
                firebaseModule,
                databaseModule
            )
        }
    }
}