package com.udacity.asteroidradar

import android.app.Application
import androidx.work.*
import com.udacity.asteroidradar.database.AsteroidsDatabase
import com.udacity.asteroidradar.repository.AsteroidsRepository
import com.udacity.asteroidradar.work.RefreshDataWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit


// app wide initializations / tasks
class AsteroidsApplication: Application(), Configuration.Provider {

    // instantiate database (as singleton object - same lifecycle as app)
    // ... using 'by lazy' so the database and the repository are only created when they're needed
    //     rather than when the application starts
    // see: https://developer.android.com/codelabs/android-room-with-a-view-kotlin#12
    private val database by lazy { AsteroidsDatabase.getDatabase(this) }

    // instantiate repository
    val repository by lazy { AsteroidsRepository(database.asteroidsDao) }

    // add a coroutine scope to be used with WorkManger scheduled work
    private val applicationScope = CoroutineScope(Dispatchers.Default)

    // configure WorkManager to schedule some recurring work (daily update of asteroids DB)
    private fun delayedInit() = applicationScope.launch {
        setupRecurringWork()
    }

    // configure the actual work to be scheduled by the
    private fun setupRecurringWork() {

        // define some constraints und which the repeating request should be scheduled:
        // WIFI, charging
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresCharging(true)
            .build()

        // define configuration of WorkManager job: scheduling frequency, constraints (see above)
        val repeatingRequest = PeriodicWorkRequestBuilder<RefreshDataWorker>(
            20,
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        // register 'repeating request' with WorkManager for the specified 'work job'
        //
        // !!!!!!!!!!!!!!!!!!! NOTE:
        //
        // ... use 'ExistingPeriodicWorkPolicy.REPLACE' (at least for one run of the app) to set
        //     the constraints and the frequency of the work to be scheduled (otherwise it remains
        //     stuck on whatever was requested the first time the worker was registered - after
        //     [deleting + re-]installing the app)
        // ... subsequently (= after one run), the policy can be (/ should be?) changed to KEEP
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            RefreshDataWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            repeatingRequest
        )

    }

    // other app-wide initializations, needed immediately --> on creation of the app
    override fun onCreate() {
        super.onCreate()

        // initialize Timber library (logging)
        Timber.plant(Timber.DebugTree())

        // initialize WorkManager - running on coroutine scope 'applicationScope'
        delayedInit()

    }

    // Initialize WorkManager (needed after WM 2.6, see:
    // https://developer.android.com/topic/libraries/architecture/workmanager/advanced/custom-configuration#on-demand
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
    }

}

