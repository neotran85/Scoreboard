package com.bidaappscoreboard.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.View.OnFocusChangeListener
import androidx.lifecycle.lifecycleScope
import com.bidaappscoreboard.R
import com.bidaappscoreboard.common.HideKeyboard.hideKeyboard
import com.bidaappscoreboard.store.CustomerStore
import com.bidaappscoreboard.store.ScoreBoard
import kotlinx.android.synthetic.main.activity_login_screen.*
import kotlinx.coroutines.launch

class LoginScreen : AppCompatActivity() {

    var TAG = "LoginScreen"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_screen)

        init()
    }

    private fun init() {
        login()
        ScoreBoard.nameDevice = Settings.Secure.getString(contentResolver, "bluetooth_name")
        editId.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                v.hideKeyboard()
            }
        }
        editPassword.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                v.hideKeyboard()
            }
        }
    }

    private fun login() {
        textErrorLogin.visibility = View.INVISIBLE

        btnLogin.setOnClickListener {
            lifecycleScope.launch {
                textErrorLogin.visibility = View.INVISIBLE
                val userName = editId.text.toString()
                val password = editPassword.text.toString()

                // validate input
                if (userName.length < 3 || password.length < 3) {
                    textErrorLogin.visibility = View.VISIBLE
                    return@launch
                }

                // call login
                if (ScoreBoard.apiLogin(userName, password)) {
                    if (ScoreBoard.loginFirst) {
                        ScoreBoard.loginFirst = false
                        CustomerStore.loginID = userName
                        val intent = Intent(application, SettingScreen::class.java)
                        intent.putExtra("CallFirst", true)
                        startActivity(intent)
                    } else {
                        CustomerStore.loginID = userName
                        val intent = Intent(application, HomeScreen::class.java)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    textErrorLogin.visibility = View.VISIBLE
                }
            }
        }
    }
}