package com.udacity.asteroidradar.main

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import coil.load
import coil.transform.CircleCropTransformation
import coil.transform.GrayscaleTransformation
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.R
import com.udacity.asteroidradar.databinding.FragmentMainBinding
import com.udacity.asteroidradar.repository.AsteroidsRepository

class MainFragment : Fragment() {

    // (lazy) initialization of viewModel associated with MainFragment
    // ... using 'application' as parameter
    private val viewModel: MainViewModel by lazy {

        // fetch 'activity' reference to get parameter 'application' for the provisioning of the VM
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onViewCreated()"
        }

        // use ViewModel factory function defined in ViewModel to get a reference to the
        // ViewModel with 'application' as parameter
        ViewModelProvider(this,
            MainViewModel.Factory(activity.application)).get(MainViewModel::class.java)

    }

    // create fragment view
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        // inflate MainFragment layout
        val binding = FragmentMainBinding.inflate(inflater)

        // allows Data Binding to be scoped to the lifecycle of this Fragment (UI related LD
        // observers are only called if the lifecycle allows for it (= app in the foreground, etc.)
        binding.lifecycleOwner = this

        // note: by the time the viewModel is (lazily) initialized (--> this line, below), the
        // fragment has already been inflated (--> lines above)
        // --> references to LiveData have been 'hooked up' (observers to trigger UI updates)
        // --> LiveData must be initialized in the 'init { ... }' function of the ViewModel so that
        //     onCreateView (= this function) can return a properly initialized View and the
        //     Android 'event loop' (?!) can call upon the registered observers (without crashing)
        binding.viewModel = viewModel


        // observer of LiveData 'navigateToAsteroidDetails'
        // - this performs the actual navigation to the details fragment
        // - 'navigation done' is indicated by clearing the selected asteroid from
        //   LiveData 'navigateToAsteroidDetails' (null)
        // - the data of the selected asteroid is sent to the details fragment as bundle (safeArgs)
        //   --> this is why the data class has to be made 'Parcelize-able'
        viewModel.navigateToAsteroidDetails.observe(viewLifecycleOwner, {
            if ( null != it ) {
                this.findNavController().navigate(MainFragmentDirections.actionShowDetail(it))
                viewModel.displayAsteroidDetailsComplete()
            }
        })

        // set RV adapter, using constructor parameter 'OnClickListener' to provide a lambda
        // function, which is used during ViewHolder binding to install an OnClick listener to each
        // RV view item, using the corresponding data element as parameter
        //
        // ... function 'displayAsteroidDetails' sets the LiveData _navigateToAsteroidDetails
        // to the provided Asteroid element (during ViewHolder binding) --> this is used to
        // trigger navigation to the details fragment
        binding.asteroidRecycler.adapter = AsteroidRecyclerAdapter(
            AsteroidRecyclerAdapter.OnClickListener {
                // trigger navigation - selected asteroid data sent as bundle (SafeArgs)
                viewModel.displayAsteroidDetails(it)
            }
        )

        // enable overlay menu
        setHasOptionsMenu(true)

        // return the fully initialized View object of the inflated layout (= the fragment)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_overflow_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    // update asteroid display data in accordance with selected DB scope
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        // set LiveData 'asteroids' with newly scoped query results
        viewModel.dbScope.value =
            when(item.itemId) {
                R.id.show_today_menu -> MainViewModel.AsteroidsFilter.SHOW_TODAY
                R.id.show_week_menu -> MainViewModel.AsteroidsFilter.SHOW_UPCOMING
                else -> MainViewModel.AsteroidsFilter.SHOW_ALL
            }

        // done evaluating the selected menu item
        return true

    }
}
