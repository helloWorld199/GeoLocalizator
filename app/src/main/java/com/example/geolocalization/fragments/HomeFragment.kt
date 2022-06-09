/*
DAVIDE RIZZOTTI 1216409

HomeFragment permette di visualizzare un interfaccia in cui i lati di latitudine,
longitudine e altitudine sono aggiornati in tempo reale. Mentre i dati devono essere recuperati,
verranno visualizzati dei particolari simboli di caricamento.
E' presente inoltre un bottone che permette di passare al fragment successivo (LocationsListFragment)
 */

package com.example.geolocalization.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.findNavController
import com.example.geolocalization.R
import com.example.geolocalization.services.BackgroundLocationService
import pl.droidsonroids.gif.GifImageView

class HomeFragment : Fragment() {

    private lateinit var tvLat : TextView
    private lateinit var tvLong : TextView
    private lateinit var tvAlt : TextView

    private lateinit var firstLoading : GifImageView
    private lateinit var secondLoading : GifImageView
    private lateinit var thirdLoading : GifImageView

    // Variabile utilizzata per nascondere le gif di caricamento quando
    // viene ricevuto un risultato
    private var resultReceived : Boolean = false

    val uiUpdated : BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(null, "--------- onReceive ----------")
            if (intent != null) {
                if(resultReceived) {
                    firstLoading.visibility = View.GONE
                    secondLoading.visibility = View.GONE
                    thirdLoading.visibility = View.GONE
                }
                tvLat.text = intent.getStringExtra(BackgroundLocationService.LATITUDE_KEY)
                tvLong.text = intent.getStringExtra(BackgroundLocationService.LONGITUDE_KEY)
                tvAlt.text = intent.getStringExtra(BackgroundLocationService.ALTITUDE_KEY)

            }
            //Ho ricevuto un risultato, quindi non dovrò più riprodurre il simbolo di caricamento
            resultReceived=false
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(null,"------- HomeFragment ---> onCreateView() -------")
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        tvLat = view.findViewById(R.id.latitude)
        tvLong = view.findViewById(R.id.longitude)
        tvAlt = view.findViewById(R.id.altitude)

        firstLoading = view.findViewById(R.id.first_loading)
        secondLoading = view.findViewById(R.id.second_loading)
        thirdLoading = view.findViewById(R.id.third_loading)


        val recentLocationsButton : Button = view.findViewById(R.id.elevatedButton)
        recentLocationsButton.setOnClickListener{
            view.findNavController()
                .navigate(R.id.action_homeFragment_to_locationsListFragment)
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        Log.d(null,"------- HomeFragment ---> onResume() -------")
        resultReceived = true
        //Registro il ricevitore, quindi rendo il fragment disponibile alla
        //ricezione di nuove posizioni
        context?.registerReceiver(uiUpdated, IntentFilter("LOCATION_UPDATED"))
    }

    override fun onPause() {
        Log.d(null,"------- HomeFragment ---> onPause() -------")
        //"De-registro" il ricevitore, quindi rendo il fragment non più
        //disponibile alla ricezione di nuove posizioni
        context?.unregisterReceiver(uiUpdated)
        super.onPause()
    }
}