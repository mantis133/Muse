package com.example.muse

import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private data class PlaylistPosition(val pos: Int)

class PlaylistSongSelection : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        setContentView(R.layout.playlist_song_list)
        val bigContainer = findViewById<LinearLayout>(R.id.SongList)

        loadNav()

        if (MediaManager.songs == null){val nothing = TextView(this);nothing.text = "No Playlist is loaded"; nothing.textSize = 30f; nothing.textAlignment = TextView.TEXT_ALIGNMENT_CENTER; nothing.setTextColor(resources.getColor(R.color.black)); bigContainer.addView(nothing);return}

        val songList = MediaManager.songs!!

        val scroll = ScrollView(this)
        scroll.isVerticalScrollBarEnabled = false
        val offsetPx = resources.getDimensionPixelOffset(R.dimen.loaded_playlist_bottom_offset)
        scroll.setPadding(0,0,0,offsetPx)
        bigContainer.addView(scroll)
        val host = LinearLayout(this)
        host.orientation = LinearLayout.VERTICAL
        scroll.addView(host)
        val context = this

        GlobalScope.launch(Dispatchers.IO){
            for (i in 0 until songList.size) {
                var song = songList[i]
                val file = File(song)
                val mmr = MediaMetadataRetriever()
                mmr.setDataSource(file.path) // not knowing this caused many problems
                val name = file.nameWithoutExtension
                var artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                artist = when (artist) {
                    null -> "Unknown"
                    else -> artist
                }
                val img = if (mmr.embeddedPicture != null) {
                    BitmapFactory.decodeByteArray(
                        mmr.embeddedPicture,
                        0,
                        mmr.embeddedPicture!!.size
                    )
                } else {
                    null
                }

                val container = LinearLayout(context)
                container.orientation = LinearLayout.HORIZONTAL
                if (MediaManager.playlistPosition == i) {
                    container.setBackgroundColor(resources.getColor(R.color.pink_light))
                    container.setOnClickListener{
                        Intent(context, MainActivity::class.java).also { startActivity(it) }
                        overridePendingTransition(R.anim.enter_from_right,R.anim.exit_to_left)
                    }
                } else {
                    container.setOnClickListener {
                        Log.d("some", "$i")
                        MediaManager.loadPlaylistPosition(i) // ERROR: Array index overflowing past
                        Intent(context, MainActivity::class.java).also { startActivity(it) }
                        Intent(context, MusicService::class.java).also { it.action = MusicService.Actions.UNREGISTER.toString(); startService(it) }
                        Intent(context, MusicService::class.java).also{ it.action = MusicService.Actions.START.toString();startService(it) }
                        Intent(context, MusicService::class.java).also { it.action = MusicService.Actions.PAUSED.toString(); startService(it) }
                        overridePendingTransition(R.anim.enter_from_right,R.anim.exit_to_left)
                    }
                }

                val titleAuthorContainer = LinearLayout(context)
                titleAuthorContainer.orientation = LinearLayout.VERTICAL
                titleAuthorContainer.gravity = Gravity.CENTER

                val titleView = TextView(context)
                titleAuthorContainer.addView(titleView)
                titleView.text = name

                val artistView = TextView(context)
                titleAuthorContainer.addView(artistView)
                artistView.text = artist

                val thumbnailView = ImageView(context)
                thumbnailView.setImageBitmap(img)
                val thumbnailViewWindowLayout = LinearLayout.LayoutParams(
                    200, 200
                )
                thumbnailView.layoutParams = thumbnailViewWindowLayout


                container.addView(thumbnailView)
                container.addView(titleAuthorContainer)

                withContext(Dispatchers.Main){
                    host.addView(container)
                }

            }
        }
    }

    private fun loadNav(){
        // I cbf writing a separate function per page so Im copy and pasting a template in every file.
        val playlistItemsButton = findViewById<ImageButton>(R.id.SongListButtonPLP)
        val musicControlsButton = findViewById<ImageButton>(R.id.mediaControlsButtonPLP)
        val playlistSelectionButton = findViewById<ImageButton>(R.id.SongSelectionButtonPLP)

//        playlistItemsButton.setOnClickListener {
//
//        }

        musicControlsButton.setOnClickListener {
            val i = Intent(this@PlaylistSongSelection, MainActivity::class.java)
            startActivity(i)
            overridePendingTransition(R.anim.enter_from_right,R.anim.exit_to_left)
        }

        playlistSelectionButton.setOnClickListener {
            val i = Intent(this@PlaylistSongSelection, SongSelection::class.java)
            startActivity(i)
            overridePendingTransition(R.anim.enter_from_right,R.anim.exit_to_left)
        }
    }

    fun sticksAndStones(pos:Int) : Int{
        return pos
    }
}