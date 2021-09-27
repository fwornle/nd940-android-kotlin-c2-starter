package com.udacity.asteroidradar.main

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import coil.load
import coil.transform.CircleCropTransformation
import coil.transform.GrayscaleTransformation
import com.udacity.asteroidradar.R
import com.udacity.asteroidradar.databinding.FragmentMainBinding

class MainFragment : Fragment() {

    // (lazy) initialize viewModel associated with MainFragment
    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        // inflate MainFragment layout
        val binding = FragmentMainBinding.inflate(inflater)
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

        setHasOptionsMenu(true)
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
