package com.nebulabook.cloudos.data.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.nebulabook.cloudos.MainActivity
import com.nebulabook.cloudos.NebulaApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class LaptopLidForegroundService : Service() {
    private val scope=CoroutineScope(SupervisorJob()+Dispatchers.IO)
    private var wakeLock:PowerManager.WakeLock?=null
    override fun onCreate() {
        super.onCreate()
        val pm=getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock=pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"NebulaBook:CloudSession").apply { acquire(8*60*60*1000L) }
        getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel(CHANNEL,"NebulaBook session",NotificationManager.IMPORTANCE_LOW))
    }
    override fun onStartCommand(intent:Intent?,flags:Int,startId:Int):Int {
        if(intent?.action==ACTION_STOP) {
            scope.launch {
                val ssh=(application as NebulaApplication).sessionSsh
                runCatching { ssh.execStreamingCommand("docker stop nebulabook_os 2>/dev/null || true").collect { } }
                ssh.disconnect()
                (application as NebulaApplication).sessionStopped.tryEmit(Unit)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf(startId)
            }
            return START_NOT_STICKY
        }
        val close=PendingIntent.getService(this,1,Intent(this,javaClass).setAction(ACTION_STOP),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val open=PendingIntent.getActivity(this,2,Intent(this,MainActivity::class.java),PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val notification=NotificationCompat.Builder(this,CHANNEL).setSmallIcon(android.R.drawable.ic_menu_view)
            .setContentTitle("NebulaBook · SSH session active")
            .setContentText("Remote desktop is connected. Close Lid requests container stop and closes the tunnel.")
            .setOngoing(true).setContentIntent(open).addAction(0,"Close Lid",close).build()
        startForeground(42,notification)
        return START_NOT_STICKY
    }
    override fun onDestroy() { scope.cancel(); wakeLock?.let { if(it.isHeld) it.release() }; wakeLock=null; super.onDestroy() }
    override fun onBind(intent:Intent?):IBinder?=null
    companion object { private const val CHANNEL="nebula_session"; const val ACTION_STOP="com.nebulabook.cloudos.STOP_SESSION" }
}
