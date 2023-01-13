package com.bidaappscoreboard.store

import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.exception.ApolloException
import com.bidaappscoreboard.CustomerPlaceQuery
import com.bidaappscoreboard.MainActivity
import com.bidaappscoreboard.service.apolloClient

object CustomerStore {
    var lastError: RuntimeException? = null
    private const val KEY_CUSTOMER_ID = "CUSTOMER_ID"
    private const val KEY_LOGIN_ID = "LOGIN_ID"
    private const val KEY_LOCATION_ID = "LOCATION_ID"

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

    var statusUpdateWhenClickTurn: Boolean = false

    var loginID: String?
        get() {
            return preferences().getString(CustomerStore.KEY_LOGIN_ID, null)
        }
        set(token: String?) {
            preferences().edit().apply {
                putString(CustomerStore.KEY_LOGIN_ID, token)
                apply()
            }
        }

    var customerID: String?
        get() {
            return preferences().getString(CustomerStore.KEY_CUSTOMER_ID, null)
        }
        set(token: String?) {
            preferences().edit().apply {
                putString(CustomerStore.KEY_CUSTOMER_ID, token)
                apply()
            }
        }

    var locationID: String?
        get() {
            return preferences().getString(CustomerStore.KEY_LOCATION_ID, null)
        }
        set(value: String?) {
            preferences().edit().apply {
                putString(CustomerStore.KEY_LOCATION_ID, value)
                apply()
            }
        }

    var customerPlaceList: List<CustomerPlaceQuery.Customer_place>? = null

    suspend fun apiCustomerPlace() : Boolean {
        var result2: ApolloResponse<CustomerPlaceQuery.Data> = try {
            apolloClient?.query(CustomerPlaceQuery(customer_id = customerID!!))!!.execute()
        } catch (e: ApolloException) {
            e.printStackTrace()
            lastError = e
            return false
        }

        customerPlaceList = result2.data?.customer_place
        return customerPlaceList != null
    }
}