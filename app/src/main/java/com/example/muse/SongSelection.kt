package com.example.muse

import android.content.ComponentName
import android.os.Bundle
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Environment
import android.os.PersistableBundle
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.core.view.setMargins
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.muse.ui.theme.MuseTheme
import com.example.muse.util.Playlist
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Vector

class SongSelection : ComponentActivity() {

    lateinit var con : MediaController

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        setContentView(R.layout.song_selection);


        val externalStorageVolumes: Array<File?> = ContextCompat.getExternalFilesDirs(this, null)
        val sdCardStorage = externalStorageVolumes[1] // should be sd card. causes a error when no sd card in present (java.lang.ArrayIndexOutOfBoundsException)

//        val musicDir = File(Environment.getExternalStorageDirectory(), "Music")
//        val musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        val playlistDir = File(sdCardStorage, MediaManager.playlistLocation)
        var files = playlistDir.listFiles()

        val displaySpecs = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displaySpecs)
        val screenWidth = displaySpecs.widthPixels
        val screenHeight = displaySpecs.heightPixels

        val topLayer = findViewById<LinearLayout>(R.id.TopLayer)
        val scrollView = ScrollView(this)
        val superLinearLayout = LinearLayout(this)
        superLinearLayout.orientation = LinearLayout.VERTICAL
        scrollView.addView(superLinearLayout)
        topLayer.addView(scrollView)


        files?.forEach { file ->
            Log.d("file", "File: ${file.name}")
            if (file.extension == "m3u"){
                val pl = MediaManager.quickBuildPlaylistFromM3UFile(file, sdCardStorage!!)
                val linearLayout = LinearLayout(this)
                linearLayout.orientation = LinearLayout.VERTICAL

                val playlistName = TextView(this)
                playlistName.text = file.nameWithoutExtension
                playlistName.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                playlistName.textSize = 50f
                playlistName.setTextColor(resources.getColor(R.color.black))

                val playlistCover = ImageView(this)
                when (pl.cover){
                    null -> playlistCover.setImageResource(R.drawable.home_icon)
                    else -> playlistCover.setImageBitmap(pl.cover)
                }
                playlistCover.adjustViewBounds = true
                playlistCover.scaleType = ImageView.ScaleType.FIT_CENTER
                playlistCover.setBackgroundColor(resources.getColor(R.color.nice_blue))


                linearLayout.setOnClickListener {
                    Log.d("click ", "click")
//                    val mediaPlayer = Intent(this@SongSelection, MainActivity::class.java)
//                    startActivity(mediaPlayer)
                    val list = MediaManager.buildPlaylistFromM3UFile(file, sdCardStorage)
                    MediaManager.loadPlaylist(list)
                    MediaManager.initialize(con) // con should exist by the point this is clicked ???
                    val l = list.songs.get(0)

                    if (MediaManager.loadPlaylistIntoMediaSession(list.songs, con)){
                        con.prepare()
                    }
                }

                val maxSize = Math.min(screenWidth,screenHeight)


                val margins = 70

                val layoutParams = LinearLayout.LayoutParams(
                    maxSize,maxSize
                )
                val nameParams = LinearLayout.LayoutParams(
                    maxSize-(margins*2), 40
                )
//                layoutParams.marginStart = margins
//                layoutParams.marginEnd = margins
//                nameParams.marginStart = margins
//                layoutParams.setMargins(margins)
//                nameParams.setMargins(margins)
                playlistCover.layoutParams = layoutParams
//                playlistName.layoutParams = nameParams
//                linearLayout.layoutParams = layoutParams
//
                linearLayout.addView(playlistCover)
                linearLayout.addView(playlistName)

                superLinearLayout.addView(linearLayout)
            }
        }

        loadNav()

    }

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        val externalStorageVolumes: Array<File?> = ContextCompat.getExternalFilesDirs(this, null)
//        val sdCardStorage = externalStorageVolumes[1] // should be sd card. causes a error when no sd card in present (java.lang.ArrayIndexOutOfBoundsException)
//
////        val musicDir = File(Environment.getExternalStorageDirectory(), "Music")
//        val musicDir = sdCardStorage?.absolutePath.let { File(it) }
////        val musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
//        val playlistDir = File(musicDir, MediaManager.playlistLocation)
//        var files = playlistDir.listFiles()
//
//        val displaySpecs = DisplayMetrics()
//        windowManager.defaultDisplay.getMetrics(displaySpecs)
//        val screenWidth = displaySpecs.widthPixels
//        val screenHeight = displaySpecs.heightPixels
//
//        val playlists = files.map { MediaManager.quickBuildPlaylistFromM3UFile(File(it.path), sdCardStorage!!) }
//
//        setContent {
//            MuseTheme{
//                Column {
////                    val data by remember { mutableStateOf("Initial Data") }
//                    for (file in files){
//                        val data: Playlist;
//                        LaunchedEffect(Unit) {
//
//                            // Perform your long-running task here (e.g., network call, database query)
//                            val result = withContext(Dispatchers.IO) {
//                                MediaManager.quickBuildPlaylistFromM3UFile(
//                                    file,
//                                    sdCardStorage!!
//                                )
//                            }
//
//                            // Update the UI with the result
//                            data = result
//                        }
//
//                        PlaylistCard(playlist = data)
//                    }
//                }
//            }
//        }
//    }

    override fun onStart() {
        super.onStart()
        val sessionToken = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        val controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture.addListener({con = controllerFuture.get()}, MoreExecutors.directExecutor())
    }

    private fun loadNav(){
        // I cbf writing a separate function per page so Im copy and pasting a template in every file.
        val playlistItemsButton = findViewById<ImageButton>(R.id.songListButton)
        val musicControlsButton = findViewById<ImageButton>(R.id.mediaControlsButton)
        val playlistSelectionButton = findViewById<ImageButton>(R.id.songSelectionButton)

        playlistItemsButton.setOnClickListener {
            val i = Intent(this@SongSelection, PlaylistSongSelection::class.java)
            startActivity(i)
            overridePendingTransition(R.anim.enter_from_left,R.anim.exit_to_right)
        }

        musicControlsButton.setOnClickListener {
            val i = Intent(this@SongSelection, MainActivity::class.java)
            startActivity(i)
            overridePendingTransition(R.anim.enter_from_left,R.anim.exit_to_right)
        }

        playlistSelectionButton.setOnClickListener {

        }
    }

    override fun onStop() {
        super.onStop()
        con.release()
    }
}