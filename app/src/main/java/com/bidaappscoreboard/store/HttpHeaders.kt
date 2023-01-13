package com.bidaappscoreboard.store

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log
import java.util.*
import kotlin.collections.HashMap

object HttpHeaders {
    var deviceID: String? = null
    var pushToken: String? = null
    const val osType: String = "android"
    var osVersion: String = Build.VERSION.RELEASE
    var appVersion: String? = null

    @SuppressLint("HardwareIds")
    fun init(context: Context) {
        if (this.deviceID == null){
            var androidID = ""
            try {
                androidID = Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ANDROID_ID)

                Log.e("Android ID :: ", androidID)
            } catch (e: Exception){
                // TODO: show error
            }
            deviceID = androidID
        }

        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        this.appVersion = info.versionName

        Log.d("Header all :: ", "" + this.deviceID + "  " +this.osType + " " + this.osVersion + " " + this.appVersion)
    }

    fun toJson() = HashMap(
        hashMapOf(
            "X-DEVICE-ID" to this.deviceID,
            "X-OS-TYPE" to this.osType,
            "X-OS-VERSION" to this.osVersion,
            "X-APP-VERSION" to this.appVersion,
            "X-PUSH-TOKEN" to this.pushToken,
        )
    )
}