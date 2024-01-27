package com.example.muse

import android.util.Log
import java.io.IOException
import java.io.File
import java.util.Vector

data class Playlist(val title: String, val coverPath: String?, val songs: Vector<String>)

class Lib {
    fun read_dir(path:String){
        val dir = File(path)
        if (dir.isFile){
            throw Exception("needs to be a directory idiot")
        }
        var files = dir.listFiles()

        files.forEach {
            file -> run {
                Log.d("File Name", "${file.name}")
                if (file.extension == "m3u") {
                    Log.d("content", "${file.readLines()}")
                } else if (file.extension == "mp3"){
                    Log.d("Real Name", "${file.nameWithoutExtension}")
                }
            }
        }
    }

    fun read_m3u(path:String): Playlist{
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
            if (File(line).isFile){
                songs.add(line)
            } else if (line.startsWith("#EXTIMG")){
                playlistArt = line.replace("#EXTIMG: ", "")
            }
        }
        return Playlist(title, playlistArt, songs)
    }
}