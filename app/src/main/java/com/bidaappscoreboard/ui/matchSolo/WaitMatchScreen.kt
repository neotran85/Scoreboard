package com.bidaappscoreboard.ui.matchSolo

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import com.bidaappscoreboard.R
import com.bidaappscoreboard.common.SetImage
import com.bidaappscoreboard.store.MatchSoloStore
import kotlinx.android.synthetic.main.activity_wait_solo_match.*
import com.bidaappscoreboard.store.CameraStore

class WaitMatchScreen : AppCompatActivity() {

    private var nameModel: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wait_solo_match)
        init()
    }

    @SuppressLint("SetTextI18n")
    private fun init() {
        val nameModel = intent.getIntExtra("NameModel", 1)

        setupInfoPlayer()
        transferMatchScreen()

        // start new Match
        MatchSoloStore.newMatch(nameModel)

        // start record video
        MatchSoloStore.currentMatchID?.let { CameraStore.startVideoRecord(it) }

        tv_model_player1.text = "(${MatchSoloStore.nameModel})"
        tv_model_player2.text = "(${MatchSoloStore.nameModel})"
    }

    private fun transferMatchScreen() {
        val timer = object : CountDownTimer(5000, 1000) {

            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                val intent = Intent(this@WaitMatchScreen, MatchScreen::class.java)
                startActivity(intent)
                finish()
            }
        }

        timer.start()
    }

    private fun setupInfoPlayer() {
        // Setup player 1
        SetImage.setImage(avatar1, MatchSoloStore.arrayProfilePlayer[MatchSoloStore.arrayLocationProfilePlayer[0]].url, this)
        tvNamePlayer1.text = MatchSoloStore.arrayProfilePlayer[MatchSoloStore.arrayLocationProfilePlayer[0]].displayname
        tv_handy_1.text = MatchSoloStore.arrayHandy[MatchSoloStore.arrayLocationProfilePlayer[0]].toString()
        tv_table_1_2.text = MatchSoloStore.arrayProfilePlayer[MatchSoloStore.arrayLocationProfilePlayer[0]].avg.toString()
        tv_table_2_2.text = MatchSoloStore.arrayProfilePlayer[MatchSoloStore.arrayLocationProfilePlayer[0]].hr.toString()
        tv_player1.text = MatchSoloStore.arrayProfilePlayer[MatchSoloStore.arrayLocationProfilePlayer[0]].rank.toString()


        // Setup player 2
        SetImage.setImage(avatar2, MatchSoloStore.arrayProfilePlayer[MatchSoloStore.arrayLocationProfilePlayer[1]].url, this)
        tvNamePlayer2.text = MatchSoloStore.arrayProfilePlayer[MatchSoloStore.arrayLocationProfilePlayer[1]].displayname
        tv_handy_2.text = MatchSoloStore.arrayHandy[MatchSoloStore.arrayLocationProfilePlayer[1]].toString()
        tv_table_player2_1_2.text = MatchSoloStore.arrayProfilePlayer[MatchSoloStore.arrayLocationProfilePlayer[1]].avg.toString()
        tv_table_player2_2_2.text = MatchSoloStore.arrayProfilePlayer[MatchSoloStore.arrayLocationProfilePlayer[1]].hr.toString()
        tv_player2.text = MatchSoloStore.arrayProfilePlayer[MatchSoloStore.arrayLocationProfilePlayer[1]].rank.toString()
    }

}