package com.mobile.tamatami

import android.app.Application
import com.mobile.tamatami.di.AppContainer

class TamatamiApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(applicationContext)
    }
}
