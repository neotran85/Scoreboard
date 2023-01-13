package com.bidaappscoreboard.ui.watchMatchAgain

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.SeekBar
import com.bidaappscoreboard.R
import com.bidaappscoreboard.common.NpaLinearLayoutManager
import com.bidaappscoreboard.store.CameraStore
import com.bidaappscoreboard.store.CustomerStore
import com.bidaappscoreboard.store.MatchSoloStore
import kotlinx.android.synthetic.main.activity_watch_match_again_screen.*
import kotlin.properties.Delegates

class WatchMatchAgainScreen : AppCompatActivity() {
    private val TAG = "WatchMatchAgainScreen"

    private lateinit var watchMatchAgainTurnAdapter: WatchMatchAgainTurnAdapter
    private lateinit var watchMatchAgainTimeAdapter: WatchMatchAgainTimeAdapter

    private var watchHistoryTurn : Boolean = false
    private var currentTime by Delegates.notNull<Long>()

    private var trackingTouch: Boolean = false
    private var isPause: Boolean = false
    private var isFullScreen: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watch_match_again_screen)
        currentTime = System.currentTimeMillis()

        Log.d(TAG, "time WatchMatchAgainScreen: $currentTime")
        init()
    }

    private fun init() {
        setupVideoPlayback()
        setupInfoPlayer()
        showHistory()
        showListHistoryTime()

        containBackButtonWatchAgain.setOnClickListener {
            CameraStore.stopPlayback()
            finish()
        }

        if (CustomerStore.statusUpdateWhenClickTurn) {
            Log.d(TAG, "Update success")
        }
    }

    private fun showHistory() {
        ic_check_square.setOnClickListener {
            if (watchHistoryTurn) {
                watchHistoryTurn = false
                showListHistoryTime()
            } else {
                watchHistoryTurn = true
                showListHistoryTurn()
            }
        }
    }

    private fun showListHistoryTime() {
        // playback seconds
        val listTime = mutableListOf(0, 15, 30, 60, 120, 180, 240, 300, 360)

        ic_check_square.setImageResource(R.drawable.ic_checkbox_uncheck)
        table_history_turn.visibility = View.GONE
        contain_recycler_history_turn.visibility = View.GONE
        contain_recycler_history_time.visibility = View.VISIBLE

        recyclerTableHistoryTime.apply {
            layoutManager = NpaLinearLayoutManager(this@WatchMatchAgainScreen)
            watchMatchAgainTimeAdapter = WatchMatchAgainTimeAdapter(this@WatchMatchAgainScreen, listTime, currentTime)
            adapter = watchMatchAgainTimeAdapter
        }
    }

    private fun setupInfoPlayer() {
        tv_name_player_1.text = MatchSoloStore.arrayProfilePlayer[MatchSoloStore.arrayLocationProfilePlayer[0]].displayname
        tv_name_player_2.text = MatchSoloStore.arrayProfilePlayer[MatchSoloStore.arrayLocationProfilePlayer[1]].displayname
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showListHistoryTurn() {

        ic_check_square.setImageResource(R.drawable.ic_checkbox_checked)
        table_history_turn.visibility = View.VISIBLE
        contain_recycler_history_turn.visibility = View.VISIBLE
        contain_recycler_history_time.visibility = View.GONE

        recyclerTableHistoryTurn.apply {
            var list = MatchSoloStore.listResultMatch

            // calculate turn end time
            for (i in 0 until list.size) {
                Log.d(TAG, "turn $i: ${list[i]}")

                // skip first turn and last turn
                // begin time of turn =
                if (i != 0 && i != list.size -1) {
                    if (list[i].time1 != null && list[i].time1!! > 0) {
                        list[i -1].timeEndTurn = list[i].time1
                    }
                }

                // semi-final turn
                // if last turn have begin time then
                //      semi last turn end time = begin time of last turn
                // other
                //      semi last turn end time = end of video
                if (i == list.size - 2) {
                    if (list[i + 1].time1 != null && list[i + 1].time1!! > 0) {
                        list[i].timeEndTurn = list[i + 1].time1
                    } else {
                        list[i].timeEndTurn = currentTime
                    }
                }
            }

            Log.d(TAG, "list turn: $list")

            layoutManager = NpaLinearLayoutManager(this@WatchMatchAgainScreen)
            watchMatchAgainTurnAdapter = WatchMatchAgainTurnAdapter(this@WatchMatchAgainScreen, list, currentTime)
            adapter = watchMatchAgainTurnAdapter
        }
    }

    private fun setupVideoPlayback() {
        CameraStore.attachPlaybackView(videoPlaybackLayout,
            onStarted = { totalMilliSeconds: Long? ->
                Log.d(TAG, "playback totalMilliSeconds::: $totalMilliSeconds")
                // begin playback call
                if (totalMilliSeconds != null) {
                    val totalSeconds = totalMilliSeconds / 1000
//                    timePlayback.text = formatTime(seconds = totalSeconds)
                    timeSlider.max = totalMilliSeconds.toInt()
                    img_play.visibility = View.VISIBLE
                    img_replay.visibility = View.INVISIBLE
                    img_play.setBackgroundResource(R.drawable.ic_pause_video_vector)
                    isPause = false
                } else {
                    // playback paused
                    img_play.visibility = View.VISIBLE
                    img_replay.visibility = View.INVISIBLE
                    img_play.setBackgroundResource(R.drawable.ic_start_video_vector)
                    isPause = true
                }
            }, onStopped = {
                img_play.visibility = View.INVISIBLE
                img_replay.visibility = View.VISIBLE
            }, onProgress = { currentMilliseconds: Long ->
                // playback in progress
                val remainSeconds = (timeSlider.max - currentMilliseconds) / 1000
                timePlayback.text = formatTime(remainSeconds)
                // Log.d(TAG, "currentMilliseconds:::$currentMilliseconds timeSlider.max ${timeSlider.max} ::::remainSeconds $remainSeconds")

                if (!trackingTouch) {
                    // do not update progress if user interactive with timeSlider
                    timeSlider.progress = currentMilliseconds.toInt()
                }
            })

        timeSlider.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                if (fromUser) {
                    Log.d(TAG, "onProgressChanged: $progress")
                    CameraStore.changePlaybackTime(progress)
                    val remainSeconds = (timeSlider.max - progress) / 1000
                    timePlayback.text = formatTime(remainSeconds.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                trackingTouch = true
                CameraStore.pausePlayback()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                trackingTouch = false
                if (seekBar != null) {
                    CameraStore.changePlaybackTime(seekBar.progress)
                }
                CameraStore.resumePlayback()
            }
        })

        img_play.setOnClickListener {
            if (isPause) {
                CameraStore.resumePlayback()
            } else {
                CameraStore.pausePlayback()
            }
        }

        img_full.setOnClickListener {
            if (isFullScreen) {
                // TODO
                img_full.setBackgroundResource(R.drawable.ic_zoom_video)
                contain_table_history.visibility = View.VISIBLE
                isFullScreen = false
            } else {
                // TODO
                img_full.setBackgroundResource(R.drawable.ic_minimize_video)
                contain_table_history.visibility = View.GONE
                isFullScreen = true
            }
        }

        img_replay.setOnClickListener {
            Log.d(TAG, "img_replay click")
            CameraStore.reloadPlayback()
        }
    }

    private fun formatTime(seconds: Long): String {
        val minutes = ((seconds % 86400) % 3600) / 60
        val hours = ((seconds % 86400) / 3600)
        var second = ((seconds % 86400) % 3600) % 60
        if (second < 0) {
            second = 0
        }
        return String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", second)
    }
}