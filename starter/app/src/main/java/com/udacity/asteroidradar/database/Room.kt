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

package com.example.android.devbyteviewer.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import com.udacity.asteroidradar.database.DatabaseAsteroid

// define database access object (DAO) through which we can interact with the DB
@Dao
interface AsteroidsDao {

    // fetch all available asteroids from DB
    @Query("select * from asteroid_data_table")
    fun getAsteroids(): LiveData<List<DatabaseAsteroid>>

    // insert asteroids - should the data of an asteroid have changed, overwrite old entry
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg asteroids: DatabaseAsteroid)

}

// define the DB for local storage of asteroid data (as abstract class)
@Database(entities = [DatabaseAsteroid::class], version = 2)
abstract class AsteroidsDatabase : RoomDatabase() {

    // DB has a reference to the DAO (abstract, as it's but an interface)
    abstract val asteroidsDao: AsteroidsDao

}

// singleton instance of the DB --> this instantiates the DB (under the 'singleton' name 'INSTANCE')
private lateinit var INSTANCE: AsteroidsDatabase

// getter function to retrieve a reference to the (singleton) DB object
fun getDatabase(context: Context): AsteroidsDatabase {

    // ensure thread safety
    synchronized(AsteroidsDatabase::class.java) {

        // DB already initialized?
        if (!::INSTANCE.isInitialized) {

            // nope - initialize it here (once and for all)
            INSTANCE = Room.databaseBuilder(
                context.applicationContext,
                AsteroidsDatabase::class.java,
                "asteroids")
                .fallbackToDestructiveMigration()
                .build()
        }

    }

    // return reference to (initialized) DB object
    return INSTANCE
}