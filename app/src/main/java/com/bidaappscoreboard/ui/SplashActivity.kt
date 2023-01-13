package com.bidaappscoreboard.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bidaappscoreboard.BuildConfig
import com.bidaappscoreboard.MainActivity
import com.bidaappscoreboard.R
import com.bidaappscoreboard.config.AppConfig.initAppConfig
import com.bidaappscoreboard.service.initApolloClient
import com.bidaappscoreboard.service.saveLogcatToFile
import com.bidaappscoreboard.store.CameraStore
import com.bidaappscoreboard.store.HttpHeaders
import com.bidaappscoreboard.store.Metadata
import com.bidaappscoreboard.store.ScoreBoard
import com.github.ybq.android.spinkit.sprite.Sprite
import com.github.ybq.android.spinkit.style.ThreeBounce
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File


@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    var TAG = "SplashActivity"
    private var versionName: String = BuildConfig.VERSION_NAME

    private companion object{
        //PERMISSION request constant, assign any value
        private const val STORAGE_PERMISSION_CODE = 100
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_splash)

        val progressBar = findViewById<View>(R.id.spin_kit) as ProgressBar
        val textVersion = findViewById<View>(R.id.textVersion) as TextView

        val threeBounce: Sprite = ThreeBounce()
        threeBounce.color = Color.RED
        progressBar.indeterminateDrawable = threeBounce

        textVersion.text = "Version $versionName"

        super.onCreate(savedInstanceState)

        initLaunch(this)
    }

    private fun initLaunch(activity: Activity) {
        lifecycleScope.launch {
            // waiting for Main context init
            while (MainActivity.appContext == null) {
                delay(2000)
            }

            // setup env
            initAppConfig(activity)

            // init camera
            CameraStore.initCamera(MainActivity.appContext!!)

            if (checkPermission()){
                Log.d(TAG, "onCreate: Permission already granted, create folder")
                checkPermissionOK()
            } else {
                Log.d(TAG, "onCreate: Permission was not granted, request")
                requestPermission()
            }

            // check permission
            // while (!com.bidaappscoreboard.service.checkPermission(activity)) {
            //  delay(3000)
            // }
        }
    }

    private suspend fun checkPermissionOK(){
        // Folder Movies
        val folderName = "${Environment.DIRECTORY_DOCUMENTS}/scoreboard/"
        //create folder using name we just input
        val file = File("${Environment.getExternalStorageDirectory()}/$folderName")
        //create folder
        var folderCreated = true
        if (!file.exists()) {
            folderCreated = file.mkdirs()
        }
        Log.d(TAG, "create folder:::$folderCreated:::${file.path}")

        //show if folder created or not
        if (folderCreated) {
            // Logcat
            if (ScoreBoard.enableDebug) {
                saveLogcatToFile(MainActivity.appContext!!)
            }

            HttpHeaders.init(MainActivity.appContext!!)
            initApolloClient(MainActivity.appContext!!)

            // check metadata
            Metadata.getMetaData()

            // check login
            if(ScoreBoard.checkLogin()) {
                // login -> to home page
                val intent = Intent(MainActivity.appContext, HomeScreen::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            } else {
                // not login -> to login page
                val intent = Intent(MainActivity.appContext, LoginScreen::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        } else {
            Log.d(TAG, "Folder not created....")
        }
    }

    private fun requestPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            //Android is 11(R) or above
            try {
                Log.d(TAG, "requestPermission: try")
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                val uri = Uri.fromParts("package", this.packageName, null)
                intent.data = uri
                storageActivityResultLauncher.launch(intent)
            }
            catch (e: Exception){
                Log.e(TAG, "requestPermission: ", e)
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                storageActivityResultLauncher.launch(intent)
            }
        }
        else{
            //Android is below 11(R)
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE
            )
        }
    }

    private val storageActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        Log.d(TAG, "storageActivityResultLauncher: ")
        //here we will handle the result of our intent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            //Android is 11(R) or above
            if (Environment.isExternalStorageManager()){
                //Manage External Storage Permission is granted
                Log.d(TAG, "storageActivityResultLauncher: Manage External Storage Permission is granted")

                GlobalScope.launch {
                    checkPermissionOK()
                }
            }
            else{
                //Manage External Storage Permission is denied....
                Log.d(TAG, "storageActivityResultLauncher: Manage External Storage Permission is denied....")
                Log.d(TAG, "Manage External Storage Permission is denied....")
            }
        }
        else{
            //Android is below 11(R)
        }
    }

    private fun checkPermission(): Boolean{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            //Android is 11(R) or above
            Environment.isExternalStorageManager()
        }
        else{
            //Android is below 11(R)
            val write = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val read = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE){
            if (grantResults.isNotEmpty()){
                //check each permission if granted or not
                val write = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val read = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (write && read){
                    //External Storage Permission granted
                    Log.d(TAG, "onRequestPermissionsResult: External Storage Permission granted")
                    GlobalScope.launch {
                        checkPermissionOK()
                    }
                }
                else{
                    //External Storage Permission denied...
                    Log.d(TAG, "onRequestPermissionsResult: External Storage Permission denied...")
                }
            }
        }
    }
}