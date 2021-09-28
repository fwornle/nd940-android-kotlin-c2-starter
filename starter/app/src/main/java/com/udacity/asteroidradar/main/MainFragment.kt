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

        // Allows Data Binding to observe LiveData with the lifecycle of this Fragment
        binding.lifecycleOwner = this

        // note: by the time the viewModel is (lazily) initialized (--> this line, below), the
        // fragment has already been inflated (--> lines above)
        // --> data loading spinner logic (see BindingAdapters.kt) have been set-up and become
        //     effective as soon as the time consuming net API calls are triggered
        // --> same for DB API calls, once the repo has been implemented (Room)
        binding.viewModel = viewModel


        // observer of LiveData 'navigateToAsteroidDetails'
        // - this performs the actual navigation to the details fragment
        // - navigation done is indicated by clearing the selected asteroid from
        //   LiveData 'navigateToAsteroidDetails' (null)
        // - the data of the selected asteroid has been sent to the details fragment as bundle
        //   (safeArgs) --> this is why the data class had to be made 'Parcelize-able'
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

        // overlay menu
        setHasOptionsMenu(true)

        // return the View object of the inflated layout (= the fragment)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_overflow_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return true
    }
}
