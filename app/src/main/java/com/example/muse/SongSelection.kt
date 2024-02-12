package com.example.muse

import android.os.Bundle
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Environment
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.core.view.setMargins
import java.io.File

class SongSelection : ComponentActivity() {

    private lateinit var TestButton : Button

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        setContentView(R.layout.song_selection);


        val externalStorageVolumes: Array<File?> = ContextCompat.getExternalFilesDirs(this, null)
        val sdCardStorage = externalStorageVolumes[1]?.absolutePath // should be sd card. causes a error when no sd card in present (java.lang.ArrayIndexOutOfBoundsException)

//        val musicDir = File(Environment.getExternalStorageDirectory(), "Music")
        val musicDir = File(sdCardStorage)
//        val musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        val playlistDir = File(musicDir, MediaManager.playlistLocation)
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
                Log.d("song list", "${MediaManager.read_m3u(file.path,this).songs}")
                val pl = MediaManager.quick_read_m3u(file.path, sdCardStorage!!)
                val linearLayout = LinearLayout(this)
                linearLayout.orientation = LinearLayout.VERTICAL

                val playlistName = TextView(this)
                playlistName.text = file.nameWithoutExtension
                playlistName.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                playlistName.textSize = 50f
                playlistName.setTextColor(resources.getColor(R.color.black))

                val playlistCover = ImageView(this)
                when (pl.coverPath){
                    null -> playlistCover.setImageResource(R.drawable.home_icon)
                    else -> playlistCover.setImageBitmap(BitmapFactory.decodeFile(pl.coverPath))
                }
                playlistCover.adjustViewBounds = true
                playlistCover.scaleType = ImageView.ScaleType.FIT_CENTER
                playlistCover.setBackgroundColor(resources.getColor(R.color.nice_blue))


                linearLayout.setOnClickListener {
                    Log.d("click ", "click")
                    val mediaPlayer = Intent(this@SongSelection, MainActivity::class.java)
                    startActivity(mediaPlayer)
                    val list = MediaManager.read_m3u(file.path, this)
                    MediaManager.loadPlaylist(list)
                    MediaManager.loadPlaylistTrack()
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
}