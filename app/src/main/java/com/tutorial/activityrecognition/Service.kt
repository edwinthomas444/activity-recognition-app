package com.tutorial.activityrecognition
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder

// create a bound service class
// clients attach to the service
class AudioService: Service(){
    private lateinit var mediaPlayer: MediaPlayer

    // a variable that is returned in onBind() callback function
    private val binder = AudioBinder()

    inner class AudioBinder : Binder(){
        fun getService(): AudioService = this@AudioService
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer.create(this, R.raw.run_audio)
    }

    // unbind calls onDestroy if all connections to service are disconnected
    override fun onDestroy(){
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
    }

    // custom functions which is called from the client
    fun startPlayback() {
        // called when app activity is resumed
        mediaPlayer?.start()
    }

    fun pausePlayback() {
        // called when app activity is paused
        mediaPlayer?.pause()
    }

}