package com.bidaappscoreboard.ui.matchSolo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bidaappscoreboard.R
import com.bidaappscoreboard.store.MatchSoloStore
import com.bidaappscoreboard.ui.HomeScreen
import com.bidaappscoreboard.ui.findAnOppnent.FindAnOpponent
import kotlinx.android.synthetic.main.activity_match_solo_screen.*

class MatchSoloScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_solo_screen)

        init()
        btn_back.setOnClickListener {
            val intent = Intent(this, HomeScreen::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }

    private fun init() {
        runFindAnOpponent()
    }

    override fun onBackPressed() {
        val intent = Intent(this, HomeScreen::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun runFindAnOpponent() {
        play_carom.setOnClickListener {
            MatchSoloStore.nameModel = "CHƠI PHĂNG"
            MatchSoloStore.matchType = MatchSoloStore.BI_TYPE_FRANCE

            val intent = Intent(this@MatchSoloScreen, FindAnOpponent::class.java)
            startActivity(intent)
            finish()
        }

        play_carom_1.setOnClickListener {
            MatchSoloStore.nameModel = "1 BĂNG"
            MatchSoloStore.matchType = MatchSoloStore.BI_TYPE_CAROM_1

            val intent = Intent(this@MatchSoloScreen, FindAnOpponent::class.java)
            startActivity(intent)
            finish()
        }

        play_carom_3.setOnClickListener {
            MatchSoloStore.nameModel = "3 BĂNG"
            MatchSoloStore.matchType = MatchSoloStore.BI_TYPE_CAROM_2

            val intent = Intent(this@MatchSoloScreen, FindAnOpponent::class.java)
            startActivity(intent)
            finish()
        }
    }
}