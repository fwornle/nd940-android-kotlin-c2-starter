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
import com.example.android.devbyteviewer.database.AsteroidsDao
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.BuildConfig
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.api.ApodApi
import com.udacity.asteroidradar.api.AsteroidsNeoWsApi
import com.udacity.asteroidradar.api.asDatabaseModel
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.database.asDomainModel
import com.udacity.asteroidradar.main.NetApiStatus
import kotlinx.coroutines.*
import org.json.JSONObject
import retrofit2.Response
import timber.log.Timber
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

// initiate repo for our data
// 'dependency injection: DAO of DB passed via the constructor
class AsteroidsRepository(private val asteroidsDao: AsteroidsDao) {

    // fetch API key from build config parameter NASA_API_KEY, see: build.gradle (:app)
    private val API_KEY = BuildConfig.NASA_API_KEY


    // LiveData for Astronomy Picture of the Day (APOD) to be displayed atop the asteroids list
    private val _apod= MutableLiveData<PictureOfDay?>()
    val apod: LiveData<PictureOfDay?>
        get() = _apod

    // LiveData for storing the status of the most recent API request (to 'ApodApi')
    private val _statusApod = MutableLiveData<NetApiStatus>()
    val statusApod: LiveData<NetApiStatus>
        get() = _statusApod

    // LiveData for storing the status of the most recent API request (to 'AsteroidsNeoWsApi')
    private val _statusNeoWs = MutableLiveData<NetApiStatus>()
    val statusNeoWs: LiveData<NetApiStatus>
        get() = _statusNeoWs


    // define upcoming week
    private var upcomingWeekDates: ArrayList<String>
    private var dateToday: String
    private var dateNextWeek: String


    // upon instantiating the repository class (from ViewModel)
    init {

        // make sure all LiveData elements have defined values
        // ... omitting this, appears to cause an ('obscure') crash
        //     - presumably caused by Android calling the LD observer (to update the UI) and
        //       receiving invalid data (null)
        //     - possibly the crash happens in the BindingAdapter, when fetching statusNeoWs or
        //       statusApod (not sure)
        _statusNeoWs.value = NetApiStatus.DONE
        _statusApod.value = NetApiStatus.DONE
        _apod.value = null

        // rolling week for data fetch from NASA server - delimited by today ... today + 7 days
        upcomingWeekDates = getNeoWsDownloadDates()
        dateToday = upcomingWeekDates[0]    // today
        dateNextWeek = upcomingWeekDates[1]      // today + one week

    }


    // fetch different scopes of data from DB: all asteroids from today on
    fun fetchAsteroidsFromTodayOn(): LiveData<List<Asteroid>> {
        return asteroidsDao.getAsteroidsFromTodayOn(dateToday).map {
            //database.asteroidsDao.getAllAsteroids().map {
            it.asDomainModel()
        }
    }

    // fetch different scopes of data from DB: all asteroids approaching today
    fun fetchAsteroidsApproachingToday(): LiveData<List<Asteroid>> {
        return asteroidsDao.getAsteroidsApproachingToday(dateToday).map {
            //database.asteroidsDao.getAllAsteroids().map {
            it.asDomainModel()
        }
    }

    // fetch different scopes of data from DB: all asteroids stored in DB
    fun fetchAsteroidsAll(): LiveData<List<Asteroid>> {
        return asteroidsDao.getAllAsteroids().map {
            it.asDomainModel()
        }
    }

    // method to retrieve list of asteroids
    suspend fun refreshAsteroidsInDB() {

        // set initial status
        // ... use postValue when setting LiveData values from background threads, see:
        // https://stackoverflow.com/questions/51299641/difference-of-setvalue-postvalue-in-mutablelivedata
        _statusNeoWs.postValue(NetApiStatus.LOADING)

        // send GET request to server - coroutine to avoid blocking the main (UI) thread
        withContext(Dispatchers.IO) {

            // attempt to read data from server
            try{
                // initiate the (HTTP) GET request using the provided query parameters
                // (... the URL ends on '?start_date=<startDate.value>&end_date=<...>&...' )
                Timber.i("Sending GET request for NASA/NeoWs data from ${dateToday} to ${dateNextWeek}")
                val response: Response<String> = async {
                    AsteroidsNeoWsApi.retrofitServiceScalars
                        .getAsteroids(dateToday, dateNextWeek, API_KEY)
                }.await()

                // got any valid data back?
                // ... see: https://johncodeos.com/how-to-parse-json-with-retrofit-converters-using-kotlin/
                if (response.isSuccessful) {
                    Timber.i("NeoWs GET response received (parsing...)")

                    // new network data
                    val netAsteroidData =
                        parseAsteroidsJsonResult(JSONObject(response.body()!!)).asDatabaseModel()

                    // set status to keep UI updated
                    _statusNeoWs.postValue(NetApiStatus.DONE)
                    Timber.i("NeoWs GET request complete (success)")

                    // store network data in DB
                    //
                    // DAO method 'insertAll' allows to be called with 'varargs'
                    // --> convert to (typed) array and use 'spread operator' to turn to 'varargs'
                    asteroidsDao.insertAll(*netAsteroidData.toTypedArray())
                    Timber.i("NeoWs data stored in DB")

                }  // if(response.isSuccessful)

            } catch (e: Exception) {

                // something went wrong
                _statusNeoWs.postValue(NetApiStatus.ERROR)
                Timber.i("NeoWs GET request complete (failure)")
                Timber.i("Exception: ${e.message} // ${e.cause}")

            }

       }  // coroutine scope (IO)

    }  // refreshAsteroidsInDB()


    // updating Astronomy Picture of the Day (APOD) data in DB (by accessing the NASA/APOD server)
    suspend fun fetchPictureOfTheDay() {

        // send GET request to server - coroutine to avoid blocking the UI thread
        withContext(Dispatchers.IO) {

            // set initial status
            _statusApod.postValue(NetApiStatus.LOADING)

            // attempt to read data from server
            try{
                // initiate the (HTTP) GET request
                Timber.i("Sending GET request for NASA/APOD data")
                val response: Response<PictureOfDay> = async {
                    ApodApi.retrofitServiceMoshi.getPictureOfDay(API_KEY)
                }.await()

                // received anything useful?
                if (response.isSuccessful) {
                    response.body()?.let {
                        _apod.postValue(it)
                    }
                }

                // set status to keep UI updated
                _statusApod.postValue(NetApiStatus.DONE)
                Timber.i("APOD GET request complete (success)")

            } catch (e: Exception) {

                // something went wrong --> reset Picture of Day LiveData
                _apod.postValue(null)
                _statusApod.postValue(NetApiStatus.ERROR)
                Timber.i("APOD GET request complete (failure)")
                Timber.i("Exception: ${e.message} // ${e.cause}")

            }

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

