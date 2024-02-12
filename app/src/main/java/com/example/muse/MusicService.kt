package com.example.muse

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.os.IBinder
import android.util.Log

class MusicService : Service() {

    private lateinit var receiver: BroadcastReceiver
    private lateinit var notification: Notification
    private val TAG = "MUSIC_SERVICE"

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Actions.START.toString() -> {
                start()
            }
            Actions.STOP.toString() -> {
                stopSelf()
            }
            Actions.UNREGISTER.toString() -> {
                try {
                    unregisterReceiver(receiver)
                } catch(e:UninitializedPropertyAccessException){ /* triggers when the receiver is already registered */ }
            }
            Actions.PAUSED.toString() -> {
//                Intent("com.muse.ACTION_pause_play").also { sendBroadcast(it) }
                Log.d("some","some")
                setPauseIcon(1)
            }
            Actions.PLAYING.toString() -> {
//                Intent("com.muse.ACTION_pause_play").also { sendBroadcast(it) }
                Log.d("some","other")
                setPlayIcon(1)
            }
            Actions.UPDATE.toString() -> {
                notification = updateNotifInfo()
                startForeService()
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    private fun start(){
        registerBroadcastReceiver()

        val mediaSession = buildMediaSession()

        notification = updateNotifInfo()

        startForeService()

    }

    enum class Actions {
        START,
        STOP,
        UNREGISTER,
        PAUSED,
        PLAYING,
        UPDATE
    }

    private fun registerBroadcastReceiver(){
        receiver = BroadcastTransiver()
        val filter = IntentFilter().apply {
            addAction("com.muse.ACTION_pause_play")
            addAction("com.muse.ACTION_next")
            addAction("com.muse.ACTION_last")
        }

        registerReceiver(receiver,filter)
    }

    private fun buildMediaSession() : MediaSession {
        val mediaSession = MediaSession(this, "player service")

        val stateBuilder = PlaybackState.Builder().setActions(
                PlaybackState.ACTION_PLAY
                    or PlaybackState.ACTION_PAUSE
                    or PlaybackState.ACTION_SKIP_TO_NEXT
                    or PlaybackState.ACTION_SKIP_TO_PREVIOUS
        )

        mediaSession.setPlaybackState(stateBuilder.build())
        mediaSession.setCallback(AccessoriesFunctions())

        // Start the Media Session since the activity is active
        mediaSession.isActive = true

        val metadataBuilder = MediaMetadata.Builder()
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_TITLE, "fck")
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_ARTIST, "Artist Title")
        metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ART,MediaManager.AlbumArtBitMap)
        mediaSession.setMetadata(metadataBuilder.build())

        return mediaSession
    }

    private fun updateNotifInfo() : Notification{

        val mediaStyle = Notification.MediaStyle()

        val notificationOpenAppIntent = Intent(this, MainActivity::class.java)
        notificationOpenAppIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        val pendingNotificationOpenAppIntent = PendingIntent.getActivity(this, 70000000, notificationOpenAppIntent, PendingIntent.FLAG_MUTABLE)

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

        mediaStyle.setShowActionsInCompactView(0,1,2)

        return Notification.Builder(this, "com.muse.music_channel")
            .setStyle(mediaStyle)

            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setSmallIcon(R.drawable.home_icon)
            .setLargeIcon(MediaManager.AlbumArtBitMap)
            .setContentTitle(MediaManager.SongName)
            .setContentText(MediaManager.ArtistName)
            .setColor(resources.getColor(R.color.purple_200))

            .addAction(skipLastAction)
            .addAction(pauseAction)
            .addAction(skipNextAction)
            .setContentIntent(pendingNotificationOpenAppIntent)
            .build()
    }

    private fun setPauseIcon(actionPosition:Int){
        notification.actions[actionPosition] = Notification.Action.Builder(
            R.drawable.pause_button,
            "Pause",
            PendingIntent.getBroadcast(this, 743729, Intent("com.muse.ACTION_pause_play"), PendingIntent.FLAG_MUTABLE)
        ).build()
        startForeService()
    }

    private fun setPlayIcon(actionPosition: Int){
        notification.actions[actionPosition] = Notification.Action.Builder(
            R.drawable.play_button,
            "Play",
            PendingIntent.getBroadcast(this, 743729, Intent("com.muse.ACTION_pause_play"), PendingIntent.FLAG_MUTABLE)
        ).build()
        startForeService()
    }

    private fun startForeService(){
        if (Build.VERSION.SDK_INT >= 29){
            startForeground( 1, notification, FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(1, notification)
        }
    }
}


private class AccessoriesFunctions : MediaSession.Callback() { // need to replace the calls to MediaManager with intent call to clean up the code
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