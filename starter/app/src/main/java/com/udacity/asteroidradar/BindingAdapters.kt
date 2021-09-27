package com.udacity.asteroidradar

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import coil.load

// layout properties with attribute <... app:apod ...> call upon this code
@BindingAdapter("apod")
fun bindImage(imgView: ImageView, apod: PictureOfDay?) {
    apod?.let {
        // load image using "coil"
        // ... see: https://github.com/coil-kt/coil#readme
        // Coil (suspends the current coroutine; non-blocking and thread safe)
        imgView
            // .load("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTc9mTlmQ_nD24rgudSHzKPsAZdSKn861Z0bw&usqp=CAU")
            .load(it.url)
            {
                crossfade(true)
                placeholder(R.drawable.placeholder_picture_of_day)
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
