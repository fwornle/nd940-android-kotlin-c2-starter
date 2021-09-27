package com.udacity.asteroidradar.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.PictureOfDay
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// Moshi builder
private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

// Retrofit builder
// ... using Moshi converter factory
//     --> GET@APOD yields a simple JSON record
//     --> turn into POJO using moshi
private val retrofitMoshi = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(Constants.BASE_URL)
    .build()

// service interface (exposed by retrofit library)
// ... using retrofit's built-in coroutine capabilities (>= 2.6.0)
interface ApodApiService {

    // HTTP GET for 'Astronomy Picture of the Day' (APOD) API endpoint "planetary/apod"
    // --> returns a JSON record with details of the picture of "today"
    @GET(Constants.APOD_API_URL)
    suspend fun getPictureOfDay(
        @Query("api_key") apiKey: String,
    ): Response<PictureOfDay>
}

// expose service to the app through 'singleton' object
// ... as instantiation of retrofit is 'expensive' --> done once here to be used by all modules
// ... calling ApodApi.retrofitServiceMoshi will initialize retrofit with its i/f 'ApodApiService'
object ApodApi {

    // simple JSON --> let moshi parse it
    val retrofitServiceMoshi: ApodApiService by lazy {
        retrofitMoshi.create(ApodApiService::class.java)
    }

}