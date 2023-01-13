package com.bidaappscoreboard.ui.matchSolo

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bidaappscoreboard.MainActivity
import com.bidaappscoreboard.R
import com.bidaappscoreboard.common.NpaLinearLayoutManager
import kotlinx.android.synthetic.main.activity_match_screen.*
import com.bidaappscoreboard.common.SetImage
import com.bidaappscoreboard.model.*
import com.bidaappscoreboard.store.CameraStore
import com.bidaappscoreboard.store.MatchHistoryStore
import com.bidaappscoreboard.store.MatchSoloStore
import com.bidaappscoreboard.ui.HomeScreen
import com.bidaappscoreboard.ui.watchMatchAgain.WatchMatchAgainScreen
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_match_screen.constrain_player_1
import kotlinx.android.synthetic.main.activity_match_screen.constrain_player_2
import kotlinx.android.synthetic.main.custom_popup_detail_scoreboard.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.schedule
import kotlin.math.roundToLong


class MatchScreen : AppCompatActivity() {

    private var TAG = "MatchScreen"

    private lateinit var tableResultAdapter: TableResultAdapter
    private lateinit var tableDetailScoreboardAdapter: TableDetailScoreboardAdapter

    private var pointPlayer1: Int = 0
    private var pointPlayer2: Int = 0
    private var scoreTurnPlayer1: Int = 0
    private var scoreTurnPlayer2: Int = 0
    private var checkNumberTurn: Int = 0
    private var numberTurn: Int = 1
    private var numberTurnTemporary: Int = 1
    private var scoreTurnPlayer1Temporary: Int? = null
    private var totalPointPlayer1: Int = 0
    private var totalPointPlayer2: Int = 0
    private var hrPlayer1: Int = 0
    private var hrPlayer2: Int = 0

    private var player1Turn: Boolean = true
    private var statusScoreboard: Boolean = false
    private var timerStarted: Boolean = false
    private var timerStarted2: Boolean = true
    private var showMenu: Boolean = false
    private var overTimeTurn: Boolean = false

    private var timer: Timer? = null
    private var timer2: Timer? = null
    private var timerTask: TimerTask? = null
    private var timerTask2: TimerTask? = null
    private var time: Double = 0.0
    private var time2: Int = 40
    private var timeMillisecondPlayer1: Long? = null
    private var timeMillisecondPlayer2: Long? = null

    private var listTurn: MutableList<ResultMatch> = mutableListOf()
    private var listInfoPlayer: MutableList<ResultMatch> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_screen)

        timer = Timer()
        timer2 = Timer()

        init()
        ic_reload_match_screen_2.setOnClickListener {
            if (timerStarted2) {
                resetTappedTimeTurn()
                tv_time2.visibility = View.VISIBLE
                constrain_time_line.visibility = View.VISIBLE
                tv_time_up.visibility = View.INVISIBLE
                constrain_time_tool.setBackgroundResource(R.drawable.background_contain_tool_time)
            }
        }
    }

    private fun init() {
        runVideo()
        setupPlayer()
        writePoint()
        changeTurns()
        openScoreBoard()
        showMenu()
        stopStartTappedTimeMatch()
        gotoWatchAgain()
        stopStartTappedTimeTurn()

        ic_start_time.setOnClickListener {
            if (!overTimeTurn) {
                Log.d(TAG, "Over time turn false")
                timerStarted2 = timerStarted2 != true
                stopStartTappedTimeTurn()
            }
        }

        ic_shutdown.setOnClickListener {
            val dialog = Dialog(this@MatchScreen)
            dialog.setContentView(R.layout.custom_popup_cancel_match)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            val containCloseDialog = dialog.findViewById<ConstraintLayout>(R.id.contain_close_cancel_match)
            val containConfirmCancelMatch = dialog.findViewById<ConstraintLayout>(R.id.contain_confirm_cancel_match)

            dialog.show()

            containCloseDialog.setOnClickListener {
                dialog.dismiss()
            }

            containConfirmCancelMatch.setOnClickListener {
                timerStarted2 = false
                stopStartTappedTimeTurn()
                if (pointPlayer1 > pointPlayer2) {
                    setupInfoPlayer()
                    Log.d(TAG, "Shutdown match :: ${listTurn.size - 1} + $scoreTurnPlayer1")
                    listTurn[listTurn.size - 1] = ResultMatch(scoreTurnPlayer1, numberTurn, 0 )
                    showMatchResult(player1Win = true, clickShutDown = true)
                } else {
                    setupInfoPlayer()
                    listTurn[listTurn.size - 1] = ResultMatch(scoreTurnPlayer1, numberTurn, scoreTurnPlayer2)
                    showMatchResult(player1Win = false, clickShutDown = true)
                }

                MatchSoloStore.arrayProfilePlayer = arrayListOf(
                    Profile("", 25, "", 0.0, 0, 0),
                    Profile("", 25, "", 0.0, 0, 0)
                )
            }
        }

        contain_see_scoreboard.setOnClickListener {
            showDetailScoreboard()
        }

        //Create list info player
        listInfoPlayer = mutableListOf(
            ResultMatch(totalPointPlayer1, MatchSoloStore.TOTAL_TITLE, totalPointPlayer2),
            ResultMatch(0.0, MatchSoloStore.TURN_AVG_TITLE, 0.0),
            ResultMatch(hrPlayer1, MatchSoloStore.TURN_HR_TITLE, hrPlayer2)
        )

        listTurn = mutableListOf(ResultMatch(-1, numberTurn, -2))

        showListResultMatch()
    }

    private fun gotoWatchAgain() {
        ic_start_video.setOnClickListener {
            MatchSoloStore.listResultMatch = listTurn

            val intent = Intent(this, WatchMatchAgainScreen::class.java)
            startActivity(intent)
        }
    }

    private fun showListResultMatch() {
        recyclerTableResult.apply {
            layoutManager = NpaLinearLayoutManager(this@MatchScreen)
            tableResultAdapter = TableResultAdapter(this@MatchScreen, listTurn, false)
            adapter = tableResultAdapter
        }.scrollToPosition(listTurn.size - 1)
        recyclerTableInfo.apply {
            layoutManager = NpaLinearLayoutManager(this@MatchScreen)
            tableResultAdapter = TableResultAdapter(this@MatchScreen, listInfoPlayer, true)
            adapter = tableResultAdapter
        }
    }

    private fun showMenu() {
        ic_option.setOnClickListener {
            if (!showMenu) {
                ic_option.setBackgroundResource(R.drawable.ic_open_menu)
                contain_menu.visibility = View.VISIBLE
                showMenu = true
            } else {
                ic_option.setBackgroundResource(R.drawable.ic_close_menu)
                contain_menu.visibility = View.INVISIBLE
                showMenu = false
            }
        }
    }

    private fun resetTappedTimeTurn() {
        ic_reload_match_screen_2.setBackgroundResource(R.drawable.ic_reload_4)
        if (timerStarted2) {
            ic_start_time.setBackgroundResource(R.drawable.ic_pause_time)
        } else {
            ic_start_time.setBackgroundResource(R.drawable.ic_start_time)
        }
        time2 = 40
        reloadTime(time2)
    }

    private fun stopStartTappedTimeMatch() {
        if (!timerStarted)
        {
            timerStarted = true
            startTime()

        } else {
            timerStarted = false
            timerTask?.cancel()
        }
    }

    private fun stopStartTappedTimeTurn() {
        if (timerStarted2) {
            ic_start_time.setBackgroundResource(R.drawable.ic_pause_time)
            ic_reload_match_screen_2.setBackgroundResource(R.drawable.ic_reload_4)
            startTimeTurn()
        } else {
            ic_start_time.setBackgroundResource(R.drawable.ic_start_time)
            ic_reload_match_screen_2.setBackgroundResource(R.drawable.ic_reload_2)
            timerTask2?.cancel()
        }
    }

    private fun startTime() {
        timerTask = object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    time++
                    tv_total_time.text = getTimerText()
                }
            }
        }
        timer?.scheduleAtFixedRate(timerTask, 0, 1000)
    }

    private fun startTimeTurn() {
        timerTask2 = object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    if (time2 > 0) {
                        time2--

                        overTimeTurn = false
                    } else if (time2 == 0) {
                        tv_time2.visibility = View.INVISIBLE
                        constrain_time_line.visibility = View.INVISIBLE
                        tv_time_up.visibility = View.VISIBLE

                        overTimeTurn = true

                        ic_start_time.setBackgroundResource(R.drawable.ic_start_time)
                        constrain_time_tool.setBackgroundResource(R.drawable.background_contain_tool_time_2)
                    }

                    when(time2) {
                        39 -> line_time_39.visibility = View.INVISIBLE
                        38 -> line_time_38.visibility = View.INVISIBLE
                        37 -> line_time_37.visibility = View.INVISIBLE
                        36 -> line_time_36.visibility = View.INVISIBLE
                        35 -> line_time_35.visibility = View.INVISIBLE
                        34 -> line_time_34.visibility = View.INVISIBLE
                        33 -> line_time_33.visibility = View.INVISIBLE
                        32 -> line_time_32.visibility = View.INVISIBLE
                        31 -> line_time_31.visibility = View.INVISIBLE
                        30 -> line_time_30.visibility = View.INVISIBLE
                        29 -> line_time_29.visibility = View.INVISIBLE
                        28 -> line_time_28.visibility = View.INVISIBLE
                        27 -> line_time_27.visibility = View.INVISIBLE
                        26 -> line_time_26.visibility = View.INVISIBLE
                        25 -> line_time_25.visibility = View.INVISIBLE
                        24 -> line_time_24.visibility = View.INVISIBLE
                        23 -> line_time_23.visibility = View.INVISIBLE
                        22 -> line_time_22.visibility = View.INVISIBLE
                        21 -> line_time_21.visibility = View.INVISIBLE
                        20 -> line_time_20.visibility = View.INVISIBLE
                        19 -> line_time_19.visibility = View.INVISIBLE
                        18 -> line_time_18.visibility = View.INVISIBLE
                        17 -> line_time_17.visibility = View.INVISIBLE
                        16 -> line_time_16.visibility = View.INVISIBLE
                        15 -> line_time_15.visibility = View.INVISIBLE
                        14 -> line_time_14.visibility = View.INVISIBLE
                        13 -> line_time_13.visibility = View.INVISIBLE
                        12 -> line_time_12.visibility = View.INVISIBLE
                        11 -> line_time_11.visibility = View.INVISIBLE
                        10 -> line_time_10.visibility = View.INVISIBLE
                        9 -> line_time_9.visibility = View.INVISIBLE
                        8 -> line_time_8.visibility = View.INVISIBLE
                        7 -> line_time_7.visibility = View.INVISIBLE
                        6 -> line_time_6.visibility = View.INVISIBLE
                        5 -> line_time_5.visibility = View.INVISIBLE
                        4 -> line_time_4.visibility = View.INVISIBLE
                        3 -> line_time_3.visibility = View.INVISIBLE
                        2 -> line_time_2.visibility = View.INVISIBLE
                        1 -> line_time_1.visibility = View.INVISIBLE
                        0 -> line_time_0.visibility = View.INVISIBLE
                    }
                    tv_time2.text = time2.toString()
                }
            }
        }
        timer2?.schedule(timerTask2, 0, 1000)
    }

    private fun getTimerText(): String {
        val rounded = time.roundToLong()

        val minutes = ((rounded % 86400) % 3600) / 60
        val hours = ((rounded % 86400) / 3600)
        val second = ((rounded % 86400) % 3600) % 60

        //Log.e(TAG, "Minutes :: $minutes")
        //Log.e(TAG, "Hours :: $hours")

        return formatTime(minutes, hours, second)
    }

    private fun formatTime(minutes: Long, hours: Long, second: Long): String {
        return String.format("%02d", hours) + " : " + String.format("%02d", minutes) + " : " + String.format("%02d", second)
    }

    //Reload time
    private fun reloadTime(timeMax: Int) {
        tv_time2.text = timeMax.toString()

        line_time_39.visibility = View.VISIBLE
        line_time_38.visibility = View.VISIBLE
        line_time_37.visibility = View.VISIBLE
        line_time_36.visibility = View.VISIBLE
        line_time_35.visibility = View.VISIBLE
        line_time_34.visibility = View.VISIBLE
        line_time_33.visibility = View.VISIBLE
        line_time_32.visibility = View.VISIBLE
        line_time_31.visibility = View.VISIBLE
        line_time_30.visibility = View.VISIBLE
        line_time_29.visibility = View.VISIBLE
        line_time_28.visibility = View.VISIBLE
        line_time_27.visibility = View.VISIBLE
        line_time_26.visibility = View.VISIBLE
        line_time_25.visibility = View.VISIBLE
        line_time_24.visibility = View.VISIBLE
        line_time_23.visibility = View.VISIBLE
        line_time_22.visibility = View.VISIBLE
        line_time_21.visibility = View.VISIBLE
        line_time_20.visibility = View.VISIBLE
        line_time_19.visibility = View.VISIBLE
        line_time_18.visibility = View.VISIBLE
        line_time_17.visibility = View.VISIBLE
        line_time_16.visibility = View.VISIBLE
        line_time_15.visibility = View.VISIBLE
        line_time_14.visibility = View.VISIBLE
        line_time_13.visibility = View.VISIBLE
        line_time_12.visibility = View.VISIBLE
        line_time_11.visibility = View.VISIBLE
        line_time_10.visibility = View.VISIBLE
        line_time_9.visibility = View.VISIBLE
        line_time_8.visibility = View.VISIBLE
        line_time_7.visibility = View.VISIBLE
        line_time_6.visibility = View.VISIBLE
        line_time_5.visibility = View.VISIBLE
        line_time_4.visibility = View.VISIBLE
        line_time_3.visibility = View.VISIBLE
        line_time_2.visibility = View.VISIBLE
        line_time_1.visibility = View.VISIBLE
        line_time_0.visibility = View.VISIBLE
    }

    //Setup player
    private fun setupPlayer() {
        MatchSoloStore.arrayHandy
        //Show player 1
        tv_NamePlayer1_match_screen.text = MatchSoloStore.arrayProfilePlayer[MatchSoloStore.arrayLocationProfilePlayer[0]].displayname
        tv_handy_player1_match_screen.text = MatchSoloStore.arrayHandy[MatchSoloStore.arrayLocationProfilePlayer[0]].toString()
        SetImage.setImage(avatar1_match_screen, MatchSoloStore.arrayProfilePlayer[MatchSoloStore.arrayLocationProfilePlayer[0]].url, this)

        //Show player 2
        tv_NamePlayer2_match_screen.text = MatchSoloStore.arrayProfilePlayer[MatchSoloStore.arrayLocationProfilePlayer[1]].displayname
        tv_handy_player2_match_screen.text = MatchSoloStore.arrayHandy[MatchSoloStore.arrayLocationProfilePlayer[1]].toString()
        SetImage.setImage(avatar2_match_screen, MatchSoloStore.arrayProfilePlayer[MatchSoloStore.arrayLocationProfilePlayer[1]].url, this)
    }

    //Run video
    private fun runVideo() {
        CameraStore.attachLiveView(videoLayout)
    }

    private fun showDetailScoreboard() {
        val dialog = Dialog(this@MatchScreen)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.custom_popup_detail_scoreboard)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val edtEndPointPlayer1 = dialog.findViewById<TextView>(R.id.vt_2_1)
        val edtEndPointPlayer2 = dialog.findViewById<TextView>(R.id.vt_2_2)
        val totalPointPlayer1 = dialog.findViewById<TextView>(R.id.vt_0_1)
        val totalPointPlayer2 = dialog.findViewById<TextView>(R.id.vt_0_2)
        val remainingPoints1 = dialog.findViewById<TextView>(R.id.vt_1_1)
        val remainingPoints2 = dialog.findViewById<TextView>(R.id.vt_1_2)
        val hrPointPlayer1 = dialog.findViewById<TextView>(R.id.vt_3_1)
        val hrPointPlayer2 = dialog.findViewById<TextView>(R.id.vt_3_2)

        val btnCloseDetailScoreBoard = dialog.findViewById<Button>(R.id.btn_close_detail_scoreboard)
        val icCloseDetailScoreBoard = dialog.findViewById<ImageView>(R.id.ic_dismiss_detail_scoreboard)
        val vtNamePlayer1 = dialog.findViewById<TextView>(R.id.vt_title_2)
        val vtNamePlayer2 = dialog.findViewById<TextView>(R.id.vt_title_3)

        val recyclerTableDetailScoreboard = dialog.findViewById<RecyclerView>(R.id.recyclerTableDetailScoreboard)

        //Show end point
        edtEndPointPlayer1.text = MatchSoloStore.arrayHandy[MatchSoloStore.arrayLocationProfilePlayer[0]].toString()
        edtEndPointPlayer2.text = MatchSoloStore.arrayHandy[MatchSoloStore.arrayLocationProfilePlayer[1]].toString()

        //Show name player
        vtNamePlayer1.text = MatchSoloStore.arrayProfilePlayer[MatchSoloStore.arrayLocationProfilePlayer[0]].displayname
        vtNamePlayer2.text = MatchSoloStore.arrayProfilePlayer[MatchSoloStore.arrayLocationProfilePlayer[1]].displayname

        //Show total point
        totalPointPlayer1.text = pointPlayer1.toString()
        totalPointPlayer2.text = pointPlayer2.toString()

        //Show remaining point
        remainingPoints1.text = (MatchSoloStore.arrayHandy[MatchSoloStore.arrayLocationProfilePlayer[0]] - pointPlayer1).toString()
        remainingPoints2.text = (MatchSoloStore.arrayHandy[MatchSoloStore.arrayLocationProfilePlayer[1]] - pointPlayer2).toString()

        //Show hr
        hrPointPlayer1.text = hrPlayer1.toString()
        hrPointPlayer2.text = hrPlayer2.toString()

        recyclerTableDetailScoreboard.apply {
            layoutManager = NpaLinearLayoutManager(this@MatchScreen)
            tableDetailScoreboardAdapter = TableDetailScoreboardAdapter(this@MatchScreen, listTurn)
            adapter = tableDetailScoreboardAdapter
        }.scrollToPosition(listTurn.size - 1)

        btnCloseDetailScoreBoard.setOnClickListener {
            dialog.dismiss()
        }

        icCloseDetailScoreBoard.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showMatchResult(player1Win: Boolean, clickShutDown: Boolean) {
        val dialog = Dialog(this@MatchScreen)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.custom_popup_end_match)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.show()

        val btnLuuKQ = dialog.findViewById<Button>(R.id.btnLuuKQ)
        val btnClose = dialog.findViewById<Button>(R.id.btnCloseDialogEndMatch)

        val tvTotalPlayer1 = dialog.findViewById<TextView>(R.id.vt_0_0)
        val tvTotalPlayer2 = dialog.findViewById<TextView>(R.id.vt_0_2)
        val avgPlayer1 = dialog.findViewById<TextView>(R.id.vt_1_0)
        val avgPlayer2 = dialog.findViewById<TextView>(R.id.vt_1_2)
        val hrPlayer1 = dialog.findViewById<TextView>(R.id.vt_2_0)
        val hrPlayer2 = dialog.findViewById<TextView>(R.id.vt_2_2)
        val tvNamePlayer1 = dialog.findViewById<TextView>(R.id.tvNamePlayer1EndMatch)
        val tvNamePlayer2 = dialog.findViewById<TextView>(R.id.tvNamePlayer2EndMatch)

        val imgAvatarPlayer1 = dialog.findViewById<CircleImageView>(R.id.avatar_end_match_player1)
        val imgAvatarPlayer2 = dialog.findViewById<CircleImageView>(R.id.avatar_end_match_player2)

        val icWinPlayer1 = dialog.findViewById<ImageView>(R.id.ic_win_1)
        val icWinPlayer2 = dialog.findViewById<ImageView>(R.id.ic_win_2)
        val icLosePlayer1 = dialog.findViewById<ImageView>(R.id.ic_lose_1)
        val icLosePlayer2 = dialog.findViewById<ImageView>(R.id.ic_lose_2)
        val icDismiss = dialog.findViewById<ImageView>(R.id.ic_dismiss_end_match)

        val recyclerTableResultEndMatch = dialog.findViewById<RecyclerView>(R.id.recyclerTableResultEndMatch)

        //Show result win lose of player
        if (player1Win) {
            icWinPlayer1.visibility = View.VISIBLE
            icLosePlayer1.visibility = View.GONE
            icWinPlayer2.visibility = View.GONE
            icLosePlayer2.visibility = View.VISIBLE
        } else {
            icWinPlayer1.visibility = View.GONE
            icLosePlayer1.visibility = View.VISIBLE
            icWinPlayer2.visibility = View.VISIBLE
            icLosePlayer2.visibility = View.GONE
        }

        //Setup avatar
        SetImage.setImage(imgAvatarPlayer1, MatchSoloStore.arrayProfilePlayer[MatchSoloStore.arrayLocationProfilePlayer[0]].url, this)
        SetImage.setImage(imgAvatarPlayer2, MatchSoloStore.arrayProfilePlayer[MatchSoloStore.arrayLocationProfilePlayer[1]].url, this)

        //Setup display name
        tvNamePlayer1.text = MatchSoloStore.arrayProfilePlayer[MatchSoloStore.arrayLocationProfilePlayer[0]].displayname
        tvNamePlayer2.text = MatchSoloStore.arrayProfilePlayer[MatchSoloStore.arrayLocationProfilePlayer[1]].displayname

        //Setup list result match
        if (listTurn.isNotEmpty() && !clickShutDown) {
            listTurn.removeLast()
        }

        if (!clickShutDown) {
            listTurn[listTurn.size - 1] = ResultMatch(scoreTurnPlayer1, numberTurn, scoreTurnPlayer2, timeMillisecondPlayer1, timeMillisecondPlayer2)
        }

        recyclerTableResultEndMatch.apply {
            layoutManager = NpaLinearLayoutManager(this@MatchScreen)
            tableResultAdapter = TableResultAdapter(this@MatchScreen, listTurn, true)
            adapter = tableResultAdapter
        }

        tvTotalPlayer1.text = listInfoPlayer[0].player1TurnScore.toString()
        tvTotalPlayer2.text = listInfoPlayer[0].player2TurnScore.toString()

        avgPlayer1.text = listInfoPlayer[1].player1TurnScore.toString()
        avgPlayer2.text = listInfoPlayer[1].player2TurnScore.toString()

        hrPlayer1.text = listInfoPlayer[2].player1TurnScore.toString()
        hrPlayer2.text = listInfoPlayer[2].player2TurnScore.toString()

        btnClose.setOnClickListener {
            endMatch(false)
            dialog.dismiss()
            MatchSoloStore.arrayProfilePlayer = arrayListOf(
                Profile("", 25, "", 0.0, 0, 0),
                Profile("", 25, "", 0.0, 0, 0)
            )
        }

        icDismiss.setOnClickListener {
            endMatch(false)
            dialog.dismiss()
            MatchSoloStore.arrayProfilePlayer = arrayListOf(
                Profile("", 25, "", 0.0, 0, 0),
                Profile("", 25, "", 0.0, 0, 0)
            )
        }

        btnLuuKQ.setOnClickListener {
            endMatch(true)
            dialog.dismiss()
            MatchSoloStore.arrayProfilePlayer = arrayListOf(
                Profile("", 25, "", 0.0, 0, 0),
                Profile("", 25, "", 0.0, 0, 0)
            )
        }

        stopStartTappedTimeMatch()
    }

    //Show the match result
    private fun endMatch(save: Boolean) {
        CameraStore.stopVideoRecord()
        val matchID = MatchSoloStore.currentMatchID

        if (matchID != null) {
            if (save) {
                val matchScoreBoardData = MatchSoloStore.endMatch(pointPlayer1, pointPlayer2, listTurn)
                MatchHistoryStore.saveMatch(matchScoreBoardData)
                GlobalScope.launch {
                    CameraStore.saveRecord(matchID) {
                        if (it != null) {
                            // after upload (and compress) success, update matchjson on scoreboard
                            val matchVideoRecord = MatchVideoRecord(
                                id = it.data?.uploadId,
                                live = it.data?.liveUrl,
                                thumb = it.data?.thumbUrl,
                                download = it.data?.originUrl,
                                duration = (it.metadata?.duration?.div(1000))?.toInt() ?: 0
                            )
                            matchScoreBoardData.videoRecord = matchVideoRecord
                            MatchHistoryStore.saveMatch(matchScoreBoardData)
                        } else {
                            // if cannot record video, sent to server empty video data
                            matchScoreBoardData.videoRecord = MatchVideoRecord()
                        }

                        // upload match to server
                        GlobalScope.launch {
                            if (MatchHistoryStore.uploadMatch(matchScoreBoardData)) {
                                Log.i(TAG, "upload Match success $matchID")
                                MatchHistoryStore.saveMatch(matchScoreBoardData, true)
                            } else {
                                Log.i(TAG, "upload Match fail $matchID")
                            }
                        }
                    }
                }
            } else {
                CameraStore.deleteRecord(matchID)
            }
        } else {
            Log.e(TAG, "endMatch but matchID is null")
        }

        val intent = Intent(MainActivity.appContext, HomeScreen::class.java)
        startActivity(intent)
    }

    //Show point
    private fun writePoint() {

        //Set point
        setPoint()

        //Show point
        tv_point_1.text = pointPlayer1.toString()
        tv_point_2.text = pointPlayer2.toString()
    }

    //Set point
    private fun setPoint() {

        //Increase point player 1

        constrain_color_player_1.setOnClickListener {
            if (player1Turn) {

                //Extra points every turn
                scoreTurnPlayer1++
                showScoreInTurnPlayer1(scoreTurnPlayer1)

                pointPlayer1++
                if (pointPlayer1 == MatchSoloStore.arrayHandy[MatchSoloStore.arrayLocationProfilePlayer[0]]) {
                    setupInfoPlayer()
                    timerStarted2 = timerStarted2 != true
                    stopStartTappedTimeTurn()
                    listTurn[listTurn.size - 1] = ResultMatch(scoreTurnPlayer1, numberTurn, 0)
                    listTurn.add(ResultMatch(null, null, null))
                    showMatchResult(player1Win = true, clickShutDown = false)
                }

                tv_point_1.text = pointPlayer1.toString()
                resetTappedTimeTurn()
            }
        }

        ic_plus_1.setOnClickListener {

            if (player1Turn) {

                //Extra points every turn
                scoreTurnPlayer1++
                showScoreInTurnPlayer1(scoreTurnPlayer1)

                pointPlayer1++
                if (pointPlayer1 == MatchSoloStore.arrayHandy[MatchSoloStore.arrayLocationProfilePlayer[0]]) {
                    setupInfoPlayer()
                    timerStarted2 = timerStarted2 != true
                    stopStartTappedTimeTurn()
                    listTurn[listTurn.size - 1] = ResultMatch(scoreTurnPlayer1, numberTurn, 0)
                    listTurn.add(ResultMatch(null, null, null))
                    showMatchResult(player1Win = true, clickShutDown = false)
                }

                tv_point_1.text = pointPlayer1.toString()
                resetTappedTimeTurn()
            }
        }

        constrain_color_player_2.setOnClickListener {
            if (!player1Turn) {
                if (pointPlayer2 == 999) {
                    pointPlayer2 = 999
                } else {
                    //Extra points every turn
                    scoreTurnPlayer2++
                    showScoreInTurnPlayer2(scoreTurnPlayer2)

                    pointPlayer2++
                    if (pointPlayer2 == MatchSoloStore.arrayHandy[MatchSoloStore.arrayLocationProfilePlayer[1]]) {
                        setupInfoPlayer()
                        timerStarted2 = timerStarted2 != true
                        stopStartTappedTimeTurn()
                        listTurn[listTurn.size - 1] = ResultMatch(scoreTurnPlayer1, numberTurn, scoreTurnPlayer2)
                        listTurn.add(ResultMatch(null, null, null))
                        showMatchResult(player1Win = false, clickShutDown = false)
                    }
                }

                tv_point_2.text = pointPlayer2.toString()
                resetTappedTimeTurn()
            }
        }

        //Increase point player 2
        ic_plus_2.setOnClickListener {

            if (!player1Turn) {
                if (pointPlayer2 == 999) {
                    pointPlayer2 = 999
                } else {
                    //Extra points every turn
                    scoreTurnPlayer2++
                    showScoreInTurnPlayer2(scoreTurnPlayer2)

                    pointPlayer2++
                    if (pointPlayer2 == MatchSoloStore.arrayHandy[MatchSoloStore.arrayLocationProfilePlayer[1]]) {
                        setupInfoPlayer()
                        timerStarted2 = timerStarted2 != true
                        stopStartTappedTimeTurn()
                        listTurn[listTurn.size - 1] = ResultMatch(scoreTurnPlayer1, numberTurn, scoreTurnPlayer2)
                        listTurn.add(ResultMatch(null, null, null))
                        showMatchResult(player1Win = false, clickShutDown = false)
                    }
                }

                tv_point_2.text = pointPlayer2.toString()
                resetTappedTimeTurn()
            }
        }

        //Reduce point player 1
        ic_minus_1.setOnClickListener {

            if (player1Turn) {
                if (pointPlayer1 == 0) {
                    pointPlayer1 = 0
                } else {
                    pointPlayer1--
                }

                tv_point_1.text = pointPlayer1.toString()

                //Extra points every turn
                scoreTurnPlayer1--
                if (scoreTurnPlayer1 < 0) {
                    scoreTurnPlayer1 = 0
                }
                showScoreInTurnPlayer1(scoreTurnPlayer1)
            }
        }

        //Reduce point player 2
        ic_minus_2.setOnClickListener {

            if (!player1Turn) {
                if (pointPlayer2 == 0) {
                    pointPlayer2 = 0
                } else {
                    pointPlayer2--
                }

                tv_point_2.text = pointPlayer2.toString()

                //Extra points every turn
                scoreTurnPlayer2--
                if (scoreTurnPlayer2 < 0) {
                    scoreTurnPlayer2 = 0
                }
                showScoreInTurnPlayer2(scoreTurnPlayer2)
            }
        }
    }

    //Set info player
    private fun setupInfoPlayer() {

        //Set total point
        totalPointPlayer1 += scoreTurnPlayer1
        totalPointPlayer2 += scoreTurnPlayer2
        listInfoPlayer[0].player1TurnScore = totalPointPlayer1
        listInfoPlayer[0].player2TurnScore = totalPointPlayer2

        //Set avg player 1
        if (listTurn[listTurn.size - 1].player1TurnScore.toString().toInt() < 0 && listTurn.size == 1) {
            listInfoPlayer[1].player1TurnScore = String.format("%.3f", (totalPointPlayer1.toDouble() / (1))).toDouble()
        } else {
            listInfoPlayer[1].player1TurnScore = String.format("%.3f", (totalPointPlayer1.toDouble() / (listTurn.size))).toDouble()
        }

        //Set avg player 2
        if (listTurn[listTurn.size - 1].player2TurnScore.toString().toInt() < 0 && listTurn.size == 1) {
            listInfoPlayer[1].player2TurnScore = String.format("%.3f", (totalPointPlayer2.toDouble() / (1))).toDouble()
        } else {
            listInfoPlayer[1].player2TurnScore = String.format("%.3f", (totalPointPlayer2.toDouble() / (listTurn.size))).toDouble()
        }

        //Set hr player 1
        if (scoreTurnPlayer1 > hrPlayer1) {
            hrPlayer1 = scoreTurnPlayer1
            listInfoPlayer[2].player1TurnScore = hrPlayer1
        } else {
            listInfoPlayer[2].player1TurnScore = hrPlayer1
        }

        //Set hr player 2
        if (scoreTurnPlayer2 > hrPlayer2) {
            hrPlayer2 = scoreTurnPlayer2
            listInfoPlayer[2].player2TurnScore = hrPlayer2
        } else {
            listInfoPlayer[2].player1TurnScore = hrPlayer1
        }
    }

    //Change Turns
    private fun changeTurns() {

        //Show screen when start from player1
        constrain_player_1.setBackgroundResource(R.drawable.background_red_radius)
        if(MatchSoloStore.arrayChoseBall[1] == "white") {
            constrain_player_2.setBackgroundResource(R.drawable.custom_edittext_1)
            constrain_color_player_2.setBackgroundResource(R.drawable.custom_edittext_1)
            constrain_color_player_1.setBackgroundResource(R.drawable.custom_background_player_2)
        } else {
            constrain_player_2.setBackgroundResource(R.drawable.custom_background_player_2)
            constrain_color_player_2.setBackgroundResource(R.drawable.custom_background_player_2)
            constrain_color_player_1.setBackgroundResource(R.drawable.custom_edittext_1)
        }
        constrain_turns_player_1.visibility = View.VISIBLE
        constrain_turn_player_2.visibility = View.INVISIBLE

        btn_turn.setOnClickListener {

            resetTappedTimeTurn()

            setupInfoPlayer()

            tv_time2.visibility = View.VISIBLE
            constrain_time_line.visibility = View.VISIBLE
            tv_time_up.visibility = View.INVISIBLE
            constrain_time_tool.setBackgroundResource(R.drawable.background_contain_tool_time)

            val timeMilliSecondCurrent = System.currentTimeMillis()

            //Confirm right turn of player 1
            player1Turn = player1Turn != true

            //Show player has turn
            if (player1Turn) {
                constrain_player_1.setBackgroundResource(R.drawable.background_red_radius)
                if(MatchSoloStore.arrayChoseBall[1] == "white") {
                    constrain_player_2.setBackgroundResource(R.drawable.custom_edittext_1)
                    constrain_color_player_2.setBackgroundResource(R.drawable.custom_edittext_1)
                } else {
                    constrain_player_2.setBackgroundResource(R.drawable.custom_background_player_2)
                    constrain_color_player_2.setBackgroundResource(R.drawable.custom_background_player_2)
                }
                constrain_turns_player_1.visibility = View.VISIBLE
                constrain_turn_player_2.visibility = View.INVISIBLE
            } else {
                if(MatchSoloStore.arrayChoseBall[0] == "white") {
                    constrain_player_1.setBackgroundResource(R.drawable.custom_edittext_1)
                    constrain_color_player_1.setBackgroundResource(R.drawable.custom_edittext_1)
                } else {
                    constrain_player_1.setBackgroundResource(R.drawable.custom_background_player_2)
                    constrain_color_player_1.setBackgroundResource(R.drawable.custom_background_player_2)
                }
                constrain_player_2.setBackgroundResource(R.drawable.background_red_radius)
                constrain_turns_player_1.visibility = View.INVISIBLE
                constrain_turn_player_2.visibility = View.VISIBLE
            }
            
            //Check total number turn in match
            checkTotalTurn()

            //Add score in turn into list total turn
            if (numberTurn == 1) {
                timeMillisecondPlayer2 = timeMilliSecondCurrent
                //Log.d(TAG, "Time player 2 :: $timeMillisecondPlayer2")
                listTurn[0] = ResultMatch(scoreTurnPlayer1, numberTurn, MatchSoloStore.TURN_PROGRESSING, timeMillisecondPlayer1, timeMillisecondPlayer2)
            } else {
                if (numberTurn != numberTurnTemporary) {
                    listTurn[numberTurn - 2] = ResultMatch(scoreTurnPlayer1Temporary, numberTurn - 1, scoreTurnPlayer2, timeMillisecondPlayer1, timeMillisecondPlayer2)
                    listTurn.add(ResultMatch(MatchSoloStore.TURN_PROGRESSING, numberTurn, MatchSoloStore.TURN_WAITING, timeMillisecondPlayer1, timeMillisecondPlayer2))

                    timeMillisecondPlayer1 = timeMilliSecondCurrent
                    //Log.d(TAG, "Time player 1 :: $timeMillisecondPlayer1")

                } else {
                    timeMillisecondPlayer2 = timeMilliSecondCurrent
                    //Log.d(TAG, "Time player 2 :: $timeMillisecondPlayer2")
                    listTurn[numberTurn - 1] = ResultMatch(scoreTurnPlayer1, numberTurn, MatchSoloStore.TURN_PROGRESSING, timeMillisecondPlayer1, timeMillisecondPlayer2)
                }
            }

            scoreTurnPlayer1Temporary = scoreTurnPlayer1
            numberTurnTemporary = numberTurn

            //Reset score when change turn
            scoreTurnPlayer1 = 0
            scoreTurnPlayer2 = 0
            showScoreInTurnPlayer1(scoreTurnPlayer1)
            showScoreInTurnPlayer2(scoreTurnPlayer2)

            showListResultMatch()
        }
    }

    private fun checkTotalTurn() {
        checkNumberTurn++

        if (checkNumberTurn == 2) {

            checkNumberTurn = 0
            numberTurn++

            tv_turn.text = numberTurn.toString()
        }
    }

    //Show score in turn player 1
    private fun showScoreInTurnPlayer1(score: Int) {
        if(score == 0) {
            ic_minus_1.visibility = View.INVISIBLE
        } else {
            ic_minus_1.visibility = View.VISIBLE
        }
        score_in_turn_1.text = score.toString()
    }

    //Show score in turn player 2
    private fun showScoreInTurnPlayer2(score: Int) {
        if(score == 0) {
            ic_minus_2.visibility = View.INVISIBLE
        } else {
            ic_minus_2.visibility = View.VISIBLE
        }
        score_in_turn_2.text = score.toString()
    }

    //Open scoreboard
    private fun openScoreBoard() {

        video_view_match.visibility = View.VISIBLE
        recyclerTableResult.visibility = View.INVISIBLE
        recyclerTableInfo.visibility = View.INVISIBLE

        ic_open_scoreboard.setBackgroundResource(R.drawable.ic_close_scoreboard)
        ic_open_scoreboard.setOnClickListener {
            statusScoreboard = statusScoreboard != true

            if (statusScoreboard) {
                video_view_match.visibility = View.INVISIBLE
                recyclerTableResult.visibility = View.VISIBLE
                recyclerTableInfo.visibility = View.VISIBLE
                ic_open_scoreboard.setBackgroundResource(R.drawable.ic_open_scoreboard)
            } else {
                video_view_match.visibility = View.VISIBLE
                recyclerTableResult.visibility = View.INVISIBLE
                recyclerTableInfo.visibility = View.INVISIBLE
                ic_open_scoreboard.setBackgroundResource(R.drawable.ic_close_scoreboard)
            }
        }
    }
}