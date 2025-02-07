package com.catchad.app

import android.app.Application
import com.catchad.app.di.viewModelModule
import com.catchad.core.di.dataModule
import com.catchad.core.di.databaseModule
import com.catchad.core.di.firebaseModule
import com.catchad.core.di.helperModule
import com.catchad.core.di.repositoryModule
import com.catchad.core.di.retrofitModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.logger.Level

class CatchAdApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.NONE)
            androidContext(this@CatchAdApplication)
            modules(
                dataModule,
                helperModule,
                repositoryModule,
                viewModelModule,
                firebaseModule,
                databaseModule,
                retrofitModule
            )
        }
    }
}