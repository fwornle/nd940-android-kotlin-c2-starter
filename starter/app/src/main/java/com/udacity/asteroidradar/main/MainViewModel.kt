package com.udacity.asteroidradar.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.BuildConfig
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.api.ApodApi
import com.udacity.asteroidradar.api.AsteroidsNeoWsApi
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response
import timber.log.Timber
import java.lang.Exception


// network API status (to maintain a transparent user experience)
enum class NetApiStatus { LOADING, ERROR, DONE }

// ViewModel for MainFragment
class MainViewModel : ViewModel() {

    // fetch API key from build config parameter NASA_API_KEY, see: build.gradle (:app)
    private val API_KEY = BuildConfig.NASA_API_KEY

    // LiveData for storing the status of the most recent API request (to 'AsteroidsNeoWsApi')
    private val _statusNeoWs = MutableLiveData<NetApiStatus>()
    val statusNeoWs: LiveData<NetApiStatus>
        get() = _statusNeoWs

    // LiveData for storing the status of the most recent API request (to 'ApodApi')
    private val _statusApod = MutableLiveData<NetApiStatus>()
    val statusApod: LiveData<NetApiStatus>
        get() = _statusApod

    // LiveData for list of asteroids to be displayed
    private val _asteroids = MutableLiveData<ArrayList<Asteroid>>()
    val asteroids: LiveData<ArrayList<Asteroid>>
        get() = _asteroids

    // LiveData for Astronomy Picture of the Day (APOD) to be displayed atop the asteroids list
    private val _apod= MutableLiveData<PictureOfDay?>()
    val apod: LiveData<PictureOfDay?>
        get() = _apod

    // LiveData for navigation (implicitly by 'asteroid selected' or not)
    private val _navigateToAsteroidDetails = MutableLiveData<Asteroid?>()
    val navigateToAsteroidDetails: LiveData<Asteroid?>
        get() = _navigateToAsteroidDetails

    // setter function for when no asteroid has been selected (for display of it's details)
    // ... triggers navigation to 'details' fragment
    fun displayAsteroidDetails(daSelectedAsteroid: Asteroid) {
        _navigateToAsteroidDetails.value = daSelectedAsteroid
    }

    // (re-)setter function for when navigation has been completed
    fun displayAsteroidDetailsComplete() {
        _navigateToAsteroidDetails.value = null
    }

    init {
        // app starts on asteroids overview fragment (in state 'LOADING') --> not 'in navigation'
        _navigateToAsteroidDetails.value = null

        // fetch asteroids data - also initializes LiveData _status to LOADING
        val startDate = "2021-09-26"    // must be today or future (APP requirement)
        val endDate = "2021-10-01"      // must be within the next 7 days (API limitation)
        getAsteroids(startDate, endDate)

        // fetch Astronomy Picture of the Day (APOD)
        getPictureOfDay()

    }

    // method to retrieve list of asteroids
    private fun getAsteroids(
        startDate: String,
        endDate: String,
    ) {

        // send GET request to server - coroutine to avoid blocking the UI thread
        viewModelScope.launch {

            // set initial status
            _statusNeoWs.value = NetApiStatus.LOADING

            // attempt to read data from server
            try{
                // initiate the (HTTP) GET request using the provided query parameters
                // (... the URL ends on '?start_date=<startDate.value>&end_date=<...>&...' )
                Timber.i("Sending NewWs GET request")
                val response: Response<String> = AsteroidsNeoWsApi.retrofitServiceScalars
                    .getAsteroids(startDate, endDate, API_KEY)

                // got data back?
                // ... see: https://johncodeos.com/how-to-parse-json-with-retrofit-converters-using-kotlin/
                if (response.isSuccessful) {
                    _asteroids.value = parseAsteroidsJsonResult(JSONObject(response.body()!!))
                }

                // set status to keep UI updated
                _statusNeoWs.value = NetApiStatus.DONE

            } catch (e: Exception) {

                // something went wrong --> reset _asteroids list
                _asteroids.value = ArrayList()
                _statusNeoWs.value = NetApiStatus.ERROR

            }

        }  // viewModelScope (coroutine)

    }  // getAsteroids()


    // method to retrieve Astronomy Picture of the Day (APOD)
    private fun getPictureOfDay() {

        // send GET request to server - coroutine to avoid blocking the UI thread
        viewModelScope.launch {

            // set initial status
            _statusApod.value = NetApiStatus.LOADING

            // attempt to read data from server
            try{
                // initiate the (HTTP) GET request
                Timber.i("Sending APOD GET request")
                val response: Response<PictureOfDay> = ApodApi.retrofitServiceMoshi
                    .getPictureOfDay(API_KEY)

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

        }  // viewModelScope (coroutine)

    }  // getPictureOfDay()

}  // onViewCreated [MainFragment]