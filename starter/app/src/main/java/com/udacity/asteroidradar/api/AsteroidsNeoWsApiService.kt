package com.udacity.asteroidradar.api

import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.database.DatabaseAsteroid
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// Retrofit builder
// ... using Scalars converter factory
//     --> GET@NeoWs yields a complex JSON record
//     --> parse ourselves
private val retrofitScalars = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .baseUrl(Constants.BASE_URL)
    .build()

// service interface (exposed by retrofit library)
// ... using retrofit's built-in coroutine capabilities (>= 2.6.0)
interface AsteroidsNeoWsApiService {

    // HTTP GET for 'Asteroids NeoWs' API endpoint "neo/rest/v1/feed" --> returns list of asteroids
    // (JSON) in selected time span
    // ... @Query annotates the query parameters
    @GET("${Constants.NEOWS_API_URL}/feed")
    suspend fun getAsteroids(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("api_key") apiKey: String,
    ): Response<String>

}

// expose service to the app through 'singleton' object
// ... as instantiation of retrofit is 'expensive' --> done once here to be used by all modules
// ... calling AsteroidsNeoWsApi.retrofitService will initialize retrofit with its i/f
// 'AsteroidsNeoWsApiService'
object AsteroidsNeoWsApi {

    // complex JSON --> parse ourselves
    val retrofitServiceScalars: AsteroidsNeoWsApiService by lazy {
        retrofitScalars.create(AsteroidsNeoWsApiService::class.java)
    }

}

// convert from 'data transfer object' (net) to 'database object'
fun List<Asteroid>.asDatabaseModel(): List<DatabaseAsteroid> {
    return map {
        DatabaseAsteroid (
            id = it.id,
            codename = it.codename,
            closeApproachDate = it.closeApproachDate,
            absoluteMagnitude = it.absoluteMagnitude,
            estimatedDiameter = it.estimatedDiameter,
            relativeVelocity = it.relativeVelocity,
            distanceFromEarth = it.distanceFromEarth,
            isPotentiallyHazardous = it.isPotentiallyHazardous,
        )
    }
}
