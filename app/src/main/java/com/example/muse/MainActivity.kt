package com.example.muse

import android.Manifest
import android.app.Activity
import android.app.Notification
import android.app.Notification.MediaStyle
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.session.MediaSession
import android.os.Bundle
import android.os.Environment
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


//    private lateinit var mediaPlayer: MediaPlayer
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

        setContentView(R.layout.music_controls);

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

        ThumbnailImage.setImageResource(R.drawable.home_icon)

        updateSongInfo()

        SongSeekBar.setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                CurrentSongPosText.text = "${MilliSecsToMins(progress)}"
                MediaManager.trackPosition = progress
                if (fromUser) {
                    MediaManager.mediaPlayer?.seekTo(progress)
                }

                if (progress == SongSeekBar.max){
                    MediaManager.pause()
                    PlayButton.setImageResource(R.drawable.play_button)
//                    MediaManager.skipNext()
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

        }

//        musicControlsButton.setOnClickListener {
//
//        }

        playlistSelectionButton.setOnClickListener {
            val i = Intent(this@MainActivity, SongSelection::class.java)
            startActivity(i)
            overridePendingTransition(R.anim.fade_in,R.anim.fade_out)
        }
    }

    private fun updateSongInfo(){
        try{
            val dur = MediaManager.mediaPlayer?.duration ?: 0
            SongSeekBar.max = dur
            TotalSongTime.text = MilliSecsToMins(dur)

            SongNameTexView.text = MediaManager.SongName
            ArtistTextView.text = MediaManager.ArtistName
        } catch (e: UninitializedPropertyAccessException){
            SongNameTexView.text = "No Song Playing"
            ArtistTextView.text = "Unknown"
        }
    }

    private fun onSkipNext(button: ImageButton){
        button.setOnClickListener {
            MediaManager.skipNext()
            updateSongInfo()
        }
    }

    private fun onSkipLast(button: ImageButton){
        button.setOnClickListener {
            MediaManager.skipLast()
            updateSongInfo()
        }

    }

    private fun onPlayPause(button:ImageButton){
        if (MediaManager.isPaused) {
            button.setImageResource(R.drawable.play_button)
        } else {
            button.setImageResource(R.drawable.pause_button)
        }
        button.setOnClickListener{
            MediaManager.isPaused = !MediaManager.isPaused
            if (MediaManager.isPaused) {
                button.setImageResource(R.drawable.play_button)
                MediaManager.pause()
            } else {
                button.setImageResource(R.drawable.pause_button)
                MediaManager.play()
            }
        }
    }

    private fun onShuffle(button: ImageButton){
        if (MediaManager.Shuffled) {
            button.setColorFilter(R.color.purple_500)
        } else {
            button.setColorFilter(null)
        }
        button.setOnClickListener{
            MediaManager.Shuffled = !MediaManager.Shuffled
            if (MediaManager.Shuffled) {
                button.setColorFilter(R.color.purple_500)
            } else {
                button.setColorFilter(null)
            }
        }
    }

    private fun onLoop(button: ImageButton){
        if (MediaManager.looping) {
            button.setColorFilter(R.color.purple_500)
        } else {
            button.setColorFilter(null)
        }
        button.setOnClickListener {
            MediaManager.looping = !MediaManager.looping
            if (MediaManager.looping) {
                button.setColorFilter(R.color.purple_500)
            } else {
                button.setColorFilter(null)
            }
        }
    }

    override fun onResume() { // think this is when the activity regains focus
        super.onResume()
//        songPosition?.let { mediaPlayer?.seekTo(it) }
//        mediaPlayer?.start()
        Log.d("resum", "resumed")
    }

    override fun onPause() { // think this means when the app is running but the activity is not in focus
        super.onPause()
//        mediaPlayer?.pause()
//        songPosition = mediaPlayer?.currentPosition
        Log.d("paws", "pawsed")
    }

    override fun onDestroy() {
        super.onDestroy()
        MediaManager.kill()
    }

}

