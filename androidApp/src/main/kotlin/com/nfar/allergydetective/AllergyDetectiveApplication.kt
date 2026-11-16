package com.nfar.allergydetective

import android.app.Application
import com.nfar.allergydetective.di.startApp

class AllergyDetectiveApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startApp(this)
    }
}
