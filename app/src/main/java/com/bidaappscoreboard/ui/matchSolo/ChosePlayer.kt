package com.bidaappscoreboard.ui.matchSolo

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bidaappscoreboard.R
import com.bidaappscoreboard.common.HideKeyboard.hideKeyboard
import com.bidaappscoreboard.model.Profile
import com.bidaappscoreboard.store.MatchSoloStore
import com.bidaappscoreboard.store.ScoreBoard
import com.bidaappscoreboard.ui.RegisterScreen
import com.bidaappscoreboard.ui.findAnOppnent.FindAnOpponent
import kotlinx.android.synthetic.main.activity_chose_player.*
import kotlinx.android.synthetic.main.activity_register_screen.*
import kotlinx.android.synthetic.main.custom_popup_confirm_participation.*

class ChosePlayer : AppCompatActivity() {

    private val TAG: String = "ChosePlayer"
    private var playerSetup: Int = 0
    private var changePlayer = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chose_player)

        init()
        changePlayer = intent.getBooleanExtra("ChangePlayer", false)
        playerSetup = intent.getIntExtra("NumberPlayer", 0)
        containBackButtonChosePlayer.setOnClickListener {
            if (!changePlayer) {
                MatchSoloStore.arrayProfilePlayer[playerSetup] = Profile("", 25, "", 0.0, 0, 0)
            }
            finish()
        }
    }

    private fun init() {
        gotoSignup()
        showDialogConfirmParticipation()
        tv_device_name.text = ScoreBoard.nameDevice
        actionBar?.setDisplayHomeAsUpEnabled(false)
        edit_phone_number_chose_player.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                v.hideKeyboard()
            }
        }
    }

    override fun onBackPressed() {
        if (!changePlayer) {
            MatchSoloStore.arrayProfilePlayer[playerSetup] = Profile("", 25, "", 0.0, 0, 0)
        }
        finish()
    }

    @SuppressLint("SetTextI18n")
    private fun showDialogConfirmParticipation() {
        val dialog = Dialog(this@ChosePlayer)
        dialog.setContentView(R.layout.custom_popup_confirm_participation)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val editParticipation = dialog.findViewById<TextView>(R.id.tv_notice_participation)
        val icClose = dialog.findViewById<ImageView>(R.id.ic_close)
        val btnSendAgain = dialog.findViewById<Button>(R.id.btn_send_again)

        icClose.setOnClickListener {
            dialog.dismiss()
        }

        btnSendAgain.setOnClickListener {
            gotoFindOpponent()
        }

        contain_ic_check.setOnClickListener {
            if (edit_phone_number_chose_player.text.length != 9) {
                // TODO: show error validate phone number
            } else {
                //editParticipation.text = "Đã gửi tin nhắn xác nhận đến số điện thoại 0${edit_phone_number_chose_player.text}. Vui lòng nhấp vào đường dẫn để hoàn tất Đăng ký"
                //dialog.show()

                if (playerSetup == 0) {
                    MatchSoloStore.arrayPhone[MatchSoloStore.arrayLocationProfilePlayer[0]] = "0" + edit_phone_number_chose_player.text
                }

                if (playerSetup == 1) {
                    MatchSoloStore.arrayPhone[MatchSoloStore.arrayLocationProfilePlayer[1]] = "0" + edit_phone_number_chose_player.text
                }

                gotoFindOpponent()
            }
        }
    }

    private fun gotoFindOpponent() {
        val intent = Intent(this, FindAnOpponent::class.java)
        startActivity(intent)
        finish()
    }

    private fun gotoSignup() {
        tvHD_1.visibility = View.INVISIBLE
        btn_goto_signup.visibility = View.INVISIBLE
        btn_goto_signup.setOnClickListener {
            val intent = Intent(this, RegisterScreen::class.java)
            startActivity(intent)
        }
    }
}