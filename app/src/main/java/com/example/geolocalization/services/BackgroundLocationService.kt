/*
DAVIDE RIZZOTTI 1216409

La classe BackgroundLocationService si occupa di creare, avviare e gestire
il servizio di ricezione e registrazione delle posizioni.
Il servizio viene creato quando l'app viene aperta, resta attivo quando l'app
non è visibile sullo schermo, e viene terminato quando viene chiusa manualmente.
Quando viene ricevuta una posizione, questa viene inviata al fragment HomeFragment
tramite un Intent (per far si che la posizione possa essere aggiornata in
tempo reale) e salvata in un database, in modo tale che lo storico venga mantenuto
anche quando l'app viene terminata e riavviata.

NOTA IMPORTANTE: Nonostante l'intervallo di aggiornamento della posizione sia di 2 secondi,
all'apertura dell'app (e più in generale all'avvio del servizio), c'è un intervallo di assestamento
che va da 10 secondi a 1/2 minuti in cui l'acquisizione delle posizioni non è regolare. Immagino sia
dovuto a qualche impostazione/regolazione interna dell'API utilizzata
 */

package com.example.geolocalization.services

import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.example.geolocalization.R
import com.example.geolocalization.data.MyLocation
import com.example.roomtesting20.data.AppDatabase
import com.example.roomtesting20.data.MyLocationDao
import java.util.*
import kotlin.math.roundToInt

//Intervallo di aggiornamento della posizione in millisecondi
const val UPDATE_INTERVAL : Long = 2000

class BackgroundLocationService : Service() {

    //Oggetto tramite il quale possiamo chiedere aggiornamenti in tempo reale sulla posizione
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    //Oggetto utilizzato per stabilire le caratteristiche della richiesta
    //(ogni quanto dev'essere fatta, quando tempo al massimo aspettare...)
    private lateinit var locationRequest : LocationRequest

    //Oggetto utilizzato per ricevere gli aggiornamenti sulla posizione del dispositivo
    private lateinit var locationCallback : LocationCallback

    /**
     * Non mi connetto al servizio in modalità Bind, è stato lasciato solo per richiesta
     * di override.
     */
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(null, "---------- onStartCommand di BackgroundLocationService ---------")
        if (intent != null) {
            if (intent.getBooleanExtra(PLAY_START, false)){
                //Avvio il servizio in foreground
                startForegroundTracking()

                startLocationUpdates()
                if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.lastLocation.addOnSuccessListener {
                        val intent: Intent = Intent("LOCATION_UPDATED")
                        if (it != null) {
                            intent.putExtra(LONGITUDE_KEY, it.longitude.toString())
                            intent.putExtra(LATITUDE_KEY, it.latitude.toString())

                            //Approssimo l'altitudine a due cifre dopo la virgola
                            val altitude = (it.altitude * 100.0).roundToInt() / 100.0
                            intent.putExtra(ALTITUDE_KEY, altitude.toString())

                            sendBroadcast(intent)
                        }
                    }
                }
            }
        }
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()

        Log.d(null, "---------- onCreate di BackgroundLocationService ---------")

        myLocationDao = AppDatabase.getDatabase(applicationContext).myLocationDao()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.create().apply {
            //Aggiorna la posizione ogni UPDATE_INTERVAL secondi
            interval = UPDATE_INTERVAL
            fastestInterval = UPDATE_INTERVAL
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {

            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)

                /*Log.d(null, "--------- onLocationResult ---------")
                //Prendi l'ultima posizione aggiornata e aggiorna la UI
                var i = 0
                for(location in p0.locations){
                    Log.d(null, "-----" + i)
                    Log.d(null, location.latitude.toString())
                    Log.d(null, location.longitude.toString())
                    Log.d(null, location.altitude.toString())
                    i++
                }*/

                //Invia la posizione al ricevitore in modo che l'UI venga aggiornata
                val location = p0.lastLocation
                val intent : Intent = Intent("LOCATION_UPDATED")
                intent.putExtra(LONGITUDE_KEY, location.longitude.toString())
                intent.putExtra(LATITUDE_KEY, location.latitude.toString())

                //Approssimo l'altitudine a due cifre dopo la virgola
                val altitude = (location.altitude * 100.0).roundToInt() / 100.0
                intent.putExtra(ALTITUDE_KEY, altitude.toString())

                //Invio l'intent al ricevitore
                sendBroadcast(intent)

                //Salvo la data del momento dell'acquisizione della posizione attuale
                val date_elements = Calendar.getInstance().time.toString().split(" ")
                //Chiave primaria dell'elemento nel database: tempo in millisecondi
                val key = Calendar.getInstance().timeInMillis

                //Log.d(null, key.toString())
                Log.d(null, date_elements[1] + date_elements[2] + date_elements[3])

                //Inserimento nel database
                myLocationDao?.insert(MyLocation(key, location.latitude, location.longitude, altitude,
                    date_elements[1] + " " + date_elements[2] + " \n" + date_elements[3]))

                //Quando il mio database contiene 250 posizioni, elimino le 100 più vecchie,
                //dato che quelle utili sono le 150 più recenti. Senza questo passaggio, il database si
                //riempirebbe creando problemi di inefficenza
                if(myLocationDao!!.getRowsNumber() >= 250)
                    myLocationDao!!.deletePastLocations()

            }
        }
    }

    /**
     * Richiede che venga avviata la procedura per ottenere aggiornamenti sulla posizione
     * in tempo reale.
     */
    private fun startLocationUpdates(){
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }

    /**
     * Richiede che venga terminata la richiesta di aggiornamenti sulla posizione
     * in tempo reale.
     */
    private fun stopLocationUpdates() {
        val intent : Intent = Intent("LOCATION_UPDATED")
        intent.putExtra(LONGITUDE_KEY, "Not Tracking Location")
        intent.putExtra(LATITUDE_KEY, "Not Tracking Location")
        intent.putExtra(ALTITUDE_KEY, "Not Tracking Location")

        sendBroadcast(intent)

        stopForeground(true)
        Log.d(null, "-------- stopLocationUpdates -------")
        //Smetti di ricevere aggiornamenti sulla posizione
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    /**
     * Avvia il servizio in foreground e crea la notifica che avvisa l'utente
     * che è iniziata l'acquisizione in foreground.
     */
    private fun startForegroundTracking(){
        Log.d(null, "------- startForegroundTracking ---> creazione notification channel -------")
        // Create the NotificationChannel, but only on API level 26+ because
        // the NotificationChannel class is new and not in the support library.
        // See https://developer.android.com/training/notify-user/channels
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = getString(R.string.channel_name)
            val description = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description
            // Register the channel with the system
            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }
        Log.d(null, "------- startForegroundTracking ---> creazione notifica -------")

        // Costruzione della notifica di foreground
        val notificationBuilder: Notification.Builder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                Notification.Builder(applicationContext, CHANNEL_ID)
            else
                Notification.Builder(applicationContext)
        notificationBuilder.setContentTitle(getString(R.string.app_name))
        notificationBuilder.setContentText(getString(R.string.location_tracking_notification))
        notificationBuilder.setSmallIcon(R.drawable.location_notification)
        val notification = notificationBuilder.build()
        // Runs this service in the foreground,
        // supplying the ongoing notification to be shown to the user
        val notificationID = 5786423 // An ID for this notification unique within the app
        startForeground(notificationID, notification)
    }

    override fun onDestroy() {
        stopLocationUpdates()
        super.onDestroy()
    }


    companion object
    {
        //Id della notifica
        private const val CHANNEL_ID = "LForegroundServiceID"
        //Chiave tramite la quale possiamo avviare il service dalla MainActivity
        const val PLAY_START = "FLocationStart"
        //Chiavi per mandare la posizione all'HomeFragment tramite un Intent
        const val LONGITUDE_KEY = "Longitude"
        const val LATITUDE_KEY = "Latitude"
        const val ALTITUDE_KEY = "Altitude"
        //Oggetto tramite il quale posso interfacciarmi con il database
        var myLocationDao: MyLocationDao? = null
    }
}