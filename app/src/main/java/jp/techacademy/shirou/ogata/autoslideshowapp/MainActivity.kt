package jp.techacademy.shirou.ogata.autoslideshowapp

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.os.Handler
import android.provider.MediaStore
import android.content.ContentUris
import android.database.Cursor
import android.graphics.Color
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(),View.OnClickListener {

    private var cursor:Cursor? = null

    private val PERMISSIONS_REQUEST_CODE = 100

    private var mTimer: Timer? = null
    private var mHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
        }

        //ボタンのクリックリスナ設定
        button_back.setOnClickListener(this)
        button_next.setOnClickListener(this)
        button_play.setOnClickListener(this)

    }

    override fun onClick(v: View) {
        if(cursor != null) {
            when (v.id) {
                R.id.button_back -> {
                    if(mTimer == null && !cursor!!.moveToPrevious()){
                        cursor!!.moveToLast()
                    }
                    showImage()
                }
                R.id.button_next -> {
                    if(mTimer == null && !cursor!!.moveToNext()){
                        cursor!!.moveToFirst()
                    }
                    showImage()
                }
                R.id.button_play -> {
                    if (mTimer == null){
                        mTimer = Timer()
                        mTimer!!.schedule(object : TimerTask() {
                            override fun run() {
                                mHandler.post {
                                    if(!cursor!!.moveToNext()){
                                        cursor!!.moveToFirst()
                                    }
                                    showImage()
                                }
                            }
                        }, 2000, 2000)
                        button_play.text ="停止"
                        button_next.isClickable = false
                        button_back.isClickable = false
                        button_next.setTextColor(Color.parseColor("#aaaaaa"))
                        button_back.setTextColor(Color.parseColor("#aaaaaa"))
                    }else{
                        mTimer!!.cancel()
                        mTimer = null
                        button_play.text = "再生"
                        button_next.isClickable = true
                        button_back.isClickable = true
                        button_next.setTextColor(Color.parseColor("#000000"))
                        button_back.setTextColor(Color.parseColor("#000000"))                    }
                }
            }
        }
    }

    private fun getContentsInfo() {
        // 画像の情報を取得する
        val resolver = contentResolver
        cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            null
        )

        //1枚目を表示
        if (cursor!!.moveToFirst()) {
            showImage()
        }
    }

    //現在のカーソル位置にある画像を表示
    private fun showImage(){
        val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
        val id = cursor!!.getLong(fieldIndex)
        val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)


        imageView.setImageURI(imageUri)
        textView.text = "${cursor!!.position + 1}/${cursor!!.count}"
    }

}
