package com.example.muse

import android.Manifest
import android.app.Activity
import android.app.Notification
import android.app.Notification.MediaStyle
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.session.MediaSession
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore.Audio.Media
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.ui.graphics.Color
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.lang.IllegalStateException
import java.io.File
import java.lang.IllegalArgumentException


class MainActivity : ComponentActivity() {
    private lateinit var PlayButton: ImageButton
    private lateinit var NextButton : ImageButton
    private lateinit var LastButton : ImageButton
    private lateinit var ShuffleButton : ImageButton
    private lateinit var LoopButton : ImageButton

    private lateinit var SongSeekBar: SeekBar
    private lateinit var CurrentSongPosText: TextView
    private lateinit var TotalSongTime: TextView

    private lateinit var ThumbnailImage: ImageView
    private lateinit var SongNameTexView: TextView
    private lateinit var ArtistTextView : TextView

    private lateinit var TestButton : ImageButton
    private lateinit var receiver : BroadcastReceiver


    private var PlaylistCurrentPosition = 2
    private var songPosition: Int? = 0


    // Declare the permissions as constants
    private val REQUEST_CODE = 1
    private val PERMISSIONS = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    // Check if the permissions are granted
    private fun hasPermissions(): Boolean {
        for (permission in PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    // Request the permissions if not granted
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_CODE)
    }

    // Handle the result of the permission request
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with accessing storage emulated 0
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(this, "Please grant the permissions to access storage emulated 0", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.music_controls)

        updateSongInfoRecursive()

        receiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                when(p1?.action){
                    "com.muse.update_song_info" -> {
                        updateSongInfo()
                    }
                }
            }
        }
        val intentFilter = IntentFilter().apply {
            addAction("com.muse.update_song_info")
        }

        registerReceiver(receiver,intentFilter)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("com.muse.music_channel", "Media Playback", NotificationManager.IMPORTANCE_LOW)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }


        PlayButton = findViewById<ImageButton>(R.id.PlayButton)
        NextButton = findViewById(R.id.NextButton)
        LastButton = findViewById(R.id.LastButton)
        ShuffleButton = findViewById(R.id.ShuffleButton)
        LoopButton = findViewById(R.id.LoopButton)

        SongSeekBar = findViewById(R.id.SongSeekBar)
        CurrentSongPosText = findViewById(R.id.CurrentSongPositionText)
        TotalSongTime = findViewById(R.id.TotalSongTimeText)

        ThumbnailImage = findViewById(R.id.ThumbNailImg)
        SongNameTexView = findViewById(R.id.SongName)
        ArtistTextView = findViewById(R.id.ArtistsNames)

//        ThumbnailImage.scaleType = ImageView.ScaleType.CENTER_CROP

        updateSongInfo()

        SongNameTexView.isSelected = true

        SongSeekBar.setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                CurrentSongPosText.text = "${MilliSecsToMins(progress)}"
                MediaManager.trackPosition = progress
                if (fromUser) {
                    MediaManager.mediaPlayer?.seekTo(progress)
                }

                if (progress == SongSeekBar.max){
//                    MediaManager.pause()
                    PlayButton.setImageResource(R.drawable.play_button)
//                    MediaManager.skipNext()
                    SongSeekBar.progress = 0
                    updateSongInfo()
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })


        val updateSeekBar = object : Runnable {
            override fun run() {
                try {
                    SongSeekBar.progress = MediaManager.mediaPlayer?.currentPosition ?: 0
                    SongSeekBar.postDelayed(this, 1000)
//                    updateSongInfoRecursive()
                } catch (e:IllegalStateException) {
                    // this just happens when the app swaps activity
                    // this is because the media player is released causing the top call in the try block to access a variable that doesn't exist
                    // idk what to put here cause leaving it blank just works
                }
            }
        }

        SongSeekBar.post(updateSeekBar)

        onSkipNext(NextButton)
        onSkipLast(LastButton)
        onPlayPause(PlayButton)
        onShuffle(ShuffleButton)
        onLoop(LoopButton)
        loadNav()

        val externalStorageVolumes: Array<File?> = ContextCompat.getExternalFilesDirs(this, null)
        val sdCardStorage = externalStorageVolumes
        for (file in sdCardStorage){
            Log.d("dirs","$file")
        }

    }

    override fun onStart() {
        super.onStart()
        Log.d("creation","start")

    }

    override fun onStop() {
        super.onStop()
        Log.d("creation","stop")
    }

//    private fun requestPermissions(){
//        when {
//            ContextCompat.checkSelfPermission(
//                this,
//                Manifest.permission.READ_EXTERNAL_STORAGE
//            ) == PackageManager.PERMISSION_GRANTED -> {
//                // perms allowed
//            }
//            ActivityCompat.shouldShowRequestPermissionRationale(
//                this,
//                Manifest.permission.READ_EXTERNAL_STORAGE
//            ) -> {
//                // man idek
//        } else -> {
//            // perms not asked yet
//            requestPermisionLauncher.launch(
//                Manifest.permission.READ_EXTERNAL_STORAGE
//            )
//        }
//
//        }
//    }

    private fun MilliSecsToMins(Milliseconds: Int): String {
        val seconds = Milliseconds / 1000
        val mins = seconds / 60
        val secs = seconds % 60
        val totalStringMins = String.format("%02d", mins)
        val totalStringSecs = String.format("%02d", secs)
        return "$totalStringMins:$totalStringSecs"
    }

    private fun loadNav(){
        // I cbf writing a separate function per page so Im copy and pasting a template in every file.
        val playlistItemsButton = findViewById<ImageButton>(R.id.SongListButton)
        val musicControlsButton = findViewById<ImageButton>(R.id.mediaControlsButton)
        val playlistSelectionButton = findViewById<ImageButton>(R.id.SongSelectionButton)

        playlistItemsButton.setOnClickListener {
            val i = Intent(this@MainActivity, PlaylistSongSelection::class.java)
            startActivity(i)
            overridePendingTransition(R.anim.enter_from_left,R.anim.exit_to_right)
        }

//        musicControlsButton.setOnClickListener {
//
//        }

        playlistSelectionButton.setOnClickListener {
            val i = Intent(this@MainActivity, SongSelection::class.java)
            startActivity(i)
            overridePendingTransition(R.anim.enter_from_right,R.anim.exit_to_left)
        }
    }

    private fun updateSongInfo(){
        try{
            val dur = MediaManager.mediaPlayer?.duration ?: 0
            SongSeekBar.max = dur
            TotalSongTime.text = MilliSecsToMins(dur)

            SongNameTexView.text = MediaManager.SongName
            ArtistTextView.text = MediaManager.ArtistName
            when(MediaManager.AlbumArtBitMap){
                null -> ThumbnailImage.setImageResource(R.drawable.home_icon)
                else -> ThumbnailImage.setImageBitmap(MediaManager.AlbumArtBitMap)
            }
        } catch (e: UninitializedPropertyAccessException){
            SongNameTexView.text = "No Song Playing"
            ArtistTextView.text = "Unknown"
            ThumbnailImage.setImageResource(R.drawable.home_icon)
        }
        if (MediaManager.isPaused) {
            PlayButton.setImageResource(R.drawable.play_button)
        } else {
            PlayButton.setImageResource(R.drawable.pause_button)
        }
    }

    private fun updateSongInfoRecursive(){
        try {
            val mediaPlayer = MediaManager.mediaPlayer!!
            mediaPlayer.setOnCompletionListener {
                MediaManager.skipNext()
                updateSongInfo()
                Intent(this, MusicService::class.java).also { it.action = MusicService.Actions.UPDATE.toString(); startService(it) }
                updateSongInfoRecursive()
            }
        } catch(e: NullPointerException) {

        }
    }

    private fun onSkipNext(button: ImageButton){
        button.setOnClickListener {
            MediaManager.skipNext()
            updateSongInfo()
            Intent(this, MusicService::class.java).also { it.action = MusicService.Actions.UPDATE.toString(); startService(it) }
        }
    }

    private fun onSkipLast(button: ImageButton){
        button.setOnClickListener {
            MediaManager.skipLast()
            updateSongInfo()
            Intent(this, MusicService::class.java).also { it.action = MusicService.Actions.UPDATE.toString(); startService(it) }
        }

    }

    private fun onPlayPause(button:ImageButton){
        if (MediaManager.isPaused) {
            button.setImageResource(R.drawable.play_button)
        } else {
            button.setImageResource(R.drawable.pause_button)
        }

        try {
            val mightFile = MediaManager.SongName
        } catch (e: UninitializedPropertyAccessException){
            return
        }

        button.setOnClickListener{
            MediaManager.isPaused = !MediaManager.isPaused
            if (MediaManager.isPaused) {
                button.setImageResource(R.drawable.play_button)
                MediaManager.pause()
                Intent(this, MusicService::class.java).also { it.action = MusicService.Actions.UNREGISTER.toString(); startService(it) }
                Intent(this, MusicService::class.java).also{ it.action = MusicService.Actions.START.toString();startService(it) }
                Intent(this, MusicService::class.java).also {it.action = MusicService.Actions.PLAYING.toString(); startService(it)}
            } else {
                Log.d("play","button")
                button.setImageResource(R.drawable.pause_button)
                MediaManager.play()
                Intent(this, MusicService::class.java).also { it.action = MusicService.Actions.UNREGISTER.toString(); startService(it) }
                Intent(this, MusicService::class.java).also{ it.action = MusicService.Actions.START.toString();startService(it) }
                Intent(this, MusicService::class.java).also { it.action = MusicService.Actions.PAUSED.toString(); startService(it) }

            }
        }
    }

    private fun onShuffle(button: ImageButton){

        if (MediaManager.playlist == null){return}

        if (MediaManager.shuffled) {
            button.setColorFilter(R.color.banana)
        } else {
            button.setColorFilter(null)
        }
        button.setOnClickListener{
            MediaManager.toggleShuffle()
            MediaManager.loadPlaylistTrack()
            MediaManager.play()
            updateSongInfo()
            Intent(this, MusicService::class.java).also { it.action = MusicService.Actions.UNREGISTER.toString(); startService(it) }
            Intent(this, MusicService::class.java).also{ it.action = MusicService.Actions.START.toString();startService(it) }
            Intent(this, MusicService::class.java).also { it.action = MusicService.Actions.PAUSED.toString(); startService(it) }
            if (MediaManager.shuffled) {
                button.setColorFilter(R.color.banana)
            } else {
                button.setColorFilter(null)
//                button.setColorFilter(R.color.darker_yellow)
            }
        }
    }

    private fun onLoop(button: ImageButton){
        if (MediaManager.looping) {
            button.setColorFilter(R.color.banana)
        } else {
            button.setColorFilter(null)
        }
        button.setOnClickListener {
            MediaManager.looping = !MediaManager.looping
            if (MediaManager.looping) {
                button.setColorFilter(R.color.banana)
            } else if (!MediaManager.looping) {
                button.setColorFilter(null)
//                button.setColorFilter(R.color.darker_yellow)
            }
        }
    }

    override fun onResume() { // think this is when the activity regains focus
        super.onResume()
        updateSongInfo()
//        songPosition?.let { mediaPlayer?.seekTo(it) }
//        mediaPlayer?.start()
        Log.d("resum", "resumed")
    }

    override fun onPause() { // think this means when the app is running but the activity is not in focus
        super.onPause()
//        mediaPlayer?.pause()
//        songPosition = mediaPlayer?.currentPosition
        Log.d("paws", "pawsed")
//        unregisterReceiver(receiver)
    }



    override fun onDestroy() {
        super.onDestroy()
        MediaManager.kill()
        unregisterReceiver(receiver)
        Intent(this, MusicService::class.java).also{it.action = MusicService.Actions.STOP.toString();startService(it)}
    }

}

