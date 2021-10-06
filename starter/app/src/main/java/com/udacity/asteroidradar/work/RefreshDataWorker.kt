package com.udacity.asteroidradar.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.udacity.asteroidradar.AsteroidsApplication
import retrofit2.HttpException
import timber.log.Timber

// use WorkManager to do work - derived from CoroutineWorker, as we have async work to be done
class RefreshDataWorker(appContext: Context, params: WorkerParameters):
    CoroutineWorker(appContext, params) {

    // UUID for our work (to be scheduled by WorkManager)
    companion object {
        const val WORK_NAME = "AsteroidsDataWorker"
    }

    // define work to be done
    override suspend fun doWork(): Result {

        return try {

            // fetch asteroids data - also initializes LiveData _statusNeoWs to LOADING
            // ... received data is used to update the DB
            Timber.i("Running scheduled work (refreshAsteroidsInDB)")
            (applicationContext as AsteroidsApplication).repository.refreshAsteroidsInDB()

            // return 'success' - done
            Timber.i("Scheduled work (refreshAsteroidsInDB) run successfully")
            Result.success()

        } catch (e: HttpException) {

            // return 'failure' - retry
            Timber.i("Scheduled work (refreshAsteroidsInDB) could not be run - retrying")
            Result.retry()

        }

    }  // doWork()

}