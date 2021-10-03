/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
// fw-210927: adjusted for com.udacity.asteroidradar

package com.udacity.asteroidradar.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

// define database access object (DAO) through which we can interact with the DB
@Dao
interface AsteroidsDao {

    // fetch all available asteroids from DB
    @Query("select * from asteroid_data_table order by closeApproachDate asc")
    fun getAllAsteroids(): LiveData<List<DatabaseAsteroid>>

    // fetch coming asteroids from DB
    @Query("select * from asteroid_data_table where closeApproachDate >= :dateStart order by closeApproachDate asc")
    fun getAsteroidsFromTodayOn(dateStart: String): LiveData<List<DatabaseAsteroid>>

    // fetch coming asteroids from DB
    @Query("select * from asteroid_data_table where closeApproachDate = :dateToday order by closeApproachDate asc")
    fun getAsteroidsApproachingToday(dateToday: String): LiveData<List<DatabaseAsteroid>>

    // insert asteroids - should the data of an asteroid have changed, overwrite old entry
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg asteroids: DatabaseAsteroid)

}

// define the DB for local storage of asteroid data (as abstract class)
@Database(entities = [DatabaseAsteroid::class], version = 2)
abstract class AsteroidsDatabase : RoomDatabase() {

    // DB has a reference to the DAO (abstract, as it's but an interface)
    abstract val asteroidsDao: AsteroidsDao

    // DB instance
    companion object {

        // Singleton prevents multiple instances of database opening at the same time
        // ... see: https://developer.android.com/codelabs/android-room-with-a-view-kotlin#7
        @Volatile
        private var INSTANCE: AsteroidsDatabase? = null

        fun getDatabase(context: Context): AsteroidsDatabase {

            // create the DB - only done once
            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AsteroidsDatabase::class.java,
                    "asteroids"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                // set (singleton) INSTANCE to newly created DB
                INSTANCE = instance

                // ... and return it
                instance

            }  // synchronized

        }  // getDatabase()

    }  // companion object

}  // class: AsteroidsDatabase
