package com.llk.bsd

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {
    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }
    external fun bsPatch(oldApk: String, patchFile: String, output: String)

    lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        context = this

        sample_text.text = BuildConfig.VERSION_NAME
    }

    fun update(view: View) {
        makeFile()
    }

    fun makeFile() = runBlocking {
        val job = async {
            var patch = File(Environment.getExternalStorageDirectory(), "patch").getAbsolutePath()
            var oldApk = getApplicationInfo().sourceDir
            var output = createNewApk().getAbsolutePath()
            Log.e("llk", "makeFile: old=${oldApk}, patch=${patch}, output=${output}")

            if (File(patch).exists() && File(output).exists()) {
                Log.e("llk", "bsPatch")
                bsPatch(oldApk, patch, output)
            }

            return@async File(output)
        }

        installApk(job.await())
    }

    fun installApk(apk: File){
        if(!apk.exists()){
            return
        }

        Log.e("llk", "installApk")

        var intent = Intent(Intent.ACTION_VIEW)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var fileUri = FileProvider.getUriForFile(context, context.getApplicationInfo().packageName + ".fileprovider", apk)
            intent.setDataAndType(fileUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(apk), "application/vnd.android.package-archive")
        }
        context.startActivity(intent)
    }

    fun createNewApk(): File{
        var apk = File(Environment.getExternalStorageDirectory(), "bsdiff.apk")
        if(!apk.exists()){
            try {
                apk.createNewFile()
            }catch (e:IOException){
                Log.e("llk", "!!!!createNewApk: IOException e=${e.localizedMessage}")
            }
        }
        return apk
    }
}
