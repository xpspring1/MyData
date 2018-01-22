package com.example.myhttp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.net.URL

class MainActivity : AppCompatActivity() ,AnkoLogger{

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        httpRequest.onClick {
            Thread(Runnable {
                var url=URL("http://192.168.1.102/get_data.xml")
                var myClient=OkHttpClient()
                var myRequest=Request.Builder()
                        .url(url)
                        .build()
                var myResponse=myClient.newCall(myRequest).execute()
                var mytext:String=myResponse.body()!!.string()
                parseXMLWithPull(mytext)
               // runOnUiThread {
               //     responseText.text=mytext  //此处不能使用子线程的变量进行计算，会引发网络操作在主线程的错误
               // }
            }).start()
        }
        jsonParse.onClick {
            Thread(Runnable {
                var url=URL("http://192.168.1.102/get_data.json")
                var myClient=OkHttpClient()
                var myRequest=Request.Builder()
                        .url(url)
                        .build()
                var myResponse=myClient.newCall(myRequest).execute()
                var mytext:String=myResponse.body()!!.string()
                parseJSONWithGson(mytext)

            }).start()
        }
    }
    fun parseJSONWithGson(jsonData:String){
        var gson=Gson()
        var appList:List<App> =gson.fromJson(jsonData,object:TypeToken<List<App>>(){}.type)
        for(item:App in appList){
            info("id is ${item.getId()}")
            info("name is ${item.getName()}")
            info("version is ${item.getVersion()}")
        }
    }
    fun parseXMLWithPull(xmlData:String){
        var factory=XmlPullParserFactory.newInstance()
        var xmlPullParser=factory.newPullParser()
        var eventType=xmlPullParser.eventType
        xmlPullParser.setInput(StringReader(xmlData))
        var id=""
        var name=""
        var version=""
        while (eventType!=XmlPullParser.END_DOCUMENT){
            var nodename=xmlPullParser.name
            when(eventType){
                XmlPullParser.START_TAG -> {
                    when(nodename){
                        "id" -> id=xmlPullParser.nextText()
                        "name" -> name=xmlPullParser.nextText()
                        "version" -> version=xmlPullParser.nextText()
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (nodename=="app"){
                        info("id is $id")
                        info("name is $name")
                        info("version is $version")
                    }
                }
            }
            eventType=xmlPullParser.next()
        }

    }
    class App{
        private var id:String=""
        private var name:String=""
        private var version:String=""
        fun getId():String{return id}
        fun getName():String{return name}
        fun getVersion():String{return version}
        fun setId(id:String){this.id=id}
        fun setName(name:String){this.name=name}
        fun setVersion(version:String){this.version=version}
    }
}
