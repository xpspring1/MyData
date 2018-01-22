package com.blackcat.xpsong.media

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.ctx
import org.jetbrains.anko.info
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.toast
import java.io.File

class MainActivity : AppCompatActivity(),AnkoLogger {
    private val TAKE_PHOTO=1
    private val PICK_PHOTO=2
    private var flag=0
    private lateinit var imageUri:Uri
    private var myMedia=MediaPlayer()
    var myHandler=MyHandler()
    lateinit var myThread:MyThread
    var allowRunning=true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //调用相机显示照片
        take_photo.onClick {
            var outputImage= File(externalCacheDir, "output_image.jpg")
            if(outputImage.exists()) {outputImage.delete()}
            outputImage.createNewFile()
            if(Build.VERSION.SDK_INT>=24){
                imageUri=FileProvider.getUriForFile(ctx,"com.blackcat.media.fileprovider",outputImage)
            }else{
                imageUri=Uri.fromFile(outputImage)
            }
            var intent=Intent("android.media.action.IMAGE_CAPTURE")
            intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri)
            startActivityForResult(intent,TAKE_PHOTO)
        }
        //从相册中选取照片
        pick_photo.onClick {
            if(ContextCompat.checkSelfPermission(ctx,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf("android.permission.WRITE_EXTERNAL_STORAGE"),1)
            }
            else {
                var intent=Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                startActivityForResult(intent,PICK_PHOTO)
            }
        }

        audio_play.onClick {
            if(ContextCompat.checkSelfPermission(ctx,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),1)
            }else{
                if(!myMedia.isPlaying){
                    myMedia.reset()
                    initMediaPlayer()
                    allowRunning=true
                    //线程被打断后，不能直接使用start(),必须先对其重新初始化！
                    myThread=MyThread("newThread")
                    myThread.start()
                }
            }

        }
        audio_kill.onClick {
            myThread.interrupt()
            info { if (myThread.isAlive) "线程未死" else "线程已死" }
            allowRunning=false
            myMedia.reset()
        }

        audio_test.onClick {
            info { if (myThread.isAlive) "线程未死" else "线程已死" }
        }
    }

     inner class MyHandler:Handler(){
         override fun handleMessage(msg: Message?) {
             super.handleMessage(msg)
             if (msg?.what==1){

                 myMedia.seekTo(210000)
                 myMedia.start()
             }
         }
     }
    inner class MyThread(name:String):Thread(name){
        var count=1
        var flag="线程第${count}次运行"
        override fun run() {
            super.run()
            try {
                while (allowRunning && !this.isInterrupted) {
                    info(flag)
                    var myMsg=Message()
                    myMsg.what=1
                    myHandler.sendMessage(myMsg)
                    sleep(26000)
                    count+=1
                    flag="线程第${count}次运行"
                }
            }
            catch (e:InterruptedException){
                e.printStackTrace()
                flag="线程被打断，不应该看到此提示"
                info("捕获到打断，执行返回")
                return
            }
        }
    }

    fun initMediaPlayer(){
        myMedia.setDataSource(File(Environment.getExternalStorageDirectory(),"funv.mp3").path)
        myMedia.prepare()
    }

    override fun onDestroy() {
        super.onDestroy()
        myMedia.stop()
        myMedia.release()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            TAKE_PHOTO -> {
                if(resultCode== Activity.RESULT_OK){
                    //var bitmap:Bitmap=BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))
                    //picture.setImageBitmap(bitmap)
                    Glide.with(ctx).load(imageUri).into(picture)
                }
            }
            PICK_PHOTO -> {
                if(resultCode== Activity.RESULT_OK){
                    Glide.with(ctx).load(data?.data).into(picture)
                }
            }
        }

    }

}
