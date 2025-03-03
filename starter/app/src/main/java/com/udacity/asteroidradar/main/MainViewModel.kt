package com.udacity.asteroidradar.main

import androidx.lifecycle.*
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.repository.AsteroidsRepository
import kotlinx.coroutines.launch


// network API status (to maintain a transparent user experience)
enum class NetApiStatus { LOADING, ERROR, DONE }

// ViewModel for MainFragment
// ... injected parameter 'repository' is the data source for this ViewModel
class MainViewModel(private val repo: AsteroidsRepository) : ViewModel() {

    // enum for scoping asteroid fetches from DB
    enum class AsteroidsFilter(val value: String) {
        SHOW_TODAY("today"),
        SHOW_UPCOMING("upcoming"),
        SHOW_ALL("all")
    }

    // hoist LiveData from repo (so that layouts/Views only depend on the ViewModel - encapsulation)
    // ... Astronomy Picture of the Day (APOD) meta data
    val apod: LiveData<PictureOfDay?> = repo.apod

    // hoist LiveData from repo (so that layouts/Views only depend on the ViewModel - encapsulation)
    // ... status of the most recent API request (to 'AsteroidsApodApi')
    val statusApod: LiveData<NetApiStatus> = repo.statusApod

    // hoist LiveData from repo (so that layouts/Views only depend on the ViewModel - encapsulation)
    // ... status of the most recent API request (to 'AsteroidsNeoWsApi')
    val statusNeoWs: LiveData<NetApiStatus> = repo.statusNeoWs


    // define LiveData for storing the currently selected scope for data from DB (query 'filter')
    // ... this 'scope' is applied to the LiveData value of 'asteroids' (see below)
    // ... motivated by: https://developer.android.com/codelabs/advanced-kotlin-coroutines#6
    var dbScope: MutableLiveData<AsteroidsFilter> = MutableLiveData(AsteroidsFilter.SHOW_UPCOMING)

    // define LiveData for list of asteroids from DB
    // ... the data source / value of 'asteroids' is whatever function repo.fetchAsteroidsFromDB
    //     returns - or, in other words:
    // ... everytime dbScope is altered (selected item of overlay menu), Android runs the registered
    //     switchMap lambda; the return value of this lambda is stored in LiveData 'asteroids'
    // ... as 'asteroids' has been defined as LiveData, this also triggers the UI observers which
    //     ultimately update the RecyclerView items
    val asteroids: LiveData<List<Asteroid>> = dbScope.switchMap { filter ->
        when (filter) {
            AsteroidsFilter.SHOW_TODAY -> repo.fetchAsteroidsApproachingToday()
            AsteroidsFilter.SHOW_UPCOMING -> repo.fetchAsteroidsFromTodayOn()
            else -> repo.fetchAsteroidsAll()
        }
    }


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

        // fetch Astronomy Picture of the Day (APOD)
        // ... HTTP requests are run off the UI thread
        // ... associated all of these coroutines with the viewModelScope (= a coroutineScope)
        viewModelScope.launch {

            // fetch APOD data - also initializes LiveData _statusApod to LOADING
            repo.fetchPictureOfTheDay()

        }

        // all other ('hoisted') LiveData elements (= the ones originating in the repo object) are
        // initialized in the 'init { ... }' section of 'repo'

    }  // init


    /**
     * Factory for constructing MainViewModel with injected 'repository' parameter
     */
    class MainViewModelFactory(private val repository: AsteroidsRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}  // onViewCreated [MainFragment]