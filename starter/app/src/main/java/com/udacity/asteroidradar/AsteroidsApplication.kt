package com.udacity.asteroidradar

import android.app.Application
import com.example.android.devbyteviewer.database.AsteroidsDatabase
import com.udacity.asteroidradar.repository.AsteroidsRepository
import timber.log.Timber

// app wide initializations / tasks
class AsteroidsApplication: Application() {

    // instantiate database (as singleton object - same lifecycle as app)
    // ... using 'by lazy' so the database and the repository are only created when they're needed
    //     rather than when the application starts
    // see: https://developer.android.com/codelabs/android-room-with-a-view-kotlin#12
    private val database by lazy { AsteroidsDatabase.getDatabase(this) }

    // instantiate repository
    val repository by lazy { AsteroidsRepository(database.asteroidsDao) }

    // other app-wide initializations, needed immediately --> on creation of the app
    override fun onCreate() {
        super.onCreate()

        // initialize Timber library (logging)
        Timber.plant(Timber.DebugTree())

    }

}