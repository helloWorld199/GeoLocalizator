/*
DAVIDE RIZZOTTI 1216409

LocationsListAdapter è l'oggetto di tipo Adapter per la mia RecyclerView utilizzata.
 */


package com.example.geolocalization.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.geolocalization.R
import com.example.geolocalization.data.MyLocation

class LocationsListAdapter(list : List<MyLocation>) : RecyclerView.Adapter<LocationsListAdapter.MyViewHolder>(){

    //Lista delle posizioni da mostrare nella lista. Conterrà le più recenti
    //150 posizioni acquisite.
    private val locationsList = list

    class MyViewHolder(itemView : View): RecyclerView.ViewHolder(itemView) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.location_item, parent, false))
    }

    override fun getItemCount(): Int {
        return locationsList.size
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = locationsList[position]
        holder.itemView.findViewById<TextView>(R.id.item_longitude).text = currentItem.longitude.toString()
        holder.itemView.findViewById<TextView>(R.id.item_latitude).text = currentItem.latitude.toString()
        holder.itemView.findViewById<TextView>(R.id.item_altitude).text = currentItem.altitude.toString()
        holder.itemView.findViewById<TextView>(R.id.itemDate).text = currentItem.date
    }

}