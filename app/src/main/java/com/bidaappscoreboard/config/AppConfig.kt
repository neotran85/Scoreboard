package com.bidaappscoreboard.config

import android.app.Activity
import java.util.*

private const val CONFIG = "config.properties"

object AppConfig {
    public final var API_URL: String = ""
    public final var SOCKET_URL: String = ""
    public final var MEDIA_URL: String = ""

    fun initAppConfig(activity: Activity) {
        try {
            val properties = Properties()
            properties.load(activity.assets.open(CONFIG))
            API_URL = properties.getProperty("API_URL")
            SOCKET_URL = properties.getProperty("SOCKET_URL")
            MEDIA_URL = properties.getProperty("MEDIA_URL")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}