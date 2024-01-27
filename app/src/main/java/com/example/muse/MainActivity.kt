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
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.ui.graphics.Color
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

    private lateinit var TestButton : Button


    private lateinit var mediaPlayer: MediaPlayer
    private var PlaylistCurrentPosition = 2
    private var songPosition: Int? = 0


    private val requestPermisionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean -> if (isGranted) {
        Log.i("Permission :","Granted")
        } else {
            Log.i("Permission :", "Denied")
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


        val filepath = "/sdcard/Music/Bust Your Knee Caps (Johnny Don't Leave Me) - Pomplamoose.mp3"
        val filepath2 = "/sdcard/Music/T & Sugah x NCT - Stardust (feat. Miyoki) [Lyrics].mp3"
        val filepath3 = "/sdcard/Music/Faith Marie - Toxic Thoughts.mp3"
        Log.d("can I change this","cool dudes")

//        mediaPlayer = MediaManager.getMediaPlayer(this, filepath)
        mediaPlayer = MediaManager.getMediaPlayer(this, filepath2)


        val dur = mediaPlayer.duration ?: 0
        SongSeekBar.max = dur
        TotalSongTime.text = MilliSecsToMins(dur)

        SongNameTexView.text = MediaManager.SongName
        ArtistTextView.text = MediaManager.ArtistName



        SongSeekBar.setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                CurrentSongPosText.text = "${MilliSecsToMins(progress)}"
                if (fromUser) {
                    mediaPlayer.seekTo(progress)
                }

                if (progress == SongSeekBar.max){
                    MediaManager.skipNext()
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })



        val updateSeekBar = object : Runnable {
            override fun run() {
                try {
                    SongSeekBar.progress = mediaPlayer.currentPosition ?: 0
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

        TestButton = findViewById(R.id.TESTBUTTON)
        TestButton.setOnClickListener {
            val i = Intent(this@MainActivity, SongSelection::class.java)
            startActivity(i)
        }
    }

    private fun requestPermissions(){
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                // perms allowed
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) -> {
                // man idek
        } else -> {
            // perms not asked yet
            requestPermisionLauncher.launch(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }

        }
    }

    private fun MilliSecsToMins(Milliseconds: Int): String {
        val seconds = Milliseconds / 1000
        val mins = seconds / 60
        val secs = seconds % 60
        val totalStringMins = String.format("%02d", mins)
        val totalStringSecs = String.format("%02d", secs)
        return "$totalStringMins:$totalStringSecs"
    }

    private fun onSkipNext(button: ImageButton){
        button.setOnClickListener {
            PlaylistCurrentPosition += 1
        }
    }

    private fun onSkipLast(button: ImageButton){
        button.setOnClickListener {
            PlaylistCurrentPosition -= 1

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

    override fun onResume() {
        super.onResume()
//        songPosition?.let { mediaPlayer?.seekTo(it) }
//        mediaPlayer?.start()
        Log.d("resum", "resumed")
    }

    override fun onPause() {
        super.onPause()
//        mediaPlayer?.pause()
//        songPosition = mediaPlayer?.currentPosition
        Log.d("paws", "pawsed")
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }

}

//@Composable
//fun Greeting(name: String, modifier: Modifier = Modifier) {
//    Text(
//        text = "Hello $name!",
//        modifier = modifier
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    MuseTheme {
//        Greeting("Android")
//    }
//}