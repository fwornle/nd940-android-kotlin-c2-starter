package com.udacity.asteroidradar.main

import android.app.Application
import androidx.lifecycle.*
import com.example.android.devbyteviewer.database.getDatabase
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.repository.AsteroidsRepository


// network API status (to maintain a transparent user experience)
enum class NetApiStatus { LOADING, ERROR, DONE }

// ViewModel for MainFragment
// ... parameter 'application' is the application that this viewmodel is attached to
class MainViewModel(application: Application) : AndroidViewModel(application) {

    // create DB for local storage of Asteroid data
    private val database = getDatabase(application)

    // create repository for all data (net or DB)
    // pass on viewModelScope to be used as coroutine scope for the async operations of the repo
    val repo = AsteroidsRepository(database, viewModelScope)


    // fetch LiveData from repo (so that layouts/Views only depend on the ViewModel - encapsulation)
    // ... status of the most recent API request (to 'AsteroidsNeoWsApi')
    val statusNeoWs: LiveData<NetApiStatus>
        get() = repo.statusNeoWs

    // fetch LiveData from repo (so that layouts/Views only depend on the ViewModel - encapsulation)
    // ... list of asteroids to be displayed
    val asteroids: LiveData<List<Asteroid>>
        get() = repo.asteroids


    // fetch LiveData from repo (so that layouts/Views only depend on the ViewModel - encapsulation)
    // ... status of the most recent API request (to 'AsteroidsApodApi')
    val statusApod: LiveData<NetApiStatus>
        get() = repo.statusApod

    // fetch LiveData from repo (so that layouts/Views only depend on the ViewModel - encapsulation)
    // ... Astronomy Picture of the Day (APOD) meta data
    val apod: LiveData<PictureOfDay?>
        get() = repo.apod


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


    // upon instantiating the ViewModel (from MainFragment):
    // --> update DB from net
    init {

        // app starts on asteroids overview fragment (in state 'LOADING') --> not 'in navigation'
        _navigateToAsteroidDetails.value = null

        // all other ('hoisted') LiveData elements (= the ones originating in the repo object) are
        // initialized in the 'init { ... }' section of 'repo'

    }  // init


    /**
     * Factory for constructing MainViewModel with parameter (mostly boilerplate)
     */
    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }

}  // onViewCreated [MainFragment]