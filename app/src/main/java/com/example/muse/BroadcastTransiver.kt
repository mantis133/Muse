package com.example.muse

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BroadcastTransiver:BroadcastReceiver() {
    var counter = 0

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("Intent","message recieved")
        when(intent.action){
            "com.muse.ACTION_pause_play" -> {
                counter += 1
                Log.d("Intent", "PlayPause. counter:$counter")
                if (MediaManager.isPaused){
                    Log.d("pause","was paused")
                    MediaManager.play()

                } else {
                    Log.d("playinh","was playing")
                    MediaManager.pause()
                }

            }

            "com.muse.ACTION_next" -> {
                Log.d("Intent","Next")
                MediaManager.skipNext()
            }
            "com.muse.ACTION_last" -> {
                Log.d("Intent","Last")
                MediaManager.skipLast()
            }
            else -> {
                Log.d("Intent","fucking nothing bitch")
            }
        }
        val updateMainActivity = Intent("com.muse.update_song_info")
        context.sendBroadcast(updateMainActivity)
    }
}