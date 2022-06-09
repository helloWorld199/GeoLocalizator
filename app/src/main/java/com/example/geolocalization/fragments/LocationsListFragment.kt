/*
DAVIDE RIZZOTTI 1216409

LocationsListFragment è il secondo fragment che permette di visualizzare
le ultime 150 posizioni acquisite in una lista scorribile (RecyclerView).
Sono presenti anche tre bottoni:
--> Per mostrare la mappa ed il tragitto negli ultimi 5 minuti
--> Per aggiornare la lista delle posizioni
--> Per eliminare dalla lista tutte le posizioni acquisite fino ad ora (è irreversibile!)
 */

package com.example.geolocalization.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.geolocalization.R
import com.example.geolocalization.services.BackgroundLocationService

class LocationsListFragment : Fragment() {

    private lateinit var showMapButton : Button
    private lateinit var updateButton : Button
    private lateinit var deleteButton : Button
    private lateinit var adapter : LocationsListAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_locations_list, container, false)


        adapter = LocationsListAdapter(BackgroundLocationService.myLocationDao!!.getAll())

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.adapter = adapter

        showMapButton = view.findViewById(R.id.showMapButton)
        updateButton = view.findViewById(R.id.updateButton)
        deleteButton = view.findViewById(R.id.deleteButton)

        updateButton.setOnClickListener{
            //Log.d(null,"------- LocationsListFragment ---> updateButton().setOnClickListener -------")
            recyclerView.adapter = LocationsListAdapter(BackgroundLocationService.myLocationDao!!.getAll())
        }

        showMapButton.setOnClickListener{
            view.findNavController()
                .navigate(R.id.action_locationsListFragment_to_mapFragment)
        }

        deleteButton.setOnClickListener{
            BackgroundLocationService.myLocationDao!!.deleteAll()
            recyclerView.adapter = LocationsListAdapter(BackgroundLocationService.myLocationDao!!.getAll())
        }

        return view
    }

}