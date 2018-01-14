package com.blackcat.xpsong.media

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.ctx
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.io.File

class MainActivity : AppCompatActivity(),AnkoLogger {
    private val TAKE_PHOTO=1
    private val PICK_PHOTO=2
    private lateinit var imageUri:Uri
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
