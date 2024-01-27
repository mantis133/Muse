package com.example.muse

import android.os.Bundle
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.view.marginTop
import androidx.core.view.setMargins
import java.io.File

class SongSelection : ComponentActivity() {

    private lateinit var TestButton : Button

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        setContentView(R.layout.song_selection);
        overridePendingTransition(R.anim.fade_in,R.anim.fade_out)
        TestButton = findViewById(R.id.TESTCONTROLS)

        TestButton.setOnClickListener {
            var i = Intent(this@SongSelection, MainActivity::class.java)
            startActivity(i)
        }

        val dir = File("/sdcard/Music/Playlists/")
        var files = dir.listFiles()

        val displaySpecs = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displaySpecs)
        val screenWidth = displaySpecs.widthPixels
        val screenHeight = displaySpecs.heightPixels

        val scrollView = ScrollView(this)
        val superLinearLayout = LinearLayout(this)
        scrollView.addView(superLinearLayout)


        files?.forEach { file ->
            Log.d("file", "File: ${file.name}")
            if (file.extension == "m3u"){
                val linearLayout = LinearLayout(this)
                linearLayout.orientation = LinearLayout.VERTICAL

                val playlistName = TextView(this)
                playlistName.text = file.nameWithoutExtension

                val playlistCover = ImageButton(this)
                playlistCover.setImageResource(R.drawable.home_icon)
                playlistCover.scaleType = ImageView.ScaleType.CENTER_INSIDE


                val maxSize = Math.min(screenWidth,screenHeight)

                val bitmap = BitmapFactory.decodeResource(resources, R.drawable.home_icon)

                val margins = 70

                val layoutParams = LinearLayout.LayoutParams(
                    maxSize-(margins*2),maxSize-(margins*2)
                )
                val nameParams = LinearLayout.LayoutParams(
                    maxSize-(margins*2), 40
                )
                layoutParams.marginStart = margins
                layoutParams.marginEnd = margins
                nameParams.marginStart = margins
                layoutParams.setMargins(margins)
                nameParams.setMargins(margins)
                playlistCover.layoutParams = layoutParams
                playlistName.layoutParams = nameParams
                linearLayout.layoutParams = layoutParams

                linearLayout.addView(playlistCover)
                linearLayout.addView(playlistName)

                superLinearLayout.addView(linearLayout)
            }
        }
        setContentView(scrollView)



//        val lib = Lib()
//        lib.read_dir("/sdcard/Music")




    }
}