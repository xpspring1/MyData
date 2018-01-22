package com.example.mydownload

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Binder
import android.os.Environment
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jetbrains.anko.ctx
import org.jetbrains.anko.toast
import java.io.File
import java.io.InputStream
import java.io.RandomAccessFile

class DownloadService : Service() {
    private var downloadTask:DownloadTask?=null
    private var downloadUrl:String?=null
    private var listener=object:DownloadListener{
        override fun onProgress(progress: Int) {
            getNotificationManager().notify(1,getNotification("Downloading...",progress))
        }

        override fun onSuccess() {
            downloadTask=null
            stopForeground(true)
            getNotificationManager().notify(1,getNotification("Download Success",-1))
            toast("Download Success!")
        }

        override fun onFailed() {
            downloadTask=null
            stopForeground(true)
            getNotificationManager().notify(1,getNotification("Download Failed",-1))
            toast("Download Failed!")
        }

        override fun onPaused() {
            downloadTask=null
            toast("Download Paused!")
        }

        override fun onCancled() {
            downloadTask=null
            stopForeground(true)
            toast("Download Canceled!")
        }
    }
    //定义两个Notification函数
    private fun getNotificationManager():NotificationManager{
        var gnm=getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return gnm
    }
    private fun getNotification(title:String,progress: Int):Notification{
        var intent=Intent(ctx,MainActivity::class.java)
        var pi= PendingIntent.getActivity(ctx,0,intent,0)
        var builder=NotificationCompat.Builder(ctx,"download")
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(resources,R.mipmap.ic_launcher))
                .setContentIntent(pi)
                .setContentTitle(title)
        if(progress>0){
            builder.setContentText("$progress%")
            builder.setProgress(100,progress,false)
        }
        return builder.build()
    }

    //返回绑定对象
    private var mBinder=DownloadBinder()
    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    //定义DownloadBinder,与Activity绑定后用来操控异步的DownloadTask对象,因此加inner前缀
    inner class DownloadBinder:Binder(){
        fun startDownlaod(url:String){
            if(downloadUrl==null){
                downloadUrl=url
                downloadTask= DownloadTask(listener)
                downloadTask!!.execute(downloadUrl)
                startForeground(1,getNotification("Downloading...",0))
                toast("Downloading...")
            }
        }
        fun pauseDownload(){
            if(downloadTask!=null){
                downloadTask!!.pauseDownload()
            }
        }
        fun cancelDownload(){
            if (downloadTask!=null){
                downloadTask!!.cancelDownload()
            }else{
                if(downloadUrl !=null){
                    var filename:String=downloadUrl!!.substring(downloadUrl!!.lastIndexOf("/"))
                    var directory:String=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path
                    var file= File(directory+filename)
                    if(file.exists()){file.delete()}
                    getNotificationManager().cancel(1)
                    stopForeground(true)
                    toast("Download Canceled!")
                }
            }
        }
    }
}
//定义异步下载任务类
class DownloadTask(paramlistener:DownloadListener) :AsyncTask<String,Int,Int>(){

    val TYPE_SUCCESS=0
    val TYPE_FAILED=1
    val TYPE_PAUSED=2
    val TYPE_CANCELED=3

    private var listener:DownloadListener=paramlistener
    private var isCanceled=false
    private var isPaused=false
    private var lastProgress=0


    override fun doInBackground(vararg params: String): Int {
        var ins:InputStream?=null
        var savedFile:RandomAccessFile?=null
        var file:File?=null
        try {
            var downloadLength: Long = 0
            var downloadUrl: String = params[0]
            var filename: String = downloadUrl.substring(downloadUrl.lastIndexOf("/"))
            var directory: String = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path
            file = File(directory + filename)
            if (file.exists()) {
                downloadLength = file.length()
            }
            var contentLength: Long = getContentLength(downloadUrl)
            if (contentLength == 0L) { //Long型要加L符号
                return TYPE_FAILED
            } else if (contentLength == downloadLength) {
                return TYPE_SUCCESS
            }
            //开始执行下载任务
            var client = OkHttpClient()
            var request = Request.Builder()
                    .addHeader("RANGE", "bytes=$downloadLength-")
                    .url(downloadUrl)
                    .build()
            var response = client.newCall(request).execute()
            if (response != null) {
                ins=response.body()!!.byteStream()
                savedFile=RandomAccessFile(file,"rw")
                savedFile.seek(downloadLength)
                var b=ByteArray(1024)
                var total=0
                var len=0
                fun myread():Int{ len=ins.read(b);return len}
                while (myread()!=-1){
                    if(isCanceled){
                        return TYPE_CANCELED
                    }else if(isPaused){
                        return TYPE_PAUSED
                    }else{
                        total+=len
                        savedFile.write(b,0,len)
                        //var progress:Int=((total+downloadLength)*100/contentLength)
                        TODO()

                    }
                }

                //TODO
            }
        }catch (e:Exception){
            e.printStackTrace()
        }finally {
            //TODO()
        }


        return TYPE_FAILED
    }

    override fun onProgressUpdate(vararg values: Int?) {
        var progress=values[0]
        if(progress!!>lastProgress){
            listener.onProgress(progress)
            lastProgress=progress
        }

    }

    override fun onPostExecute(result: Int?) {
        when(result){
            TYPE_SUCCESS -> {listener.onSuccess()}
            TYPE_FAILED -> {listener.onFailed()}
            TYPE_PAUSED -> {listener.onPaused()}
            TYPE_CANCELED -> {listener.onCancled()}
        }
    }
    fun pauseDownload(){isPaused=true}
    fun cancelDownload(){isPaused=true}
    fun getContentLength(downloadUrl:String):Long{
        var client=OkHttpClient()
        var request=Request.Builder()
                .url(downloadUrl)
                .build()
        var response=client.newCall(request).execute()
        if(response!=null&&response.isSuccessful){
            var long=response.body()!!.contentLength()
            return long
        }
        return 0
    }
}
//定义借口
interface DownloadListener{
    fun onProgress(progress:Int)
    fun onSuccess()
    fun onFailed()
    fun onPaused()
    fun onCancled()
}