package com.bidaappscoreboard.ui

import android.annotation.SuppressLint
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bidaappscoreboard.R
import com.bidaappscoreboard.common.HideKeyboard.hideKeyboard
import com.bidaappscoreboard.store.RegisterStore
import kotlinx.android.synthetic.main.activity_login_screen.*
import kotlinx.android.synthetic.main.activity_register_screen.*

class RegisterScreen : AppCompatActivity() {

    private val TAG = "RegisterScreen"
    var choseSex: Array<Boolean> = arrayOf(false, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_screen)

        init()
        btn_back.setOnClickListener {
            finish()
        }
    }

    private fun init() {
        logUp()
        chooseSex()
        edit_first_last_name.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                v.hideKeyboard()
            }
        }
        edit_phone_number.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                v.hideKeyboard()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun logUp() {
        var statusNumber = false
        var statusInputText = false

        btn_register.setOnClickListener {

            val dialog = Dialog(this@RegisterScreen)
            dialog.setContentView(R.layout.custom_popup_signup)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            val tvNotice = dialog.findViewById<TextView>(R.id.tv_notice_register)
            val icClose = dialog.findViewById<ImageView>(R.id.ic_close)

            icClose.setOnClickListener {
                dialog.dismiss()
            }

            if (edit_phone_number.text.toString() == RegisterStore.phoneNumberTest) {
                tv_error_register.visibility = View.VISIBLE
                statusNumber = false
            } else {
                tv_error_register.visibility = View.GONE
                tvNotice.text = "Đã gửi tin nhắn xác nhận đến số điện thoại 0${edit_phone_number.text}. Vui lòng nhấp vào đường dẫn để hoàn tất Đăng ký"
                statusNumber = true
            }

            if (edit_first_last_name.text.isNotEmpty() && edit_phone_number.text.length == 9) {
                tv_error_register.visibility = View.GONE
                statusInputText = true
            } else if (edit_first_last_name.text.isEmpty()) {
                tv_error_register.text = "Hãy nhập họ và tên"
                tv_error_register.visibility = View.VISIBLE
                statusInputText = false
            } else if (edit_phone_number.text.length != 9) {
                tv_error_register.text = "Số điện thoại không hợp lệ"
                tv_error_register.visibility = View.VISIBLE
                statusInputText = false
            }

            if (statusNumber && statusInputText) {
                dialog.show()
            }
        }
    }

    private fun chooseSex() {
        iconCheckBoxMale.setOnClickListener {
            choseSex = arrayOf(true, false)
            checkStatusSex()
        }

        iconCheckBoxFemale.setOnClickListener {
            choseSex = arrayOf(false, true)
            checkStatusSex()
        }

        checkStatusSex()
    }

    private fun checkStatusSex() {
        when {
            choseSex[0] -> {
                iconCheckBoxMale.setImageResource(R.drawable.radio_check)
                iconCheckBoxFemale.setImageResource(R.drawable.radio_uncheck)
            }
            choseSex[1] -> {
                iconCheckBoxMale.setImageResource(R.drawable.radio_uncheck)
                iconCheckBoxFemale.setImageResource(R.drawable.radio_check)
            }
            else -> {
                iconCheckBoxMale.setImageResource(R.drawable.radio_uncheck)
                iconCheckBoxFemale.setImageResource(R.drawable.radio_uncheck)
            }
        }
    }
}