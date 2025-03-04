package com.catchad.app.di

import com.catchad.app.presentation.main.MainActivity
import com.catchad.app.presentation.main.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    scope<MainActivity> { viewModel { MainViewModel(get()) } }
}