package com.example.muse

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.os.Environment
import android.util.Log
import android.view.View
import java.io.File
import java.io.IOException


object MediaManager {
    var mediaPlayer: MediaPlayer? = null
    var trackPosition: Int = 0
    var isPaused = true
    var Shuffled = false
    var looping = false

    lateinit var SongName : String
    lateinit var ArtistName : String
    lateinit var AlbumArtBitMap : Bitmap

    fun getMediaPlayer(context: Context, path:String): MediaPlayer {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer().apply {
                try {
                    setDataSource(path)
                    prepare()
                } catch (e: IOException) {
                    e.printStackTrace()
                    Log.d("loading error", "File no load right")
                }
            }
        } else {
//            mediaPlayer!!.release()
//            mediaPlayer = MediaPlayer().apply {
//                try {
//                    setDataSource(path)
//                    prepare()
//                } catch (e: IOException) {
//                    e.printStackTrace()
//                }
//            }
        }
        val mmr = MediaMetadataRetriever()
        SongName = File(path).name
        val artists =mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUTHOR)
        ArtistName = when (artists){
            null -> "Unknown"
            else -> artists
        }

        return mediaPlayer!!
    }

    fun loadTrack(path: String){
        val location = Environment.getExternalStorageDirectory().absolutePath
        val file = File(location, path)
        mediaPlayer?.setDataSource(file.path)
        mediaPlayer?.prepare()
    }

    fun play(){
        mediaPlayer?.start()
    }

    fun pause(){
        mediaPlayer?.pause()
    }

    fun skipNext(){}

    fun skipLast(){}
}


private class MediaFunctions {

}



//private fun playAudio(view: View){
//    mediaPlayer?.start()
//}
//
//private fun pauseAudio(view: View){
//    if (mediaPlayer?.isPlaying == true) {
//        mediaPlayer?.pause()
//    }
//}
//
//private fun loadPlayer(Playlist: Array<String>){
//    var filepaths = arrayOf(
//        "/sdcard/Music/Bust Your Knee Caps (Johnny Don't Leave Me) - Pomplamoose.mp3",
//        "/sdcard/Music/Faith Marie - Toxic Thoughts.mp3",
//        "/sdcard/Music/The Jolly Rogers - XXV_ The Flying Dutchman.mp3"
//    )
//
//    mediaPlayer = MediaPlayer().apply{
//        try {
//            setDataSource(filepaths[currPos%3])
//            prepare()
//        } catch (e:IOException) {
//            e.printStackTrace()
//        }
//    }
//}