/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
// fw-210927: adjusted for com.udacity.asteroidradar

package com.udacity.asteroidradar.repository

import android.annotation.SuppressLint
import androidx.lifecycle.*
import com.example.android.devbyteviewer.database.AsteroidsDatabase
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.BuildConfig
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.api.ApodApi
import com.udacity.asteroidradar.api.AsteroidsNeoWsApi
import com.udacity.asteroidradar.api.asDatabaseModel
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.database.DatabaseAsteroid
import com.udacity.asteroidradar.database.asDomainModel
import com.udacity.asteroidradar.main.NetApiStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Response
import timber.log.Timber
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

// initiate repo for our data
// 'dependency injection: DB passed via the constructor
class AsteroidsRepository(private val database: AsteroidsDatabase) {

    // fetch API key from build config parameter NASA_API_KEY, see: build.gradle (:app)
    private val API_KEY = BuildConfig.NASA_API_KEY


    // LiveData for storing the status of the most recent API request (to 'ApodApi')
    private val _statusApod = MutableLiveData<NetApiStatus>()
    val statusApod: LiveData<NetApiStatus>
        get() = _statusApod

    // LiveData for Astronomy Picture of the Day (APOD) to be displayed atop the asteroids list
    private val _apod= MutableLiveData<PictureOfDay?>()
    val apod: LiveData<PictureOfDay?>
        get() = _apod


    // LiveData for storing the status of the most recent API request (to 'AsteroidsNeoWsApi')
    private val _statusNeoWs = MutableLiveData<NetApiStatus>()
    val statusNeoWs: LiveData<NetApiStatus>
        get() = _statusNeoWs

    // LiveData for list of asteroids to be displayed
    private val _asteroids = MutableLiveData<List<Asteroid>>()
    val asteroids: LiveData<List<Asteroid>>
        get() = _asteroids


    // upon instantiating the repository class (from ViewModel)
    init {

        // fetch asteroid data from DB and use Transformation.map to turn the (LiveData)
        // List<DatabaseAsteroid> to its corresponding 'domain format', i. e. a (LiveData)
        // List<Asteroid>
        // ... see: https://proandroiddev.com/livedata-transformations-4f120ac046fc
        _asteroids.value = database.asteroidsDao.getAsteroids().map { it.asDomainModel() }.value

    }


    // updating asteroid data in DB (by accessing the NASA/NeoWs server - HTTP/GET)
    suspend fun refreshAsteroids() {

        // rolling week for data fetch from NASA server - delimited by today ... today + 7 days
        val upcomingWeekDates: ArrayList<String> = getNeoWsDownloadDates()
        val startDate = upcomingWeekDates[0]
        val endDate = upcomingWeekDates[1]

        // send GET request to server - coroutine to avoid blocking the UI thread
        withContext(Dispatchers.IO) {

            // new network data
            var netAsteroidData: List<DatabaseAsteroid>? = null

            // set initial status
            _statusNeoWs.value = NetApiStatus.LOADING

            // attempt to read data from server
            try{

                // initiate the (HTTP) GET request using the provided query parameters
                // (... the URL ends on '?start_date=<startDate.value>&end_date=<...>&...' )
                Timber.i("Sending GET request for NASA/NeoWs data from ${startDate} to ${endDate}")
                val response: Response<String> = AsteroidsNeoWsApi.retrofitServiceScalars
                    .getAsteroids(startDate, endDate, API_KEY)
                Timber.i("NeoWs GET request complete")

                // got data back?
                // ... see: https://johncodeos.com/how-to-parse-json-with-retrofit-converters-using-kotlin/
                if (response.isSuccessful) {
                    netAsteroidData =
                        parseAsteroidsJsonResult(JSONObject(response.body()!!)).asDatabaseModel()
                }

                // set status to keep UI updated
                _statusNeoWs.value = NetApiStatus.DONE

            } catch (e: Exception) {

                // something went wrong --> reset _asteroids list
                _asteroids.value = ArrayList()
                _statusNeoWs.value = NetApiStatus.ERROR

            }

            // store network data in DB
            //
            // DAO method 'insertAll' allows to be called with 'varargs'
            // --> convert to (typed) array and use 'spread operator' to turn to 'varargs'
            netAsteroidData?.let {
                database.asteroidsDao.insertAll(*it.toTypedArray())
            }

            // store saved data in LiveData (for ViewModel --> UI)
            _asteroids.value = netAsteroidData?.asDomainModel()

        }  // coroutine scope (IO)

    }  // refreshAsteroids()


    // updating Astronomy Picture of the Day (APOD) data in DB (by accessing the NASA/APOD server)
    suspend fun refreshPictureOfDay() {

        // send GET request to server - coroutine to avoid blocking the UI thread
        withContext(Dispatchers.IO) {

            // set initial status
            _statusApod.value = NetApiStatus.LOADING

            // attempt to read data from server
            try{
                // initiate the (HTTP) GET request
                Timber.i("Sending APOD GET request")
                val response: Response<PictureOfDay> = ApodApi.retrofitServiceMoshi
                    .getPictureOfDay(API_KEY)
                Timber.i("APOD GET request complete")

                // received anything useful?
                if (response.isSuccessful) {
                    response.body()?.let {
                        _apod.value = it
                    }
                }

                // set status to keep UI updated
                _statusApod.value = NetApiStatus.DONE

            } catch (e: Exception) {

                // something went wrong --> reset Picture of Day LiveData
                _apod.value = null
                _statusApod.value = NetApiStatus.ERROR

            }

// TODO - if APOD data needed in DB
// TODO - if APOD data needed in DB
// TODO - if APOD data needed in DB
//
//            // store network data in DB
//            //
//            // DAO method 'insertAll' allows to be called with 'varargs'
//            // --> convert to (typed) array and use 'spread operator' to turn to 'varargs'
//            netApodData?.let {
//                database.asteroidsDao.insertAll(*it.toTypedArray())
//            }
//
//            // store saved data in LiveData (for ViewModel --> UI)
//            _apod.value = netApodData?.asDomainModel()

        }  // coroutine scope (IO)

    }  // refreshPictureOfDay()


    // determine start_date, end_date for download from NASA/NeoWs
    @SuppressLint("WeekBasedYear")
    private fun getNeoWsDownloadDates(): ArrayList<String> {
        val formattedDateList = ArrayList<String>()

        val calendar = Calendar.getInstance()

        // start_date, end_date
        for (i in 1..2) {
            val currentTime = calendar.time
            val dateFormat = SimpleDateFormat(Constants.API_QUERY_DATE_FORMAT, Locale.getDefault())
            formattedDateList.add(dateFormat.format(currentTime))
            calendar.add(Calendar.DAY_OF_YEAR, Constants.DEFAULT_END_DATE_DAYS)
        }

        // array with 2 strings: start_date, end_date
        return formattedDateList

    }  // getNeoWsDownloadDates()

}

