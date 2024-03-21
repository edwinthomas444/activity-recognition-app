package com.tutorial.activityrecognition

import android.Manifest
import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.tutorial.activityrecognition.ui.theme.ActivityRecognitionTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var locationMem = mutableListOf<Pair<Double, Double>>()
    private var position = -1
    private var positionMem = -1
    private var observationWin = mutableListOf<Int>()
    val maxObservations = 20
    var recognizedActivity by mutableStateOf(0)
    var previousState by mutableStateOf(0)
    var startTime by mutableStateOf(System.currentTimeMillis())

    // define the audio player
    // client that binds to the service
    private lateinit var aService: AudioService
    // variable to store if the service is bound
    private var aBound : Boolean = false

    // anonymous class definition and assignment to an instance of that class
    // the anonymous class overrides Service Connection
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, serviceBinder: IBinder?) {
            // binder is passed to serviceBinder by calling onBind() of Service
            // 'as' casts it
            val binder = serviceBinder as AudioService.AudioBinder
            // instance of the Service class itself
            // as getService returns this@AudioService
            aService = binder.getService()
            aBound = true
            Log.d(TAG, "Service connected")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            // this is called when android system loses connection to the service
            // not invoked when unbind() is called from the client side
            aBound = false
            Log.d(TAG, "Service disconnected")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = FusedLocationProviderClient(this)
        locationRequest = LocationRequest.create().apply {
            interval = 2000 // 2 seconds
            fastestInterval = 1000 // 1 second
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        // innitialize the db
        val db = SqliteDB(this, null)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                // val currTime = SystemClock.elapsedRealtimeNanos()
                // val thresholdTime = 2 * 1e9// half a minute for the threshold
                for (location in locationResult.locations) {
                    //val timeDiff = currTime - location.elapsedRealtimeNanos
                    // Log.e(TAG, "Time Diff: ${timeDiff} ${location.speed}")
                    position = (position+1)%2
                    positionMem = (positionMem+1)%maxObservations

                    var status = 0
                    if (locationMem.size ==2){
                        locationMem[position] = Pair(location.latitude, location.longitude)

                        var diff_lat = Math.abs(locationMem[0].first - locationMem[1].first)
                        var diff_long = Math.abs(locationMem[0].second - locationMem[1].second)

                        if (diff_lat<= 9e-6 && diff_long <= 9e-6){
                            // very less difference between position track of user
                            // indicates stationary
                            Log.e(ContentValues.TAG, "User is stationary")
                            status = 0
                        }else if (location.speed >= 2 && location.speed <4){
                            Log.e(ContentValues.TAG, "User is walking")
                            status = 1
                        }else if (location.speed >=4 && location.speed <9){
                            Log.e(ContentValues.TAG, "User is running")
                            status = 2
                        }else {
                            Log.e(ContentValues.TAG, "User is driving")
                            status = 3
                        }
                        //Log.e(TAG, "Driving..${location.speed}")
                        //Log.e(TAG, "Longitude Latitude: ${location.longitude} ${location.latitude}")
                        Log.e(ContentValues.TAG, "${locationMem} diff: ${diff_lat} ${diff_long} speed: ${location.speed}")
                    }else{
                        locationMem.add(position,Pair(location.latitude, location.longitude))
                    }
                    // add rolling buffer updates
                    if (observationWin.size<maxObservations){
                        observationWin.add(status)
                    }else{
                        observationWin[positionMem] = status
                        // update the status
                        val maxOccurrencesEntry = observationWin.groupingBy { it }
                            .eachCount()
                            .maxByOrNull { it.value }

                        recognizedActivity = maxOccurrencesEntry?.key ?: 0

                        if (recognizedActivity != previousState){
                            // logic for getting end time and display a toast
                            val endTime = System.currentTimeMillis()
                            val seconds = (endTime - startTime) / 1000
                            val minutes = seconds / 60
                            val extraSeconds = seconds % 60
                            val message = "${toastMessagesfromPrevious[previousState]} $minutes min $extraSeconds sec"
                            showActivityUpdateToast(message)

                            // process current date and time
                            val calendar = Calendar.getInstance()
                            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                            val date = dateFormat.format(calendar.time)
                            val time = timeFormat.format(calendar.time)

                            // store activity updates in the database
                            db.addEntry(date, time, minutes.toInt(), ActivityCodesToString[previousState] ?: "Null Activity")

                            // play music
                            // if the new activity is run start playing music
                            if (ActivityCodesToString[recognizedActivity] == "Run"){
                                if (aBound){
                                    aService?.startPlayback()
                                }
                            }else{
                                if (aBound){
                                    aService?.pausePlayback()
                                }
                            }

                            // update start time
                            startTime = endTime
                            // update previous state
                            previousState = recognizedActivity



                        }
                        Log.d(ContentValues.TAG, "Observation Win: ${observationWin}")
                        Log.d(ContentValues.TAG, "Stable Entry: ${recognizedActivity}")
                    }
                }
            }
        }

        // granting permissions if not already granted
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                Constants.REQUEST_CODE
            )
        }
        else{
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
        }

        setContent {
            ActivityRecognitionTheme {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // A surface container using the 'background' color from the theme
                    var displayActivity by remember { mutableStateOf(this.recognizedActivity) }
                    displayActivity = recognizedActivity
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Image(
                            painter = painterResource(id = ActivityCodesToBackground[displayActivity]!!),
                            contentDescription = "Screen App Background",
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.matchParentSize()
                        )
                        Column(modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center){
                            // Recognized Activity Logo
                            ElevatedCard {
                                Image(
                                    painter = painterResource(id = ActivityCodesToImage[displayActivity]!!),
                                    contentDescription = "Screen App Background",
                                    contentScale = ContentScale.FillBounds,
                                    modifier = Modifier.size(300.dp, 350.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                )
                            }
                            // Activity Message
                            ElevatedCard(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .width(300.dp)
                                    .height(150.dp)
                                    .background(color = Color.White)
                            ) {
                                Column(modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center){

                                    // first text with the recognized activity
                                    Text(text = "Recognized Activity: ${ActivityCodesToString[displayActivity]}\n ${ActivityCodesToDescription[displayActivity]}",
                                        style = TextStyle(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = Color.Black),
                                        textAlign = TextAlign.Center
                                    )

                                    // second text with the quote
                                    Text(text = "\"${activityQuotes[displayActivity]}\"",
                                        style = TextStyle(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = Color.Black),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    fun showActivityUpdateToast(message: String){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onStart(){
        super.onStart()
        // Intent takes the current context (which is this application) and
        // the class object of service
        // .also applies processing over the intent object as a pipeline after it
        // the code creates an intent object and uses it to bind to a service with
        // a service connection. Note: bind_auto_create creates the service if its not already created
        Intent(this, AudioService::class.java).also {
                intent -> bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
        Log.d(TAG, "onStart() of service")
    }
    // on app exit
    // we could also implement unbind with the connection in the onDestroy()
    // but onStop() comes before onDestroy() and terminates the bind
    override fun onStop() {
        super.onStop()
        // stop service
        unbindService(connection)
        aBound = false
    }

    override fun onPause(){
        super.onPause()
        if (aBound && ActivityCodesToString[recognizedActivity] == "Run"){
            aService?.pausePlayback()
        }
    }

    override fun onResume() {
        super.onResume()
        if (aBound && ActivityCodesToString[recognizedActivity] == "Run"){
            aService?.startPlayback()
        }
    }

    // Tag for debugging purposes
    companion object {
        private const val TAG = "MainActivity"
    }
}


