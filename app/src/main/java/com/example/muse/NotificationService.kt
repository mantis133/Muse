package com.example.muse

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat

class NotificationService: Service() {

    private lateinit var mediaSession: MediaSession
    private var wakeLock : PowerManager.WakeLock? = null

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()

//        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
//
//        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "muse::NotificationWakeLock")
//
//        wakeLock?.acquire(30*60*1000L /*30 minutes*/)

        val receiver = BroadcastTransiver()
        val filter = IntentFilter().apply {
            addAction("com.muse.ACTION_pause_play")
            addAction("com.muse.ACTION_next")
            addAction("com.muse.ACTION_last")
        }

        registerReceiver(receiver,filter)

        val notificationOpenAppIntent = Intent(this, MainActivity::class.java)
        notificationOpenAppIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP

        val pendingNotificationOpenAppIntent = PendingIntent.getActivity(this, 70000000, notificationOpenAppIntent, PendingIntent.FLAG_MUTABLE)

        mediaSession = MediaSession(this, "player service")

        mediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS
                or MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS
        )

        val stateBuilder = PlaybackState.Builder()
            .setActions(PlaybackState.ACTION_PLAY
                    or PlaybackState.ACTION_PAUSE
                    or PlaybackState.ACTION_SKIP_TO_NEXT
                    or PlaybackState.ACTION_SKIP_TO_PREVIOUS
            )
        mediaSession.setPlaybackState(stateBuilder.build())

        mediaSession.setCallback(MySessionCallback())

// Start the Media Session since the activity is active
        mediaSession.isActive = true

        val metadataBuilder = MediaMetadata.Builder()
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_TITLE, "fck")
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_ARTIST, "Artist Title")
        mediaSession.setMetadata(metadataBuilder.build())

        val mediaStyle = Notification.MediaStyle()

        val pauseIntent = Intent("com.muse.ACTION_pause_play")
        val pendingPauseIntent = PendingIntent.getBroadcast(this, 743729, pauseIntent, PendingIntent.FLAG_MUTABLE)

        val skipNextIntent = Intent("com.muse.ACTION_next")
        val pendingNextIntent = PendingIntent.getBroadcast(this, 743730, skipNextIntent, PendingIntent.FLAG_MUTABLE)

        val skipLastIntent = Intent("com.muse.ACTION_last")
        val pendingLastIntent = PendingIntent.getBroadcast(this, 743731, skipLastIntent, PendingIntent.FLAG_MUTABLE)

        val pauseAction = Notification.Action.Builder(
            R.drawable.pause_button,
            "pause",
            pendingPauseIntent
        ).build()

        val skipNextAction = Notification.Action.Builder(
            R.drawable.next_button,
            "Skip Next",
            pendingNextIntent
        ).build()

        val skipLastAction = Notification.Action.Builder(
            R.drawable.last_button,
            "Skip Last",
            pendingLastIntent
        ).build()

        val notification = Notification.Builder(this@NotificationService, "com.muse.mediaInfo")
            .setStyle(mediaStyle)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setSmallIcon(R.drawable.home_icon)
            .setLargeIcon(MediaManager.AlbumArtBitMap)
            .setContentTitle(MediaManager.SongName)
            .setContentText(MediaManager.ArtistName)
            .setProgress(50, 200, false)
            .setAutoCancel(false)
            .setTimeoutAfter(86400000 * 3)
            .addAction(skipLastAction)
            .addAction(pauseAction)
            .addAction(skipNextAction)
            .setContentIntent(pendingNotificationOpenAppIntent)
            .build()
        mediaStyle.setShowActionsInCompactView(0,1,2)
        

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("com.muse.mediaInfo", "Media Playback", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(1000, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession.release()
    }
}

private class MySessionCallback : MediaSession.Callback() { // need to replace the calls to MediaManager with intent call to clean up the code
    override fun onPlay() {
        // Handle the play action
        Log.d("playyyy","playyyy")
        MediaManager.play()
    }

    override fun onPause() {
        // Handle the pause action
        Log.d("puasess","puasess")
        MediaManager.pause()
    }

    override fun onSkipToNext() {
        super.onSkipToNext()
        Log.d("next","next")
        MediaManager.skipNext()
    }

    override fun onSkipToPrevious() {
        super.onSkipToPrevious()
        Log.d("last","last")
        MediaManager.skipLast()
    }
}