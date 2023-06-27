package com.d0st.bhadoozindex

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App :Application() {

    override fun onCreate() {
        super.onCreate()
    }

    override fun onTerminate() {
        Log.wtf("Bhadoo","**** App Terminated ****")
        super.onTerminate()
    }

    override fun onTrimMemory(level: Int) {
        Log.wtf("Bhadoo","**** App onTrimMemory ****")
        super.onTrimMemory(level)
    }

    override fun onLowMemory() {
        Log.wtf("Bhadoo","**** App has Low Memory ****")
        super.onLowMemory()
    }

}