package com.bidaappscoreboard.store

import android.content.SharedPreferences
import android.provider.Settings
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.apollographql.apollo3.exception.ApolloException
import com.apollographql.apollo3.network.http.HttpInfo
import com.bidaappscoreboard.CMSLoginScoreboardMutation
import com.bidaappscoreboard.CMSRenewTokenMutation
import com.bidaappscoreboard.MainActivity
import com.bidaappscoreboard.service.apolloClient

object ScoreBoard {
    private const val TAG = "ScoreBoard"

    private const val KEY_RENEW_TOKEN = "RENEW_TOKEN"
    private const val KEY_SCOREBOARD_ID = "SCOREBOARD_ID"
    private const val KEY_SCOREBOARD_PASS_PROTECT = "SCOREBOARD_PASS_PROTECT"
    private const val KEY_SCOREBOARD_ENABLE_PROTECT = "SCOREBOARD_ENABLE_PROTECT"
    private const val KEY_SCOREBOARD_ENABLE_DEBUG = "SCOREBOARD_ENABLE_DEBUG"
    private const val KEY_SCOREBOARD_LOGIN_FIRST = "SCOREBOARD_LOGIN_FIRST"
    private const val KEY_SCOREBOARD_DEVICE_NAME = "SCOREBOARD_DEVICE_NAME"
    private const val KEY_SCOREBOARD_ESTABLISHMENT = "SCOREBOARD_ESTABLISHMENT"

    private fun preferences(): SharedPreferences {
        val context = MainActivity.appContext
        val masterKeyAlias: String = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
            "secret_shared_prefs",
            masterKeyAlias,
            context!!,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        return sharedPreferences
    }


    var token : String? = null
    var lastError: RuntimeException? = null
    var renewToken: String?
        get() {
            return preferences().getString(KEY_RENEW_TOKEN, null)
        }
        set(token: String?) {
            preferences().edit().apply {
                putString(KEY_RENEW_TOKEN, token)
                apply()
            }
        }

    var scoreboardID: String?
            get() {
                return preferences().getString(KEY_SCOREBOARD_ID, null)
            }
            set(token: String?) {
                preferences().edit().apply {
                    putString(KEY_SCOREBOARD_ID, token)
                    apply()
                }
            }

    var passwordProtect: String?
        get() {
            return preferences().getString(KEY_SCOREBOARD_PASS_PROTECT, "")
        }
        set(pass: String?) {
            preferences().edit().apply {
                putString(KEY_SCOREBOARD_PASS_PROTECT, pass)
                apply()
            }
        }

    var enableProtect: Boolean?
        get() {
            return preferences().getBoolean(KEY_SCOREBOARD_ENABLE_PROTECT, false)
        }
        set(status: Boolean?) {
            preferences().edit().apply {
                if (status != null) {
                    putBoolean(KEY_SCOREBOARD_ENABLE_PROTECT, status)
                }
                apply()
            }
        }

    var enableDebug: Boolean
        get() {
            return preferences().getBoolean(KEY_SCOREBOARD_ENABLE_DEBUG, true)
        }
        set(status: Boolean) {
            preferences().edit().apply {
                putBoolean(KEY_SCOREBOARD_ENABLE_DEBUG, status)
            }
        }

    var loginFirst: Boolean
        get() {
            return preferences().getBoolean(KEY_SCOREBOARD_LOGIN_FIRST, true)
        }
        set(status: Boolean) {
            preferences().edit().apply {
                putBoolean(KEY_SCOREBOARD_LOGIN_FIRST, status)
                apply()
            }
        }

    var nameDevice : String? = ""

    var nameEstablishment : String?
        get() {
            return preferences().getString(KEY_SCOREBOARD_ESTABLISHMENT, "")
        }
        set(value: String?) {
            preferences().edit().apply {
                putString(KEY_SCOREBOARD_ESTABLISHMENT, value)
                apply()
            }
        }

    fun clearAll() {
        renewToken = null
        token = null
        scoreboardID = null
    }

    suspend fun checkLogin(): Boolean {
        // login already, get new access token
        if (renewToken != null) {
            apiRenewToken()
            return true
        }

        // need login for get new refresh_token
        clearAll()
        return false
    }

    suspend fun apiRenewToken() : String? {
        val result = try {
            apolloClient?.mutation(CMSRenewTokenMutation())!!.execute()
        } catch (e: ApolloException) {
            e.printStackTrace()
            lastError = e
            return null
        }

        token = result.data?.CMSRenewToken?.access_token

        Log.d(TAG,"Renew Token::::::::::::::::::$token")

        return token
    }

    suspend fun apiLogin(userName: String, password: String): Boolean {
        val result = try {
            apolloClient?.mutation(CMSLoginScoreboardMutation(userName, password))!!.execute()
        } catch (e: ApolloException) {
            e.printStackTrace()
            lastError = e
            return false
        }
        token = result.data?.CMSLoginScoreboard?.data?.access_token
        scoreboardID = result.data?.CMSLoginScoreboard?.data?.id
        CustomerStore.customerID = result.data?.CMSLoginScoreboard?.data?.ref_id

        // get refreshToken from cookie header: ELRT=xxxxxxx
        val headers = result.executionContext[HttpInfo]?.headers
        var refreshToken = ""
        if (headers != null) {
            for (header in headers) {
                if (header.name == "Set-Cookie") {
                    refreshToken = header.value
                }
            }
        }

        if (refreshToken == "") {
            lastError = RuntimeException("Refresh Token Not Found")
            return false
        } else {
            renewToken = refreshToken
        }

        return true
    }
}