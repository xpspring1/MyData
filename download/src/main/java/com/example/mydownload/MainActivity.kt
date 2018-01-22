package com.example.mydownload

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.toast

class MainActivity : AppCompatActivity() {
    //定义一个binder和一个connection,与服务进行绑定
    var downloadBinder=DownloadService().DownloadBinder()
    var connection=object:ServiceConnection{
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            downloadBinder=service as DownloadService.DownloadBinder
        }

        override fun onServiceDisconnected(name: ComponentName?) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var intent=Intent(this@MainActivity,DownloadService::class.java)
        startService(intent)        //启动服务
        bindService(intent,connection, Context.BIND_AUTO_CREATE)        //绑定服务
        //获取权限
        if(ContextCompat.checkSelfPermission(ctx,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),1)
        }
        //设置按钮动作
        startDownload.onClick {
            var url="http://127.0.0.1/CloudMusic.apk"
            downloadBinder.startDownlaod(url)
        }
        pauseDownlaod.onClick {
            downloadBinder.pauseDownload()
        }
        cancelDownload.onClick {
            downloadBinder.cancelDownload()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            1 -> {
                if(grantResults.size>0 && grantResults[0]!=PackageManager.PERMISSION_GRANTED){
                    toast("拒绝权限将无法使用程序")
                    finish()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(connection)
    }
}

