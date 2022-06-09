/*
DAVIDE RIZZOTTI 1216409

MyLocation fornisce una definizione delle posizioni che verranno salvate nel database.
Ho deciso di salvare le informazioni principali quali latitudine, longitudine altitudine
e la data di acquisizione.
Come chiave primaria ho scelto un numero intero progressivo che aumenta al crescre
delle posizioni salvate nel database
 */

package com.example.geolocalization.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MyLocation(
    @PrimaryKey var timeStamp : Long,
    @ColumnInfo val latitude : Double,
    @ColumnInfo val longitude : Double,
    @ColumnInfo val altitude : Double,
    @ColumnInfo val date: String) {
}
