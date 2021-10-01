package com.udacity.asteroidradar

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.udacity.asteroidradar.main.AsteroidRecyclerAdapter
import com.udacity.asteroidradar.main.NetApiStatus

// layout properties with attribute <... app:apodImage ...> call upon this code
@BindingAdapter("apodImage")
fun bindImage(imgView: ImageView, apod: PictureOfDay?) {
    apod?.let {
        // load image using "coil" (https://github.com/coil-kt/coil#readme)
        // REF: https://betterprogramming.pub/how-to-use-coil-kotlins-native-image-loader-d6715dda7d26
        // ... suspends the current coroutine; non-blocking and thread safe
        // ... built-in image cache --> each APOD only loaded once
        imgView
            // TEST images:
            //.load("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTc9mTlmQ_nD24rgudSHzKPsAZdSKn861Z0bw&usqp=CAU")
            //.load("https://media.istockphoto.com/photos/sunrise-at-quiraing-isle-of-skye-scotland-picture-id143177040")
            // actual APOD image
            .load(it.url)
            {
                crossfade(true)
                placeholder(R.drawable.loading_animation)   // during loading of actual image
                error(R.drawable.ic_broken_image)           // retrieval of image unsuccessful
            }
    }
}


// layout properties with attribute <... app:listData ...> call upon this code
// ... this binds the data provided by the AsteroidRecyclerAdapter to the RV view element
@BindingAdapter("listData")
fun bindRecyclerView(recyclerView: RecyclerView, data: List<Asteroid>?) {
    val adapter = recyclerView.adapter as AsteroidRecyclerAdapter
    adapter.submitList(data)
}

// layout properties with attribute <... app:netStatus ...> call upon this code
@BindingAdapter("netStatus")
fun bindStatus(statusImageView: ImageView, status: NetApiStatus) {
    when (status) {
        NetApiStatus.LOADING -> {
            statusImageView.visibility = View.VISIBLE
            statusImageView.setImageResource(R.drawable.loading_animation)
            //statusImageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
            statusImageView.adjustViewBounds = true
        }
        NetApiStatus.ERROR -> {
            statusImageView.visibility = View.VISIBLE
            statusImageView.setImageResource(R.drawable.ic_connection_error)
            //statusImageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
            statusImageView.adjustViewBounds = false
        }
        NetApiStatus.DONE -> {
            statusImageView.visibility = View.GONE
        }
    }
}

// layout properties with attribute <... app:statusIcon ...> call upon this code
@BindingAdapter("statusIcon")
fun bindAsteroidStatusImage(imageView: ImageView, isHazardous: Boolean) {
    if (isHazardous) {
        imageView.setImageResource(R.drawable.ic_status_potentially_hazardous)
    } else {
        imageView.setImageResource(R.drawable.ic_status_normal)
    }
}

// layout properties with attribute <... app:asteroidStatusImage ...> call upon this code
@BindingAdapter("asteroidStatusImage")
fun bindDetailsStatusImage(imageView: ImageView, isHazardous: Boolean) {
    if (isHazardous) {
        imageView.setImageResource(R.drawable.asteroid_hazardous)
    } else {
        imageView.setImageResource(R.drawable.asteroid_safe)
    }
}

// layout properties with attribute <... app:astronomicalUnitText ...> call upon this code
@BindingAdapter("astronomicalUnitText")
fun bindTextViewToAstronomicalUnit(textView: TextView, number: Double) {
    val context = textView.context
    textView.text = String.format(context.getString(R.string.astronomical_unit_format), number)
}

// layout properties with attribute <... app:kmUnitText ...> call upon this code
@BindingAdapter("kmUnitText")
fun bindTextViewToKmUnit(textView: TextView, number: Double) {
    val context = textView.context
    textView.text = String.format(context.getString(R.string.km_unit_format), number)
}

// layout properties with attribute <... app:velocityText ...> call upon this code
@BindingAdapter("velocityText")
fun bindTextViewToDisplayVelocity(textView: TextView, number: Double) {
    val context = textView.context
    textView.text = String.format(context.getString(R.string.km_s_unit_format), number)
}
