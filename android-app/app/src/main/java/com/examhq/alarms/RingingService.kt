package com.examhq.alarms

import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.IBinder

class RingingService : Service() {
    private var player: MediaPlayer? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) { stopNow(); return START_NOT_STICKY }
        val alarmIntent = intent ?: Intent()
        startForeground(9201, Notifications.alarmNotification(this, alarmIntent))
        if (player == null) {
            try {
                val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                player = MediaPlayer().apply {
                    setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
                    setDataSource(this@RingingService, uri); isLooping = true; prepare(); start()
                }
            } catch (_: Exception) { }
        }
        return START_STICKY
    }
    private fun stopNow() { try { player?.stop() } catch (_: Exception) {}; player?.release(); player = null; stopForeground(STOP_FOREGROUND_REMOVE); stopSelf() }
    override fun onDestroy() { try { player?.release() } catch (_: Exception) {}; player = null; super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null
    companion object { const val ACTION_START = "com.examhq.alarms.START"; const val ACTION_STOP = "com.examhq.alarms.STOP" }
}
