package com.bidaappscoreboard.store

import android.os.Environment
import android.util.Log
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.exception.ApolloException
import com.bidaappscoreboard.BidaCreateMatchMutation
import com.bidaappscoreboard.model.MatchScoreBoardData
import com.bidaappscoreboard.service.*
import com.google.gson.Gson
import java.io.File

object MatchHistoryStore {
    const val TAG = "MatchHistoryStore"
    private val MATCH_HISTORY_LOCATION = "${Environment.getExternalStorageDirectory()}/${Environment.DIRECTORY_DOCUMENTS}/scoreboard/"

    fun saveMatch(data: MatchScoreBoardData, done: Boolean = false): Boolean {
        if (data.match.id == null) {
            Log.e(TAG, "Match ID is null")
            return false
        }

        val f = File(MATCH_HISTORY_LOCATION)
        if (f.exists() && f.isDirectory) {
//            Log.d(TAG, "Directory Exists")
        } else {
            f.mkdirs()
            Log.d(TAG,"Directory Created")
        }

        val fileName = "${data.match.id}.json"
        val gson = Gson()
        val matchJSON: String = gson.toJson(data)

        writeFile(MATCH_HISTORY_LOCATION, fileName, matchJSON)

        val processFileName = "${data.match.id}.progress"
        if (done) {
            deleteFile(MATCH_HISTORY_LOCATION, processFileName)
        } else {
            writeFile(MATCH_HISTORY_LOCATION, processFileName, System.currentTimeMillis().toString())
        }

        return true
    }

    fun getMatch(matchID: String): MatchScoreBoardData?  {
        val fileName = "$matchID.json"
        val matchJSON = readFile(MATCH_HISTORY_LOCATION, fileName)

        return try {
            val gson = Gson()
            gson.fromJson(matchJSON, MatchScoreBoardData::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "getMatch Exception: ${e.message}")
            null
        }
    }

    fun listMatchHistory(): ArrayList<String>? {
        return null
    }

    suspend fun uploadMatch(data: MatchScoreBoardData): Boolean {
        if (data.match.id == null || data.users.size == 0 || data.scoreBoard.size == 0) {
            Log.e(TAG, "uploadMatch data validate fail.")
            return false
        }

        val gson = Gson()
        val matchJson: String = gson.toJson(data)

        Log.d(TAG, "upload data, :::: $data")

        val response : ApolloResponse<BidaCreateMatchMutation.Data> = try {
            apolloClient?.mutation((BidaCreateMatchMutation(
                customer_place_id = CustomerStore.locationID!!,
                end_at = data.match.endTime.toString(),
                start_at = data.match.startTime.toString(),
                scoreboard_data = matchJson,
                total_turn = data.scoreBoard.size,
                type = data.match.matchType ?: MatchSoloStore.BI_TYPE_FRANCE
            )))!!.execute()
        } catch (e: ApolloException) {
            e.printStackTrace()
            return false
        }

        if (response.hasErrors()) {
            Log.e(TAG, "uploadMatch error ${gson.toJson(response.errors)}")
            return false
        }

        val progressFileName = "${data.match.id}.progress"
        deleteFile(MATCH_HISTORY_LOCATION, progressFileName)

        return true
    }
}