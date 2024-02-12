package com.example.muse

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.registerReceiver
import java.io.File
import java.io.FileNotFoundException
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

    var playlist: Playlist? = null

    lateinit var SongName : String
    lateinit var ArtistName : String
    var AlbumArtBitMap : Bitmap? = null



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
        if (playlist == null){return}
        if (playlist!!.songs.size == 0){return}
        val file = File(playlist!!.songs[playlistPosition])
        mediaPlayer = if (mediaPlayer == null) {
            MediaPlayer()
        } else {
            mediaPlayer!!.release()
            MediaPlayer()
        }
        mediaPlayer?.setDataSource(file.path)
        mediaPlayer?.prepare()

        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(file.path) // not knowing this caused many problems
        SongName = file.nameWithoutExtension
        val artists = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
        ArtistName = when (artists){
            null -> "Unknown"
            else -> artists
        }
        AlbumArtBitMap = if (mmr.embeddedPicture != null) {BitmapFactory.decodeByteArray(mmr.embeddedPicture, 0, mmr.embeddedPicture!!.size)} else {null}
        mediaPlayer!!.setOnCompletionListener{
            skipNext()

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
        if (playlist == null){return}
        playlistPosition += 1
        if (playlistPosition >= playlist!!.songs.size){
            if (!looping) {
                playlistPosition = playlist!!.songs.size - 1
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
        if (playlist == null){return}
        playlistPosition -= 1
        if (playlistPosition < 0) {
            if (!looping){
                playlistPosition = 0
                mediaPlayer?.seekTo(0)
            } else {
                playlistPosition = playlist!!.songs.size - 1
                pause()
                loadPlaylistTrack()
                play()
            }
        } else if (trackPosition > 30_000) {
            mediaPlayer?.seekTo(0)
            playlistPosition += 1
        } else {
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
        var playlistArtPath: String? = null
        lines.forEach { line ->
            line.replace("\n","")
            if (File(localStorage?.absolutePath, line).isFile){
                songs.add(localStorage?.absolutePath+"/"+line)
            } else if (File(sdCardStorage?.absolutePath, line).isFile) {
                songs.add(sdCardStorage?.absolutePath+"/"+line)
            } else if (line.startsWith("#EXTIMG")){
                playlistArtPath = line.replace("#EXTIMG: ", "")
                playlistArtPath = "${ sdCardStorage }/Thumbnails/${playlistArtPath}"
            }
        }
        return Playlist(title, playlistArtPath, songs, songs.size)
    }

    fun quick_read_m3u(path: String, storage:String): Playlist{
        var playlistFile: File? = null
        var playlistArtPath:String? = null
        try {
            playlistFile = File(path)
            playlistFile.readLines().forEach { line ->
                line.replace("\n","")
                if (line.startsWith("#EXTIMG")){
                    playlistArtPath = line.replace("#EXTIMG: ", "")
                    playlistArtPath = "${ storage }/Thumbnails/${playlistArtPath}"
                }
            }

        } catch(e:FileNotFoundException){

        } finally {
            val title = playlistFile!!.nameWithoutExtension
            return Playlist(title, playlistArtPath, Vector<String>(), 0)
        }
    }

    fun kill(){
        pause()
        mediaPlayer?.release()
    }
}

private class MediaFunctions {
    fun getStorageDirectories(context: Context){
        val externalStorageVolumes: Array<File?> = ContextCompat.getExternalFilesDirs(context, null)
        val localStorage = externalStorageVolumes[0] // should be primary / local storage
        val sdCardStorage = externalStorageVolumes[1] // should be sd card
        StorageLocations.values().size
    }

    private enum class StorageLocations{
        Internal,
        SDCard
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