/*
DAVIDE RIZZOTTI 1216409

MyLocationDao è un'interfaccia personalizzata che consente di definire i metodi tramite
i quali vengono inseriti, rimossi e letti gli elementi dal database.
 */

package com.example.roomtesting20.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.geolocalization.data.MyLocation


@Dao
interface MyLocationDao {
    // Mostro le ultime 150 posizioni acquisite. 150 perchè acquisisco una posizione ogni 2 secondi.
    // In questo modo recupero le posizioni acquisite negli ultimi 5 minuti.
    @Query("SELECT * FROM mylocation ORDER BY timeStamp DESC LIMIT 150")
    fun getAll(): List<MyLocation>

    @Query("DELETE FROM mylocation")
    fun deleteAll()

    @Query("SELECT COUNT(*) FROM mylocation")
    fun getRowsNumber(): Int

    //Elimino le 100 posizioni più vecchie. (Vedi fine metodo onCreate di BackgroundServiceLocation
    @Query ("DELETE FROM mylocation WHERE timeStamp IN (SELECT timeStamp FROM mylocation ORDER BY timeStamp ASC LIMIT 100)")
    fun deletePastLocations()

    @Insert
    fun insert(myLocation: MyLocation)

    @Delete
    fun delete(myLocation: MyLocation)
}