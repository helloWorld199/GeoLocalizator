/*
DAVIDE RIZZOTTI 1216409

Funzionalità principali della classe MainActivity:
--> Chiedere i permessi di acquisire la posizione del dispositivo all'utente
--> Avviare il servizio di acquisizione della posizione, gestito dalla classe
    BackgroundLocationService
 */

package com.example.geolocalization

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.geolocalization.services.BackgroundLocationService

// Codice utilizzato per la richiesta dei permessi. Vedi onRequestPermissionsResult()
const val PERMISSIONS_FINE_LOCATION = 99

class MainActivity : AppCompatActivity() {

    private var permissionsObtained: Boolean = false
    private var isPlaying : Boolean = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Log.d(null, "------- MainActivity ---> onCreate() -------")
        setContentView(R.layout.activity_main)

        // Chiedi i permessi
        getPermissions()
        //Log.d(null, "------- MainActivity ---> onCreate() ---> Richiesta fatta -------")

        // Se ho i permessi, e il servizio non è attivo, avvialo.
        if(permissionsObtained && !isPlaying){
            val i : Intent = Intent(applicationContext, BackgroundLocationService::class.java)
            i.putExtra(BackgroundLocationService.PLAY_START, true)
            startForegroundService(i)
            isPlaying = true
        }

    }

    /*
        Funzione di callback per i risultati ottenuti dalla richiesta dei permessi. Per accedere
        ai risultati viene usato il "codice" PERMISSIONS_FINE_LOCATION, passato precedentemente
        alla funzione che richiede i permessi "requestPermissions()"
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            PERMISSIONS_FINE_LOCATION -> {
                // Se la richiesta viene cancellata, l'array con i risultati sarà vuoto
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //Log.d(null, "------- MainActivity ---> onRequestPermissionsResult ---> permessi ottenuti? -------")
                    // Una volta ottenuti i permssi, avvia il servizio di registrazione della posizione
                    val i : Intent = Intent(applicationContext, BackgroundLocationService::class.java)
                    i.putExtra(BackgroundLocationService.PLAY_START, true)
                    startForegroundService(i)
                    permissionsObtained = true
                    isPlaying = true
                } else {
                    //Log.d(null,"permessi non dati")

                    // Messaggio pop up che avvisa l'utente della necessità dei permessi affinchè
                    // l'app funzioni correttamente
                    val toast : Toast = Toast.makeText(this, "E' necessario dare i permessi affinchè l'app funzioni!", Toast.LENGTH_SHORT)
                    toast.show()
                    //Log.d(null, "------- MainActivity ---> onRequestPermissionsResult ---> Richiedi di nuovo i permessi -------")

                    // Aspetto 2,5 secondi prima di rifare la richiesta dei permessi, in modo
                    // tale che il toast sia interamente leggibile.
                    object : CountDownTimer(2500, 1000) {
                        override fun onTick(p0: Long) {
                        }
                        override fun onFinish() {
                            getPermissions()
                        }
                    }.start()

                }
            }
        }
    }

    /*
        Funzione che esegue la richiesta dei permessi
     */
    private fun getPermissions() {
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // Richiedo i permessi
                    requestPermissions(
                        arrayOf(
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                        ), PERMISSIONS_FINE_LOCATION
                    )
                }
        } else {
            permissionsObtained = true
        }
    }

    override fun onDestroy() {
        //Log.d(null, "------- MainActivity ---> onDestroy() -------")

        // L'activity viene distrutta, quindi fermo il servizio
        val i : Intent = Intent(applicationContext, BackgroundLocationService::class.java)
        stopService(i)
        isPlaying = false

        super.onDestroy()
    }
}