package com.blackcat.xpsong.sharedprefop

import android.app.Activity
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteOpenHelper
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.support.v4.app.ActivityCompat
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.toast
import java.io.File


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var pref=getSharedPreferences("sharedData", Context.MODE_PRIVATE)
        var editor:SharedPreferences.Editor=pref.edit()
        writeBtn.onClick {
            editor.putString("name","宋小二")
            editor.putInt("age",33)
            editor.putBoolean("married",false)
            editor.apply()        }
        clearBtn.onClick {
            prefData.setText("")
        }
        readBtn.onClick {
            var data:String="name is:"+pref.getString("name","")+"   age is: "+pref.getInt("age",0)+"if married is :"+pref.getBoolean("married",true)
            prefData.setText(data)
        }
        //创建数据库
        SQLiteDatabase.loadLibs(this) //在所有数据库操作之前执行

        var mydb=MySQLiteOpenHelper(ctx,"BigBookStore.db",null,1)
        var db=mydb.getWritableDatabase("mylittlekitty")
        var values=ContentValues()

        adddata.onClick {
            values.put("name","The song code")
            values.put("author","xpspring1")
            values.put("pages",454)
            values.put("price",19.98)
            db.insert("Book",null,values)
            values.clear()
            values.put("name","The song's second code")
            values.put("author","xpspring2")
            values.put("pages",499)
            values.put("price",12.98)
            db.insert("Book",null,values)
            values.clear()
            values.put("name","The song's third code")
            values.put("author","xpspring3")
            values.put("pages",999)
            values.put("price",11.28)
            db.insert("Book",null,values)
            values.clear()
        }
        updatedata.onClick {
            values.put("price",126.66)
            db.update("Book",values,"author=?", arrayOf("xpspring1"))
            values.clear()
        }
        deletedata.onClick {
            db.delete("Book","id=?", arrayOf("2"))
        }
        querydata.onClick {
            var mCursor=db.query("Book", arrayOf("author"),"id=?", arrayOf("5"),null,null,null)
            if(mCursor.moveToFirst()){
                var bookauthor=mCursor.getString(mCursor.getColumnIndex("author"))
                prefData.setText(bookauthor)
            }
            mCursor.close()
        }
        //拨打电话
        phoneCall.onClick {
            if (ContextCompat.checkSelfPermission(ctx,android.Manifest.permission.CALL_PHONE)!=PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf("android.permission.CALL_PHONE"),1)
            }
            else{
                var intent= Intent(Intent.ACTION_CALL)
                //intent.setData(Uri.parse("tel:10010"))
                intent.data=Uri.parse("tel:10010")
                startActivity(intent)
                }
        }
        //读取联系人
        contactInfo.onClick {
            if (ContextCompat.checkSelfPermission(ctx,android.Manifest.permission.READ_CONTACTS)!=PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf("android.permission.READ_CONTACTS"),1)
            }
            else{
                readContact()
            }
        }
        //发送通知
        notification.onClick {
            var intent=Intent(ctx,NotifyActivity::class.java)
            var pi:PendingIntent= PendingIntent.getActivity(ctx,0,intent,0)
            var nManager=getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            var myNotification=NotificationCompat.Builder(ctx,"testNotify")
                    .setContentTitle("这是标题")
                    .setContentText("这是通知正文")
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    //.setSound(Uri.fromFile(File("/system/media/audio/ringtones/Hello_Ya.ogg")))
                    //.setVibrate(longArrayOf(0,1000,1000,1000,1000,1000))
                    //.setLights(Color.RED,1000,1000)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .build()
            nManager.notify(1,myNotification)
        }


    }
    fun readContact(){
        var cursor:Cursor=contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,null)
        var info:String=""
        while (cursor.moveToNext()){
            var name=cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            var number=cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            info=info+name+number
        }
        prefData.setText(info)
    }
    class MySQLiteOpenHelper(context:Context, name:String, factory: SQLiteDatabase.CursorFactory?, version:Int ):SQLiteOpenHelper(context, name, factory, version){
        private val CREATE_BOOK:String=
                "create table Book("+
                "id integer primary key autoincrement,"+
                "author text,"+
                "price real,"+
                "pages integer,"+
                "name text)"
        private val CREATE_CATEGORY:String=
                "create table Category("+
                "id integer primary key autoincrement,"+
                "category_name text,"+
                "category_code integer)"
        private var mContext:Context=context
        override fun onCreate(db: SQLiteDatabase?) {
            db?.execSQL(CREATE_BOOK)
            db?.execSQL(CREATE_CATEGORY)
            Toast.makeText(mContext,"Create succeeded!",Toast.LENGTH_LONG).show()
        }

        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
            db?.execSQL("drop table if exists Book")
            db?.execSQL("drop table if exists Category")
            onCreate(db)
        }
    }
}
