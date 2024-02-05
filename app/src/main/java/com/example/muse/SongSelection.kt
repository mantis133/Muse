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
import androidx.core.view.marginTop
import androidx.core.view.setMargins
import java.io.File

class SongSelection : ComponentActivity() {

    private lateinit var TestButton : Button

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        setContentView(R.layout.song_selection);


        val externalStorageVolumes: Array<File?> = ContextCompat.getExternalFilesDirs(this, null)
        val sdCardStorage = externalStorageVolumes[1]?.absolutePath // should be sd card

//        val musicDir = File(Environment.getExternalStorageDirectory(), "Music")
        val musicDir = File(sdCardStorage)
//        val musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        val playlistDir = File(musicDir, MediaManager.playlistLocation)
        var files = musicDir.listFiles()

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
                val linearLayout = LinearLayout(this)
                linearLayout.orientation = LinearLayout.VERTICAL

                val playlistName = TextView(this)
                playlistName.text = file.nameWithoutExtension

                val playlistCover = ImageView(this)
                playlistCover.setImageResource(R.drawable.home_icon)
                playlistCover.adjustViewBounds = true
                playlistCover.scaleType = ImageView.ScaleType.FIT_CENTER
                playlistCover.setBackgroundColor(resources.getColor(R.color.pink_light))


                linearLayout.setOnClickListener {
                    Log.d("click ", "click")
                    val mediaPlayer = Intent(this@SongSelection, MainActivity::class.java)
                    startActivity(mediaPlayer)
                    val list = MediaManager.read_m3u(file.path, this)
                    MediaManager.loadPlaylist(list)
                    MediaManager.loadPlaylistTrack()
                }

//                playlistCover.setOnClickListener {
//                    val mediaPlayer = Intent(this@SongSelection, MainActivity::class.java)
//                    startActivity(mediaPlayer)
//                    val list = MediaManager.read_m3u(file.path,this)
//                    MediaManager.loadPlaylist(list)
//
//                }
//
                val maxSize = Math.min(screenWidth,screenHeight)

                val bitmap = BitmapFactory.decodeResource(resources, R.drawable.home_icon)

                val margins = 70

                val layoutParams = LinearLayout.LayoutParams(
                    maxSize-(margins*2),maxSize-(margins*2)
                )
                val nameParams = LinearLayout.LayoutParams(
                    maxSize-(margins*2), 40
                )
//                layoutParams.marginStart = margins
//                layoutParams.marginEnd = margins
//                nameParams.marginStart = margins
//                layoutParams.setMargins(margins)
//                nameParams.setMargins(margins)
//                playlistCover.layoutParams = layoutParams
//                playlistName.layoutParams = nameParams
//                linearLayout.layoutParams = layoutParams
//
                linearLayout.addView(playlistCover)
                linearLayout.addView(playlistName)

                superLinearLayout.addView(linearLayout)
            }
        }
//        setContentView(scrollView)



//        val lib = Lib()
//        lib.read_dir("/sdcard/Music")




    }
}