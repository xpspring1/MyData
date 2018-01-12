package com.blackcat.xpsong.mydata

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.io.*

class MainActivity : AppCompatActivity() {

    lateinit var writer:BufferedWriter
    lateinit var reader:BufferedReader
    lateinit var out:FileOutputStream
    lateinit var ins:FileInputStream
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        writeBtn.onClick {
            var text:String= dataText.text.toString()
            out=openFileOutput("mydata", Context.MODE_APPEND)
            writer= BufferedWriter(OutputStreamWriter(out))
            writer.write(text)
            writer.flush()

        }
        readBtn.onClick {
            ins=openFileInput("mydata")
            reader= BufferedReader(InputStreamReader(ins))
            var text:String=reader.readLine()
            dataText.setText(text)

        }
        clear.onClick {
            dataText.setText("")
        }
    }
}
