/*
DAVIDE RIZZOTTI 1216409

MapFragment è il terzo e ultimo fragment che "ospita" una mappa GoogleMap.
In questa mappa è possibile vedere il tragitto percorso negli ultimi 5 minuti,
tramite dei"marker. I marker assumono colori dal rosso al verde, per posizioni
rispettivamente più vecchie e più recenti. E' disponibile inoltre un pulsante che
permette il refresh del tragitto.
 */

package com.example.geolocalization.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.geolocalization.R
import com.example.geolocalization.services.BackgroundLocationService
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class MapFragment : Fragment(), OnMapReadyCallback {

    //Oggetto di tipo GoogleMap, che rappresenta la mappa visualizzata
    private lateinit var googleMap : GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        val supportMapFragment : SupportMapFragment = childFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment

        supportMapFragment.getMapAsync(this)

        val refreshButton : Button = view.findViewById(R.id.refreshButton)
        refreshButton.setOnClickListener{
            refreshRoute()
        }

        return view
    }

    /**
     * Quando la mappa è pronta, il parametro di questa funzione è l'oggetto
     * GoogleMap su cui lavoriamo
     */
    override fun onMapReady(p0: GoogleMap?) {
        if (p0 != null) {
            googleMap = p0
        }
        refreshRoute()
    }

    /**
     * Funzione che aggiorna il tragitto percorso. Prende dal database le ultime 150 posizioni
     * ricevute (o meno se non ce n'è abbastanza nel database, e le visualizza nella mappa
     * tramite dei marker, che assumono colori dal rosso al verde, per posizioni rispettivamente
     * più vecchie e più recenti.
     */
    private fun refreshRoute(){
        val locationsList = BackgroundLocationService.myLocationDao?.getAll()
        val locationsListSize = locationsList?.size
        //Incremento del colore del
        val colorIncrement : Float = (120.0/ locationsListSize!!.toFloat()).toFloat()
        var i = 1
        for (loc in locationsList){
            if(i == 1){
                val point = CameraUpdateFactory.newLatLng(LatLng(loc.latitude, loc.longitude))
                googleMap.moveCamera(point)
            }

            googleMap.addMarker(
                MarkerOptions()
                    .position(LatLng(loc.latitude, loc.longitude))
                    .title("Latitude:" + loc.latitude.toString() + "\nLongitude: " + loc.longitude.toString() + "\n" + loc.date)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN - (i-1)*colorIncrement))
            )
            i++
        }
    }


}