package com.udacity.asteroidradar.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.databinding.RecyclerViewItemBinding

// adapter to map ArrayList<Asteroid> data to the View(Holder) associated w/h the RV grid layout
class AsteroidRecyclerAdapter(val onClickListener: OnClickListener) :
    ListAdapter<Asteroid, AsteroidRecyclerAdapter.AsteroidViewHolder>(DiffCallback) {

    // ViewHolder for items to display Asteroid records
    class AsteroidViewHolder(private val binding: RecyclerViewItemBinding):
        RecyclerView.ViewHolder(binding.root) {

            // method to bind an entry from the data source to the view property
            // data source:
            //    RV-selected item --> position --> index in List<Asteroid> ==> Asteroid
            // view item:
            //    data bound to a property w/h name 'asteroid' ... which is the <data><variable> in
            //    the associated layout (recycler_view_item.xml --> RecyclerViewItemBinding)
            fun bind(asteroid: Asteroid) {
                binding.asteroid = asteroid
                binding.executePendingBindings()
            }
    }

    // allow RV to determine when items are the same or not
    companion object DiffCallback : DiffUtil.ItemCallback<Asteroid>() {
        override fun areItemsTheSame(oldItem: Asteroid, newItem: Asteroid): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Asteroid, newItem: Asteroid): Boolean {
            return oldItem.id == newItem.id
        }
    }

    //  adapter internal class 'OnClickListener'  (used in the constructor to the adapter to create
    //  a property 'onClickListener' (of type OnClickListener)
    //
    //  - this (internal) class defines in it's primary constructor a property 'clickListener' of
    //    type '(asteroid: Asteroid) -> Unit', i. e. a lambda function w/h parameter
    //    'asteroid'
    //  - instantiating this (internal) class yields an object (of type OnClickListener) with a
    //    method 'onClick' of the above described type, i. e. '(ast: Asteroid) -> Unit', where
    //    the method's body is the provided lambda function
    //  - as the internal class is instantiated in the primary constructor (as property
    //    'onClickListener') the adapter class (AsteroidRecyclerAdapter) can make use of it in
    //    whatever adapter activity needs it
    //  - intended use: during binding of a newly scrolled-in RV view item, the provided lambda is
    //    adapted (call-up parameter) to the data associated with this particular view item and
    //    then registered as the OnClick listener of this view item
    //
    class OnClickListener(val clickListener: (asteroid: Asteroid) -> Unit) {
        fun onClick(asteroid: Asteroid) = clickListener(asteroid)
    }

    /*
     *  AsteroidRecyclerAdapter methods to be overridden (3x)
     */
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AsteroidViewHolder {
        return AsteroidViewHolder(RecyclerViewItemBinding
            .inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: AsteroidViewHolder, position: Int) {

        // get view in question
        val asteroid = getItem(position)

        // set OnClick listener of the RV view item (at 'position') which is to be bound to it's
        // associated data element of list 'ArrayList<Asteroid>'
        //
        // - the view item can be found from the ViewHolder via getItemView() or, in Kotlin,
        //   from property 'itemView' of the extended ViewHolder class (Kotlin extensions, ktx)
        // - setOnClickListener uses the lambda function registered as adapter class property
        //   'onClickListener' during instantiation of the adapter class
        // - ... to set the OnClick listener of the RV view item to the lambda while using
        //   the list data element associated with the RV view item as call-up parameter (asteroid)
        holder.itemView.setOnClickListener {
            onClickListener.onClick(asteroid)
        }

        // bind the data element to the RV view item
        // ... so that it can be used in the layout (DataBinding - <data><variable> ...)
        holder.bind(asteroid)
    }

}