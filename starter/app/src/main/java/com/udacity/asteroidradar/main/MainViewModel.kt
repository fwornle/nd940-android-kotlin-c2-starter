package com.udacity.asteroidradar.main

import android.app.Application
import androidx.lifecycle.*
import com.example.android.devbyteviewer.database.getDatabase
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.repository.AsteroidsRepository
import kotlinx.coroutines.launch


// network API status (to maintain a transparent user experience)
enum class NetApiStatus { LOADING, ERROR, DONE }

// ViewModel for MainFragment
// ... parameter 'application' is the application that this viewmodel is attached to
class MainViewModel(application: Application) : AndroidViewModel(application) {

    // create DB for local storage of Asteroid data
    private val database = getDatabase(application)

    // create repository for all data (net or DB)
    val repo = AsteroidsRepository(database)


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

        // fetch asteroids data - also initializes LiveData _status to LOADING
        viewModelScope.launch {

            // HTTP GET for Near Earth Objects (NeoWs) data
            repo.refreshAsteroids()

            // HTTP GET for Astronomy Picture of the Day (APOD) data
            repo.refreshPictureOfDay()

        }

    }

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