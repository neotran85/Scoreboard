package com.bidaappscoreboard.store

import android.util.Log
import com.bidaappscoreboard.model.*

object MatchSoloStore {
    private const val TAG = "MatchSoloStore"

    const val TURN_PROGRESSING: Int = -1
    const val TURN_WAITING: Int = -2
    const val TURN_AVG: Int = 11111
    const val TURN_HR: Int = 33333
    const val TURN_AVG_TITLE: Int = 22222
    const val TOTAL_TITLE: Int = 44444
    const val TURN_HR_TITLE: Int = 55555

    const val BI_TYPE_FRANCE = 1
    const val BI_TYPE_CAROM_1 = 2
    const val BI_TYPE_CAROM_2 = 3

    var arrayChoseBall: ArrayList<String> = arrayListOf("white", "yellow")

    var listResultMatch: MutableList<ResultMatch> = mutableListOf()

    var currentMatchID: String? = null

    var player1Result: Profile = Profile() // fake
    var player2Result: Profile = Profile() // fake
    var arrayProfilePlayer: ArrayList<Profile> = arrayListOf(  // fake
        Profile("", 25, "", 0.0, 0, 0),
        Profile("", 25, "", 0.0, 0, 0),
    )
    var arrayLocationProfilePlayer: ArrayList<Int> = arrayListOf(0, 1)

    var arrayHandy: ArrayList<Int> = arrayListOf(20, 20) // real
    var arrayPhone: ArrayList<String> = arrayListOf("", "") // real

    var beginTime: Long = 0

    var nameModel: String = ""
    var matchType: Int = MatchSoloStore.BI_TYPE_FRANCE

    fun getPlayer1 () { // fake
        player1Result = Profile("NEW PLAYER 1", 0,
            Metadata.metaGeneral?.generalOptions?.default_avatar_male, 1.444, 10, 90)
    }

    fun getPlayer2 () { // fake
        player2Result = Profile("NEW PLAYER 2", 0,
            Metadata.metaGeneral?.generalOptions?.default_avatar_male, 1.746, 15, 79)
    }

    fun newMatch(type: Int) {
        currentMatchID = ScoreBoard.scoreboardID + "-" + System.currentTimeMillis().toString()
        beginTime = System.currentTimeMillis()
        matchType = type
    }

    fun endMatch(pointPlayer1: Int, pointPlayer2: Int, listTurn: MutableList<ResultMatch>) : MatchScoreBoardData {
        val endTime = System.currentTimeMillis()
        val matchData = MatchData(
            id = currentMatchID,
            startTime = beginTime,
            endTime = endTime,
            matchType = matchType
        )
        val users : ArrayList<MatchUser> = arrayListOf (
            MatchUser( // user 1
                index = 1,
                userID = null,
                phone = arrayPhone[arrayLocationProfilePlayer[0]] ?: "",
                handy = arrayHandy[arrayLocationProfilePlayer[0]] ?: 0,
                totalScore = pointPlayer1
            ),
            MatchUser( // user 2
                index = 2,
                userID = null,
                phone = arrayPhone[arrayLocationProfilePlayer[1]] ?: "",
                handy = arrayHandy[arrayLocationProfilePlayer[1]] ?: 0,
                totalScore = pointPlayer2
            )
        )

        val matchScoreBoard = arrayListOf<MatchScoreBoard>()

        for (i in 0 until listTurn.size) {
            matchScoreBoard.add(
                MatchScoreBoard(
                    1,
                    listTurn[i].player1TurnScore as Int?,
                    listTurn[i].numberTurn,
                    listTurn[i].time1 ?: beginTime
                )
            )
            matchScoreBoard.add(
                MatchScoreBoard(
                    2,
                    listTurn[i].player2TurnScore as Int?,
                    listTurn[i].numberTurn,
                    listTurn[i].time2
                )
            )
        }

        val matchScoreBoardData = MatchScoreBoardData(
            match = matchData,
            users = users,
            scoreBoard = matchScoreBoard,
            videoRecord = null
        )

        currentMatchID = null
        beginTime = 0
        return matchScoreBoardData
    }
}