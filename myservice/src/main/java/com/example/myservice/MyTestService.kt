package com.example.myservice

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

class MyTestService : Service() ,AnkoLogger{
    var myBinder=DownloadBinder()

    override fun onBind(intent: Intent): IBinder? {
        return myBinder
    }

    override fun onCreate() {
        super.onCreate()
        info("onCreate executed")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        info("onStartCommand executed")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        info("onDestroy executed")
    }

     inner class DownloadBinder:Binder(){
        fun startDownload(){
            info("Binder.startDownload is executed")
        }
        fun getProgress(){
            info("Binder.getProgerss is executed")
        }
    }
}
