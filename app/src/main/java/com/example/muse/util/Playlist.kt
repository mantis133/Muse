package com.example.muse.util

import android.graphics.Bitmap
import androidx.media3.common.MediaItem
import java.util.Vector

data class Playlist(
    val path : String,
    val title: String,
    val cover: Bitmap?,
    val songs: List<Song>,
    val size: Int
)

data class Song(
    val path : String,
    val name: String,
    val artists: String,
    val albumCover: Bitmap?,
) {
    fun intoMediaItem() : MediaItem {
        return MediaItem.fromUri(path)
    }
}

