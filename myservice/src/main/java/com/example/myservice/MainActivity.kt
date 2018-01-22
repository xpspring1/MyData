package com.example.myservice

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.ctx
import org.jetbrains.anko.info
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.toast

class MainActivity : AppCompatActivity(),AnkoLogger {
    var actBinder=MyTestService().DownloadBinder()
    var connection:ServiceConnection=object:ServiceConnection{
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            actBinder=service as MyTestService.DownloadBinder
            info("连接开启")
            actBinder.startDownload()
            actBinder.getProgress()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            info("连接断开")
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        asyncTask.onClick {
            DownlodaTask().execute()
        }

        startService.onClick {
            startService(Intent(this@MainActivity,MyTestService::class.java))
        }
        stopService.onClick {
            stopService(Intent(this@MainActivity,MyTestService::class.java))
        }
        bindService.onClick {
            bindService(Intent(this@MainActivity,MyTestService::class.java),connection, Context.BIND_AUTO_CREATE)
        }
        unbindService.onClick {
            unbindService(connection)
        }

    }

    inner class DownlodaTask:AsyncTask<Unit,Int,String>(){
        override fun onPreExecute() {
            super.onPreExecute()
            progressBar.visibility=VISIBLE

        }

        override fun doInBackground(vararg params: Unit?): String {

            var myresult="任务已完成"
            for(i in 1..10){
                Thread.sleep(500)
//                publishProgress(i)
            }
            return myresult
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            progressBar.visibility= INVISIBLE
            toast("$result")
        }

        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)

//            for (i:Int? in values){
//            progressBar.progress=i as Int
//            }
        }
    }
}
