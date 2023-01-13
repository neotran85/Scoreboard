package com.bidaappscoreboard.ui

import android.app.Dialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.bidaappscoreboard.R
import kotlinx.android.synthetic.main.activity_home_screen.*
import com.bidaappscoreboard.store.Metadata
import com.bidaappscoreboard.store.ScoreBoard
import com.bidaappscoreboard.ui.matchSolo.MatchSoloScreen
import kotlinx.android.synthetic.main.activity_setting_screen.*

class HomeScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)

        init()
    }

    override fun onRestart() {
        super.onRestart()

        tv_table_name.text = ScoreBoard.nameDevice
    }

    private fun init() {
        openSetting()
        openRegister()
        openSoloMatch()

        //Get name device
        ScoreBoard.nameDevice = Settings.Secure.getString(contentResolver, "bluetooth_name")

        tv_table_name.text = ScoreBoard.nameDevice
    }

    private fun openSoloMatch() {
        solo_fight.setOnClickListener {
            val intent = Intent(this, MatchSoloScreen::class.java)
            startActivity(intent)
        }
    }

    private fun openRegister() {
        signup_now.setOnClickListener {
            val intent = Intent(this, RegisterScreen::class.java)
            startActivity(intent)
        }
    }

    private fun openSetting() {
        val dialog = Dialog(this@HomeScreen)
        dialog.setContentView(R.layout.custom_popup_input_pass_protect)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val edtInputPassProtect = dialog.findViewById<EditText>(R.id.edt_input_pass_protect)
        val tvErrorPassProtect = dialog.findViewById<TextView>(R.id.tv_error_input_pass_protect)
        val btnConfirmPassProtect = dialog.findViewById<Button>(R.id.btn_confirm_pass_protect)

        btn_setting.setOnClickListener {
            val intent = Intent(this, SettingScreen::class.java)
            if (ScoreBoard.enableProtect == true) {
                dialog.show()
                btnConfirmPassProtect.setOnClickListener {
                    if(edtInputPassProtect.text.toString() == ScoreBoard.passwordProtect) {
                        dialog.dismiss()
                        edtInputPassProtect.text = null
                        startActivity(intent)
                    } else {
                        tvErrorPassProtect.visibility = View.VISIBLE
                    }
                }
            } else {
                startActivity(intent)
            }
        }
    }
}