package com.catchad.app.di

import com.catchad.app.presentation.main.MainViewModel
import org.koin.dsl.module

val viewModelModule = module {
    single { MainViewModel(get()) }
}