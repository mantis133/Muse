package com.example.muse

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException
import java.util.Vector

data class Playlist(val title: String, val coverPath: String?, val songs: Vector<String>, val size:Int)

object MediaManager {
    val playlistLocation = "Playlists"
    var mediaPlayer: MediaPlayer? = null
    var playlistPosition: Int = 0
    var isPaused = true
    var Shuffled = false
    var looping = false

    var trackPosition: Int = 0

    lateinit var playlist: Playlist

    lateinit var SongName : String
    lateinit var ArtistName : String
    lateinit var AlbumArtBitMap : Bitmap

    fun loadTrack(path: String){
        val location = Environment.getExternalStorageDirectory().absolutePath

        val file = File(location, path)
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer()
        } else {
            mediaPlayer!!.release()
            mediaPlayer = MediaPlayer()
        }
        mediaPlayer?.setDataSource(file.path)
        mediaPlayer?.prepare()

        val mmr = MediaMetadataRetriever()
        SongName = File(path).name
        val artists =mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUTHOR)
        ArtistName = when (artists){
            null -> "Unknown"
            else -> artists
        }
    }

    fun loadPlaylist(list:Playlist){
        playlistPosition = 0
        playlist = list
        isPaused = true
    }

    fun loadPlaylistTrack(){
        val file = File(playlist.songs[playlistPosition])
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer()
        } else {
            mediaPlayer!!.release()
            mediaPlayer = MediaPlayer()
        }
        mediaPlayer?.setDataSource(file.path)
        mediaPlayer?.prepare()

        val mmr = MediaMetadataRetriever()
        SongName = file.name
        val artists = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUTHOR)
        ArtistName = when (artists){
            null -> "Unknown"
            else -> artists
        }
    }

    fun play(){
        isPaused = false
        mediaPlayer?.start()
    }

    fun pause(){
        isPaused = true
        mediaPlayer?.pause()
    }

    fun skipNext(){
        playlistPosition += 1
        if (playlistPosition >= playlist.songs.size){
            if (!looping) {
                playlistPosition = playlist.songs.size - 1
            } else {
                playlistPosition = 0
                pause()
                loadPlaylistTrack()
                play()
            }

        } else {
            pause()
            loadPlaylistTrack()
            play()
        }
    }

    fun skipLast(){
        playlistPosition -= 1
        if (playlistPosition < 0) {
            if (!looping){
                playlistPosition = 0
                mediaPlayer?.seekTo(0)
            } else {
                playlistPosition = playlist.songs.size - 1
                pause()
                loadPlaylistTrack()
                play()
            }
        } else if (trackPosition > 30_000) {
            Log.d("did", "correct")
            mediaPlayer?.seekTo(0)
            playlistPosition += 1
        } else {
            Log.d("wroing", "bad")
            pause()
            loadPlaylistTrack()
            play()
        }
    }

    fun read_m3u(path:String, context: Context): Playlist{

        val externalStorageVolumes: Array<File?> = ContextCompat.getExternalFilesDirs(context, null)
        val localStorage = externalStorageVolumes[0] // should be primary / local storage
        val sdCardStorage = externalStorageVolumes[1] // should be sd card

        val playlistFile = File(path)
        if (playlistFile.extension != "m3u") {
            throw Exception("not a playlist file")
        }
        val title = playlistFile.nameWithoutExtension
        var lines = playlistFile.readLines()
        var songs = Vector<String>()
        var playlistArt: String? = null
        lines.forEach { line ->
            line.replace("\n","")
            if (File(localStorage?.absolutePath, line).isFile){
                songs.add(localStorage?.absolutePath+"/"+line)
            } else if (File(sdCardStorage?.absolutePath, line).isFile) {
                songs.add(sdCardStorage?.absolutePath+"/"+line)
            } else if (line.startsWith("#EXTIMG")){
                playlistArt = line.replace("#EXTIMG: ", "")
            }
        }
        return Playlist(title, playlistArt, songs, songs.size)
    }

    fun kill(){
        mediaPlayer?.release()
    }
}


private class MediaFunctions {
    fun getStorageDirectories(context: Context){
        val externalStorageVolumes: Array<File?> = ContextCompat.getExternalFilesDirs(context, null)
        val localStorage = externalStorageVolumes[0] // should be primary / local storage
        val sdCardStorage = externalStorageVolumes[1] // should be sd card
    }
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