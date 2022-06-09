/*
DAVIDE RIZZOTTI 1216409

Questa classe definisce quali entità fanno parte del database, e le funzioni
per ottenere un'istanza del database e l'interfaccia tramite la quale eseguire
le operazioni di inserimento, rimozione e lettura.
 */

package com.example.roomtesting20.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.geolocalization.data.MyLocation

@Database(entities = [MyLocation::class], version = 1)
abstract class AppDatabase : RoomDatabase(){
    /*
        Funzione che restituisce l'interfaccia Dao del mio database
     */
    abstract fun myLocationDao(): MyLocationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        /*
            Funzione che restituisce un istanza del database
         */
        fun getDatabase(context: Context): AppDatabase {
            // Non è il caso di quest'app, ma potrebbe essere che due
            // thread chiedano di creare un'istanza dello stesso database in
            // contemporanea, creando un evidente incompatibilità: per questo
            // uso synchronized.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "myLocations_database"
                )
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}