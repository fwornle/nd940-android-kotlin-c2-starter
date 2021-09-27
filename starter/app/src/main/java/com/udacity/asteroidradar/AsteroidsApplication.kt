package com.udacity.asteroidradar

import android.app.Application
import timber.log.Timber

// app wide initializations / tasks
class AsteroidsApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        // initialize Timber library (logging)
        Timber.plant(Timber.DebugTree())

    }

}